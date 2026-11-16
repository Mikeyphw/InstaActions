from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class R1CD02HF01APKContract(unittest.TestCase):
    def test_apk_evidence_guard_and_noninvocation(self):
        src=(JAVA/'R1CStaticShapeProbe.java').read_text()
        for term in ('Linstagram/features/stories/fragment/ReelViewerFragment;',
                     'Lcom/instagram/model/reels/ReelItem;',
                     'getDescriptor()', 'MAX_RAW_MATCHES',
                     'modernListShape', 'MODERN_LIST_CANDIDATE_UNVERIFIED',
                     'Class.forName(className, false, loader)'):
            self.assertIn(term,src)
        for forbidden in ('.invoke(', 'getMethodInstance(', 'hook(', 'performClick(', 'getText('):
            self.assertNotIn(forbidden,src)
        self.assertIn('return false;', (JAVA/'R1CDiscoveryPolicy.java').read_text())
        self.assertIn('MUTATING_ACTIONS_ENABLED = false',(JAVA/'R1ProbePolicy.java').read_text())
    def test_historical_version_contracts_are_not_stale(self):
        for path in (ROOT/'tests').glob('test_r1b_hf*_contract.py'):
            src=path.read_text()
            self.assertNotIn("versionName '0.4.1-r1c-d02-hf01'",src,path.name)
            self.assertIn("versionName '0.5.2-r1c-d07-repost-experimental'",src,path.name)
    def test_feed_and_story_rules_are_independent(self):
        src=(JAVA/'R1CStaticShapeProbe.java').read_text()
        self.assertIn('if ("story_page_bind".equals(marker))',src)
        self.assertIn('descriptor.endsWith(")V")',src)
        self.assertIn('p[p.length - 1] == int.class ? 2 : 1',src)
        self.assertIn('if (methods.length > R1CDiscoveryPolicy.MAX_METHODS_PER_OWNER)',src)
        report=(ROOT/'docs/R1C_D02_HF01_APK455_STATIC_EVIDENCE.md').read_text()
        self.assertIn('fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',report)
