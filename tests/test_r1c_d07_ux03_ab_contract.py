"""D07 UX03 visible A/B capture lifecycle, original R0-R8 roadmap and D08 audit."""
from pathlib import Path
import json,subprocess,shutil,tempfile,unittest
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class UX03_AB(unittest.TestCase):
    def test_primary_recovery_remains_five_ordered_steps(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        steps=['STEP 1 OF 5','STEP 2 OF 5','STEP 3 OF 5','STEP 4 OF 5','STEP 5 OF 5']
        self.assertEqual(sorted(p.index(s) for s in steps),[p.index(s) for s in steps])
        self.assertLess(p.index('STEP 5 OF 5'),p.index('OPTIONAL · COMPARE ACCOUNT A'))
        self.assertLess(p.index('OPTIONAL · COMPARE ACCOUNT A'),p.index('Show D05/D06 diagnostic tools'))
        for mark in ('Recovery OFF now · emergency stop','4A · Confirm ITM Repost recovery is OFF',
            '4B · Enable native Repost','5B · Copy D07 safe Repost report',
            'InstaEclipse stays ON. Restart Instagram after changing scope.',
            'D06_MEDIA_IDENTITY=NOT_VERIFIED','ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED'):
            self.assertIn(mark,p)
    def test_a_b_controls_visible_and_guarded(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        self.assertLess(p.index('A2 · Stop A'),p.index('Optional · Start B read-only comparison'))
        self.assertLess(p.index('Optional · Start B read-only comparison'),p.index('B3 · Stop B',p.index('Optional · Start B read-only comparison')))
        self.assertLess(p.index('B3 · Stop B',p.index('Optional · Start B read-only comparison')),p.index('B4 · Copy combined A/B report'))
        self.assertIn('R1CD07ComparisonFlow.canStopA(',p)
        self.assertIn('R1CD07ComparisonFlow.canStartB(',p)
        self.assertIn('R1CD07ComparisonFlow.canStopB(',p)
        self.assertIn('R1CD07GateTrace.stop()',p)
        self.assertIn('R1CD07GateTrace.begin(index)',p)
        self.assertIn('runStartB()',p)
        self.assertIn('R1CD07GateTrace.clear()',p)
        self.assertNotIn('advancedBody, "Optional · Start B read-only comparison"',p)
        self.assertIn('A process restart loses both captures.',p)
    def test_stop_preserves_counts_reset_really_clears(self):
        s=(J/'R1CD07GateTrace.java').read_text()
        self.assertIn('static int cohortEvents(int index) {return STATE.eventsFor(index);}',s)
        self.assertIn('STATE.stop();return "P04 stopped',s)
        self.assertIn('R1CD07ExactGateTrial.onCaptureStop();STATE.stop()',s)
        self.assertIn('R1CD07ExactGateTrial.reset();STATE.clear();R1CD07PredicateTrace.clear();',s)
        self.assertIn('synchronized int eventsFor(int cohort)',(J/'R1CD07TraceSummary.java').read_text())
    def test_no_action_or_identity_upgrade(self):
        p=(J/'R1CD05FabPanel.java').read_text()
        policy=(J/'R1CD07ComparisonFlow.java').read_text()
        for forbidden in ('hook(', 'setResult(', 'chain.proceed(', 'Class.forName(', 'performRepost(', 'getMediaId(', 'getOnClickListener('):
            self.assertNotIn(forbidden,p+policy)
        meta=json.loads((ROOT/'project.json').read_text())
        self.assertEqual(meta['r1c_d07_repost_device_acceptance'],'NOT_RUN')
        self.assertEqual(meta['r1c_d07_ux03_device_acceptance'],'NOT_RUN')
        self.assertFalse(meta['r1c_d08_feed_repost_enabled'])
        self.assertFalse(meta['r1c_d08_avatar_render_modification_enabled'])
        self.assertFalse(meta['r1c_d06_visible_media_verified'])
        self.assertFalse(meta['r1c_d06_action_hooks_enabled'])
        self.assertEqual(meta['scope_packages'],['com.instagram.android'])
        self.assertIn('R1CD07ComparisonFlow',(ROOT/'tools/verify_r1c_apk.py').read_text())
    def test_original_objective_preserved(self):
        roadmap=(ROOT/'ROADMAP.md').read_text()
        audit=(ROOT/'docs/R1C_D08_FEED_REPOST_FRIEND_AVATAR_AUDIT.md').read_text()
        home=(J/'MainActivity.java').read_text()
        for ui in ('Original InstaActions goals · R0–R8','R2 contextual action UI',
                   'R3 explicit Like/Save/Repost/Download/Playback',
                   'R4 native sharing','R8 release','Feed Repost remain separate D08 investigations'):
            self.assertIn(ui,home)
        for key in ('## R0', '## R1A', '## R1B','## R1C','## R2','## R3','## R4','## R5','## R6','## R7','## R8'):
            self.assertIn(key,roadmap)
        for term in ('quick actions','Feed','Reels','Stories','InstaEclipse',
            'Friends avatars','Feed Repost','can_repost','X.0G4F','RepostsRepository',
            'NOT QUALIFIED','no new hook'):
            self.assertIn(term.lower(),audit.lower())
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'Requires local JDK for pure Java tests only')
    def test_pure_java_27_safety_cases(self):
        with tempfile.TemporaryDirectory(prefix='ina-ab-') as tmp:
            files=['R1CD07ComparisonFlow.java','R1CD07TraceSummary.java','R1CD07VariantShape.java']
            cmd=['javac','-encoding','UTF-8','-d',tmp]+[str(J/x) for x in files]+[str(ROOT/'tests/java/com/mikeyphw/instaactions/R1CD07ComparisonFlowSmoke.java')]
            p=subprocess.run(cmd,capture_output=True,text=True,timeout=50)
            self.assertEqual(p.returncode,0,p.stderr)
            r=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD07ComparisonFlowSmoke'],capture_output=True,text=True,timeout=20)
            self.assertEqual(r.returncode,0,r.stderr)
            self.assertIn('INA_D07_UX03_AB_FLOW_PASS=27',r.stdout)
if __name__=='__main__':unittest.main()
