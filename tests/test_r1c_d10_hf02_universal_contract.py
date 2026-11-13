"""D10 source-only fail-closed A/B, avatar, passive, cumulative export contracts."""
from pathlib import Path
import json,subprocess,tempfile,shutil,unittest
R=Path(__file__).resolve().parents[1];J=R/'app/src/main/java/com/mikeyphw/instaactions'
class D10UniversalContract(unittest.TestCase):
    def test_all_modes_in_dropdown_and_exclusive_lifecycle(self):
        p=(J/'R1CD05FabPanel.java').read_text();g=(J/'R1CD09DiagnosticGate.java').read_text()
        for s in ('Friend avatars · read-only A/B','Passive overview · auto ON',
                  'runStartA','runStopA','runConfirmSwitch','runStartB','runStopB',
                  'R1CD10ComparisonFlow.complete()', 'R1CD10ComparisonFlow.abort()',
                  'R1CD10ReportArchive.capture', 'Finish current diagnostic',
                  'Abort current diagnostic','R1CD09DiagnosticGate.active(',
                  'R1CD07Recovery.isEnabled()'):
            self.assertIn(s,p,s)
        self.assertIn('Mode.PASSIVE',g)
        self.assertIn('if(state==State.RUNNING)return "REFUSED:',g)
        self.assertIn('accountTransitionExpected',g)
    def test_explicit_cohort_no_default_and_every_diagnostic_archived(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertNotIn('cohortPicker.getSelectedItemPosition()',p)
        self.assertIn('R1CD08FeedAudit.beginCohort(index)',p)
        self.assertIn('R1CD10AvatarAudit.begin(index)',p)
        self.assertIn('R1CD08MediaEvidence.setManualCohort(index)',p)
        self.assertIn('R1CD07GateTrace.begin(index)',p)
        self.assertIn('R1CD09AccountSentinel.markManualSwitch()',p)
        self.assertIn('reportForSelectedMode()',p)
        self.assertIn('"ABORTED_PARTIAL_NOT_QUALIFIED"',p)
        self.assertIn('"FINISHED"',p)
    def test_passive_uses_only_existing_hooks(self):
        f=(J/'R1CD10PassiveLedger.java').read_text();g=(J/'R1CD07GateTrace.java').read_text()
        c=(J/'R1CD04PassiveProbe.java').read_text();a=(J/'R1CD09AccountSentinel.java').read_text()
        self.assertIn('UI_REPOST',f)
        self.assertIn('MEDIA_MENU',c)
        self.assertIn('SESSION_CALLBACK',a)
        self.assertIn('R1CD10PassiveLedger.observe',g)
        for s in ('hook(', 'getUsername(', 'getMediaId(', 'getDeclaredFields(', 'readBitmap(', 'performClick('):
            self.assertNotIn(s,f)
    def test_archive_and_export_are_safe_bounded(self):
        a=(J/'R1CD10ReportArchive.java').read_text();p=(J/'R1CD05FabPanel.java').read_text()
        for v in ('MAX_RECORDS=28','MAX_TOTAL_CHARS=180_000','MAX_RECORD_CHARS=16_000',
                  'lost_oldest=', 'NOT full Android logcat','current Instagram process'):
            self.assertIn(v,a)
        for v in ('"Copy ALL diagnostics', '"Export ALL diagnostics',
                  'Intent.ACTION_SEND','Intent.EXTRA_TEXT', 'ClipData.newPlainText',
                  'R1CD10ReportArchive.all('):
            self.assertIn(v,p)
        self.assertNotIn('writeText(',a)
        self.assertNotIn('getSharedPreferences(',a)
    def test_avatar_is_aggregate_view_hint_not_identity(self):
        a=(J/'R1CD10AvatarAudit.java').read_text()
        for v in ('MAX','Friends','ImageView','MAX_MS', 'NOT_VERIFIED','MAX_VIEWS'):
            if v in ('MAX','MAX_MS','MAX_VIEWS'): continue
            self.assertIn(v,a)
        for forbidden in ('getBitmap(', 'getDrawable(', 'getUsername(', 'getId(',
                          'getAccountId(', 'setImageDrawable(', 'setResult('):
            self.assertNotIn(forbidden,a)
    def test_original_roadmap_and_qualifications(self):
        d=json.loads((R/'project.json').read_text())
        for k in ('r1c_d10_a_b_all_active_diagnostics_source',
                  'r1c_d10_passive_existing_callbacks_source',
                  'r1c_d10_all_export_process_volatile_source'):
            self.assertTrue(d[k])
        for k in ('r1c_d10_account_identity_verified','r1c_d10_avatar_identity_verified',
                  'r1c_d10_feed_repost_qualified','r1c_d10_media_identity_verified',
                  'r1c_d10_hosted_ci_qualified','r1c_d06_visible_media_verified',
                  'r1c_d08_feed_repost_enabled'):
            self.assertFalse(d[k])
        self.assertEqual(d['r1c_d10_device_acceptance'],'NOT_RUN')
        for heading in ('## R0','## R2','## R3','## R4','## R5','## R6','## R7','## R8'):
            self.assertIn(heading,(R/'ROADMAP.md').read_text())
    @unittest.skipUnless(shutil.which('java') and shutil.which('javac'),'JDK required')
    def test_pure_java_ab_passive_and_105_original_safety(self):
        names=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
               'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence',
               'R1CD09DiagnosticGate','R1CD09AccountSentinel',
               'R1CD10ComparisonFlow','R1CD10PassiveLedger','R1CD10ReportArchive']
        smokes=['R1CD10UniversalSmoke','R1CD09SessionAndModesSmoke',
                'R1CD08ReadOnlySmoke','R1CD07RecoverySmoke']
        with tempfile.TemporaryDirectory(prefix='ina-d10-jvm-') as tmp:
            cmd=['javac','-encoding','UTF-8','-d',tmp]+[str(J/(v+'.java')) for v in names]+[
                str(R/'tests/java/com/mikeyphw/instaactions'/(v+'.java')) for v in smokes]
            done=subprocess.run(cmd,capture_output=True,text=True,timeout=90)
            self.assertEqual(done.returncode,0,done.stderr)
            for cls,marker in [('R1CD10UniversalSmoke','INA_D10_UNIVERSAL_AB_PASSIVE_EXPORT_NEGATIVES_PASS='),
                               ('R1CD09SessionAndModesSmoke','INA_D09_SESSION_AND_MODE_NEGATIVES_PASS='),
                               ('R1CD08ReadOnlySmoke','INA_D08_READONLY_NEGATIVES_PASS='),
                               ('R1CD07RecoverySmoke','INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS=105')]:
                p=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.'+cls],
                     capture_output=True,text=True,timeout=30)
                self.assertEqual(p.returncode,0,p.stderr+p.stdout)
                self.assertIn(marker,p.stdout)
if __name__=='__main__':unittest.main()
