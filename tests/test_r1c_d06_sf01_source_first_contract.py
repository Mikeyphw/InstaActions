from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class SourceFirstContract(unittest.TestCase):
    def test_exact_source_anchors(self):
        s=(JAVA/'R1CD06UpstreamConformance.java').read_text()
        for needle in ('e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d',
                       'PLAYBACK_CONTROLS', 'UNSAVE',
                       'ClipsViewPagerImpl_getViewAtIndex','R1CModernReelsPolicy.exact',
                       'MAX_QUERY_RESULTS = 32','MAX_PAGER_FIELDS = 512',
                       'UNIQUE_WITH_VIEWPAGER2_FIELD','DIFFERENT_METHOD',
                       'AMBIGUOUS_VIEWPAGER2_FIELDS'):
            self.assertIn(needle,s)
    def test_after_sha_gate_only(self):
        s=(JAVA/'R1CD04PassiveProbe.java').read_text()
        self.assertLess(s.index('validBuild('),s.index('installedCounter = counter;'))
        self.assertLess(s.index('installedCounter = counter;'),s.index('startAfterExactGate('))
        s2=(JAVA/'R1CD06UpstreamConformance.java').read_text()
        self.assertIn('R1CD04PassiveProbe.isInstalled()',s2)
        self.assertIn('Class.forName(matches.get(0).getClassName(), false, loader)',s2)
    def test_no_hook_or_media_data_read(self):
        s=(JAVA/'R1CD06UpstreamConformance.java').read_text()
        for bad in ('hook(', '.intercept(', '.getId(', '.getUsername(', '.getCaption(',
                    'getSharedPreferences(', 'getPrimaryClip(', 'setResult(',
                    'field.get(', 'getText(', 'Media.get'):
            self.assertNotIn(bad,s)
    def test_panel_and_report_connected(self):
        panel=(JAVA/'R1CD05FabPanel.java').read_text()
        session=(JAVA/'R1CD05FabSession.java').read_text()
        entry=(JAVA/'InstaActionsEntry.java').read_text()
        self.assertIn('R1CD06UpstreamConformance.summary()',panel)
        self.assertIn('R1CD06UpstreamConformance.report()',session)
        self.assertIn('targetApk, moduleNativeLibDir, instagramLoader',entry)
        self.assertIn('INA_D06_CONSOLIDATED_FINDINGS_V1',session)
        self.assertIn('NEXT_REQUIRED_CONFIRMATION=',session)
        self.assertIn('INA_D06_DEEP_DIAGNOSIS_V1',session)
        self.assertIn('INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1',session)
    def test_acceptance_remains_unqualified(self):
        import json
        d=json.loads((ROOT/'project.json').read_text())
        self.assertTrue(d['r1c_d06_source_first_ci_qualified'])
        self.assertEqual(d['r1c_d06_source_first_device_acceptance'],'USER_REPORTED_IG455_SOURCE_CONFORMANCE_ONLY')
        self.assertFalse(d['r1c_d06_visible_media_verified'])
        self.assertIn('ONLY', (ROOT/'docs/R1C_D06_SOURCE_FIRST_FINDINGS.md').read_text().upper())
if __name__=='__main__':unittest.main()
