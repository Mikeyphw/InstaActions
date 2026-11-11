from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions/InstaActionsEntry.java'

class R1BRuntimeDiagnosticsContract(unittest.TestCase):
    def test_earliest_hook_callbacks_have_framework_and_android_logs(self):
        s = JAVA.read_text(encoding='utf-8')
        assert s.index('INA_DIAG_MODULE_LOADED') < s.index('getModuleApplicationInfo()')
        self.assertIn('Log.i(DIAGNOSTIC_TAG, event)', s)
        self.assertIn('log(Log.INFO, null, event)', s)
        self.assertIn('onPackageLoaded(PackageLoadedParam param)', s)
        self.assertIn('onPackageReady(PackageReadyParam param)', s)
        self.assertIn('INA_DIAG_PACKAGE_LOADED', s)
        self.assertIn('INA_DIAG_PACKAGE_READY', s)
        self.assertIn('INA_DIAG_GUARD_SKIP reason=', s)
        self.assertIn('INA_DIAG_MODULE_INFO_FAILED type=', s)

    def test_no_unexpected_hooks_or_user_content(self):
        s = JAVA.read_text(encoding='utf-8')
        self.assertEqual(s.count('hook(resumed)'), 1)
        self.assertEqual(s.count('hook(leaving)'), 1)
        self.assertIn('chain.proceed()', s)
        self.assertIn('INA_DIAG_FIRST_ACTIVITY_RESUMED actions=off', s)
        self.assertIn('R1BContextMonitor.resumed', s)
        self.assertIn('R1BContextMonitor.paused', s)
        for forbidden in ('getText(', 'getOnClickListener(', 'performClick(',
                          'getSharedPreferences(', 'HttpURLConnection',
                          'getAccounts(', 'getCookie(', 'getMessage('):
            self.assertNotIn(forbidden,s)

    def test_bumped_version_and_diagnostics_doc(self):
        gradle = (ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 14',gradle)
        self.assertIn("versionName '0.4.5-r1c-d05-fab01'",gradle)
        doc = (ROOT/'docs/R1B_HF01_RUNTIME.md').read_text()
        for key in ('INA_DIAG_MODULE_LOADED', 'INA_DIAG_PACKAGE_READY',
                    'INA_DIAG_HOOKS_INSTALLED', 'INA_DIAG_FIRST_ACTIVITY_RESUMED'):
            self.assertIn(key,doc)
