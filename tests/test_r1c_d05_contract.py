from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class D05Contract(unittest.TestCase):
    def test_weak_reference_only_and_invalidations(self):
        s=(JAVA/'R1CD05ReferenceContinuity.java').read_text()
        for token in ('WeakReference<Object>', 'prior == candidate',
                      'SAME_REFERENCE', 'DIFFERENT_REFERENCE',
                      'BASELINE_COLLECTED','SKIPPED_UNSTABLE',
                      'SKIPPED_WRONG_TYPE','previous.clear()',
                      'screen != R1BNavigationPolicy.Screen.REELS'):
            self.assertIn(token,s)
        for forbidden in ('getId(', 'identityHashCode(', 'getText(',
                          'getContentDescription(', 'getDeclaredFields(',
                          'toString(', '.invoke(', 'getOnClickListener(', 'performClick('):
            self.assertNotIn(forbidden,s)
    def test_existing_d04_gate_and_hook_preserved(self):
        p=(JAVA/'R1CD04PassiveProbe.java').read_text()
        e=(JAVA/'InstaActionsEntry.java').read_text()
        self.assertIn('String sha = sha256(apk)',p)
        self.assertIn('matches != 1',p)
        self.assertIn('private final R1CD05ReferenceContinuity continuity',p)
        self.assertIn('INA_R1C_D05_REFERENCE ordinal=',p)
        self.assertIn('R1CD04PassiveProbe.paused();',e)
        self.assertIn('return observation.proceed();',e)
        self.assertIn('counter.observe(observation.getArg(1)',e)
        self.assertIn('MAX_RECORDED_EVENTS = 64',(JAVA/'R1CD04Policy.java').read_text())
        self.assertIn('ACTIONS_ENABLED = false',(JAVA/'R1CD04Policy.java').read_text())
        self.assertIn('BINDING_HOOKS_ENABLED = false',(JAVA/'R1CDiscoveryPolicy.java').read_text())
        self.assertIn('MEDIA_IDENTITY_VERIFIED = false',(JAVA/'R1CDiscoveryPolicy.java').read_text())
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',(JAVA/'R1ProbeController.java').read_text())
        for src in (p,e):
            for forbidden in ('getId(', 'setOnClickListener(', 'performClick(', '.invoke('):
                self.assertNotIn(forbidden,src)
    def test_versions_and_docs(self):
        b=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 13',b)
        self.assertIn("versionName '0.4.4-r1c-d05'",b)
        p=(ROOT/'project.json').read_text()
        self.assertIn('PASS_REELS_MENU_MEDIA_TYPE_ONLY',p)
        self.assertIn('D04 verified device evidence',(ROOT/'docs/R1C_D05_REFERENCE_CONTINUITY.md').read_text())
        for path in (ROOT/'tests').glob('test_*.py'):
            for line in path.read_text().splitlines():
                if line.lstrip().startswith('self.assertIn(') and ('versionCode ' in line or "versionName '" in line):
                    self.assertIn('versionCode 13' if 'versionCode' in line else '0.4.4-r1c-d05',line,str(path))
        verifier=(ROOT/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CD05ReferenceContinuity'",verifier)
        self.assertIn("b'INA_R1C_D05_REFERENCE'",verifier)
