from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class R1CD03Contract(unittest.TestCase):
    def test_independent_query_and_exact_descriptor(self):
        p=(JAVA/'R1CModernReelsPolicy.java').read_text()
        s=(JAVA/'R1CMediaCandidateProbe.java').read_text()
        for token in ('X.07EF', '->A05(LX/03jS;Lcom/instagram/feed/media/Media;)',
                      'MAX_MATCHES = 64', 'IG455_EXACT_DESCRIPTOR_CANDIDATE',
                      'NO_EXACT_DESCRIPTOR_MATCH','INCOMPLETE'):
            self.assertIn(token,p)
        for token in ('INA_R1C_DIRECT_REELS raw=',
                      '.declaredClass(R1CModernReelsPolicy.OWNER)',
                      '.paramCount(2)', '.returnType("java.util.ArrayList")',
                      'getDescriptor()', 'binding_hooks=off actions=off'):
            self.assertIn(token,s)
        for text in (p,s):
            for forbidden in ('getMethodInstance(', '.invoke(', 'setAccessible(',
                              'hook(', 'getText(', 'performClick('):
                self.assertNotIn(forbidden,text)
    def test_stale_contract_audit_and_original_guards(self):
        gradle=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 14',gradle)
        self.assertIn("versionName '0.4.5-r1c-d05-fab01'",gradle)
        for p in (ROOT/'tests').glob('test_*.py'):
            if p.name == 'test_r1c_d03_contract.py': continue
            for line in p.read_text().splitlines():
                if line.lstrip().startswith('self.assertIn(') and ('versionCode ' in line or "versionName '" in line):
                    self.assertIn('versionCode 14' if 'versionCode' in line else '0.4.5-r1c-d05-fab01',line,p.name)
        disc=(JAVA/'R1CDiscoveryPolicy.java').read_text()
        self.assertIn('MEDIA_IDENTITY_VERIFIED = false',disc)
        self.assertIn('BINDING_HOOKS_ENABLED = false',disc)
        self.assertIn('ACTION_HOOKS_ENABLED = false',disc)
        self.assertIn('TARGET_VERSION = "455.0.0.8.72"',disc)
        r1=(JAVA/'R1ProbeController.java').read_text()
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',r1)
        apk=(ROOT/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CModernReelsPolicy'",apk)
        self.assertIn("b'INA_R1C_DIRECT_REELS'",apk)
