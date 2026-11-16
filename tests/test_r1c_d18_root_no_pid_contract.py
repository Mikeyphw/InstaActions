"""D18 historical-only diagnostics and strict live PID boundaries."""
from pathlib import Path
import json, shutil, subprocess, tempfile, unittest
R=Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'
class D18RootNoPid(unittest.TestCase):
    def test_root_auto_script_remains_read_only(self):
        s=(J/'R1CD17AutoRootDiagnostics.java').read_text()
        self.assertIn("printf 'INA_D18_PS",s)
        self.assertIn('ps -A -o PID,NAME',s)
        self.assertIn('pidof com.instagram.android',s)
        self.assertIn('logcat -d -t 1000',s)
        self.assertIn('new ProcessBuilder("su","-c",READ_ONLY_COMMAND)',s)
        for x in ('chmod ', 'chown ', 'pm grant','settings put','mount -o','getUsername(','.getToken('):
            self.assertNotIn(x,s)
    def test_inherited_live_privacy_and_app_export(self):
        p=(J/'R1CD17RootLogParser.java').read_text()
        for x in ('pids.contains(pid)', 'MAX_REPORT_EVENTS = 72', 'SAFE_DETAIL',
                  'THREADTIME', 'HISTORICAL_UNVERIFIED_MARKERS_ONLY', 'pidof_main_count=',
                  'ps_aux_count=', 'No matching markers does NOT prove hooks were never loaded'):
            self.assertIn(x,p)
        app=(J/'MainActivity.java').read_text()
        self.assertIn('R1CD17AutoRootDiagnostics.onAppResumed(this,this::refreshFabLogs)',app)
        self.assertIn('Export FAB logs',app)
        self.assertIn('Copy FAB logs',app)
        props=json.loads((R/'project.json').read_text())
        self.assertFalse(props['r1c_d18_fab_visible_on_device'])
        self.assertEqual(props['r1c_d18_fab_device_acceptance'],'NOT_RUN')
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_no_pid_historical_vs_live(self):
        with tempfile.TemporaryDirectory(prefix='ina-d18-jvm-') as d:
            src=Path(d)/'D18RootSmoke.java'
            src.write_text(r'''package com.mikeyphw.instaactions;
public final class D18RootSmoke {
 static void ok(boolean yes,String why){if(!yes)throw new AssertionError(why);}
 public static void main(String[] args){
  String base="INA_D17_UID\n0\nINA_D17_PID\nINA_D18_PS\nINA_D17_LOGCAT\n";
  String old="11-14 12:05:44.123 4222 44 I INA_FAB : INA_FAB_EVIDENCE FAB_FAILURE detail=SECRET_ACCOUNT\n";
  String output=base+old+"INA_D17_END\n";
  R1CD17RootLogParser.Result r=R1CD17RootLogParser.parse(output,false,0,false);
  ok(r.rootGranted,"root");ok(!r.processSeen,"not running");
  ok(r.report.contains("INSTAGRAM_PROCESS_NOT_RUNNING"),"status");
  ok(r.accepted==0 && r.otherPid==1,"not live");
  ok(r.report.contains("HISTORICAL_UNVERIFIED_MARKERS_ONLY"),"history retained");
  ok(r.report.contains("INA_FAB_FAB_FAILURE"),"marker only");
  ok(!r.report.contains("SECRET_ACCOUNT") && !r.report.contains("4222"),"no foreign details/PID");
  String ps="INA_D17_UID\n0\nINA_D17_PID\nINA_D18_PS\n7777 com.instagram.android\n9999 com.instagram.android:push\nINA_D17_LOGCAT\n";
  String now="11-14 12:05:44.123 7777 44 I INA_FAB : INA_FAB_EVIDENCE FAB_ATTEMPT detail=ATTEMPT=1\n";
  R1CD17RootLogParser.Result p=R1CD17RootLogParser.parse(ps+now+"INA_D17_END\n",false,0,false);
  ok(p.processSeen && p.accepted==1,"ps fallback");
  ok(p.report.contains("PIDOF_MISSED_PROCESS_PS_FALLBACK"),"detect pidof mismatch");
  ok(p.report.contains("ps_aux_count=1"),"aux separate");
  ok(!p.report.contains("7777"),"PID redaction");
  String onlyAux=ps.replace("7777 com.instagram.android\n","");
  R1CD17RootLogParser.Result a=R1CD17RootLogParser.parse(onlyAux+now+"INA_D17_END\n",false,0,false);
  ok(!a.processSeen,"aux not main");
  ok(a.report.contains("AUXILIARY_PROCESSES_ONLY_MAIN_NOT_FOUND"),"aux report");
  String denied="INA_D17_UID\n2000\nINA_D17_PID\n1234\nINA_D18_PS\n1234 com.instagram.android\nINA_D17_LOGCAT\n"+now+"INA_D17_END\n";
  R1CD17RootLogParser.Result n=R1CD17RootLogParser.parse(denied,false,1,false);
  ok(!n.rootGranted && !n.report.contains("INA_FAB_FAB_ATTEMPT"),"no denied root contents");
  System.out.println("INA_D18_NO_PID_HISTORICAL_SAFETY_PASS=16");
 }
} ''')
            cp=subprocess.run(['javac','-encoding','UTF-8','-d',d,str(J/'R1CD17RootLogParser.java'),str(src)],capture_output=True,text=True)
            self.assertEqual(cp.returncode,0,cp.stderr)
            x=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.D18RootSmoke'],capture_output=True,text=True)
            self.assertEqual(x.returncode,0,x.stderr)
            self.assertIn('INA_D18_NO_PID_HISTORICAL_SAFETY_PASS=16',x.stdout)

if __name__=='__main__': unittest.main()
