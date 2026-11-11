from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class D05HF02SourceAndDocsContract(unittest.TestCase):
    def test_truthful_device_acceptance_window_and_scope(self):
        doc = (ROOT / 'docs/R1C_D05_REFERENCE_CONTINUITY.md').read_text()
        self.assertIn('InstaEclipse enabled throughout', doc)
        self.assertIn('superseded for current acceptance', doc)
        for stage in ('A1', 'A2', 'B1', 'B2'):
            self.assertIn(stage, doc)
        self.assertIn('60 seconds', doc)
        self.assertIn('INCONCLUSIVE', doc)
        self.assertNotIn('Run the same A/B continuity sequence in two independent conditions:', doc)
    def test_upstream_audit_makes_no_false_hook_identity_claim(self):
        doc = (ROOT / 'docs/R1C_D05_INSTAECLIPSE_SOURCE_AUDIT.md').read_text()
        for term in ('e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d',
                     'installReduceOptionsListPatch', 'PLAYBACK_CONTROLS',
                     'UNSAVE', 'X.07EF.A05', 'POSSIBLE SAME METHOD, NOT PROVEN',
                     'R1C D06', 'R4', 'R5', 'GPL-3.0'):
            self.assertIn(term, doc)
    def test_old_version_gate_and_security_still_unmodified(self):
        source = (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/R1CD05ReferenceContinuity.java').read_text()
        d04 = (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/R1CD04PassiveProbe.java').read_text()
        self.assertIn('WeakReference<Object>', source)
        self.assertIn('return Result.SKIPPED_WRONG_TYPE;', source)
        self.assertIn('INA_R1C_D05_REFERENCE ordinal=', d04)
        self.assertIn('String sha = sha256(apk)', d04)
        self.assertIn('MAX_RECORDED_EVENTS = 64',
            (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/R1CD04Policy.java').read_text())

    def test_hf02_source_and_roadmap_status(self):
        roadmap = (ROOT / 'ROADMAP.md').read_text()
        self.assertIn('R1C', roadmap)
        self.assertIn('D05', roadmap)
        self.assertIn('D06', roadmap)
        self.assertIn('D05 HF02', roadmap)
        self.assertNotIn('Repository target: `Mikeyphw/InstaActions` (private)', roadmap)
        d04 = (ROOT / 'docs/R1C_D04_PASSIVE_RUNTIME_BINDING.md').read_text()
        self.assertIn('DEVICE_ACCEPTED_MENU_TYPE_ONLY', d04)
        self.assertNotIn('SOURCE_PREPARED / HOSTED_CI_NOT_RUN / DEVICE_NOT_RUN', d04)
        self.assertIn('r1c_d05_evidence_policy',
            (ROOT/'tests/test_r1c_d05_evidence_policy.py').read_text())
        import importlib.util
        spec = importlib.util.spec_from_file_location('hf02_evidence', ROOT/'tools/r1c_d05_evidence_policy.py')
        module = importlib.util.module_from_spec(spec)
        import sys
        sys.modules[spec.name] = module
        spec.loader.exec_module(module)
        self.assertEqual(module.parse_logcat('caption=private').records, ())
