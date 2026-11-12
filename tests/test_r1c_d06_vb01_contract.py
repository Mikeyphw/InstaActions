"""D06 VB01: strict visible page candidate, no media identity promotion."""
from pathlib import Path
import json
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class VB01(unittest.TestCase):
    def test_upstream_source_and_full_ambiguous_page_refusal(self):
        a=(JAVA/'R1CD06VisiblePageBinding.java').read_text()
        for token in ('ReelsControlsHook', 'ReelDownloadHook', 'ViewPager2',
                'getOrientation','getCurrentItem','getChildAdapterPosition',
                'AMBIGUOUS_PAGERS','AMBIGUOUS_CURRENT_PAGE', 'NO_DIRECT_MEDIA',
                'VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE', 'MAX_RUNTIME_MS = 35L',
                'getGlobalVisibleRect','Looper.getMainLooper()', 'hasType('):
            self.assertIn(token,a)
        for forbidden in ('getId(', 'getCaption(', 'getUsername(', 'setOnClickListener(',
                          'performClick(', 'setResult(', 'sendBroadcast(', 'startActivity('):
            self.assertNotIn(forbidden,a)
    def test_candidate_compared_to_exact_media_on_view_owned_fields_only(self):
        a=(JAVA/'R1CD06DirectViewMedia.java').read_text()
        for token in ('field.getType() != mediaClass','selected != value',
                      'MAX_VIEWS = 160','MAX_FIELDS = 4096','MAX_MEDIA_FIELDS = 16',
                      'Result.AMBIGUOUS','Result.MISMATCH', 'Result.VIEW_COUNT_LIMIT'):
            self.assertIn(token,a)
        for forbidden in ('getId(', 'getCaption(', 'getUsername(', 'getTag(',
                          '.invoke(', 'getDeclaredMethods(', 'getText('):
            self.assertNotIn(forbidden,a)
    def test_main_entry_keeps_single_passive_hook(self):
        entry=(JAVA/'InstaActionsEntry.java').read_text()
        hook=(JAVA/'R1CD04PassiveProbe.java').read_text()
        self.assertEqual(entry.count('return observation.proceed();'),1)
        self.assertIn('R1CD06VisiblePageBinding.observe(',hook)
        self.assertIn('INA_R1C_D06_VISIBLE_PAGE',hook)
        self.assertIn('WeakReference<Activity>',hook)
        self.assertIn('R1CD05FabPanel.hasActiveWindow()',hook)
        self.assertIn('counter.clearActivity()',hook)
        self.assertIn('R1CD05FabPanel.record(',hook)
        self.assertNotIn('Media.getId(',hook)
    def test_fab_source_and_privacy(self):
        panel=(JAVA/'R1CD05FabPanel.java').read_text()
        session=(JAVA/'R1CD05FabSession.java').read_text()
        self.assertIn('CAPTURE.currentVisiblePage()',panel)
        self.assertIn('D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED',session)
        self.assertIn('INA_D06_VISIBLE_PAGE_REPORT_V1',session)
        self.assertIn('matches != stageEvents[i]',session)
        self.assertIn('MEDIA_IDENTITY=NOT_VERIFIED',session)
        self.assertNotIn('setScope(',panel)
    def test_version_and_unqualified_identity(self):
        p=json.loads((ROOT/'project.json').read_text())
        self.assertEqual(p['module_version'],'0.5.0-r1c-d06-source-first-1')
        self.assertTrue(p['r1c_d06_visible_page_source'])
        self.assertFalse(p['r1c_d06_visible_page_ci_qualified'])
        self.assertFalse(p['r1c_d06_visible_media_verified'])
        self.assertFalse(p['r1c_d06_action_hooks_enabled'])
        self.assertEqual(p['r1c_d06_visible_page_device_acceptance'],'NOT_RUN')
        self.assertEqual(p['r1c_d05_fab_instaeclipse_scope_policy'],'KEEP_ENABLED')
        self.assertIn("versionCode 19",(ROOT/'app/build.gradle').read_text())
        self.assertIn("b'INA_R1C_D06_VISIBLE_PAGE'", (ROOT/'tools/verify_r1c_apk.py').read_text())
if __name__=='__main__': unittest.main()
