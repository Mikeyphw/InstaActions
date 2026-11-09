from pathlib import Path
import json
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions'

class R1BSurfaceContract(unittest.TestCase):
    def test_read_only_observer_and_version_gate(self):
        monitor = (JAVA/'R1BContextMonitor.java').read_text()
        entry = (JAVA/'InstaActionsEntry.java').read_text()
        policy = (JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('OnGlobalLayoutListener', monitor)
        self.assertIn('OnScrollChangedListener', monitor)
        self.assertIn('getResourcePackageName(id)', monitor)
        self.assertIn('R1ProbePolicy.supportedVersion', monitor)
        self.assertIn('MAX_VIEWS = 384', policy)
        self.assertIn('MIN_SAMPLE_INTERVAL_MS = 800', policy)
        self.assertIn('R1BContextMonitor.resumed', entry)
        self.assertIn('R1BContextMonitor.paused', entry)
        self.assertIn('chain.proceed()', entry)
        for src in (monitor, policy):
            for forbidden in ('.getText(', '.performClick(', 'setOnClickListener(',
                              '.getOnClickListener(', 'getSharedPreferences(',
                              'HttpURLConnection', 'MediaModel', 'sendBroadcast(',
                              '.setText(', 'ClipboardManager'):
                self.assertNotIn(forbidden, src)

    def test_actions_off_and_gated_telemetry(self):
        monitor = (JAVA/'R1BContextMonitor.java').read_text()
        policy = (JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('media_id=unavailable actions=off', monitor)
        self.assertIn('Confidence.NONE', policy)
        self.assertIn('if (resourceNames == null || resourceNames.isEmpty() || truncated)', policy)
        self.assertIn('observer.removeOnGlobalLayoutListener(this)', monitor)
        self.assertIn('observer.removeOnScrollChangedListener(this)', monitor)
        self.assertIn('ui.removeCallbacks(sampler)', monitor)
        self.assertNotIn('github.token', monitor)

    def test_r1b_ci_proof(self):
        ci = (ROOT/'.github/workflows/android-r0.yml').read_text()
        self.assertIn('verify_r1b_apk.py', ci)
        self.assertIn(':app:testDebugUnitTest', ci)
        self.assertIn('upload-artifact@v4', ci)
        self.assertIn('R1BContextPolicyTest.java',
                      '\n'.join(str(p.relative_to(ROOT)) for p in ROOT.rglob('*ContextPolicyTest.java')))
        data = json.loads((ROOT/'project.json').read_text())
        self.assertEqual(data['phase'], 'r1')
        self.assertTrue(data['r1b_readonly_context_source'])
        self.assertFalse(data['r1b_media_identity_verified'])

if __name__ == '__main__': unittest.main()
