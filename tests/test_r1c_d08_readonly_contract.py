"""D08 A+C and exact X.02ib read-only source/device boundary contract."""
from pathlib import Path
import unittest,subprocess,tempfile,shutil,json
R=Path(__file__).resolve().parents[1];J=R/'app/src/main/java/com/mikeyphw/instaactions'
class D08ReadOnlyContract(unittest.TestCase):
    def test_exact_d3r_original_only_and_receiver(self):
        t=(J/'R1CD07PredicateTrace.java').read_text();e=(J/'InstaActionsEntry.java').read_text()
        for x in ('d08ExactConfigReport()', 'D08_EXACT', 'D08_RECEIVER', 'D08_COVERAGE',
                  'noteDataReceiver(', 'frame.changedByTrial', 'd08OtherKeys', 'missing_exact=',
                  'original_false=', 'original_null=', 'intervention_excluded=', 'other_key_calls='):
            self.assertIn(x,t)
        self.assertIn('R1CD07PredicateTrace.noteDataReceiver(chain.getThisObject(),',e)
        self.assertIn('Object original = chain.proceed()',e)
        self.assertIn('return R1CD07PredicateTrace.noteExactData(chain.getArg(0),original)',e)
        self.assertEqual(e.count('ina-d07-verified-d3r-'),1)
    def test_feed_ui_snapshot_never_grants_action(self):
        s=(J/'R1CD08FeedAudit.java').read_text();panel=(J/'R1CD05FabPanel.java').read_text()
        for x in ('MAX_VIEWS=512','MAX_MS=30L','R1CD07Recovery.isEnabled()',
                  'R1CD04PassiveProbe.isInstalled()', 'screen_mismatch=', 'scan_truncated=',
                  'Feed-opened Reels NOT QUALIFIED','matches(view.getContentDescription())'):
            self.assertIn(x,s)
        for x in ('Snapshot ordinary Feed post','Snapshot Feed-opened Reel',
                  'Snapshot dedicated Reels tab','D08C · VISIBLE MEDIA IDENTITY'):
            self.assertIn(x,panel)
        for forbidden in ('performClick(', 'setResult(', 'chain.proceed(', 'startActivity(', 'getId(',
                          'getUsername(', 'getCaption(', 'setEnabled(true)', 'getDeclaredFields('):
            self.assertNotIn(forbidden,s)
    def test_identity_candidates_do_not_authorize_actions(self):
        s=(J/'R1CD08MediaEvidence.java').read_text();d=(J/'R1CD04PassiveProbe.java').read_text()
        self.assertIn('R1CD08MediaEvidence.record(',d)
        self.assertIn('D08_MEDIA_IDENTITY=NOT_VERIFIED',s)
        self.assertIn('PREMENU_WEAK_CANDIDATE',s)
        self.assertIn('PAGE_REBOUND',s)
        self.assertNotIn('getId(',s)
    def test_roadmap_and_flags(self):
        d=json.loads((R/'project.json').read_text());src=(R/'ROADMAP.md').read_text()
        for name in ('## R0','## R1C','## R2','## R3','## R4','## R5','## R6','## R7','## R8'):
            self.assertIn(name,src)
        self.assertFalse(d['r1c_d08c_media_identity_verified'])
        self.assertFalse(d['r1c_d08_feed_repost_enabled'])
        self.assertFalse(d['r1c_d08_avatar_render_modification_enabled'])
        self.assertFalse(d['r1c_d08_x02ib_causality_confirmed'])
        self.assertEqual(d['r1c_d08_device_acceptance'],'NOT_RUN')
        self.assertFalse(d['r1c_d06_visible_media_verified'])
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_pure_java_d08_and_inherited_d07(self):
        names=['R1CD07Recovery','R1CD07ExactGateTrial','R1CD07PredicateTrace',
               'R1CD07BranchInputs','R1CD07InputPresence','R1CD08MediaEvidence','R1CD09DiagnosticGate']
        smoke=R/'tests/java/com/mikeyphw/instaactions/R1CD08ReadOnlySmoke.java'
        inherited=R/'tests/java/com/mikeyphw/instaactions/R1CD07RecoverySmoke.java'
        with tempfile.TemporaryDirectory(prefix='ina-d08-jvm-') as tmp:
            cmd=['javac','-encoding','UTF-8','-d',tmp]+[str(J/(name+'.java')) for name in names]+[str(smoke),str(inherited)]
            done=subprocess.run(cmd,capture_output=True,text=True,timeout=60)
            self.assertEqual(done.returncode,0,done.stderr)
            for klass,needle in [('R1CD08ReadOnlySmoke','INA_D08_READONLY_NEGATIVES_PASS='),
                                 ('R1CD07RecoverySmoke','INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS=105')]:
                run=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.'+klass],
                                   capture_output=True,text=True,timeout=30)
                self.assertEqual(run.returncode,0,run.stderr)
                self.assertIn(needle,run.stdout)
if __name__=='__main__':unittest.main()
