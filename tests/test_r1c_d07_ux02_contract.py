"""UX02 strictly ordered flow and safety contracts, Android-independent."""
from pathlib import Path
import unittest, subprocess, shutil, tempfile, json, re
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
P=(JAVA/'R1CD05FabPanel.java').read_text()
H=(JAVA/'MainActivity.java').read_text()
G=(JAVA/'R1CD07UxGuide.java').read_text()

class OrderedFlow(unittest.TestCase):
    def test_five_steps_in_exact_order_for_both_surfaces(self):
        labels=('STEP 1 OF 5 · PREPARE','STEP 2 OF 5 · START A',
                'STEP 3 OF 5 · BROWSE','STEP 4 OF 5 · CONFIRM',
                'STEP 5 OF 5 · TEST')
        self.assertEqual([P.index(x) for x in labels],sorted(P.index(x) for x in labels))
        labels2=('STEP 1/5 · PREPARE','STEP 2/5 · START A',
                 'STEP 3/5 · BROWSE','STEP 4/5 · CONFIRM','STEP 5/5 · TEST')
        self.assertEqual([H.index(x) for x in labels2],sorted(H.index(x) for x in labels2))
    def test_confirm_after_start_a_and_fresh_evidence(self):
        self.assertLess(P.index('"Start A · read-only observation"'),P.index('"4A · Confirm ITM'))
        self.assertLess(P.index('"4A · Confirm ITM'),P.index('"4B · Enable native Repost'))
        self.assertIn('itmRecoveryOffAcknowledged=false;',P[P.index('startA = mainAction'):P.index('LinearLayout browse = section')])
        self.assertIn('if (!itmRecoveryOffAcknowledged)',P)
        self.assertIn('R1CD07ExactGateTrial.eligibleForHF13Recovery()',P)
        self.assertIn('enableRecovery.setEnabled(!on',P)
        self.assertIn('&& observingA && R1CD07ExactGateTrial.eligibleForHF13Recovery()',P)
    def test_optional_controls_come_after_finish_and_hidden(self):
        self.assertGreater(P.index('Optional · Start B read-only comparison'),P.index('STEP 5 OF 5'))
        self.assertGreater(P.index('Reset A/B observations'),P.index('STEP 5 OF 5'))
        self.assertIn('advancedBody.setVisibility(View.GONE)',P)
    def test_off_accessible_at_top_and_finish(self):
        self.assertLess(P.index('Recovery OFF now · emergency stop'),P.index('STEP 1 OF 5'))
        self.assertIn('5A · Recovery OFF · restore native results',P)
        self.assertIn('5B · Copy D07 safe Repost report',P)
        self.assertIn('R1CD07GateTrace.stopExactTrial()',P)
    def test_legacy_d05_d06_help_contract_is_still_visible(self):
        # D05/D06 integration tests require these exact user-facing labels.
        # Keep them on the actual guidance surface, not in dead comments.
        self.assertIn('InstaEclipse stays ON. Restart Instagram after changing scope.',P)
        for label in ('Start A1', 'Copy safe report', 'Share safe report'):
            self.assertIn(label,P)
        self.assertLess(P.index('STEP 1 OF 5'),P.index('STEP 2 OF 5'))
        self.assertIn('advancedBody.setVisibility(View.GONE)',P)

    def test_no_native_logic_or_identity_upgrade(self):
        for bad in ('hook(', 'chain.proceed(', 'setResult(', 'Class.forName(', 'getMediaId(', 'performRepost('):
            self.assertNotIn(bad,P+G+H)
        self.assertIn('R1CD07Recovery.off("Anchored Reels navigation lost',P)
        self.assertIn('D06_MEDIA_IDENTITY=NOT_VERIFIED',P)
        self.assertIn('ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED',P)
        q=json.loads((ROOT/'project.json').read_text())
        self.assertEqual(q['r1c_d07_repost_device_acceptance'],'NOT_RUN')
        self.assertEqual(q['r1c_d07_ux02_device_acceptance'],'NOT_RUN')
        self.assertFalse(q['r1c_d06_visible_media_verified'])
        self.assertFalse(q['r1c_d06_action_hooks_enabled'])
    def test_readiness_and_step_model_are_pure(self):
        self.assertIn('static int step(',G)
        self.assertIn('static String checklist(',G)
        self.assertIn('if (!recordingA) return 2;',G)
        self.assertIn('if (!freshA) return 3;',G)
        self.assertIn('if (recoveryOn) return 5;',G)
    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_guided_java_smoke(self):
        with tempfile.TemporaryDirectory() as tmp:
            src=ROOT/'tests/java/com/mikeyphw/instaactions/R1CD07UxGuideSmoke.java'
            compile=subprocess.run(['javac','-encoding','UTF-8','-d',tmp,str(JAVA/'R1CD07UxGuide.java'),str(src)],capture_output=True,text=True,timeout=25)
            self.assertEqual(compile.returncode,0,compile.stderr)
            result=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD07UxGuideSmoke'],capture_output=True,text=True,timeout=20)
            self.assertEqual(result.returncode,0,result.stderr)
            self.assertIn('INA_D07_UX02_GUIDANCE_PASS=22',result.stdout)

if __name__=='__main__':unittest.main()
