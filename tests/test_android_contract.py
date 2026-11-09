import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / 'app/src/main/resources/META-INF/xposed'

class AndroidR0Contract(unittest.TestCase):
    def test_project_stage_and_scope(self):
        project = json.loads((ROOT / 'project.json').read_text())
        self.assertEqual(project['phase'], 'r0')
        self.assertTrue(project['r0_android_project_implemented'])
        self.assertEqual(project['scope_packages'], ['com.instagram.android'])

    def test_modern_module_metadata(self):
        entry = (META / 'java_init.list').read_text().strip()
        self.assertEqual(entry, 'com.mikeyphw.instaactions.InstaActionsEntry')
        self.assertEqual((META / 'scope.list').read_text().strip(), 'com.instagram.android')
        prop = dict(line.split('=', 1) for line in
                    (META / 'module.prop').read_text().splitlines() if line.strip())
        self.assertEqual(prop['minApiVersion'], '102')
        self.assertEqual(prop['targetApiVersion'], '102')
        self.assertEqual(prop['staticScope'], 'false')

    def test_entry_is_noop_and_api102(self):
        entry = (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/InstaActionsEntry.java').read_text()
        self.assertIn('extends XposedModule', entry)
        self.assertIn('onPackageReady', entry)
        self.assertNotIn('.hook(', entry)
        self.assertNotIn('DexKit', entry)
        self.assertIn('com.instagram.android',
                      (ROOT / 'app/src/main/java/com/mikeyphw/instaactions/ModuleConstants.java').read_text())

    def test_hosted_sdk_setup_does_not_request_removed_tools(self):
        ci = (ROOT / '.github/workflows/android-r0.yml').read_text()
        self.assertIn('android-actions/setup-android@v4', ci)
        self.assertIn("packages: 'platform-tools'", ci)
        self.assertNotIn('android-actions/setup-android@v3', ci)
        self.assertNotIn("sdkmanager --install 'tools'", ci)
        self.assertIn('actions/checkout@v7', ci)
        self.assertIn('actions/setup-java@v6', ci)
        self.assertIn('gradle/actions/setup-gradle@v6', ci)
        self.assertIn('cache-provider: basic', ci)
        self.assertIn('runs-on: ubuntu-24.04', ci)
        self.assertIn('python3 -m unittest discover', ci)

    def test_ci_is_hosted_and_has_apk_proof(self):
        ci = (ROOT / '.github/workflows/android-r0.yml').read_text()
        for token in ('gradle-version:', ':app:assembleDebug', ':app:testDebugUnitTest',
                      ':app:assembleDebugAndroidTest', 'verify_r0_apk.py', 'upload-artifact'):
            self.assertIn(token, ci)
        self.assertIn("compileOnly 'io.github.libxposed:api:102.0.0'",
                      (ROOT / 'app/build.gradle').read_text())

if __name__ == '__main__':
    unittest.main()
