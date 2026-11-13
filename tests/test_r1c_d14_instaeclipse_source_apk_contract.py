"""D14 source/IG455 APK evidence audit: no duplicate Instagram hook, no action changes."""
import json,shutil,subprocess,tempfile,unittest
from pathlib import Path
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
class D14Source(unittest.TestCase):
    def test_source_evidence_and_status(self):
        d=json.loads((P/'project.json').read_text())
        for flag in ('r1c_d14_exact_apk_native_origin_source_implemented',
                     'r1c_d14_instaeclipse_build_source_audited'):
            self.assertIs(d[flag],True,flag)
        for flag in ('r1c_d14_verified_feed_reel_origin','r1c_d14_verified_account_identity',
                     'r1c_d14_actions_enabled','r1c_d14_native_hooks_added',
                     'r1c_d14_ci_qualified'):
            self.assertIs(d[flag],False,flag)
        self.assertEqual(d['r1c_d14_device_acceptance'],'NOT_RUN')
        audit=(P/'docs/R1C_D14_HF06_INSTAECLIPSE_IG455_AUDIT.md').read_text()
        for proof in ('b1308bdf4edc2956671fe5a0ec04972ba77a25edd51e144072b9b157755d2dc5',
                      'fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',
                      'ee1c81cadfbcd9fbbee4d01568ee5ca4b2653fbc','PLAYBACK_CONTROLS',
                      'UNSAVE','ClipsViewerConfig.A00','REEL_FEED_TIMELINE'):
            self.assertIn(proof,audit)
    def test_reuse_d04_original_hook_not_competing_or_modifying(self):
        entry=(J/'InstaActionsEntry.java').read_text()
        self.assertIn('R1CD14NativeOriginProbe.prepare(instagramLoader);',entry)
        self.assertIn('R1CD14NativeOriginProbe.observe(',entry)
        self.assertEqual(entry.count('setId("ina-r1c-d04-reels-passive")'),1)
        self.assertNotIn('setId("ina-d14-',entry)
        src=(J/'R1CD14NativeOriginProbe.java').read_text()
        for evidence in ('getDeclaredField("A08")','getDeclaredField("A00")',
                         'outer.getType()!=config','inner.getType()!=source',
                         'source.isEnum()','((Enum<?>)source).name()',
                         'recordFeature(0,viewer','EXACT_IG455_FIELD_SHAPES_READY_READ_ONLY',
                         'UNAVAILABLE_FAIL_CLOSED','AUTO_REARM=FORBIDDEN'):
            self.assertIn(evidence,src)
        for forbidden in ('getUsername(', 'getUserId(', 'getId(', 'getToken(',
                          'getData(', 'getText(', 'getContentDescription(',
                          'getExtras(', 'setResult(', 'hook(', 'performClick(', 'R1CD07Recovery.arm('):
            self.assertNotIn(forbidden,src)
        ui=(J/'R1CD05FabPanel.java').read_text()
        self.assertGreaterEqual(ui.count('R1CD14NativeOriginProbe.report()'),4)
        for p in ('R1CD07Recovery.java','R1CD07GateTrace.java','R1CD07PredicateTrace.java'):
            self.assertTrue((J/p).is_file(),p)
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK required')
    def test_source_categories_and_ab_isolation(self):
        sources=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
                 'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
                 'R1CD09DiagnosticGate','R1CD09AccountSentinel','R1CD10ComparisonFlow',
                 'R1CD10PassiveLedger','R1CD10ReportArchive','R1CD12AccountLifecycle',
                 'R1CD13AutoSurface','R1CD14NativeOriginProbe']
        smoke=P/'tests/java/com/mikeyphw/instaactions/R1CD14OriginSmoke.java'
        with tempfile.TemporaryDirectory(prefix='ina-d14-smoke-') as tmp:
            r=subprocess.run(['javac','-encoding','UTF-8','-d',tmp]+
                             [str(J/(x+'.java')) for x in sources]+[str(smoke)],
                             capture_output=True,text=True,timeout=90)
            self.assertEqual(r.returncode,0,r.stderr)
            r=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD14OriginSmoke'],
                             capture_output=True,text=True,timeout=45)
            self.assertEqual(r.returncode,0,r.stderr+r.stdout)
            self.assertIn('INA_D14_SOURCE_ENUM_READONLY_AB_PASS=',r.stdout)
if __name__=='__main__':unittest.main()
