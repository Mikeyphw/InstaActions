"""RCA02 active-FAB accessibility and visible guidance contracts.

The retired D07 recovery panel has its own dormant-source safety regressions
in UX02; this file must check what users can actually reach today. Never
reinstate legacy Repost controls to satisfy historical presentation assertions.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions'
ACTIVE_FAB = JAVA / 'R1CRCA01Fab.java'  # Retained class name; runtime is RCA02.
MAIN = JAVA / 'MainActivity.java'


def _rgb(value):
    """Parse an opaque Android ARGB constant; unexpected palette is a failure."""
    if not re.fullmatch(r'0x[fF][fF][0-9a-fA-F]{6}', value):
        raise ValueError('Active UI color must be an opaque Android ARGB literal')
    return tuple(int(value[i:i + 2], 16) for i in (4, 6, 8))


def _luminance(rgb):
    linear = []
    for channel in rgb:
        v = channel / 255
        linear.append(v / 12.92 if v <= 0.04045 else ((v + 0.055) / 1.055) ** 2.4)
    return sum(a * b for a, b in zip(linear, (0.2126, 0.7152, 0.0722)))


def _contrast(first, second):
    a, b = _luminance(first), _luminance(second)
    return (max(a, b) + 0.05) / (min(a, b) + 0.05)


class UX01CurrentRca02(unittest.TestCase):
    def test_active_fab_contrast_uses_current_colors(self):
        source = ACTIVE_FAB.read_text(encoding='utf-8')
        const = {key: _rgb(value) for key, value in re.findall(
            r'\b(BG|FG|ACCENT)\s*=\s*(0x[0-9a-fA-F]{8})\b', source)}
        self.assertEqual(set(const), {'BG', 'FG', 'ACCENT'})
        match = re.search(r'b\.setBackgroundColor\((0x[0-9a-fA-F]{8})\)', source)
        self.assertIsNotNone(match, 'Active diagnostic action background must be explicit')
        action_background = _rgb(match.group(1))
        for name, foreground, background in (
            ('current text', const['FG'], const['BG']),
            ('current action labels', const['FG'], action_background),
            ('current heading', const['ACCENT'], const['BG']),
        ):
            self.assertGreaterEqual(_contrast(foreground, background), 4.5, name)
        self.assertIn('chip.setText("RCA02")', source)
        self.assertIn('chip.setAllCaps(false)', source)
        self.assertIn('b.setAllCaps(false)', source)

    def test_current_fab_is_scrollable_and_has_bounded_attachment(self):
        source = ACTIVE_FAB.read_text(encoding='utf-8')
        for expression in (
            'panel=new ScrollView(a)', 'panel.addView(body)',
            'new FrameLayout.LayoutParams(dp(310)',
            'Math.min(dp(480),Math.max(dp(230)',
            'MAIN.postDelayed(', 'RETRY_DELAYS={140L,420L,1000L}',
            'R1CD16MainSurface.inspect(a)',
            'panel.setVisibility(', 'action(body,"Close diagnostics"',
        ):
            self.assertIn(expression, source, expression)
        self.assertIn('if(detached || retries>=RETRY_DELAYS.length)return;', source)
        self.assertIn('host.addView(chip,cp)', source)
        self.assertIn('host.removeView(chip)', source)
        self.assertIn('host.removeView(panel)', source)

    def test_visible_app_guide_is_rca02_not_retired_recovery(self):
        home = MAIN.read_text(encoding='utf-8')
        self.assertIn('InstaActions · RCA02', home)
        self.assertIn('RCA02 · A1 → B → A2 · active diagnostics', home)
        self.assertIn('The old Repost enabling panel is disabled.', home)
        self.assertIn('Physical RCA02 acceptance: NOT RUN', home)
        self.assertIn('manually switch to B and confirm', home)
        self.assertIn('return to A and confirm', home)
        self.assertIn('server rollout, same Reel or account identities', home)
        self.assertLess(home.index('RCA02 · A1 → B → A2'),
                        home.index('pickerGuide=card('))
        self.assertIn('legacy.setVisibility(View.GONE)', home)
        self.assertIn('for(View legacy', home)

    def test_readonly_controls_do_not_expose_old_recovery(self):
        source = ACTIVE_FAB.read_text(encoding='utf-8')
        for caption in (
            'Start A1 · affected account', 'Stop A1 · preserve original',
            'Confirm manual switch to B', 'Start B · unaffected account',
            'Stop B · preserve original', 'Confirm manual return to A',
            'Start A2 · affected account again',
            'Stop A2 · complete comparison',
            'Copy RCA02 A1/B/A2 report', 'Clear samples · Recovery OFF',
        ):
            self.assertIn(caption, source)
        self.assertIn('R1CD07Recovery.off("RCA02 diagnostic-only UI', source)
        self.assertIn('R1CD07Recovery.off("RCA02 reset")', source)
        for forbidden in (
            'R1CD07Recovery.arm(', 'R1CD07GateTrace.armHF13',
            'performRepost(', 'setResult(', 'performClick(',
        ):
            self.assertNotIn(forbidden, source)


if __name__ == '__main__':
    unittest.main()
