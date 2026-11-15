"""D19: automatic live-root capture across app switch, no hook mutations."""
from pathlib import Path
import json, shutil, subprocess, tempfile, unittest
R=Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'

class D19LiveRootWatch(unittest.TestCase):
    def test_automatic_fgs_and_in_app_report(self):
        manifest=(R/'app/src/main/AndroidManifest.xml').read_text()
        for token in ('FOREGROUND_SERVICE_SPECIAL_USE','android:foregroundServiceType="specialUse"',
                      'R1CD19RootWatchService','android:exported="false"'):
            self.assertIn(token,manifest)
        m=(J/'MainActivity.java').read_text()
        for token in ('R1CD19RootWatchService.shouldStart(this)',
                      'startForegroundService(new Intent(this,R1CD19RootWatchService.class))',
                      'R1CD19RootWatchService.report(this)', 'Copy FAB logs','Export FAB logs',
                      'D19 LIVE CAPTURE'):
            self.assertIn(token,m)
        self.assertNotIn('Grant root now',m)

    def test_bounded_root_watch_privacy(self):
        s=(J/'R1CD19RootWatchService.java').read_text()
        for token in ('new ProcessBuilder("su","-c",MONITOR_COMMAND)', 'MONITOR_COMMAND',
                      'pidof com.instagram.android','ps -A -o PID,NAME',
                      'logcat -v threadtime -T 1 -s INA_FAB:V',
                      'MIN_RESTART_MS=210_000L','MAX_CAPTURE_MS=174_000L',
                      'INA_D19_POLL','INA_D19_MONITOR_END','startForeground(',
                      'new Thread(', 'START_NOT_STICKY','context.getSharedPreferences('):
            self.assertIn(token,s)
        for forbidden in ('pm grant','pm disable','chmod ', 'chown ', 'mount -o',
                          'setprop ', 'resetprop ', 'settings put', 'am force-stop',
                          'am start '):
            self.assertNotIn(forbidden,s)
        parser=(J/'R1CD19LiveTrace.java').read_text()
        for tok in ('MAX_EVENTS = 72','observedPids.contains(pid)',
                    'UNVERIFIED_PID','OBSERVED_INSTAGRAM_PID',
                    'Only event categories are saved'):
            self.assertIn(tok,parser)
        for forbidden in ('getUsername(', 'getCaption(', 'getContentDescription(',
                          'getAccountId(', 'getToken(', 'setResult('):
            self.assertNotIn(forbidden,parser)

    def test_no_downgrades_or_fab_acceptance_claim(self):
        meta=json.loads((R/'project.json').read_text())
        self.assertEqual(meta['scope_packages'],['com.instagram.android'])
        self.assertTrue(meta['r1c_d19_live_root_foreground_capture_source'])
        self.assertFalse(meta['r1c_d19_fab_visible_on_device'])
        self.assertEqual(meta['r1c_d19_device_acceptance'],'NOT_RUN')
        self.assertTrue(meta['r1c_d19_no_in_app_opt_in'])
        e=(J/'InstaActionsEntry.java').read_text()
        self.assertEqual(e.count('setId("ina-r1c-d04-reels-passive")'),1)
        self.assertIn('return observation.proceed();',e)
        self.assertNotIn('getId(',(J/'R1CD05FabPanel.java').read_text())
        verify=(R/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CD19RootWatchService'",verify)
        self.assertIn("b'R1CD19LiveTrace'",verify)

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK not available')
    def test_lifecycle_pid_attribution_and_privacy(self):
        with tempfile.TemporaryDirectory(prefix='ina-d19-jvm-') as tmp:
            smoke=Path(tmp)/'D19Smoke.java'
            smoke.write_text(r'''package com.mikeyphw.instaactions;
public final class D19Smoke {
 static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static String event(int pid,String suffix){return "11-15 01:22:44.111 "+pid+" 444 I INA_FAB : INA_FAB_EVIDENCE "+suffix+" detail=PRIVATE_TOKEN\n";}
 public static void main(String[] args){
   R1CD19LiveTrace x=new R1CD19LiveTrace(true);
   x.accept("INA_D19_UID");x.accept("0");x.accept("INA_D19_MONITOR_BEGIN");
   x.accept("INA_D19_POLL");x.accept("INA_D19_AUX_PROCESS");
   x.accept("INA_D19_POLL");x.accept("INA_D19_MAIN_PID=1234");
   x.accept(event(1234,"FAB_ATTEMPT").trim());
   x.accept(event(5555,"FAB_FAILURE").trim());
   x.accept("INA_D19_POLL");
   x.accept("INA_D19_POLL");
   x.accept("INA_D19_MONITOR_END");
   String report=x.report("COMPLETE_INSTAGRAM_OBSERVED");
   require(report.contains("instagram_main_seen_during_capture=true"),"ever saw IG");
   require(report.contains("observed_pid_tag_events=1"),"positive owner correlation");
   require(report.contains("main_process_exit_observations=1"),"process exit observed");
   require(report.contains("unverified_pid_markers=1"),"unverified marker");
   require(report.contains("OBSERVED_INSTAGRAM_PID INA_FAB_FAB_ATTEMPT"),"positive marker");
   require(report.contains("UNVERIFIED_PID INA_FAB_FAB_FAILURE"),"negative marker");
   require(!report.contains("1234")&&!report.contains("5555")&&!report.contains("PRIVATE_TOKEN"),"redaction");
   R1CD19LiveTrace none=new R1CD19LiveTrace(true);
   none.accept("INA_D19_UID");none.accept("0");none.accept("INA_D19_MONITOR_BEGIN");
   none.accept("INA_D19_POLL");none.accept("INA_D19_MONITOR_END");
   require(none.report("COMPLETE_NO_INSTAGRAM_SEEN").contains("NEXT=Open Instagram"),"no pid guidance");
   System.out.println("INA_D19_PID_LIVE_AND_PRIVACY_PASS=13");
 }
}''')
            cp=subprocess.run(['javac','-encoding','UTF-8','-d',tmp,
                str(J/'R1CD19LiveTrace.java'),str(smoke)],capture_output=True,text=True,timeout=25)
            self.assertEqual(cp.returncode,0,cp.stderr)
            run=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.D19Smoke'],
                capture_output=True,text=True,timeout=20)
            self.assertEqual(run.returncode,0,run.stdout+run.stderr)
            self.assertIn('INA_D19_PID_LIVE_AND_PRIVACY_PASS=13',run.stdout)
