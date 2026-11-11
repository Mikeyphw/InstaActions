"""Source restrictions and provenance for the InstaEclipse-derived D06 owner probe."""
from pathlib import Path
import unittest
import json
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class D06UpstreamContract(unittest.TestCase):
    def test_upstream_source_and_license_are_explicit(self):
        s=(JAVA/'R1CD06MenuOwnership.java').read_text()
        note=(ROOT/'third_party/InstaEclipse/NOTICE.md').read_text()
        for x in ('SPDX-License-Identifier: GPL-3.0-only',
                  'SelectedMediaAdapter.java', 'e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d',
                  'MAX_FIELDS = 64', 'MAX_CLASSES = 6', 'field.getType() != mediaType',
                  'selected != value', 'State.AMBIGUOUS', 'State.BUDGET_EXCEEDED'):
            self.assertIn(x,s)
        self.assertIn('GPL-3.0-only',note)
        self.assertIn('GNU GENERAL PUBLIC LICENSE',
                      (ROOT/'third_party/InstaEclipse/LICENSE-GPL-3.0.txt').read_text())
    def test_scope_and_hook_remain_passive_and_exact(self):
        entry=(JAVA/'InstaActionsEntry.java').read_text()
        probe=(JAVA/'R1CD04PassiveProbe.java').read_text()
        policy=(JAVA/'R1CD04Policy.java').read_text()
        self.assertIn('fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',policy)
        self.assertIn('return observation.proceed();',entry)
        self.assertEqual(entry.count('return observation.proceed();'),1)
        self.assertIn('observation.getThisObject()',entry)
        self.assertIn('observation.getArg(0)',entry)
        self.assertIn('counter.observe(observation.getArg(1),',entry)
        self.assertIn('R1CD06MenuOwnership.resolve(',probe)
        self.assertIn('INA_R1C_D06_OWNER ordinal=',probe)
        self.assertIn('static boolean beginFabCapture(',probe)
        self.assertIn('counter.invalidate();',probe)
        self.assertIn('MAX_RECORDED_EVENTS',probe)
        self.assertIn('ACTIONS_ENABLED = false',policy)
        for forbidden in ('.invoke(', 'getId(', 'getText(', 'getContentDescription(',
                          'getOnClickListener(', 'performClick('):
            self.assertNotIn(forbidden,probe)
        for forbidden in ('getId(', 'getText(', 'getCaption(', 'getUsername(',
                          'getContentDescription(', 'identityHashCode(', 'toString(',
                          '.invoke(', 'setOnClickListener(', 'performClick('):
            self.assertNotIn(forbidden,(JAVA/'R1CD06MenuOwnership.java').read_text())
    def test_fab_reports_truthful_separate_owner_and_reference_counts(self):
        session=(JAVA/'R1CD05FabSession.java').read_text()
        panel=(JAVA/'R1CD05FabPanel.java').read_text()
        for term in ('INA_D05_FAB_REPORT_V1', 'INA_D06_MENU_OWNER_REPORT_V1',
                     'D06_VISIBLE_BINDING=NOT_VERIFIED', 'OWNER_DIRECT_MATCH',
                     'CONTEXT_DIRECT_MATCH', 'ownerCounts', 'MAX_WINDOW_MS = 60_000L'):
            self.assertIn(term,session)
        self.assertIn('R1CD04PassiveProbe.beginFabCapture(',panel)
        self.assertIn('InstaEclipse stays ON',panel)
        self.assertNotIn('SYSTEM_ALERT_WINDOW',panel)
    def test_metadata_and_tests_do_not_claim_unrun_android(self):
        data=json.loads((ROOT/'project.json').read_text())
        self.assertEqual(data['scope_packages'],['com.instagram.android'])
        self.assertTrue(data['r1c_d06_source_implemented'])
        self.assertFalse(data['r1c_d06_visible_media_verified'])
        self.assertFalse(data['r1c_d06_menu_owner_verified'])
        self.assertFalse(data['r1c_d06_hosted_ci_qualified'])
        self.assertEqual(data['r1c_d06_device_acceptance'],'NOT_RUN')
        self.assertIn('versionCode 15',(ROOT/'app/build.gradle').read_text())
        self.assertIn('R1CD06MenuOwnership', (ROOT/'tools/verify_r1c_apk.py').read_text())
        self.assertTrue((ROOT/'app/src/test/java/com/mikeyphw/instaactions/R1CD06MenuOwnershipTest.java').is_file())
if __name__=='__main__': unittest.main()
