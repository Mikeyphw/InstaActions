from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class D04Contract(unittest.TestCase):
    def test_apk_pin_and_strict_method(self):
        a=(JAVA/'R1CD04PassiveProbe.java').read_text()
        pol=(JAVA/'R1CD04Policy.java').read_text()
        self.assertIn('fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',pol)
        self.assertIn('TARGET_APK_BYTES = 238346353L',pol)
        self.assertIn('Class.forName(OWNER, false, loader)',a)
        self.assertIn('Class.forName(MEDIA, false, loader)',a)
        self.assertLess(a.index('String sha = sha256(apk)'),a.index('Class.forName(MEDIA, false, loader)'))
        for tok in ('matches != 1', '|| Modifier.isStatic(m.getModifiers())',
                    'args[1] != media','ArrayList.class', 'MAX_DECLARED_METHODS',
                    'MAX_RECORDED_EVENTS', 'INA_R1C_D04_GATE', 'INA_R1C_D04_EVENT'):
            self.assertIn(tok,a)
    def test_passive_interceptor_and_stable_lifecycle(self):
        s=(JAVA/'InstaActionsEntry.java').read_text()
        mon=(JAVA/'R1BContextMonitor.java').read_text()
        self.assertIn('R1CD04PassiveProbe.onActivityResumed(',s)
        self.assertIn('setId("ina-r1c-d04-reels-passive")',s)
        self.assertIn('setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)',s)
        self.assertIn('return observation.proceed();',s)
        self.assertIn('finally {',s)
        self.assertIn('counter.observe(observation.getArg(1),',s)
        self.assertIn('R1BContextMonitor.stableScreen()',s)
        self.assertIn('activeCount > 1',mon)
        self.assertIn('lastNavigation = R1BNavigationPolicy.Screen.UNKNOWN',mon)
        self.assertIn('private volatile R1BNavigationPolicy.Screen lastNavigation;',mon)
    def test_no_actions_or_value_access_and_version_consistency(self):
        a=(JAVA/'R1CD04PassiveProbe.java').read_text()
        for forbidden in ('getId(', 'getText(', 'getContentDescription(',
                          '.invoke(', 'setAccessible(', 'getOnClickListener(',
                          'performClick(', 'setOnClickListener(', 'getFields(', 'getDeclaredFields('):
            self.assertNotIn(forbidden,a)
        self.assertIn('ACTIONS_ENABLED = false',(JAVA/'R1CD04Policy.java').read_text())
        self.assertIn('BINDING_HOOKS_ENABLED = false',(JAVA/'R1CDiscoveryPolicy.java').read_text())
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',(JAVA/'R1ProbeController.java').read_text())
        self.assertIn('versionCode 12',(ROOT/'app/build.gradle').read_text())
        self.assertIn("versionName '0.4.3-r1c-d04'",(ROOT/'app/build.gradle').read_text())
        for path in (ROOT/'tests').glob('test_*.py'):
            for line in path.read_text().splitlines():
                if line.lstrip().startswith('self.assertIn(') and ('versionCode ' in line or "versionName '" in line):
                    self.assertIn('versionCode 12' if 'versionCode' in line else '0.4.3-r1c-d04',line,str(path))
        verifier=(ROOT/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CD04PassiveProbe'",verifier)
        self.assertIn("b'INA_R1C_D04_EVENT'",verifier)
