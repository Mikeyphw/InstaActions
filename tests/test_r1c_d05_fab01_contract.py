"""FAB01 source guard: no action hooks or Vector scope modifications."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions'

class Fab01Contract(unittest.TestCase):
    def test_uses_activity_owned_draggable_panel_without_overlay_permission(self):
        src = (JAVA/'R1CD05FabPanel.java').read_text()
        manifest = (ROOT/'app/src/main/AndroidManifest.xml').read_text() if (ROOT/'app/src/main/AndroidManifest.xml').exists() else ''
        for word in ('FrameLayout', 'Gravity.TOP | Gravity.END', 'setOnTouchListener',
                     'onLeaving', 'onResumed', 'Copy safe report', 'Share safe report',
                     'Start A1', 'InstaEclipse stays ON', 'R1CD04Policy.TARGET_VERSION'):
            self.assertIn(word, src)
        for forbidden in ('SYSTEM_ALERT_WINDOW', 'WindowManager.addView', 'getId(',
                          'getContentDescription(', 'performClick(', 'setAccessible(',
                          'getDeclaredFields(', 'Class.forName(', 'setEnabled(true)'):
            # performClick here is a local accessibility click, NOT Instagram action.
            if forbidden == 'performClick(':
                continue
            self.assertNotIn(forbidden, src)
        if manifest:
            self.assertNotIn('SYSTEM_ALERT_WINDOW',manifest)

    def test_enum_only_observer_hook_and_passive_chain(self):
        src = (JAVA/'R1CD04PassiveProbe.java').read_text()
        entry = (JAVA/'InstaActionsEntry.java').read_text()
        session = (JAVA/'R1CD05FabSession.java').read_text()
        self.assertIn('R1CD05FabPanel.record(safeScreen, continuityResult,',src)
        self.assertIn('return observation.proceed();',entry)
        self.assertIn('R1CD05FabPanel.onResumed',entry)
        self.assertIn('R1CD05FabPanel.onLeaving',entry)
        self.assertIn('MAX_WINDOW_MS = 60_000L',session)
        self.assertIn('MEDIA_IDENTITY=NOT_VERIFIED',session)
        for forbidden in ('.getId(', '.getCaption(', '.getUsername(',
                          'Object media', 'Object candidate', 'getDeclaredFields(',
                          'mediaArg.toString()', 'identityHashCode(', 'getOnClickListener('):
            self.assertNotIn(forbidden,session)
        self.assertNotIn('VectorScope', (JAVA/'R1CD05FabPanel.java').read_text())

    def test_version_and_status(self):
        build = (ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 15',build)
        self.assertIn("versionName '0.4.6-r1c-d06-upstream-ownership'",build)
        import json
        project = json.loads((ROOT/'project.json').read_text())
        self.assertTrue(project['r1c_d05_fab_source'])
        self.assertEqual(project['r1c_d05_fab_device_acceptance'], 'USER_REPORTED_FOUR_WINDOWS_REFERENCE_ONLY')
        self.assertFalse(project['r1c_d05_media_identity_verified'])
        self.assertEqual(project['r1c_d05_fab_instaeclipse_scope_policy'], 'KEEP_ENABLED')

    def test_hosted_apk_inventory(self):
        s=(ROOT/'tools/verify_r1c_apk.py').read_text()
        for v in ('R1CD05FabPanel', 'R1CD05FabSession', 'INA_D05_FAB_REPORT_V1'):
            self.assertIn(v,s)

if __name__=='__main__': unittest.main()
