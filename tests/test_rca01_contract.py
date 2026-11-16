"""RCA01 currently-active read-only FAB, dual exact-APK guard and gate ledger."""
import json
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path
R=Path(__file__).resolve().parents[1]
J=R/'app/src/main/java/com/mikeyphw/instaactions'

class RCA01(unittest.TestCase):
    def test_only_new_fab_is_wired_in_both_lifecycle_paths(self):
        entry=(J/'InstaActionsEntry.java').read_text()
        bridge=(J/'R1CD12AndroidBridge.java').read_text()
        fab=(J/'R1CRCA01Fab.java').read_text()
        self.assertEqual(entry.count('R1CRCA01Fab.onResumed((Activity) owner)'),1)
        self.assertIn('R1CRCA01Fab.onLeaving((Activity) owner)',entry)
        self.assertIn('R1CRCA01Fab.onResumed(activity)',bridge)
        self.assertNotIn('R1CD05FabPanel.onResumed(',entry+bridge)
        self.assertNotIn('R1CD05FabPanel.onLeaving(',entry+bridge)
        self.assertIn('R1CD07Recovery.off("RCA01 diagnostic-only UI',fab)
        self.assertIn('new Button(a)',fab)
        self.assertIn('FrameLayout',fab)
        self.assertIn('Copy RCA01 A/B report',fab)
        self.assertNotIn('R1CD07GateTrace.armHF13',fab)
        self.assertNotIn('R1CD07Recovery.arm(',fab)
        self.assertNotIn('performClick(',fab)
        self.assertNotIn('SYSTEM_ALERT_WINDOW',(R/'app/src/main/AndroidManifest.xml').read_text())

    def test_two_exact_hashes_without_broad_version_override(self):
        policy=(J/'R1CD04Policy.java').read_text()
        for h in ('fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6',
                  '4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a'):
            self.assertIn(h,policy)
        self.assertIn('TARGET_VERSION.equals(version)',policy)
        self.assertIn('RCA01_APKM_BASE_SHA256.equals(digest)',policy)
        self.assertIn('TARGET_APK_BYTES = 238346353L',policy)
        self.assertNotIn('startsWith(version)',policy)
        self.assertIn('R1CD04PassiveProbe.isInstalled()', (J/'InstaActionsEntry.java').read_text())

    def test_exact_source_method_observers_are_read_only(self):
        entry=(J/'InstaActionsEntry.java').read_text()
        ledger=(J/'R1CRCA01GateLedger.java').read_text()
        for tok in ('Class.forName("com.instagram.feed.media.Media"','getDeclaredMethod("A3x")',
                    'getDeclaredMethod("AFV",Boolean.class)','ina-rca01-media-get-original',
                    'ina-rca01-media-set-argument-readonly','Object original=chain.proceed()',
                    'R1CRCA01GateLedger.mediaHooks('):
            self.assertIn(tok,entry)
        self.assertIn('R1CRCA01GateLedger.Site.EXACT_D3R_ORIGINAL',entry)
        self.assertIn('R1CRCA01GateLedger.recordUi(R1CD07GateTrace.activeIndex()',entry)
        for forbidden in ('hook(', 'performClick(', 'getUserId(', 'getUsername(',
                          'getMediaId(', 'getToken(', 'getCaption(', 'setResult(', 'Runtime.getRuntime()'):
            self.assertNotIn(forbidden,ledger)
        self.assertIn('SERVER_ORIGIN=UNVERIFIED',ledger)
        self.assertIn('CAN_REPOST_JSON_LINK=UNVERIFIED',ledger)
        self.assertIn('FRIENDS_SHARED_GATE=UNVERIFIED',ledger)

    def test_explicit_unknown_acceptance(self):
        obj=json.loads((R/'project.json').read_text())
        self.assertTrue(obj['rca01_source_implemented'])
        self.assertTrue(obj['rca01_diagnostic_fab_only'])
        self.assertFalse(obj['rca01_old_fab_active'])
        self.assertFalse(obj['rca01_original_media_field_causality_proven'])
        self.assertFalse(obj['rca01_shared_friends_gate_proven'])
        self.assertFalse(obj['rca01_hosted_ci_qualified'])
        self.assertEqual(obj['rca01_device_acceptance'],'NOT_RUN')

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_jvm_exact_apk_negative_policy(self):
        with tempfile.TemporaryDirectory(prefix='ina-rca01-apk-') as d:
            src=[J/'R1CD04Policy.java',R/'tests/java/com/mikeyphw/instaactions/R1CRCA01PolicySmoke.java']
            p=subprocess.run(['javac','-encoding','UTF-8','-d',d,*map(str,src)],capture_output=True,text=True,timeout=30)
            self.assertEqual(p.returncode,0,p.stderr)
            p=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.R1CRCA01PolicySmoke'],
                             capture_output=True,text=True,timeout=30)
            self.assertEqual(p.returncode,0,p.stderr+p.stdout)
            self.assertIn('INA_RCA01_EXACT_APK_NEGATIVES_PASS=9',p.stdout)

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_jvm_readonly_ledger(self):
        with tempfile.TemporaryDirectory(prefix='ina-rca01-') as d:
            src=[J/'R1CRCA01GateLedger.java',R/'tests/java/com/mikeyphw/instaactions/R1CRCA01GateLedgerSmoke.java']
            p=subprocess.run(['javac','-encoding','UTF-8','-d',d,*map(str,src)],capture_output=True,text=True,timeout=30)
            self.assertEqual(p.returncode,0,p.stderr)
            p=subprocess.run(['java','-cp',d,'com.mikeyphw.instaactions.R1CRCA01GateLedgerSmoke'],
                             capture_output=True,text=True,timeout=30)
            self.assertEqual(p.returncode,0,p.stderr+p.stdout)
            self.assertIn('INA_RCA01_LEDGER_SAFETY_PASS=20',p.stdout)

if __name__=='__main__':unittest.main()
