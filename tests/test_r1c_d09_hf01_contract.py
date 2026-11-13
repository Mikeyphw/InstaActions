"""New D08 HF01: dropdown exclusivity, safe session Repost, account-change signals."""
from pathlib import Path
import json,subprocess,shutil,tempfile,unittest
R=Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'
class D09Contract(unittest.TestCase):
    def test_dropdown_and_exclusive_mode_before_operations(self):
        s=(J/'R1CD05FabPanel.java').read_text()
        for marker in ('Spinner modePicker','"Begin selected diagnostic"',
                '"Finish current diagnostic', '"Abort current diagnostic',
                'R1CD09DiagnosticGate.active(', 'if(!requireMode(',
                'copySelectedReport()', 'reelsGroup.setVisibility(',
                'compareGroup.setVisibility(', 'feedGroup.setVisibility(',
                'mediaGroup.setVisibility(', 'accountGroup.setVisibility('):
            self.assertIn(marker,s,marker)
        g=(J/'R1CD09DiagnosticGate.java').read_text()
        self.assertIn('if(state==State.RUNNING)return "REFUSED:',g)
        self.assertIn('accountTransitionExpected',g)
        self.assertIn('state=State.FINISHED;',g)
        self.assertIn('state=State.ABORTED;',g)
    def test_foreground_session_toggle_is_not_unrestricted(self):
        s=(J/'R1CD07Recovery.java').read_text()
        p=(J/'R1CD05FabPanel.java').read_text()
        for v in ('SESSION_LIMIT = 4096','armSession(',
                  'sessionMode=keepDuringForeground',
                  'if(sessionMode && !Boolean.FALSE.equals(original))',
                  'onActivityExit()', 'onCaptureTransition()',
                  'dataPresence!=R1CD07BranchInputs.PRESENT'):
            self.assertIn(v,s,v)
        self.assertIn('setOnCheckedChangeListener(this::toggleSessionRepost)',p)
        self.assertIn('4B · Enable native Repost · 90 s max',p)
        self.assertIn('Keep Repost available · foreground session',p)
        self.assertIn('Recovery OFF · always available',p)
    def test_account_change_signal_opaque_and_fail_closed(self):
        e=(J/'InstaActionsEntry.java').read_text()
        s=(J/'R1CD09AccountSentinel.java').read_text()
        self.assertIn('R1CD09AccountSentinel.observe(chain.getArg(0))',e)
        self.assertIn('WeakReference<Object>',s)
        self.assertIn('old==userSession',s)
        self.assertIn('R1CD07Recovery.off(',s)
        for forbidden in ('getId(', 'getUsername(', 'getUserId(', 'getToken(',
                'getDeclaredField(', 'getDeclaredFields(', 'toString()',
                'System.identityHashCode(', 'getAccountId(', 'getName('):
            self.assertNotIn(forbidden,s,forbidden)
        self.assertIn('No callback does NOT prove same account',s)
    def test_feed_account_separation_media_reasons_and_receiver(self):
        f=(J/'R1CD08FeedAudit.java').read_text()
        m=(J/'R1CD08MediaEvidence.java').read_text()
        x=(J/'R1CD07PredicateTrace.java').read_text()
        for v in ('SAMPLES=new int[2][3]', 'MANUAL_AFFECTED','MANUAL_UNAFFECTED',
                  'SESSION_COUNT', 'active(R1CD09DiagnosticGate.Mode.FEED)'):
            self.assertIn(v,f)
        self.assertIn('owner=',m)
        self.assertIn('MAX_REASON_CLASSES=32',m)
        self.assertIn('active(R1CD09DiagnosticGate.Mode.MEDIA)',m)
        self.assertIn('D09_DECLARING_CLASS',x)
        self.assertIn('declaring_other_X=',x)
        self.assertIn('noteDataReceiver(chain.getThisObject(),',(J/'InstaActionsEntry.java').read_text())
    def test_preserve_other_actions_and_qualification(self):
        j=json.loads((R/'project.json').read_text())
        self.assertEqual(j['scope_packages'],['com.instagram.android'])
        for flag in ('r1c_d06_visible_media_verified','r1c_d06_action_hooks_enabled',
                'r1c_d08_feed_repost_enabled','r1c_d08c_media_identity_verified'):
            self.assertFalse(j[flag])
        self.assertEqual(j['r1c_d09_device_acceptance'],'NOT_RUN')
        for phase in ('## R0','## R1C','## R2','## R3','## R4','## R5','## R6','## R7','## R8'):
            self.assertIn(phase,(R/'ROADMAP.md').read_text())
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_jvm_contract_modes_and_105_hf13(self):
        names=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
               'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
               'R1CD09DiagnosticGate','R1CD09AccountSentinel']
        smoke=R/'tests/java/com/mikeyphw/instaactions'
        with tempfile.TemporaryDirectory(prefix='ina-hf01-jvm-') as d:
            cmd=['javac','-encoding','UTF-8','-d',d]+[str(J/(x+'.java')) for x in names]+[
                str(smoke/(name+'.java')) for name in
                ('R1CD09SessionAndModesSmoke','R1CD08ReadOnlySmoke','R1CD07RecoverySmoke')]
            build=subprocess.run(cmd,capture_output=True,text=True,timeout=80)
            self.assertEqual(build.returncode,0,build.stderr)
            for cls,sig in [('R1CD09SessionAndModesSmoke','INA_D09_SESSION_AND_MODE_NEGATIVES_PASS='),
                            ('R1CD08ReadOnlySmoke','INA_D08_READONLY_NEGATIVES_PASS='),
                            ('R1CD07RecoverySmoke','INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS=105')]:
                run=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.'+cls],
                     capture_output=True,text=True,timeout=50)
                self.assertEqual(run.returncode,0,run.stderr+run.stdout)
                self.assertIn(sig,run.stdout)
if __name__=='__main__':unittest.main()
