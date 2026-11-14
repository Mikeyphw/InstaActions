"""HF08: FAB privacy guards inherited from FAB01 and D07 UX01.

The failed D15 staged suite forbade View identifier reads in the FAB controller.
The repair compares Android-owned references instead of reading resource IDs.
"""
import unittest
from pathlib import Path
import json
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
class FabPrivacyFix(unittest.TestCase):
    def test_reproduces_legacy_fab01_forbidden_view_id(self):
        text=(J/'R1CD05FabPanel.java').read_text()
        for marker in ('getId(', 'getContentDescription(', 'getDeclaredFields(',
                       'setAccessible(', 'WindowManager.addView', 'SYSTEM_ALERT_WINDOW'):
            self.assertNotIn(marker,text,marker)
    def test_reproduces_legacy_d07_ux01_privacy(self):
        text=(J/'R1CD05FabPanel.java').read_text()
        for marker in ('.getId(', '.getId()', 'Class.forName(', '.getContentDescription('):
            self.assertNotIn(marker,text,marker)
    def test_direct_parent_reference_check(self):
        text=(J/'R1CD05FabPanel.java').read_text()
        for marker in ('View content = activity.findViewById(android.R.id.content)',
                        '(root == decor || content == decor)',
                        'decor.isAttachedToWindow()', 'current.healthy(target)',
                        'REFUSED_RETRY_CAP_REACHED'):
            self.assertIn(marker,text)
    def test_no_early_action_grants(self):
        data=json.loads((P/'project.json').read_text())
        self.assertTrue(data['r1c_d15_hf08_privacy_source_implemented'])
        self.assertFalse(data['r1c_d15_hf08_hosted_ci_qualified'])
        self.assertEqual(data['r1c_d15_hf08_device_acceptance'],'NOT_RUN')
        self.assertFalse(data['r1c_d14_actions_enabled'])
        self.assertFalse(data['r1c_d12_automatic_account_rearm'])
if __name__=='__main__':unittest.main()
