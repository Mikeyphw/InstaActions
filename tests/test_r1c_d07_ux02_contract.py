"""Current RCA02 UI contract; retain only meaningful dormant D07 safety regression.

D07's five-step recovery UX must NOT be considered a visible active flow.
"""
from pathlib import Path
import subprocess
import shutil
import tempfile
import unittest

ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
APP=(J/'MainActivity.java').read_text(encoding='utf-8')
OLD=(J/'R1CD05FabPanel.java').read_text(encoding='utf-8')
NEW=(J/'R1CRCA01Fab.java').read_text(encoding='utf-8')
GUIDE=(J/'R1CD07UxGuide.java').read_text(encoding='utf-8')

class CurrentRca02Ux(unittest.TestCase):
    def test_active_guide_first_and_historical_cards_hidden(self):
        self.assertIn('RCA02 · A1 → B → A2 · active diagnostics',APP)
        self.assertLess(APP.index('RCA02 · A1 → B → A2'),APP.index('pickerGuide=card('))
        self.assertIn('legacy.setVisibility(View.GONE)',APP)
        self.assertIn('new View[]{pickerGuide,fabGateCard,diagnosticPicker,guide',APP)
        for token in ('mediaGuide','accountGuide','passiveGuide','originalPlan','safety','limits'):
            self.assertIn(token,APP[APP.index('for(View legacy'):APP.index('R1CD17AutoRootDiagnostics.onAppResumed')])

    def test_legacy_cards_hidden_within_oncreate_java_scope(self):
        # RCA02's dormant panels are locals declared in onCreate. Hiding them
        # from onResume causes 12 javac "cannot find symbol" failures.
        on_create=APP.split('    @Override protected void onCreate(',1)[1].split(
            '    @Override protected void onResume()',1)[0]
        on_resume=APP.split('    @Override protected void onResume()',1)[1].split(
            '    private String combinedFabReport()',1)[0]
        marker='for(View legacy : new View[]{'
        self.assertEqual(APP.count(marker),1)
        self.assertIn(marker,on_create)
        self.assertNotIn(marker,on_resume)
        self.assertIn('legacy.setVisibility(View.GONE)',on_create)
        before=on_create.index(marker)
        for variable in ('pickerGuide','fabGateCard','diagnosticPicker','guide',
                         'comparison','d08','mediaGuide','accountGuide','passiveGuide',
                         'originalPlan','safety','limits'):
            # All referenced declarations must be in the same Java method,
            # above the hiding loop. Merely finding a name elsewhere is unsafe.
            import re
            pattern=r'\bLinearLayout\s+'+re.escape(variable)+r'\s*=\s*card\('
            decl=re.search(pattern,on_create)
            self.assertIsNotNone(decl,variable)
            self.assertLess(decl.start(),before,variable)

    def test_no_legacy_fab_attaches_and_current_step_order(self):
        entry=(J/'InstaActionsEntry.java').read_text()
        bridge=(J/'R1CD12AndroidBridge.java').read_text()
        for source in (entry,bridge):
            self.assertNotIn('R1CD05FabPanel.onResumed(',source)
            self.assertIn('R1CRCA01Fab.onResumed(',source)
        labels=('Start A1 · affected account','Stop A1 · preserve original',
                'Confirm manual switch to B','Start B · unaffected account',
                'Stop B · preserve original','Confirm manual return to A',
                'Start A2 · affected account again','Stop A2 · complete comparison')
        self.assertEqual([NEW.index(x) for x in labels],sorted(NEW.index(x) for x in labels))
        self.assertIn('Copy RCA02 A1/B/A2 report',NEW)
        self.assertIn('Clear samples · Recovery OFF',NEW)
        self.assertIn('phase!=3',NEW)
        self.assertIn('phase!=6',NEW)

    def test_dormant_code_retains_emergency_off_no_action_escalation(self):
        for s in (NEW,GUIDE):
            for forbidden in ('performRepost(', 'setResult(', 'getUsername(', 'getMediaId('):
                self.assertNotIn(forbidden,s)
        self.assertIn('R1CD07Recovery.off("RCA02 reset")',NEW)
        self.assertNotIn('R1CD07Recovery.arm(',NEW)
        self.assertIn('R1CD07Recovery.off("Anchored Reels navigation lost',OLD)
        self.assertIn('ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED',OLD)
        self.assertIn('D06_MEDIA_IDENTITY=NOT_VERIFIED',OLD)

    def test_still_compiles_original_pure_safety_guide(self):
        self.assertIn('static int step(',GUIDE)
        self.assertIn('static String checklist(',GUIDE)

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_original_pure_helper_regression(self):
        with tempfile.TemporaryDirectory(prefix='ina-d07-guide-') as tmp:
            sources=[J/'R1CD07UxGuide.java',ROOT/'tests/java/com/mikeyphw/instaactions/R1CD07UxGuideSmoke.java']
            c=subprocess.run(['javac','-encoding','UTF-8','-d',tmp,*map(str,sources)],capture_output=True,text=True,timeout=30)
            self.assertEqual(c.returncode,0,c.stderr)
            t=subprocess.run(['java','-cp',tmp,'com.mikeyphw.instaactions.R1CD07UxGuideSmoke'],capture_output=True,text=True,timeout=30)
            self.assertEqual(t.returncode,0,t.stderr)
            self.assertIn('INA_D07_UX02_GUIDANCE_PASS=22',t.stdout)

if __name__=='__main__':unittest.main()
