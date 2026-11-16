"""D17 automatic read-only root FAB diagnostics, fail-closed privacy and JVM parser tests."""
import json, pathlib, shutil, subprocess, tempfile, unittest
R=pathlib.Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'

class D17RootContract(unittest.TestCase):
    def test_automatic_on_resume_no_opt_in_button(self):
        app=(J/'MainActivity.java').read_text()
        self.assertIn('R1CD17AutoRootDiagnostics.onAppResumed(this,this::refreshFabLogs)',app)
        self.assertIn('Automatic Root FAB Diagnostics',app)
        self.assertIn('Copy FAB logs',app)
        self.assertIn('Export FAB logs',app)
        self.assertIn('combinedFabReport()',app)
        self.assertIn('R1CD17AutoRootDiagnostics.report(this)',app)
        self.assertNotIn('Request root access',app)
        self.assertNotIn('Grant root now',app)

    def test_automatic_su_only_read_only_commands(self):
        source=(J/'R1CD17AutoRootDiagnostics.java').read_text()
        for v in ('new ProcessBuilder("su","-c",READ_ONLY_COMMAND)',
                  'id -u', 'pidof com.instagram.android',
                  'logcat -d -t 1000 -v threadtime -s INA_FAB:V',
                  'TIMEOUT_SECONDS=25', 'MIN_INTERVAL_MS=20_000L',
                  'MAX_RAW_CHARS=160_000', 'Process.waitFor', 'putString(REPORT,report)',
                  'worker.setDaemon(true)', 'KernelSU'):
            if v=='Process.waitFor':v='process.waitFor'
            self.assertIn(v,source)
        for forbidden in ('chmod ', 'chown ', 'rm -', 'setprop ', 'settings put', 'resetprop ',
                          'pm grant', 'pm disable', 'pm enable', 'ksud module',
                          'su -c <user', 'mount -o', 'setResult('):
            self.assertNotIn(forbidden,source)

    def test_parser_drops_raw_logcat_and_foreign_pid(self):
        s=(J/'R1CD17RootLogParser.java').read_text()
        for v in ('pids.contains(pid)', 'other_pid_tag_lines_ignored',
                  'No matching markers does NOT prove hooks were never loaded',
                  'MAX_REPORT_EVENTS = 72','SAFE_DETAIL','THREADTIME'):
            self.assertIn(v,s)
        for forbidden in ('.getUsername(','.getToken(','.getCaption(','.getAccountId(',
                          'getId(', 'getContentDescription(', 'ProcessBuilder('):
            self.assertNotIn(forbidden,s)

    def test_existing_d16_guards_and_repost_disabled(self):
        recv=(J/'R1CD16LogReceiver.java').read_text()
        panel=(J/'R1CD05FabPanel.java').read_text()
        self.assertIn('getSentFromUid()',recv)
        self.assertIn('sender!=instagram',recv)
        self.assertNotIn('getId(',panel)
        self.assertNotIn('getContentDescription(',panel)
        d=json.loads((R/'project.json').read_text())
        self.assertEqual(d['scope_packages'],['com.instagram.android'])
        self.assertFalse(d['r1c_d17_fab_visible_on_device'])
        self.assertEqual(d['r1c_d17_root_granted_on_device'],'NOT_VERIFIED')
        self.assertEqual(d['r1c_d17_device_acceptance'],'NOT_RUN')
        self.assertTrue(d['r1c_d17_no_in_app_opt_in'])

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_root_parser_synthetic_positive_and_negative(self):
        with tempfile.TemporaryDirectory(prefix='ina-d17-jvm-') as d:
            smoke=pathlib.Path(d)/'RootParserSmoke.java'
            smoke.write_text(r'''package com.mikeyphw.instaactions;
public class RootParserSmoke {
    static void ok(boolean condition, String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        String basic="INA_D17_UID\n0\nINA_D17_PID\n1234\nINA_D17_LOGCAT\n";
        String event="11-14 12:05:44.123 1234 4321 I INA_FAB : INA_FAB_EVIDENCE FAB_ATTEMPT detail=ATTEMPT=1\n";
        String foreign="11-14 12:05:44.123 9999 4321 I INA_FAB : INA_FAB_EVIDENCE FAB_VISIBLE detail=SECRET_ACCOUNT\n";
        String output=basic+event+foreign+"INA_D17_END\n";
        R1CD17RootLogParser.Result r=R1CD17RootLogParser.parse(output,false,0,false);
        ok(r.rootGranted,"root");ok(r.processSeen,"pid");
        ok(r.accepted==1,"one accepted");ok(r.otherPid==1,"foreign ignored");
        ok(r.report.contains("INA_FAB_FAB_ATTEMPT"),"categorization");
        ok(!r.report.contains("SECRET_ACCOUNT"),"foreign privacy");
        ok(!r.report.contains("1234"),"no raw pid");
        ok(!r.report.contains("4321"),"no thread id");
        R1CD17RootLogParser.Result denied=R1CD17RootLogParser.parse("su: denied",false,1,false);
        ok(!denied.rootGranted,"denied");
        ok(denied.report.contains("ROOT_NOT_GRANTED_OR_SU_UNAVAILABLE"),"denied status");
        R1CD17RootLogParser.Result noPid=R1CD17RootLogParser.parse(
                "INA_D17_UID\n0\nINA_D17_PID\nINA_D17_LOGCAT\nINA_D17_END\n",false,0,false);
        ok(noPid.report.contains("INSTAGRAM_PROCESS_NOT_RUNNING"),"no PID");
        R1CD17RootLogParser.Result timeout=R1CD17RootLogParser.parse(output,true,-1,false);
        ok(timeout.report.contains("ROOT_PROBE_TIMEOUT"),"timeout");
        for(int i=0;i<200;i++){
            event+="11-14 12:05:44.123 1234 4321 I INA_FAB : INA_FAB_EVIDENCE FAB_ATTEMPT detail=ATTEMPT=1\n";
        }
        r=R1CD17RootLogParser.parse(basic+event+"INA_D17_END\n",false,0,false);
        ok(r.accepted>72,"observed beyond window");
        ok(r.report.split("INA_FAB_FAB_ATTEMPT",-1).length-1<=72,"stored events cap");
        System.out.println("INA_D17_ROOT_PARSER_NEGATIVES_PASS=12");
    }
}''')
            source=J/'R1CD17RootLogParser.java'
            x=subprocess.run(['javac','-encoding','UTF-8','-d',d,str(source),str(smoke)],capture_output=True,text=True,timeout=20)
            self.assertEqual(x.returncode,0,x.stderr)
            x=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.RootParserSmoke'],capture_output=True,text=True,timeout=20)
            self.assertEqual(x.returncode,0,x.stderr+x.stdout)
            self.assertIn('INA_D17_ROOT_PARSER_NEGATIVES_PASS=12',x.stdout)

if __name__=='__main__':unittest.main(verbosity=2)
