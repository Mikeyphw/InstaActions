from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/mikeyphw/instaactions'

class R1BHF05HybridNavigationContract(unittest.TestCase):
    def test_hybrid_navigation_is_separate_from_media_context(self):
        monitor = (JAVA/'R1BContextMonitor.java').read_text()
        nav = (JAVA/'R1BNavigationPolicy.java').read_text()
        stabilizer = (JAVA/'R1BNavigationStabilizer.java').read_text()
        self.assertIn('INA_R1B_CONTEXT surface=', monitor)
        self.assertIn('INA_R1B_NAVIGATION surface=', monitor)
        self.assertIn('INA_R1B_NAV_EVIDENCE main=', monitor)
        self.assertIn('R1BNavigationPolicy.infer(', monitor)
        self.assertIn('R1BNavigationStabilizer', monitor)
        self.assertIn('feedTabSelected', monitor)
        self.assertIn('mainFeedBarVisible', monitor)
        self.assertIn('view.isSelected()', monitor)
        self.assertIn('view.isActivated()', monitor)
        self.assertIn('MIN_STABLE_MS = 1000L', stabilizer)
        self.assertIn('Screen.HOME_FEED', nav)
        self.assertIn('media.reelsScore != 0 || media.storyScore != 0', nav)
        self.assertIn('isMainActivity', nav)

    def test_bounded_periodic_updates_and_lifecycle_cleanup(self):
        s = (JAVA/'R1BContextMonitor.java').read_text()
        self.assertIn('ui.postDelayed(this, 1600L)', s)
        self.assertIn('ui.removeCallbacks(navigationTimer);', s)
        self.assertIn('navigation.reset();', s)
        self.assertIn('MIN_SAMPLE_INTERVAL_MS', s)
        self.assertIn('MAX_VIEWS', s)
        self.assertIn('hintTimer', s)
        self.assertIn('ui.removeCallbacks(hintTimer);', s)

    def test_stronger_post_identity_and_safe_fallback(self):
        s = (JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('feedRowLike && !reelAnchor && !storyAnchor', s)
        self.assertIn('weakPostHints++', s)
        self.assertIn('Math.min(2, weakPostHints)', s)
        self.assertNotIn('post += 3; postAnchor = true;', s)
        tests = (ROOT/'app/src/test/java/com/mikeyphw/instaactions/R1BNavigationPolicyTest.java').read_text()
        self.assertIn('homeTabMerelyVisibleCannotEstablishHome', tests)
        self.assertIn('viewerCollisionsAndCandidateHintsAreUnknown', tests)
        self.assertIn('feedRowProofRemainsSeparateFromHomeScreen', tests)

    def test_action_and_privacy_guards_remain_intact(self):
        for p in ('R1BContextMonitor.java', 'R1BNavigationPolicy.java',
                  'R1BNavigationStabilizer.java', 'R1BContextPolicy.java'):
            src = (JAVA/p).read_text()
            for forbidden in ('getText(', 'getContentDescription(', 'getOnClickListener(',
                              'performClick(', 'setOnClickListener(', 'findViewById(',
                              'getAccounts(', 'getCookie(', 'HttpURLConnection'):
                self.assertNotIn(forbidden, src, (p, forbidden))
        self.assertIn('MUTATING_ACTIONS_ENABLED = false',
                      (JAVA/'R1ProbePolicy.java').read_text())
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',
                      (JAVA/'R1ProbeController.java').read_text())

    def test_version_and_acceptance_docs(self):
        gradle = (ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 8',gradle)
        self.assertIn("versionName '0.3.5-r1b-hf05'",gradle)
        docs=(ROOT/'docs/R1B_HF05_HYBRID_NAVIGATION.md').read_text()
        for token in ('HOME_FEED','POST','INA_R1B_NAVIGATION',
                      'INA_R1B_NAV_EVIDENCE','DexKit', 'NOT_QUALIFIED'):
            self.assertIn(token,docs)
        verifier=(ROOT/'tools/verify_r1b_apk.py').read_text()
        self.assertIn("b'R1BNavigationPolicy'",verifier)
        self.assertIn("b'INA_R1B_NAVIGATION'",verifier)

if __name__ == '__main__':unittest.main()
