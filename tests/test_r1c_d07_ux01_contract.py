"""UI-only source assertions. D07C still needs physical Android evidence."""
from pathlib import Path
import json,re,subprocess,shutil,tempfile,unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
PANEL=JAVA/'R1CD05FabPanel.java'
MAIN=JAVA/'MainActivity.java'
GUIDE=JAVA/'R1CD07UxGuide.java'


def luminance(rgb):
    vals=[c/255 for c in rgb]
    v=[x/12.92 if x<=0.04045 else ((x+0.055)/1.055)**2.4 for x in vals]
    return sum(x*y for x,y in zip(v,(0.2126,0.7152,0.0722)))


def ratio(a,b):
    aa,bb=luminance(a),luminance(b)
    return (max(aa,bb)+0.05)/(min(aa,bb)+0.05)


class UX01Contract(unittest.TestCase):
    def test_high_contrast_text_and_actions(self):
        src=PANEL.read_text()
        pattern=re.compile(r'private static final int (\w+) = Color\.rgb\((\d+), (\d+), (\d+)\);')
        c={m.group(1):tuple(int(m.group(i)) for i in (2,3,4)) for m in pattern.finditer(src)}
        for foreground in ('FG','SECONDARY','MUTED','ACCENT','WARNING','SUCCESS'):
            self.assertGreaterEqual(ratio(c[foreground],c['CARD']),4.5,foreground)
        self.assertGreaterEqual(ratio(c['FG'],c['PRIMARY_BG']),4.5)
        self.assertGreaterEqual(ratio(c['FG'],c['DANGER_BG']),4.5)
        apk_verifier=(ROOT/'tools/verify_r1c_apk.py').read_text()
        self.assertIn("b'R1CD07UxGuide'",apk_verifier)
        self.assertIn("b'CURRENT STATUS'",apk_verifier)
        self.assertIn('RippleDrawable',src)
        self.assertIn('setTextSize(14)',src)
        self.assertIn('setMinHeight(dp(50))',src)

    def test_mobile_width_and_scrolling(self):
        src=PANEL.read_text()
        for required in ('Math.min(dp(432)', 'width - margins[1] - dp(10)',
                         'height - margins[0] - margins[2]', 'WindowInsets.Type.systemBars()',
                         'positionChip()',
                         'setVerticalScrollBarEnabled(true)', 'setScrollbarFadingEnabled(false)',
                         'setClipToPadding(false)', 'close()', 'Close panel'):
            self.assertIn(required,src)
        self.assertNotIn('Math.min(dp(400)',src)
        self.assertNotIn('FrameLayout.LayoutParams(dp(300)',src)

    def test_guidance_is_present_and_diagnostics_optional(self):
        p=PANEL.read_text(); home=MAIN.read_text()
        for phrase in ('CURRENT STATUS', 'STEP 1 OF 5 · PREPARE', 'STEP 2 OF 5 · START A',
                       'STEP 3 OF 5 · BROWSE', 'STEP 4 OF 5 · CONFIRM',
                       'STEP 5 OF 5 · TEST', 'Show D05/D06 diagnostic tools',
                       'advancedBody.setVisibility(View.GONE)', 'ITM Repost recovery is OFF',
                       'Dedicated Reels tab only', 'Do not press Stop A/B',
                       'Copy D07 safe Repost report', 'Recovery OFF'):
            self.assertTrue(phrase in p or phrase in GUIDE.read_text(),phrase)
        for phrase in ('D07 · Native Repost recovery', 'Device acceptance: NOT RUN',
                       'Dedicated Reels tab', 'Start A', '90 seconds', 'NOT QUALIFIED',
                       'NOT VERIFIED'):
            self.assertTrue(phrase in home or phrase in p,phrase)

    def test_no_change_to_qualification_or_hook_boundaries(self):
        src=PANEL.read_text(); guide=GUIDE.read_text()
        self.assertIn('R1CD07Recovery.off("Anchored Reels navigation lost',src)
        self.assertIn('InstaActionsEntry.requestD07(activity)',src)
        self.assertIn('R1BContextMonitor.stableScreen() != R1BNavigationPolicy.Screen.REELS',src)
        self.assertIn('if (!itmRecoveryOffAcknowledged)',src)
        self.assertIn('ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED',src)
        self.assertIn('D06_MEDIA_IDENTITY=NOT_VERIFIED\nACTIONS=OFF',src.replace('\\n','\n'))
        for forbidden in ('hook(', 'chain.proceed(', 'setResult(', 'performClick(',
                          'setEnabled(true)', 'Class.forName(', '.getId(', 'getContentDescription('):
            if forbidden=='performClick(':
                # Only the local IA chip's accessibility click may be called.
                self.assertEqual(src.count('chip.performClick('),1)
                continue
            self.assertNotIn(forbidden,src if forbidden not in ('hook(',) else guide)
        project=json.loads((ROOT/'project.json').read_text())
        self.assertFalse(project['r1c_d06_visible_media_verified'])
        self.assertFalse(project['r1c_d06_action_hooks_enabled'])
        self.assertEqual(project['r1c_d07_repost_device_acceptance'],'NOT_RUN')
        self.assertEqual(project['r1c_d07_ux01_device_acceptance'],'NOT_RUN')

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'Java compiler not installed')
    def test_pure_java_next_step_state_machine(self):
        with tempfile.TemporaryDirectory(prefix='ina-ux01-') as temp:
            r=subprocess.run(['javac','-encoding','UTF-8','-d',temp,str(GUIDE),
                str(ROOT/'tests/java/com/mikeyphw/instaactions/R1CD07UxGuideSmoke.java')],
                capture_output=True,text=True,timeout=30)
            self.assertEqual(r.returncode,0,r.stderr)
            r=subprocess.run(['java','-cp',temp,
                'com.mikeyphw.instaactions.R1CD07UxGuideSmoke'],
                capture_output=True,text=True,timeout=30)
            self.assertEqual(r.returncode,0,r.stderr)
            self.assertIn('INA_D07_UX02_GUIDANCE_PASS=22',r.stdout)

if __name__=='__main__':unittest.main()
