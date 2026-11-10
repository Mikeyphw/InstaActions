from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class R1BHF02PersistentCapture(unittest.TestCase):
    def test_persistent_vectorcontext_tag(self):
        entry = (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/InstaActionsEntry.java').read_text()
        self.assertIn('Log.i(DIAGNOSTIC_TAG, event)', entry)
        self.assertIn('log(Log.INFO, null, event);', entry)
        self.assertNotIn('log(Log.INFO, DIAGNOSTIC_TAG, event);', entry)
        self.assertIn('INA_DIAG_MODULE_LOADED', entry)
        self.assertIn('INA_DIAG_PACKAGE_READY', entry)
        self.assertIn('INA_DIAG_HOOKS_INSTALLED', entry)
        self.assertIn('INA_DIAG_FIRST_ACTIVITY_RESUMED', entry)
    def test_version_change_and_actions_still_off(self):
        gradle = (ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 12', gradle)
        self.assertIn("versionName '0.4.3-r1c-d04'", gradle)
        policy = (ROOT/'app/src/main/java/com/mikeyphw/instaactions/R1ProbePolicy.java').read_text()
        self.assertIn('MUTATING_ACTIONS_ENABLED = false', policy)
        self.assertIn('chain.proceed()', (ROOT/'app/src/main/java/com/mikeyphw/instaactions/InstaActionsEntry.java').read_text())
    def test_device_doc_addresses_capture_loss(self):
        doc = (ROOT/'docs/R1B_HF02_PERSISTENT_CAPTURE.md').read_text()
        self.assertIn('VectorContext', doc)
        self.assertIn('live logcat', doc)
        self.assertIn('Do not merge R1B', doc)

if __name__ == '__main__': unittest.main()
