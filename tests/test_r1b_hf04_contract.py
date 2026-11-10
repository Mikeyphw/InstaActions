from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class R1BHF04LiveResourceEvidence(unittest.TestCase):
    def test_capped_hint_logging_and_detach_cleanup(self):
        monitor=(JAVA/'R1BContextMonitor.java').read_text()
        helper=(JAVA/'R1BResourceDiagnostics.java').read_text()
        self.assertIn('INA_R1B_RESOURCE_HINTS',monitor)
        self.assertIn('MAX_LOGS_PER_SESSION = 3',helper)
        self.assertIn('HINT_INTERVAL_MS = 8000L',helper)
        self.assertIn('ui.removeCallbacks(hintTimer);',monitor)
        self.assertIn('ui.removeCallbacks(sampler);',monitor)
        self.assertIn('result.surface == R1BContextPolicy.Surface.UNKNOWN',monitor)
        self.assertIn('view.getClass().getName()',monitor)
        self.assertIn('getResourceEntryName(id)',monitor)
        self.assertIn('MAX_DISTINCT_CLASSES = 40',helper)
        self.assertIn('MAX_IDS = 24',helper)
        self.assertIn('MAX_CLASSES = 12',helper)
    def test_instaeclipse_source_backed_feed_anchor_is_fail_closed(self):
        p=(JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('"row_feed_button_like".equals(id)',p)
        self.assertIn('"row_feed_button_save".equals(id)',p)
        self.assertIn('"media_group".equals(id)',p)
        self.assertIn('feedRowLike && !reelAnchor && !storyAnchor',p)
        self.assertIn('if (feedRowSave) post++',p)
        self.assertIn('if (feedMediaGroup) post++',p)
        t=(ROOT/'app/src/test/java/com/mikeyphw/instaactions/R1BHF04FeedAnchorsTest.java').read_text()
        self.assertIn('reelAndStoryViewerEvidenceOutranksBackgroundFeedRow',t)
        self.assertIn('supportingFeedResourcesRaiseScoreOnlyWithLikeButton',t)

    def test_no_user_content_or_actions(self):
        monitor=(JAVA/'R1BContextMonitor.java').read_text()
        helper=(JAVA/'R1BResourceDiagnostics.java').read_text()
        for source in [monitor,helper]:
            for banned in ('getText(', 'performClick(', 'setText(', 'setOnClickListener(',
                    'getOnClickListener(', 'getContentDescription(', 'getAccessibilityNodeProvider(',
                    'getAccount(', 'getCookie(', 'HttpURLConnection', 'MediaStore'):
                self.assertNotIn(banned,source)
        self.assertIn('MUTATING_ACTIONS_ENABLED = false',(JAVA/'R1ProbePolicy.java').read_text())
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',
                      (JAVA/'R1ProbeController.java').read_text())
    def test_preview_does_not_promote_context_to_action(self):
        entry=(JAVA/'InstaActionsEntry.java').read_text()
        policy=(JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('chain.proceed()',entry)
        self.assertIn('MAX_VIEWS = 384',policy)
        self.assertIn('MIN_SAMPLE_INTERVAL_MS = 800',policy)
        self.assertIn('versionQualified = R1ProbePolicy.supportedVersion',
                      (JAVA/'R1BContextMonitor.java').read_text())
    def test_build_and_docs(self):
        gradle=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 13',gradle)
        self.assertIn("versionName '0.4.4-r1c-d05'",gradle)
        self.assertIn('INA_R1B_RESOURCE_HINTS',(ROOT/'docs/R1B_HF04_FEED_HINTS.md').read_text())
        self.assertIn('b2453613ff8a7adf332d49d852a2ad4ba8d618a1',
                      (ROOT/'docs/R1B_HF04_FEED_HINTS.md').read_text())

if __name__=='__main__':unittest.main()
