"""D05 HF02 regression suite: standalone ZIP --check and GitHub repo CI copy."""
import hashlib
import io
import json
import re
import unittest
import zipfile
from unittest.mock import patch
# Load the source file directly: Termux/Hermes may preimport an unrelated
# top-level `tools` package into sys.modules. Imports using
# `from tools import ...` resolve to that external package, even when cwd
# is the InstaActions repository. Dataclasses require a registered module.
import importlib.util
import sys
from pathlib import Path

_POLICY_FILE = Path(__file__).resolve().parents[1] / 'tools' / 'r1c_d05_evidence_policy.py'
_SPEC = importlib.util.spec_from_file_location('_ina_r1c_d05_evidence_policy_local', _POLICY_FILE)
if _SPEC is None or _SPEC.loader is None:
    raise ImportError(f'Cannot load repository evidence policy: {_POLICY_FILE}')
ep = importlib.util.module_from_spec(_SPEC)
sys.modules[_SPEC.name] = ep
_SPEC.loader.exec_module(ep)
if Path(ep.__file__).resolve() != _POLICY_FILE.resolve():
    raise ImportError('Loaded evidence policy from outside the repository')


EPOCH = 1794312000.000


def log_line(marker, when=EPOCH, tag='INA_R1C'):
    return f'{when:.3f} 14321 14321 I {tag}: {marker}'


def reference(ordinal=1, screen='REELS', comparison='BASELINE'):
    return (f'INA_R1C_D05_REFERENCE ordinal={ordinal} screen={screen} '
            f'comparison={comparison} media_id=unavailable object_id=excluded '
            f'retained=weak_only actions=off')


def d04_event(ordinal=1, screen='REELS', shape='MEDIA_TYPED'):
    return (f'INA_R1C_D04_EVENT ordinal={ordinal} screen={screen} arg={shape} '
            'media_id=unavailable retained=weak_only actions=off')


def stage_defs():
    windows = [
        {'name': 'feed', 'start_epoch': EPOCH-120, 'end_epoch': EPOCH-100},
        {'name': 'reels', 'start_epoch': EPOCH-98, 'end_epoch': EPOCH-73},
        {'name': 'reels_ab_continuity', 'start_epoch': EPOCH, 'end_epoch': EPOCH+110},
        {'name': 'story', 'start_epoch': EPOCH+112, 'end_epoch': EPOCH+137},
        {'name': 'feed_return', 'start_epoch': EPOCH+139, 'end_epoch': EPOCH+159},
    ]
    seg = [
        {'name':'A1','start_epoch':EPOCH,'end_epoch':EPOCH+25},
        {'name':'A2','start_epoch':EPOCH+25,'end_epoch':EPOCH+50},
        {'name':'B1','start_epoch':EPOCH+50,'end_epoch':EPOCH+80},
        {'name':'B2','start_epoch':EPOCH+80,'end_epoch':EPOCH+110},
    ]
    return windows, seg


class ParserContracts(unittest.TestCase):
    def test_accept_exact_known_marker_canonically(self):
        examples = [
            'INA_R1C_D04_GATE status=PASS version=455 apk_sha256=match actions=off',
            'INA_R1C_D04_GATE status=HASHING mode=background actions=off',
            'INA_R1C_D04_GATE status=REFUSED reason=version_mismatch actions=off',
            'INA_R1C_D04_HOOK status=INSTALLED signature=exact_ig455 mode=passive_no_mutation actions=off',
            'INA_R1C_D04_HOOK status=REFUSED reason=signature_count count=0 actions=off',
            'INA_R1C_D04_HOOK status=FAILED type=SecurityException actions=off',
            d04_event(1), reference(1), 'INA_R1C_D04_LIMIT reached=64 actions=off',
        ]
        parsed = ep.parse_logcat('\n'.join(log_line(e) for e in examples))
        self.assertEqual(len(parsed.records), len(examples))
        self.assertEqual(parsed.rejected_markers, 0)
        self.assertIn('type=redacted', ep.sanitized_log(parsed))
        self.assertNotIn('SecurityException', ep.sanitized_log(parsed))

    def test_sensitive_field_suffix_fails_closed(self):
        good = reference()
        injected = [
            good + ' caption=myPrivateCaption',
            good + ' user=someone',
            good + ' username=mikey',
            good + ' note=https://example.org/private',
            good + ' email=user@example.org',
            good + ' text=secret',
            good + ' 😎',
            good + '\x1b[31m',
            good + ' media_id=private',
            good + ' /data/data/private',
            good.replace('actions=off','actions=on'),
            good.replace('ordinal=1','ordinal=999'),
            good.replace('screen=REELS','screen=MY_PROFILE'),
            good.replace('comparison=BASELINE','comparison=SECRET'),
            good.replace('retained=weak_only','retained=strong'),
        ]
        parsed = ep.parse_logcat('\n'.join(log_line(e) for e in injected))
        self.assertEqual(parsed.marker_candidates, len(injected))
        self.assertEqual(parsed.rejected_markers, len(injected))
        self.assertEqual(parsed.records, ())
        self.assertEqual(ep.sanitized_log(parsed), '')

    def test_wrong_tag_missing_epoch_or_inline_marker_refuses(self):
        target = reference()
        parsed = ep.parse_logcat('\n'.join([
            'I INA_R1C: ' + target,
            log_line(target, tag='OtherApp'),
            'some arbitrary log with ' + target,
            log_line(target) + ' SOME TRAILING GARBAGE',
        ]))
        self.assertEqual(len(parsed.records), 0)
        self.assertEqual(parsed.rejected_markers, 4)

    def test_wrong_pid_and_stale_epoch_rejected(self):
        legitimate = log_line(reference(), EPOCH)
        p = ep.parse_logcat(legitimate, expected_pid=91919, min_epoch=EPOCH-10)
        self.assertEqual(p.records, ())
        self.assertEqual(p.rejected_markers, 1)
        p = ep.parse_logcat(legitimate, expected_pid=14321, min_epoch=EPOCH+1)
        self.assertEqual(p.records, ())
        self.assertEqual(p.rejected_markers, 1)
        p = ep.parse_logcat(legitimate, expected_pid=14321, min_epoch=EPOCH-1)
        self.assertEqual(len(p.records), 1)

    def test_no_logs_above_resource_bound(self):
        with self.assertRaises(ValueError):
            ep.parse_logcat('z' * (ep.MAX_INPUT_CHARS + 1))
        text = '\n'.join(log_line(reference(1)) for _ in range(ep.MAX_RECORDS + 1))
        with self.assertRaises(ValueError):
            ep.parse_logcat(text)

    def test_only_ab_window_contributes_to_counts(self):
        a = [
            log_line('INA_R1C_D04_GATE status=PASS version=455 apk_sha256=match actions=off', EPOCH-119),
            log_line('INA_R1C_D04_HOOK status=INSTALLED signature=exact_ig455 mode=passive_no_mutation actions=off', EPOCH-118),
            log_line(reference(1,comparison='SAME_REFERENCE'), EPOCH-97),
            log_line(d04_event(2), EPOCH-96),
        ]
        for i, t in enumerate((10, 35, 65, 90), 3):
            a += [log_line(d04_event(i), EPOCH+t), log_line(reference(i,comparison='BASELINE'), EPOCH+t+.1)]
        a.append(log_line(reference(7,comparison='SAME_REFERENCE'), EPOCH+113))
        p = ep.parse_logcat('\n'.join(a))
        windows, seg = stage_defs()
        result = ep.summary(p, windows, seg)
        self.assertEqual(result['ab_d05_event_count'], 4)
        self.assertEqual(result['ab_d04_event_count'], 4)
        self.assertEqual(result['out_of_ab_record_count'], 5)
        self.assertEqual([result['ab_segments'][name]['d05_events'] for name in ('A1','A2','B1','B2')], [1]*4)
        self.assertEqual(result['status'], 'EVIDENCE_PRESENT_REQUIRES_MANUAL_REVIEW')
        self.assertFalse(result['current_visible_media_identity_verified'])
        self.assertFalse(result['same_instaeclipse_method_verified'])

    def test_missing_one_bucket_inconclusive(self):
        windows, seg = stage_defs()
        p = ep.parse_logcat(log_line(reference(1),EPOCH+10))
        self.assertEqual(ep.summary(p, windows, seg)['status'], 'INCONCLUSIVE_MISSING_BOUNDED_EVENTS')

    def test_missing_overlapping_incomplete_windows_fail(self):
        windows, seg = stage_defs()
        with self.assertRaises(ValueError):
            ep.summary(ep.parse_logcat(''), windows[:-1], seg)
        broken = [dict(w) for w in windows]
        broken[1]['end_epoch'] = broken[2]['start_epoch'] + 20
        with self.assertRaises(ValueError):
            ep.summary(ep.parse_logcat(''), broken, seg)
        broken_seg = [dict(s) for s in seg]
        broken_seg[1]['start_epoch'] += 2
        with self.assertRaises(ValueError):
            ep.summary(ep.parse_logcat(''), windows, broken_seg)



if __name__ == '__main__':
    unittest.main()
