import unittest,subprocess,tempfile,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class D16(unittest.TestCase):
 def test_privacy_and_version_guards(self):
  p=(J/'R1CD05FabPanel.java').read_text()
  self.assertNotIn('getId(',p)
  self.assertNotIn('getContentDescription(',p)
  self.assertIn('R1CD04Policy.TARGET_VERSION',p)
  self.assertIn('MAX_ATTACH_ATTEMPTS = 4',p)
  self.assertIn('R1CD16MainSurface.inspect(activity)',p)
 def test_bridge_is_explicit_and_no_account_data(self):
  t=(J/'R1CD16FabEvents.java').read_text()
  self.assertIn('event.setClassName("com.mikeyphw.instaactions",RECEIVER)',t)
  self.assertIn('MAX_EVENTS_PER_PROCESS=256',t)
  self.assertIn('context.sendBroadcast(event)',t)
  for forbidden in ('.getId(','.getUsername(','.getCaption(','.getAccountId(','.getCookie(','.getToken('):
   self.assertNotIn(forbidden,t)
 def test_receiver_refuses_untrusted_uid(self):
  t=(J/'R1CD16LogReceiver.java').read_text()
  for tok in ('getSentFromUid()', 'Process.INVALID_UID', 'sender!=instagram', 'getSentFromPackage()', 'validDetail(detail)', 'R1CD16LogStore.append'):
   self.assertIn(tok,t)
 def test_app_can_copy_and_export_without_fab(self):
  t=(J/'MainActivity.java').read_text()
  for tok in ('FAB Diagnostics · visible even without the IA chip','Refresh FAB logs','Copy FAB logs','Export FAB logs','R1CD16LogStore.report(this)','ClipData.newPlainText','Intent.ACTION_SEND'):
   self.assertIn(tok,t)
 def test_manifest_component_and_privacy(self):
  m=(ROOT/'app/src/main/AndroidManifest.xml').read_text()
  self.assertIn('<receiver android:name=".R1CD16LogReceiver" android:exported="true" />',m)
  self.assertIn('android:allowBackup="false"',m)
  self.assertNotIn('SYSTEM_ALERT_WINDOW',m)
 def test_safety_and_no_mutating_hooks(self):
  t=(J/'InstaActionsEntry.java').read_text()
  self.assertEqual(t.count('setId("ina-r1c-d04-reels-passive")'),1)
  self.assertIn('return observation.proceed();',t)
  self.assertIn('R1CD07Recovery.onActivityExit()',t)
  self.assertIn('R1CD16FabEvents.emit((Activity)owner,"HOOK_READY"',t)
 def test_pure_java_anchor_cases(self):
  if not shutil.which('javac') or not shutil.which('java'):self.skipTest('JDK unavailable')
  with tempfile.TemporaryDirectory() as d:
   p=Path(d); x=p/'AnchorSmoke.java';x.write_text('''package com.mikeyphw.instaactions;
public final class AnchorSmoke {
 public static void main(String[] x){
  int n=0;
  R1CD16AnchorPolicy.Decision u=R1CD16AnchorPolicy.Decision.UNKNOWN;
  for(boolean b:new boolean[]{false,true})for(boolean f:new boolean[]{false,true})
  for(boolean c:new boolean[]{false,true})for(boolean p:new boolean[]{false,true})
  for(boolean trunc:new boolean[]{false,true}){
   R1CD16AnchorPolicy.Decision a=R1CD16AnchorPolicy.qualify(false,b,f,c,p,trunc);
   if(trunc && a!=u)throw new AssertionError("truncated");
   if(a!=u && (!b || !(f||c||p)))throw new AssertionError("unanchored");
   n++;
  }
  if(R1CD16AnchorPolicy.qualify(true,false,false,false,false,true)!=R1CD16AnchorPolicy.Decision.MAIN_CLASS)throw new AssertionError("main");
  if(n!=32)throw new AssertionError("count "+n);
  System.out.println("INA_D16_ANCHOR_SMOKE_PASS="+n);
 }
}''')
   build=subprocess.run(['javac','-d',d,str(J/'R1CD16AnchorPolicy.java'),str(x)],capture_output=True,text=True)
   self.assertEqual(build.returncode,0,build.stderr)
   run=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.AnchorSmoke'],capture_output=True,text=True)
   self.assertEqual(run.returncode,0,run.stderr)
   self.assertIn('INA_D16_ANCHOR_SMOKE_PASS=32',run.stdout)

if __name__=='__main__':unittest.main(verbosity=2)
