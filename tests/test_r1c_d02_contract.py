from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class R1CD02Contract(unittest.TestCase):
    def test_source_backed_structural_counts_not_hooks(self):
        s=(JAVA/'R1CStaticShapeProbe.java').read_text()
        for token in ('void.class','Class.forName(className, false, loader)',
                      'com.instagram.feed.media.Media', 'MediaOption$Option',
                      'buttonAdderShape', 'OnClickListener.class',
                      'R1CDiscoveryPolicy.MAX_RAW_MATCHES',
                      'R1CDiscoveryPolicy.MAX_OWNER_CLASSES',
                      'R1CDiscoveryPolicy.MAX_METHODS_PER_OWNER',
                      'getDescriptor()', 'modernListShape',
                      'REEL_ITEM', 'MODERN_LIST_CANDIDATE_UNVERIFIED'):
            self.assertIn(token,s)
        for forbidden in ('.invoke(', 'hook(', 'setAccessible(', 'getText(',
                          'getOnClickListener(', 'performClick('):
            self.assertNotIn(forbidden,s)
    def test_shape_inventory_does_not_promote_identity(self):
        probe=(JAVA/'R1CMediaCandidateProbe.java').read_text()
        self.assertIn('INA_R1C_SHAPE id=',probe)
        self.assertIn('modern_list_methods=',probe)
        self.assertIn('R1CStaticShapeProbe.inspect',probe)
        self.assertIn('binding_hooks=off actions=off',probe)
        self.assertIn('identity_verified=false binding_hooks=off actions=off',probe)
        pol=(JAVA/'R1CDiscoveryPolicy.java').read_text()
        self.assertIn('MEDIA_IDENTITY_VERIFIED = false',pol)
        self.assertIn('BINDING_HOOKS_ENABLED = false',pol)
        self.assertIn('ACTION_HOOKS_ENABLED = false',pol)
        self.assertIn('return false;',pol)
        self.assertIn('MAX_RAW_MATCHES = 64',pol)
    def test_retains_dexkit_version_gate(self):
        ctl=(JAVA/'R1ProbeController.java').read_text()
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',ctl)
        policy=(JAVA/'R1CDiscoveryPolicy.java').read_text()
        self.assertIn('TARGET_VERSION = "455.0.0.8.72"',policy)
    def test_package_contract_and_updated_historical_docs(self):
        gradle=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 19',gradle)
        self.assertIn("versionName '0.5.0-r1c-d06-source-first-1'",gradle)
        script=(ROOT/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CStaticShapeProbe'",script)
        self.assertIn("b'INA_R1C_SHAPE'",script)
        self.assertIn('R1C_D02', (ROOT/'docs/R1C_D02_STATIC_SHAPE_NARROWING.md').read_text().replace('R1C-D02','R1C_D02'))
        self.assertIn('DEVICE_INVENTORY_PASS',(ROOT/'docs/R1C_D01_DEVICE_ACCEPTANCE.md').read_text())
if __name__=='__main__':unittest.main()
