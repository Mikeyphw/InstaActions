"""Fail-closed upstream-derived D06 budget HF01 regression contract."""
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class D06HF01(unittest.TestCase):
    def test_separate_budgets_and_explicit_owner_context_reasons(self):
        s=(JAVA/'R1CD06MenuOwnership.java').read_text()
        for t in ('MAX_CLASSES = 12', 'MAX_FIELDS_PER_CLASS = 1024',
                  'MAX_TOTAL_DECLARED_FIELDS = 2048','MAX_MEDIA_FIELDS = 16',
                  'OWNER_DECLARED_FIELD_LIMIT','CONTEXT_DECLARED_FIELD_LIMIT',
                  'OWNER_CLASS_DEPTH_LIMIT','CONTEXT_CLASS_DEPTH_LIMIT',
                  'OWNER_MEDIA_FIELD_LIMIT','CONTEXT_MEDIA_FIELD_LIMIT',
                  'fields.length > MAX_TOTAL_DECLARED_FIELDS - declared',
                  '++mediaFields > MAX_MEDIA_FIELDS'):
            self.assertIn(t,s)
        self.assertNotIn('MAX_FIELDS = 64',s)
        for forbidden in ('.invoke(', 'getId(', 'getCaption(', 'getUsername(',
                          'identityHashCode(', 'getOnClickListener('):
            self.assertNotIn(forbidden,s)
    def test_no_false_positive_from_one_matching_callback(self):
        s=(JAVA/'R1CD05FabSession.java').read_text()
        self.assertIn('accepted != stageEvents[i]',s)
        self.assertIn('Failures take precedence over a previous matching callback',s)
        self.assertIn('D06_VISIBLE_BINDING=NOT_VERIFIED',s)
        self.assertIn('INA_D06_MENU_OWNER_REPORT_V1',s)
    def test_java_negative_suite_covers_dense_owner(self):
        s=(ROOT/'app/src/test/java/com/mikeyphw/instaactions/R1CD06MenuOwnershipTest.java').read_text()
        for name in ('irrelevantObfuscatedFieldsDoNotSpendMediaBudget',
                     'matchingMediaFieldBudgetStillRejectsTruncation',
                     'deepOwnerHierarchyFailsWithPreciseReason',
                     'contextBudgetIsDistinctFromOwnerBudget'):
            self.assertIn(name,s)
    def test_exact_ig455_gate_and_policy_unchanged(self):
        s=(JAVA/'R1CD04Policy.java').read_text()
        self.assertIn('fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',s)
        self.assertIn('ACTIONS_ENABLED = false',s)
        e=(JAVA/'InstaActionsEntry.java').read_text()
        self.assertIn('return observation.proceed();',e)
        self.assertEqual(e.count('return observation.proceed();'),1)
if __name__=='__main__':unittest.main()
