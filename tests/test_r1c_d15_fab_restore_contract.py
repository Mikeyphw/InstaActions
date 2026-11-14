"""D15 FAB regression: attach must not depend on optional probes; absence is diagnosed and recovered."""
from pathlib import Path
import json,re,unittest
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
ENTRY=(J/'InstaActionsEntry.java').read_text()
FAB=(J/'R1CD05FabPanel.java').read_text()
BRIDGE=(J/'R1CD12AndroidBridge.java').read_text()
class FabRecovery(unittest.TestCase):
    def test_attach_before_all_optional_probes(self):
        resume=ENTRY.split('hook(resumed).intercept(chain -> {',1)[1].split('for (String method :',1)[0]
        self.assertLess(resume.index('R1CD05FabPanel.onResumed((Activity) owner)'),resume.index('R1CD12AndroidBridge.install((Activity) owner)'))
        self.assertLess(resume.index('R1CD05FabPanel.onResumed((Activity) owner)'),resume.index('R1CDiscoveryController.onActivityResumed((Activity) owner'))
        self.assertLess(resume.index('R1CD05FabPanel.onResumed((Activity) owner)'),resume.index('R1CD04PassiveProbe.onActivityResumed((Activity) owner'))
        self.assertEqual(resume.count('R1CD05FabPanel.onResumed((Activity) owner)'),1)
        self.assertIn('INA_FAB_ATTACH_EXCEPTION type=',resume)
        self.assertNotIn('catch (Throwable ignored) { /* FAB is strictly optional. */ }',resume)
    def test_separate_android_lifecycle_fallback(self):
        self.assertIn('registerActivityLifecycleCallbacks(INSTANCE)',BRIDGE)
        self.assertIn('R1CD05FabPanel.onResumed(activity)',BRIDGE)
        self.assertIn('INA_FAB_FRAMEWORK_RETRY_FAILED',BRIDGE)
        self.assertIn('R1CD12AccountLifecycle.lifecycleResumed();',BRIDGE)
    def test_missing_or_orphaned_fab_not_false_positive(self):
        self.assertIn('current.healthy(target)',FAB)
        self.assertIn('chip.getParent() == target',FAB)
        self.assertIn('!healthyCurrentRoot()',FAB)
        self.assertIn('try { old.detach(); }',FAB)
        self.assertIn('current = null;',FAB)
        self.assertIn('next.discardFailedAttach();',FAB)
        self.assertIn('ATTACHED_MAIN_ACTIVITY',FAB)
    def test_bounded_retry_uses_weak_activity_and_pause_invalidation(self):
        self.assertIn('WeakReference<Activity> expectedActivity',FAB)
        self.assertIn('WeakReference<Activity> weak = new WeakReference<>(activity)',FAB)
        self.assertIn('MAX_ATTACH_ATTEMPTS = 4',FAB)
        self.assertIn('ATTACH_DELAYS_MS = {120L, 350L, 900L}',FAB)
        self.assertIn('generation != resumeGeneration',FAB)
        self.assertIn('expectedActivity.clear();',FAB)
        self.assertIn('REFUSED_RETRY_CAP_REACHED',FAB)
        self.assertIn('activity.isFinishing() || activity.isDestroyed()',FAB)
    def test_quarantined_optional_failures_while_keeping_original_chain(self):
        self.assertIn('Object result = chain.proceed();',ENTRY)
        self.assertIn('R1CDiscoveryController.onActivityResumed((Activity) owner',ENTRY)
        self.assertEqual(ENTRY.count('setId("ina-r1c-d04-reels-passive")'),1)
        self.assertIn('setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)',ENTRY)
        self.assertNotIn('setId("ina-d15-',ENTRY)
    def test_no_scope_or_feature_gate_promotion(self):
        data=json.loads((P/'project.json').read_text())
        self.assertTrue(data['r1c_d15_fab_restore_source_implemented'])
        self.assertFalse(data['r1c_d15_fab_device_qualified'])
        self.assertEqual(data['r1c_d15_fab_device_acceptance'],'NOT_RUN')
        self.assertEqual(data['scope_packages'],['com.instagram.android'])
        for field in ('r1c_d14_verified_feed_reel_origin','r1c_d14_verified_account_identity',
                      'r1c_d14_actions_enabled','r1c_d14_native_hooks_added',
                      'r1c_d12_automatic_account_rearm'):
            self.assertFalse(data[field])
    def test_preserves_repost_safety_and_no_automated_actions(self):
        r=(J/'R1CD07Recovery.java').read_text()
        for mark in ('LIMIT = 64','SESSION_LIMIT = 4096','LEASE_NS = 90_000_000_000L', 'onActivityExit()'):
            self.assertIn(mark,r)
        self.assertNotIn('R1CD07Recovery.arm(',FAB)
        self.assertNotIn('performClick(',BRIDGE)
    def test_failed_attach_always_cleans_up(self):
        self.assertIn('MAIN.removeCallbacks(ticker)',FAB)
        self.assertIn('if (chip != null && chip.getParent() == decor) decor.removeView(chip)',FAB)
        self.assertIn('if (panel != null && panel.getParent() == decor) decor.removeView(panel)',FAB)
        self.assertIn('status("FAILED_ATTACH_" + failed.getClass().getSimpleName()',FAB)
    def test_exposes_reason_and_fallback_parent(self):
        self.assertIn('SKIPPED_VERSION_NOT_IG455',FAB)
        self.assertIn('SKIPPED_VERSION_QUERY_',FAB)
        self.assertIn('DEFERRED_NO_FRAME_LAYOUT',FAB)
        self.assertIn('activity.findViewById(android.R.id.content)',FAB)
        self.assertIn('INA_FAB_ATTACH ',FAB)
    def test_keeps_origins_and_passive_pipeline(self):
        self.assertIn('R1CD13AndroidSampler.resumed((Activity) owner)',ENTRY)
        self.assertIn('R1CD14NativeOriginProbe.observe(',ENTRY)
        self.assertIn('R1CD10PassiveLedger.observeScreen',FAB)
        self.assertIn('Copy ALL diagnostics',FAB)
        self.assertIn('Export ALL diagnostics',FAB)
    def test_no_unbounded_attach_retries_or_auto_open(self):
        self.assertNotIn('while (true)',FAB)
        self.assertNotIn('while(true)',FAB)
        self.assertNotIn('static Activity expectedActivity',FAB)
        self.assertIn('chip.bringToFront();',FAB)
        self.assertIn('static void onLeaving(Activity activity)',FAB)
if __name__=='__main__':unittest.main()
