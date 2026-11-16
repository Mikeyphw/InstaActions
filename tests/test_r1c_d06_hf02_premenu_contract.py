from pathlib import Path
import unittest,json
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class Hf02Contract(unittest.TestCase):
    def test_keeps_source_first_conformance(self):
        s=(J/'R1CD06UpstreamConformance.java').read_text()
        for x in ('PLAYBACK_CONTROLS','UNSAVE','ClipsViewPagerImpl_getViewAtIndex','MATCH_EXACT_D04'):
            self.assertIn(x,s)
    def test_snapshot_weak_and_bounded(self):
        s=(J/'R1CD06PreMenuReconciliation.java').read_text()
        for x in ('WeakReference<Activity>','WeakReference<View>','MAX_AGE_MS = 3000L',
                  'getChildAdapterPosition','getCurrentItem','PAGE_INDEX_CHANGED',
                  'PREMENU_MEDIA_MATCH_CANDIDATE'):
            self.assertIn(x,s)
        for bad in ('Media.getId(','.getCaption(','.getText(','.performClick(',
                    'setResult(','.intercept(','.getUsername('):
            self.assertNotIn(bad,s)
    def test_structural_capture_and_callback_are_independent(self):
        pre=(J/'R1CD06ViewPreflight.java').read_text()
        hook=(J/'R1CD04PassiveProbe.java').read_text()
        self.assertIn('R1CD06PreMenuReconciliation.remember(',pre)
        self.assertIn('R1CD06PreMenuReconciliation.compare(',hook)
        self.assertIn('R1CD06VisiblePageBinding.observe(',hook)
        self.assertIn('verified=NO actions=off',hook)
    def test_hosted_apk_verifies_new_diagnostic_dex(self):
        v=(ROOT/'tools/verify_r1c_apk.py').read_text()
        for x in ('R1CD06PreMenuReconciliation','R1CD06PreMenuPolicy',
                  'INA_D06_PREMENU_RECONCILIATION_V1','PREMENU_MEDIA_MATCH_CANDIDATE'):
            self.assertIn(x,v)
    def test_report_and_session_boundaries(self):
        s=(J/'R1CD05FabSession.java').read_text()
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertIn('INA_D06_PREMENU_RECONCILIATION_V1',s)
        self.assertIn('MENU_HIDES_PAGER_PREMENU_PAGE_HAS_NO_DIRECT_MEDIA',s)
        self.assertIn('CURRENT_VISIBLE_PAGE=',s)
        self.assertIn('MEDIA_IDENTITY=NOT_VERIFIED',s)
        self.assertIn('R1CD06PreMenuReconciliation.clear()',p)
        self.assertIn('CAPTURE.isActiveWindow()',p)
        self.assertIn('R1CD06PreMenuPolicy.samePosition', (J/'R1CD06PreMenuReconciliation.java').read_text())
    def test_version_and_no_actions(self):
        d=json.loads((ROOT/'project.json').read_text())
        self.assertFalse(d['r1c_d06_visible_media_verified'])
        self.assertFalse(d['r1c_d06_action_hooks_enabled'])
        self.assertEqual(d['r1c_d06_hf02_device_acceptance'],'NOT_RUN')
if __name__=='__main__':unittest.main()
