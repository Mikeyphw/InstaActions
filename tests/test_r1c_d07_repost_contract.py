"""Source-level D07 contracts; physical acceptance is a separate gate."""
from pathlib import Path
import json,re,unittest,shutil,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class D07RepostContract(unittest.TestCase):
    def test_exact_ig455_reuses_existing_d04_attestation(self):
        s=(J/'InstaActionsEntry.java').read_text()
        self.assertIn('R1CD04PassiveProbe.isInstalled()',s)
        self.assertIn('InstaActionsEntry.requestD07(activity)',(J/'R1CD05FabPanel.java').read_text())
        self.assertEqual(s.count('sha256('),0)
        self.assertEqual(s.count('deoptimize(R1CD07GateTrace.uiMethod(loader))'),1)
        self.assertIn('D07_MAX_DATA_HOOKS = 3',s)
    def test_exact_selector_and_data_only(self):
        g=(J/'R1CD07GateTrace.java').read_text()
        d=(J/'R1CD07PredicateTrace.java').read_text()
        r=(J/'R1CD07Recovery.java').read_text()
        actual=(J/'R1CD07ActualDataTarget.java').read_text()
        for mark in ('Class.forName("X.0Ie0"','getDeclaredField("$t")','getDeclaredMethod("invoke")',
                     'getInt(instance)==4','selector==11 || selector==12'):
            self.assertIn(mark,g)
        for mark in ('X.0FWn','X.0ApG','D3r','verifiedMethod(actual.getClass()'):
            self.assertIn(mark,actual)
        self.assertIn('0xdf82522e',d)
        self.assertIn('Boolean.FALSE.equals(original)',r)
        self.assertIn('frame.parent!=null',d)
        self.assertIn('onActivityExit()',s := (J/'InstaActionsEntry.java').read_text())
        for bad in ('performClick(', 'startActivity(', 'Media.getId(', 'setResult('):
            self.assertNotIn(bad,d+r+actual)
    def test_disabled_without_fresh_a_and_original_is_preserved(self):
        r=(J/'R1CD07Recovery.java').read_text()
        a=(J/'R1CD07ExactGateTrial.java').read_text()
        entry=(J/'InstaActionsEntry.java').read_text()
        for mark in ('private static boolean enabled;', 'LIMIT = 64','LEASE_NS = 90_000_000_000L',
                     'epoch++', 'onHookFailure()', 'onActivityExit()'):
            self.assertIn(mark,r)
        for mark in ('freshAWindow','freshAFalseBase','eligibleForHF13Recovery()',
                     'if (!deoptimized || !freshAWindow) return false'):
            self.assertIn(mark,a)
        self.assertIn('Object original=chain.proceed()',entry)
        self.assertIn('return R1CD07PredicateTrace.noteExactData(chain.getArg(0),original)',entry)
    def test_no_d06_identity_upgrade_or_other_features(self):
        d=json.loads((ROOT/'project.json').read_text())
        self.assertFalse(d['r1c_d06_visible_media_verified'])
        self.assertFalse(d['r1c_d06_action_hooks_enabled'])
        self.assertFalse(d['r1c_d07_repost_hosted_ci_qualified'])
        self.assertEqual(d['r1c_d07_repost_device_acceptance'],'NOT_RUN')
        self.assertEqual(d['r1c_d07_feed_posts'],'NOT_QUALIFIED')
        self.assertIn('ACTIONS=OFF',(J/'R1CD05FabPanel.java').read_text())
    def test_manual_same_activity_and_module_guard(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertIn('Confirm ITM Repost recovery is OFF',p)
        self.assertIn('if (!itmRecoveryOffAcknowledged)',p)
        self.assertIn('R1BContextMonitor.stableScreen() != R1BNavigationPolicy.Screen.REELS',p)
        self.assertIn('R1CD07Recovery.off("Anchored Reels navigation lost',p)
        self.assertIn('ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED',p)
        self.assertIn('Copy D07 safe Repost report',p)
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'), 'JDK unavailable locally; hosted Android CI still required')
    def test_105_hf13_native_recovery_negatives(self):
        files=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
                'R1CD07BranchInputs','R1CD07InputPresence']
        smoke=ROOT/'tests/java/com/mikeyphw/instaactions/R1CD07RecoverySmoke.java'
        with tempfile.TemporaryDirectory(prefix='ina-d07-jvm-') as tmp:
            cmd=['javac','-encoding','UTF-8','-d',tmp]+[str(J/(name+'.java')) for name in files]+[str(smoke)]
            build=subprocess.run(cmd,capture_output=True,text=True,timeout=90)
            self.assertEqual(build.returncode,0,build.stderr[-2000:])
            run=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD07RecoverySmoke'],
                               capture_output=True,text=True,timeout=30)
            self.assertEqual(run.returncode,0,run.stderr[-2000:])
            self.assertIn('INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS=105',run.stdout)
if __name__=='__main__': unittest.main()
