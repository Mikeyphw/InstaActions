"""D12 source-based independent account-change correlation contracts; no identity inference."""
from pathlib import Path
import json,re,subprocess,shutil,tempfile,unittest
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
T=P/'tests/java/com/mikeyphw/instaactions'
class D12AccountLifecycle(unittest.TestCase):
    def test_independent_android_dispatch_and_no_sensitive_intent_reads(self):
        e=(J/'InstaActionsEntry.java').read_text()
        b=(J/'R1CD12AndroidBridge.java').read_text()
        for s in ('Instrumentation.class.getDeclaredMethods()', 'callActivityOnNewIntent',
                  'types[0]!=Activity.class', 'types[1]!=Intent.class',
                  'android.app.ComponentCaller','chain.proceed(); // dispatch EXACTLY once',
                  'R1CD12AccountLifecycle.newIntentDelivered()',
                  'R1CD12AndroidBridge.install((Activity) owner)'):
            self.assertIn(s,e)
        for s in ('Application.ActivityLifecycleCallbacks','registerActivityLifecycleCallbacks',
                  'R1CD12AccountLifecycle.lifecycleResumed()',
                  'R1CD12AccountLifecycle.lifecyclePaused()',
                  'R1CD12AccountLifecycle.lifecycleDestroyed()'):
            self.assertIn(s,b)
        for source in (e,b):
            for forbidden in ('intent.getExtras(', 'intent.getData(', 'intent.getAction(',
                              'getUserId(', 'getUsername(', 'getToken(', 'getDeclaredFields('):
                self.assertNotIn(forbidden,source)
    def test_session_correlation_exposes_both_sources_and_unverified_identity(self):
        s=(J/'R1CD12AccountLifecycle.java').read_text()
        for mark in ('ACCOUNT_IDENTITY=NOT_VERIFIED AUTO_REARM=FORBIDDEN',
                     'D12_SIGNAL_STATUS=', 'marker_ordinal=', 'dropped_oldest=',
                     'NEW_INTENT_DELIVERED', 'USERSESSION_REFERENCE_CHANGED',
                     'OPAQUE_SESSION_REFERENCE_CHANGED_NOT_ACCOUNT_IDENTITY',
                     'new_intents_disarmed_repost=', 'SESSION', 'MAX_COUNT'):
            self.assertIn(mark,s)
        self.assertIn('R1CD07Recovery.off("New Intent delivered: account/context NOT VERIFIED")',s)
        self.assertNotIn('R1CD07Recovery.arm(',s)
        d09=(J/'R1CD09AccountSentinel.java').read_text()
        self.assertIn('R1CD12AccountLifecycle.observedSessionReferenceChange()',d09)
        self.assertIn('R1CD12AccountLifecycle.markManualSwitch()',d09)
        ledger=(J/'R1CD10PassiveLedger.java').read_text()
        self.assertIn('R1CD12AccountLifecycle.screenHint(screen)',ledger)
    def test_user_reports_and_original_roadmap_preserved(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertIn('case ACCOUNT: data=R1CD09AccountSentinel.summary()+R1CD12AccountLifecycle.report()',p)
        self.assertIn('case PASSIVE: data=R1CD10PassiveLedger.report()+R1CD12AccountLifecycle.report()',p)
        archive=(J/'R1CD10ReportArchive.java').read_text()
        self.assertIn('.append(R1CD12AccountLifecycle.report())',archive)
        package=(P/'tools/verify_r1c_apk.py').read_text()
        for marker in ("b'R1CD12AccountLifecycle'", "b'R1CD12AndroidBridge'",
                       "b'ina-d12-android-new-intent-'", "b'AUTO_REARM=FORBIDDEN'"):
            self.assertIn(marker,package)
        d=json.loads((P/'project.json').read_text())
        self.assertTrue(d['r1c_d11_hf03_source_implemented'])
        self.assertTrue(d['r1c_d12_account_lifecycle_source_implemented'])
        self.assertFalse(d['r1c_d12_verified_account_identity'])
        self.assertFalse(d['r1c_d12_verified_instagram_account_provider'])
        self.assertFalse(d['r1c_d12_automatic_account_rearm'])
        self.assertFalse(d['r1c_d12_native_repost_predicate_modified'])
        self.assertFalse(d['r1c_d12_hosted_ci_qualified'])
        self.assertEqual(d['r1c_d12_device_acceptance'],'NOT_RUN')
        for h in ('## R0','## R1A','## R1B','## R1C','## R2','## R3','## R4','## R5','## R6','## R7','## R8'):
            self.assertIn(h,(P/'ROADMAP.md').read_text())
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'requires real Java')
    def test_native_recovery_and_d12_java_regression(self):
        names=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
               'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
               'R1CD09DiagnosticGate','R1CD09AccountSentinel','R1CD10ComparisonFlow',
               'R1CD10PassiveLedger','R1CD10ReportArchive','R1CD12AccountLifecycle']
        testnames=['R1CD07RecoverySmoke','R1CD08ReadOnlySmoke','R1CD09SessionAndModesSmoke',
                   'R1CD10UniversalSmoke','R1CD11HardeningSmoke','R1CD12LifecycleSmoke']
        with tempfile.TemporaryDirectory(prefix='ina-d12-javac-') as td:
            cp=subprocess.run(['javac','-encoding','UTF-8','-d',td]
                +[str(J/(s+'.java')) for s in names]
                +[str(T/(s+'.java')) for s in testnames],capture_output=True,text=True,timeout=60)
            self.assertEqual(cp.returncode,0,cp.stderr[-4000:])
            for name in testnames:
                out=subprocess.run(['java','-cp',td,'com.mikeyphw.instaactions.'+name],
                                   capture_output=True,text=True,timeout=25)
                self.assertEqual(out.returncode,0,name+' '+out.stderr[-2000:])
                self.assertIn('PASS=',out.stdout,name+' '+out.stdout)
if __name__=='__main__':unittest.main()
