"""D11 source plus executable fail-closed regression. Android build stays hosted CI only."""
from pathlib import Path
import json, shutil, subprocess, tempfile, unittest
P=Path(__file__).resolve().parents[1]
J=P/'app/src/main/java/com/mikeyphw/instaactions'
T=P/'tests/java/com/mikeyphw/instaactions'
class D11Hardening(unittest.TestCase):
    def test_feature_provenance_separates_passive_callback_volume(self):
        c=(J/'R1CD10ComparisonFlow.java').read_text()
        for token in ('recordFeature(', 'hasFeatureCoverage()', 'FEATURE_EVIDENCE A_explicit=',
                      'missingFeatureHint()', 'INCOMPLETE_FEATURE_SNAPSHOTS_NOT_QUALIFIED',
                      'PAIRED_SCANS_SCREEN_HINT_MISMATCH_NOT_QUALIFIED',
                      'PAIRED_VIEW_CANDIDATES_AVATAR_NOT_VERIFIED','lastCompletedQuality'):
            self.assertIn(token,c)
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertIn('"FINISHED_"+quality',p)
        self.assertIn('R1CD10ComparisonFlow.recordFeature(0,true)',p)
        self.assertIn('R1CD10ComparisonFlow.missingFeatureHint()',p)
        self.assertIn('R1CD07Recovery.off("Anchored Reels navigation lost',p)
    def test_feed_and_avatar_manual_scans_are_not_owner_proofs(self):
        feed=(J/'R1CD08FeedAudit.java').read_text()
        avatar=(J/'R1CD10AvatarAudit.java').read_text()
        for token in ('recordFeature(kind,expected && !capped)',
                      'SCAN_SCOPE=ENTIRE_VISIBLE_DECOR','SCREEN_HINT_IS_NOT_POST_OWNER',
                      'Feed Repost and Feed-opened Reels NOT QUALIFIED'):
            self.assertIn(token,feed)
        self.assertIn('activeCohort!=cohort || R1CD10ComparisonFlow.cohort()!=cohort',feed)
        self.assertIn('cohort!=active || R1CD10ComparisonFlow.cohort()!=active',avatar)
        for token in ('recordFeature(surface,false)',
                      'surface_owner=NOT_FOUND', 'same ImageViews may be counted'):
            self.assertIn(token,avatar)
        for s in (feed,avatar):
            for unsafe in ('getBitmap(', 'getUsername(', 'getCaption(', 'setImageDrawable(', 'setResult('):
                self.assertNotIn(unsafe,s)
    def test_account_transition_event_order_private(self):
        a=(J/'R1CD09AccountSentinel.java').read_text()
        for token in ('MAX_EVENTS=48','MANUAL_SWITCH_MARKER_NOT_IDENTITY_PROOF',
                      'REFERENCE_CHANGED_AFTER_MARKER','REFERENCE_CHANGED_BEFORE_MARKER',
                      'ACCOUNT_EVENT_ORDER (bounded)','String.join('):
            self.assertIn(token,a)
        self.assertNotIn('userSession.toString(',a)
        self.assertNotIn('getUsername(',a)
        self.assertNotIn('getUserId(',a)
        self.assertNotIn('getToken(',a)
        self.assertIn('R1CD10ComparisonFlow.recordFeature(0,false)',a)
    def test_archive_loss_and_persistent_repost_guard_not_upgraded(self):
        a=(J/'R1CD10ReportArchive.java').read_text()
        self.assertIn('ARCHIVE_INCOMPLETE',a)
        self.assertIn('clipped_records=',a)
        self.assertIn('lost_oldest=',a)
        self.assertIn('NOT full Android logcat',a)
        self.assertIn('MAX_TOTAL_CHARS=180_000',a)
        f=json.loads((P/'project.json').read_text())
        self.assertTrue(f['r1c_d11_hf03_source_implemented'])
        self.assertFalse(f['r1c_d11_hf03_feature_identity_verified'])
        self.assertFalse(f['r1c_d11_hf03_hosted_ci_qualified'])
        self.assertFalse(f['r1c_d11_hf03_native_repost_logic_changed'])
        self.assertEqual(f['r1c_d11_hf03_device_acceptance'],'NOT_RUN')
        self.assertFalse(f['r1c_d08_feed_repost_enabled'])
        self.assertFalse(f['r1c_d06_visible_media_verified'])
        self.assertIn('## R8',(P/'ROADMAP.md').read_text())
        self.assertIn('FEATURE_EVIDENCE A_explicit=',(P/'tools/verify_r1c_apk.py').read_text())
    @unittest.skipUnless(shutil.which('java') and shutil.which('javac'), 'JDK unavailable')
    def test_217_original_and_hardened_java_checks(self):
        classes=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
                 'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
                 'R1CD09DiagnosticGate','R1CD09AccountSentinel','R1CD10ComparisonFlow',
                 'R1CD10PassiveLedger','R1CD10ReportArchive','R1CD12AccountLifecycle']
        smoke=['R1CD11HardeningSmoke','R1CD10UniversalSmoke',
               'R1CD09SessionAndModesSmoke','R1CD08ReadOnlySmoke','R1CD07RecoverySmoke']
        with tempfile.TemporaryDirectory(prefix='ina-d11-jvm-') as td:
            cmd=['javac','-encoding','UTF-8','-d',td]+[str(J/(n+'.java')) for n in classes]+[str(T/(n+'.java')) for n in smoke]
            done=subprocess.run(cmd,capture_output=True,text=True,timeout=90)
            self.assertEqual(done.returncode,0,done.stderr[-2000:])
            for name,expected in [('R1CD11HardeningSmoke','INA_D11_HARDENED_DIAGNOSTIC_NEGATIVES_PASS=37'),
                                  ('R1CD10UniversalSmoke','INA_D10_UNIVERSAL_AB_PASSIVE_EXPORT_NEGATIVES_PASS=34'),
                                  ('R1CD09SessionAndModesSmoke','INA_D09_SESSION_AND_MODE_NEGATIVES_PASS=25'),
                                  ('R1CD08ReadOnlySmoke','INA_D08_READONLY_NEGATIVES_PASS=16'),
                                  ('R1CD07RecoverySmoke','INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS=105')]:
                result=subprocess.run(['java','-cp',td,'com.mikeyphw.instaactions.'+name],
                                      capture_output=True,text=True,timeout=30)
                self.assertEqual(result.returncode,0,result.stderr[-2000:])
                self.assertIn(expected,result.stdout)
if __name__=='__main__':unittest.main()
