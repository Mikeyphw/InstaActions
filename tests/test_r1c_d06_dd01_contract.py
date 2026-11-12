import json, unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class DeepDiagnosticContract(unittest.TestCase):
    def test_content_free_bounded_preflight(self):
        s=(JAVA/'R1CD06ViewPreflight.java').read_text()
        for marker in ('MAX_NODES = 384', 'MAX_MS = 20L', 'MAX_SAMPLES_PER_WINDOW = 12',
                       'NO_VIEWPAGER2', 'NO_VERTICAL_PAGER', 'AMBIGUOUS_PAGERS', 'AMBIGUOUS_RECYCLERS','AMBIGUOUS_CENTERED_PAGES','PAGER_TOO_SMALL',
                       'VIEWER_STRUCTURE_FOUND', 'getOrientation', 'CENTERED_PAGE'):
            if marker=='CENTERED_PAGE': marker='NO_CENTERED_PAGE'
            self.assertIn(marker,s)
        for forbidden in ('getText(', 'getTag(', 'getId(', 'getUsername(', 'getCaption(', 'Media.class', 'performClick(', 'setResult('):
            self.assertNotIn(forbidden,s)
    def test_report_and_state_are_capped(self):
        s=(JAVA/'R1CD05FabSession.java').read_text()
        self.assertIn('INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1',s)
        self.assertIn('preflightSamples[i] < R1CD06ViewPreflight.MAX_SAMPLES_PER_WINDOW',s)
        self.assertIn('MEDIA_IDENTITY=NOT_VERIFIED',s)
        self.assertIn('D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED',s)
        p=(JAVA/'R1CD05FabPanel.java').read_text()
        self.assertIn('R1CD06ViewPreflight.inspect(activity)',p)
        self.assertIn('CAPTURE.currentPreflight()',p)
    def test_failure_reasons_not_conflated(self):
        s=(JAVA/'R1CD06VisiblePageBinding.java').read_text()
        d=(JAVA/'R1CD06DirectViewMedia.java').read_text()
        for marker in ('DECOR_VIEW_LIMIT','DECOR_TIME_LIMIT','PAGE_VIEW_LIMIT','PAGE_TIME_LIMIT','DIRECT_MEDIA_FIELD_LIMIT','PAGER_METHOD_FAILED','RECYCLER_METHOD_FAILED'):
            self.assertIn(marker,s)
        for marker in ('VIEW_COUNT_LIMIT','CLASS_DEPTH_LIMIT','DECLARED_FIELD_LIMIT','MEDIA_FIELD_LIMIT'):
            self.assertIn(marker,d)
    def test_version_and_unchanged_action_policy(self):
        data=json.loads((ROOT/'project.json').read_text())
        self.assertEqual(data['module_version'],'0.5.1-r1c-d06-premenu-hf02')
        self.assertTrue(data['r1c_d06_deep_diagnostics_source'])
        self.assertTrue(data['r1c_d06_deep_diagnostics_ci_qualified'])
        self.assertFalse(data['r1c_d06_action_hooks_enabled'])
        self.assertEqual(data['r1c_d05_fab_instaeclipse_scope_policy'],'KEEP_ENABLED')
        self.assertIn('versionCode 20',(ROOT/'app/build.gradle').read_text())
if __name__=='__main__':unittest.main()
