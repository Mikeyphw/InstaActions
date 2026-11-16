"""D20: FAB-only user toggle; exported provider authenticated, native actions unchanged."""
from pathlib import Path
import unittest, json, tempfile, subprocess, shutil
R=Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'

class D20FabGate(unittest.TestCase):
 def test_explicit_switch_off_by_default_and_persisted(self):
  app=(J/'MainActivity.java').read_text();store=(J/'R1CD20FabOverrideStore.java').read_text()
  for x in ('Switch fabVersionOverride','Ignore Instagram version for FAB (UI only)',
            'R1CD20FabOverrideStore.enabled(this)','R1CD20FabOverrideStore.setEnabled(this,enabled)',
            'native recovery OFF','Vector scope'):self.assertIn(x,app)
  self.assertIn('getBoolean(KEY,false)',store)
  self.assertIn('.putBoolean(KEY,value).commit()',store)
  self.assertNotIn('MODE_WORLD_READABLE',store)

 def test_content_provider_read_only_with_caller_uid(self):
  source=(J/'R1CD20FabPolicyProvider.java').read_text();manifest=(R/'app/src/main/AndroidManifest.xml').read_text()
  for x in ('Binder.getCallingUid()','getPackagesForUid(uid)','TARGET_PACKAGE.equals(pkg)',
            'URI.equals(uri)','trustedCaller()', 'R1CD20FabOverrideStore.enabled(getContext())',
            'throw new UnsupportedOperationException()'):self.assertIn(x,source)
  self.assertIn('android:authorities="com.mikeyphw.instaactions.fabpolicy"',manifest)
  self.assertIn('android:exported="true" android:grantUriPermissions="false"',manifest)
  self.assertNotIn('SYSTEM_ALERT_WINDOW',manifest)
  for x in ('Media.getId','getToken(', 'getUsername(', 'getAccountId(',
            'setResult(', 'startActivity(', 'Runtime.getRuntime().exec('):self.assertNotIn(x,source)

 def test_cross_uid_client_fails_closed(self):
  client=(J/'R1CD20FabVersionSwitch.java').read_text()
  for x in ('getContentResolver().query(', 'R1CD20FabPolicyProvider.URI',
            'cursor==null || !cursor.moveToFirst()', 'getColumnIndex(', 'cursor.getInt(column)==1',
            'catch(Throwable unavailable)', 'return false;', 'TARGET_PACKAGE.equals(instagram.getPackageName())'):
   self.assertIn(x,client)
  self.assertNotIn('su -c',client)
  self.assertNotIn('getUsername(',client)

 def test_gate_for_fab_only_and_repost_off(self):
  panel=(J/'R1CD05FabPanel.java').read_text()
  for x in ('R1CD20FabVersionSwitch.enabled(activity)',
            'R1CD20FabGatePolicy.canShowFab(exactVersion,override)',
            'SKIPPED_VERSION_NOT_IG455_OVERRIDE_OFF','VERSION_OVERRIDE_UI_ONLY',
            'next.unsupportedVersion=!exactVersion','if(unsupportedVersion){toast(',
            'keepRepost.setEnabled(!unsupportedVersion','enableRecovery.setEnabled(!on && !unsupportedVersion',
            'Unsupported Instagram version · UI-only FAB'):
   self.assertIn(x,panel)
  self.assertIn('enableRecovery.setEnabled(!on',panel) # Inherited D07 UX02 source contract
  self.assertIn('enableRecovery.setEnabled(!on && !unsupportedVersion',panel)
  self.assertNotIn('getId(',panel)
  self.assertNotIn('getContentDescription(',panel)
  entry=(J/'InstaActionsEntry.java').read_text() if (J/'InstaActionsEntry.java').exists() else ''
  if entry:self.assertNotIn('R1CD20FabVersionSwitch',entry)
  d=json.loads((R/'project.json').read_text())
  self.assertTrue(d['r1c_d20_fab_version_override_source_implemented'])
  self.assertFalse(d['r1c_d20_fab_override_default_enabled'])
  self.assertFalse(d['r1c_d20_native_repost_override_enabled'])
  self.assertFalse(d['r1c_d20_fab_visible_on_unsupported_version'])
  self.assertEqual(d['r1c_d20_device_acceptance'],'NOT_RUN')

 @unittest.skipUnless(shutil.which('java') and shutil.which('javac'),'JDK unavailable')
 def test_pure_java_fab_and_native_gate_truth_table(self):
  with tempfile.TemporaryDirectory(prefix='ina-d20-') as d:
   tmp=Path(d)/'D20Smoke.java'
   tmp.write_text('''package com.mikeyphw.instaactions;
public class D20Smoke {
 static void c(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] x){
  c(!R1CD20FabGatePolicy.canShowFab(false,false));
  c(R1CD20FabGatePolicy.canShowFab(false,true));
  c(R1CD20FabGatePolicy.canShowFab(true,false));
  c(R1CD20FabGatePolicy.canShowFab(true,true));
  c(!R1CD20FabGatePolicy.canOfferNativeRecovery(false));
  c(R1CD20FabGatePolicy.canOfferNativeRecovery(true));
  System.out.println("INA_D20_FAB_POLICY_TRUTH_TABLE_PASS=6");
 }
}''')
   c=subprocess.run(['javac','-encoding','UTF-8','-d',d,str(J/'R1CD20FabGatePolicy.java'),str(tmp)],capture_output=True,text=True,timeout=20)
   self.assertEqual(c.returncode,0,c.stderr)
   p=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.D20Smoke'],capture_output=True,text=True,timeout=20)
   self.assertEqual(p.returncode,0,p.stderr)
   self.assertIn('INA_D20_FAB_POLICY_TRUTH_TABLE_PASS=6',p.stdout)

if __name__=='__main__':unittest.main()
