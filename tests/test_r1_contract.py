from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions'

class R1ReadOnlyContract(unittest.TestCase):
    def test_dexkit_and_api102_dependencies(self):
        g = (ROOT / 'app/build.gradle').read_text()
        self.assertIn("implementation 'org.luckypray:dexkit:2.0.3'", g)
        self.assertIn("compileOnly 'io.github.libxposed:api:102.0.0'", g)
        self.assertIn('android:extractNativeLibs="true"',
                      (ROOT / 'app/src/main/AndroidManifest.xml').read_text())

    def test_read_only_lifecycle_guard(self):
        entry = (JAVA / 'InstaActionsEntry.java').read_text()
        ctl = (JAVA / 'R1ProbeController.java').read_text()
        scan = (JAVA / 'R1DexKitProbe.java').read_text()
        self.assertIn('chain.proceed()', entry)
        self.assertIn('isMainInstagramProcess(processName)', entry)
        self.assertIn('R1ProbePolicy.supportedVersion', ctl)
        self.assertIn('new Thread(', ctl)
        self.assertIn('DexKitBridge.create', scan)
        self.assertIn('bridge.findMethod(', scan)
        self.assertIn('bridge.findClass(', scan)
        for text in (entry, ctl, scan):
            self.assertNotIn('getSharedPreferences(', text)
            self.assertNotIn('ContentResolver', text)
            self.assertNotIn('HttpURLConnection', text)
        self.assertNotIn('chain.proceed(', scan)

    def test_explicit_versions_and_safe_results(self):
        policy = (JAVA / 'R1ProbePolicy.java').read_text()
        self.assertIn('"451.0.0.20.80"', policy)
        self.assertIn('"453.0.0.0.56"', policy)
        self.assertIn('AMBIGUOUS', policy)
        self.assertIn('MUTATING_ACTIONS_ENABLED = false', policy)
        self.assertIn('MAX_LOGGED_DESCRIPTORS = 1', policy)

    def test_hosted_proof_and_device_gap(self):
        ci = (ROOT / '.github/workflows/android-r0.yml').read_text()
        self.assertIn('verify_r1_apk.py', ci)
        self.assertIn('testDebugUnitTest', ci)
        self.assertIn('assembleDebugAndroidTest', ci)
        doc = (ROOT / 'docs/R1_DEVICE_ACCEPTANCE.md').read_text()
        self.assertIn('NOT_RUN', doc)
        self.assertIn('InstaEclipse', doc)

if __name__ == '__main__':
    unittest.main()
