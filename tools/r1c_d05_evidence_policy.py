"""D05 HF02: shared strict parser for read-only, privacy-safe logcat evidence.

No user-provided Instagram content is carried from full logcat into shared output.
This exact source is also shipped to repository tools/ and tested in hosted CI.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Mapping, Sequence
import re

MAX_RECORDS = 512
MAX_CANDIDATES = 10000
MAX_INPUT_CHARS = 80_000_000  # fail closed, raw log stays local
SCREENS = frozenset(('UNKNOWN', 'HOME_FEED', 'POST', 'REELS', 'STORY'))
ARG_SHAPES = frozenset(('MEDIA_TYPED', 'NULL', 'WRONG_TYPE'))
COMPARISONS = frozenset((
    'SKIPPED_UNSTABLE', 'SKIPPED_WRONG_TYPE', 'BASELINE',
    'BASELINE_COLLECTED', 'SAME_REFERENCE', 'DIFFERENT_REFERENCE',
))
KINDS = frozenset(('D04_GATE', 'D04_HOOK', 'D04_EVENT', 'D04_LIMIT', 'D05_REFERENCE'))

# Require the correct Android logcat tag and a *complete* marker, not a substring.
# The epoch clock is the Android device epoch clock, checked against host clock
# before capture. PIDs, thread IDs, and arbitrary raw log messages are discarded.
LINE = re.compile(
    r'^\s*(?P<epoch>\d{10}(?:\.\d{1,9})?)\s+(?P<pid>\d{1,8})\s+\d+\s+[VDIWEF]\s+'
    r'INA_R1C:\s+(?P<marker>INA_R1C_(?:D04_(?:GATE|HOOK|EVENT|LIMIT)|D05_REFERENCE)\s+[^\r\n]*)\s*$'
)
START_MARKER = re.compile(r'INA_R1C_D(?:04_(?:GATE|HOOK|EVENT|LIMIT)|05_REFERENCE)\b')
GATE_REFUSED = re.compile(r'^INA_R1C_D04_GATE status=REFUSED reason=(no_package|version_mismatch|missing_input|apk_size_mismatch|apk_sha256_mismatch) actions=off$')
GATE_HASHING = re.compile(r'^INA_R1C_D04_GATE status=HASHING mode=background actions=off$')
GATE_PASS = re.compile(r'^INA_R1C_D04_GATE status=PASS version=455 apk_sha256=match actions=off$')
HOOK_REFUSED = re.compile(r'^INA_R1C_D04_HOOK status=REFUSED reason=(method_limit|signature_count)(?: count=(\d{1,3}))? actions=off$')
HOOK_INSTALLED = re.compile(r'^INA_R1C_D04_HOOK status=INSTALLED signature=exact_ig455 mode=passive_no_mutation actions=off$')
HOOK_FAILED = re.compile(r'^INA_R1C_D04_HOOK status=FAILED type=[A-Za-z][A-Za-z0-9]{0,63} actions=off$')
EVENT = re.compile(r'^INA_R1C_D04_EVENT ordinal=(\d{1,2}) screen=(\w{1,12}) arg=(\w{1,16}) media_id=unavailable retained=weak_only actions=off$')
CONTINUITY = re.compile(r'^INA_R1C_D05_REFERENCE ordinal=(\d{1,2}) screen=(\w{1,12}) comparison=(\w{1,24}) media_id=unavailable object_id=excluded retained=weak_only actions=off$')
LIMIT = re.compile(r'^INA_R1C_D04_LIMIT reached=64 actions=off$')

@dataclass(frozen=True)
class EventRecord:
    epoch: float
    marker: str
    kind: str
    screen: str = ''
    arg: str = ''
    comparison: str = ''
    ordinal: int = 0

@dataclass(frozen=True)
class ParsedLog:
    records: tuple[EventRecord, ...]
    marker_candidates: int
    rejected_markers: int
    truncated: bool


def parse_marker(marker: str, epoch: float) -> EventRecord | None:
    """Return canonical, allowlisted fields only. Never forward raw tails."""
    if GATE_PASS.fullmatch(marker):
        return EventRecord(epoch, 'INA_R1C_D04_GATE status=PASS version=455 apk_sha256=match actions=off', 'D04_GATE_PASS')
    if GATE_HASHING.fullmatch(marker):
        return EventRecord(epoch, 'INA_R1C_D04_GATE status=HASHING mode=background actions=off', 'D04_GATE_HASHING')
    m = GATE_REFUSED.fullmatch(marker)
    if m:
        return EventRecord(epoch, 'INA_R1C_D04_GATE status=REFUSED reason=' + m.group(1) + ' actions=off', 'D04_GATE_REFUSED')
    if HOOK_INSTALLED.fullmatch(marker):
        return EventRecord(epoch, 'INA_R1C_D04_HOOK status=INSTALLED signature=exact_ig455 mode=passive_no_mutation actions=off', 'D04_HOOK_INSTALLED')
    m = HOOK_REFUSED.fullmatch(marker)
    if m:
        reason, count = m.groups()
        if (reason == 'signature_count') != (count is not None):
            return None
        if count is not None and int(count) > 256:
            return None
        return EventRecord(epoch, 'INA_R1C_D04_HOOK status=REFUSED reason=' + reason
                           + ((' count=' + str(int(count))) if count is not None else '')
                           + ' actions=off', 'D04_HOOK_REFUSED')
    if HOOK_FAILED.fullmatch(marker):
        # Fail type is not needed in exported evidence; never forward unexpected class names.
        return EventRecord(epoch, 'INA_R1C_D04_HOOK status=FAILED type=redacted actions=off', 'D04_HOOK_FAILED')
    if LIMIT.fullmatch(marker):
        return EventRecord(epoch, 'INA_R1C_D04_LIMIT reached=64 actions=off', 'D04_LIMIT')
    m = EVENT.fullmatch(marker)
    if m:
        number, screen, arg = m.groups()
        if not (1 <= int(number) <= 64 and screen in SCREENS and arg in ARG_SHAPES):
            return None
        text = f'INA_R1C_D04_EVENT ordinal={int(number)} screen={screen} arg={arg} media_id=unavailable retained=weak_only actions=off'
        return EventRecord(epoch, text, 'D04_EVENT', screen=screen, arg=arg, ordinal=int(number))
    m = CONTINUITY.fullmatch(marker)
    if m:
        number, screen, comparison = m.groups()
        if not (1 <= int(number) <= 64 and screen in SCREENS and comparison in COMPARISONS):
            return None
        text = f'INA_R1C_D05_REFERENCE ordinal={int(number)} screen={screen} comparison={comparison} media_id=unavailable object_id=excluded retained=weak_only actions=off'
        return EventRecord(epoch, text, 'D05_REFERENCE', screen=screen,
                           comparison=comparison, ordinal=int(number))
    return None


def parse_logcat(raw: str, expected_pid: int | None = None, min_epoch: float | None = None) -> ParsedLog:
    if len(raw) > MAX_INPUT_CHARS:
        raise ValueError('Raw logcat exceeds safe parsing bound; retained locally only')
    records: list[EventRecord] = []
    candidates = rejected = 0
    for line in raw.splitlines():
        if not START_MARKER.search(line):
            continue
        candidates += 1
        if candidates > MAX_CANDIDATES:
            raise ValueError('Too many diagnostic candidates; retained locally only')
        match = LINE.fullmatch(line)
        if match is None:
            rejected += 1
            continue
        if ((expected_pid is not None and int(match.group('pid')) != expected_pid)
                or (min_epoch is not None and float(match.group('epoch')) < min_epoch)):
            rejected += 1
            continue
        parsed = parse_marker(match.group('marker'), float(match.group('epoch')))
        if parsed is None:
            rejected += 1
            continue
        if len(records) >= MAX_RECORDS:
            raise ValueError('Too many accepted diagnostic records; retained locally only')
        records.append(parsed)
    return ParsedLog(tuple(records), candidates, rejected, False)


def sanitized_log(parsed: ParsedLog) -> str:
    # User content, PIDs, tag strings and raw logger messages do not leave the device.
    return ''.join(f'{record.epoch:.3f} {record.marker}\n' for record in parsed.records)


def window_of(epoch: float, windows: Sequence[Mapping[str, object]]) -> str | None:
    matching = [str(w['name']) for w in windows
                if float(w['start_epoch']) <= epoch < float(w['end_epoch'])]
    return matching[0] if len(matching) == 1 else None


def summary(parsed: ParsedLog, windows: Sequence[Mapping[str, object]], segments: Sequence[Mapping[str, object]]) -> dict:
    required = {'feed', 'reels', 'reels_ab_continuity', 'story', 'feed_return'}
    labels = [str(w['name']) for w in windows]
    if len(labels) != len(set(labels)) or set(labels) != required:
        raise ValueError('Missing/duplicated capture stage')
    ordered = sorted(windows, key=lambda w: float(w['start_epoch']))
    for i, win in enumerate(ordered):
        if float(win['start_epoch']) >= float(win['end_epoch']):
            raise ValueError('Non-positive stage window')
        if i and float(ordered[i-1]['end_epoch']) > float(win['start_epoch']):
            raise ValueError('Overlapping capture stages')
    ab = next(w for w in ordered if w['name'] == 'reels_ab_continuity')
    ab_start = float(ab['start_epoch'])
    ab_end = float(ab['end_epoch'])
    if ab_end - ab_start < 109 or ab_end - ab_start > 130:
        raise ValueError('A/B stage must be approximately 110 seconds')
    if [x['name'] for x in segments] != ['A1', 'A2', 'B1', 'B2']:
        raise ValueError('Missing or wrongly ordered A/B segments')
    if abs(float(segments[0]['start_epoch']) - ab_start) > .05 or abs(float(segments[-1]['end_epoch']) - ab_end) > 1.5:
        raise ValueError('A/B segment boundaries do not cover test window')
    for i, seg in enumerate(segments):
        s, e = float(seg['start_epoch']), float(seg['end_epoch'])
        if s >= e or s < ab_start or e > ab_end + 0.01:
            raise ValueError('Unsafe segment interval')
        if i and abs(float(segments[i - 1]['end_epoch']) - s) > .05:
            raise ValueError('Gaps or overlaps in A/B segments')
    ab_records = [r for r in parsed.records if ab_start <= r.epoch < ab_end]
    out_records = [r for r in parsed.records if not (ab_start <= r.epoch < ab_end)]
    by_segment: dict[str, dict] = {}
    for segment in segments:
        name = str(segment['name'])
        group = [r for r in ab_records if float(segment['start_epoch']) <= r.epoch < float(segment['end_epoch'])]
        refs = [r for r in group if r.kind == 'D05_REFERENCE']
        by_segment[name] = {
            'd04_events': sum(r.kind == 'D04_EVENT' for r in group),
            'd05_events': len(refs),
            'reels_reference_events': sum(r.screen == 'REELS' for r in refs),
            'comparison_counts': {x: sum(r.comparison == x for r in refs) for x in sorted(COMPARISONS)},
        }
    refs_ab = [r for r in ab_records if r.kind == 'D05_REFERENCE']
    d04_ab = [r for r in ab_records if r.kind == 'D04_EVENT']
    result = {
        'gateway_pass_logged': any(r.kind == 'D04_GATE_PASS' for r in parsed.records),
        'hook_install_logged': any(r.kind == 'D04_HOOK_INSTALLED' for r in parsed.records),
        'all_record_count': len(parsed.records),
        'out_of_ab_record_count': len(out_records),
        'ab_d04_event_count': len(d04_ab),
        'ab_d05_event_count': len(refs_ab),
        'ab_media_typed_events': sum(r.arg == 'MEDIA_TYPED' for r in d04_ab),
        'ab_stable_reels_reference_events': sum(r.screen == 'REELS' for r in refs_ab),
        'ab_segments': by_segment,
        'marker_candidates': parsed.marker_candidates,
        'marker_rejected': parsed.rejected_markers,
        'per_segment_observations_are_time_buckets_not_media_proof': True,
        'current_visible_media_identity_verified': False,
        'same_instaeclipse_method_verified': False,
        'source_hook_overlap_assumption': 'UNVERIFIED',
        'action_hooks_enabled': False,
    }
    # No form of result count or self-attested menu observation is a media identity grant.
    result['status'] = ('EVIDENCE_PRESENT_REQUIRES_MANUAL_REVIEW' if
        result['gateway_pass_logged'] and result['hook_install_logged'] and
        all(v['reels_reference_events'] > 0 for v in by_segment.values())
        else 'INCONCLUSIVE_MISSING_BOUNDED_EVENTS')
    return result
