"""D13 automatic read-only surface discovery: tests, privacy and safety contracts."""
from pathlib import Path
import unittest,subprocess,tempfile,json,shutil
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
class D13AutoSource(unittest.TestCase):
    def test_consolidated_d11_d12_are_present(self):
        d=json.loads((P/'project.json').read_text())
        for k in ('r1c_d11_hf03_source_implemented','r1c_d12_account_lifecycle_source_implemented',
                  'r1c_d13_auto_surface_source_implemented','r1c_d13_d11_hf03_and_d12_hf04_folded'):
            self.assertIs(d[k],True,k)
        for k in ('r1c_d13_verified_feed_reel_origin','r1c_d13_verified_account_identity',
                  'r1c_d13_automatic_account_rearm','r1c_d13_action_hooks_enabled',
                  'r1c_d13_hosted_ci_qualified'):
            self.assertIs(d[k],False,k)
        self.assertEqual(d['r1c_d13_device_acceptance'],'NOT_RUN')
        for file in ('R1CD12AndroidBridge.java','R1CD12AccountLifecycle.java',
                     'R1CD13AutoSurface.java','R1CD13AndroidSampler.java'):
            self.assertTrue((J/file).is_file(),file)
    def test_automatic_existing_view_evidence_bounded_and_no_identity_reads(self):
        s=(J/'R1CD13AndroidSampler.java').read_text()
        for marker in ('R1BContextMonitor.stableScreen()', 'feed_tab','clips_tab',
                       'MAX_VIEWS=512','MAX_MS=24','postDelayed(SAMPLE,1500L)',
                       'getResourcePackageName(id)','WeakReference<Activity>'):
            self.assertIn(marker,s)
        forbidden=('getUsername(', 'getUserId(', 'getExtras(', 'getData(',
                   'performClick(', 'getBitmap(', 'setResult(', 'getText(',
                   'getContentDescription(', 'getAccountId(')
        for marker in forbidden:self.assertNotIn(marker,s)
        self.assertNotIn('hook(',s)
        e=(J/'InstaActionsEntry.java').read_text()
        self.assertIn('R1CD13AndroidSampler.resumed((Activity) owner)',e)
        self.assertIn('R1CD13AndroidSampler.paused((Activity) owner)',e)
        self.assertIn('R1CD13AutoSurface.invalidatedByNewIntent()',e)
    def test_ui_optional_auto_mode_copy_all_and_no_native_repost_modification(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        for marker in ('AUTO discovery · Feed / Reels / Story / account A→B',
                       'D13 · AUTOMATIC SURFACE DISCOVERY · A → B',
                       'R1CD13AutoSurface.brief()', 'R1CD13AutoSurface.report()',
                       'Copy ALL diagnostics','Export ALL diagnostics',
                       'R1CD09DiagnosticGate.Mode.AUTO','case AUTO:'):
            self.assertIn(marker,p)
        self.assertIn('PASSIVE IS AUTO ON',p)
        r=(J/'R1CD07Recovery.java').read_text()
        for v in ('LIMIT = 64','SESSION_LIMIT = 4096','LEASE_NS = 90_000_000_000L'):
            self.assertIn(v,r)
    @unittest.skipUnless(shutil.which('java') and shutil.which('javac'),'JDK required')
    def test_java_fake_navigation_and_cohort_refusals(self):
        source=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
                'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
                'R1CD09DiagnosticGate','R1CD09AccountSentinel','R1CD10ComparisonFlow',
                'R1CD10PassiveLedger','R1CD10ReportArchive','R1CD12AccountLifecycle',
                'R1CD13AutoSurface']
        smoke=P/'tests/java/com/mikeyphw/instaactions/R1CD13AutoDiscoverySmoke.java'
        with tempfile.TemporaryDirectory(prefix='ina-d13-test-') as tmp:
            cmd=['javac','-encoding','UTF-8','-d',tmp]+[str(J/(x+'.java')) for x in source]+[str(smoke)]
            run=subprocess.run(cmd,capture_output=True,text=True,timeout=80)
            self.assertEqual(run.returncode,0,run.stderr)
            run=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD13AutoDiscoverySmoke'],
                capture_output=True,text=True,timeout=30)
            self.assertEqual(run.returncode,0,run.stderr+run.stdout)
            self.assertIn('INA_D13_AUTO_SURFACE_AB_FAIL_CLOSED_PASS=',run.stdout)
if __name__=='__main__':unittest.main()
