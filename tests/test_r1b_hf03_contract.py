from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'
class R1BHF03VersionSeparation(unittest.TestCase):
    def test_observer_does_not_exit_for_unqualified_instagram(self):
        src=(JAVA/'R1BContextMonitor.java').read_text()
        self.assertNotIn('if (!R1ProbePolicy.supportedVersion(info.versionName)) return;',src)
        self.assertIn('boolean versionQualified = R1ProbePolicy.supportedVersion(info.versionName);',src)
        self.assertIn('Session session = new Session(decor, versionQualified,',src)
        self.assertIn('INA_R1B_VERSION',src)
        self.assertIn('INA_R1B_OBSERVER_ATTACHED',src)
        self.assertIn('qualified=',src)
        self.assertIn('mode=read_only_preview',src)
        self.assertIn('value.replaceAll',src)
        self.assertIn('normalized.substring(0, 64)',src)
    def test_dexkit_remains_version_locked_and_actions_disabled(self):
        p=(JAVA/'R1ProbeController.java').read_text()
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',p)
        self.assertIn('INA_R1_SCAN_SKIP reason=unqualified_version actions=off',p)
        self.assertIn('R1DexKitProbe.scan',p)
        self.assertIn('MUTATING_ACTIONS_ENABLED = false', (JAVA/'R1ProbePolicy.java').read_text())
    def test_bound_and_nonmutating(self):
        src=(JAVA/'R1BContextMonitor.java').read_text()
        policy=(JAVA/'R1BContextPolicy.java').read_text()
        self.assertIn('MIN_SAMPLE_INTERVAL_MS = 800',policy)
        self.assertIn('MAX_VIEWS = 384',policy)
        for bad in ('.getText(','.performClick(','.setText(','.getOnClickListener(',
                    'setOnClickListener(', 'HttpURLConnection', 'ClipboardManager'):
            self.assertNotIn(bad,src)
        self.assertIn('observer.removeOnGlobalLayoutListener(this)',src)
        self.assertIn('observer.removeOnScrollChangedListener(this)',src)
        self.assertIn('ui.removeCallbacks(sampler)',src)
    def test_provenance(self):
        src=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 21',src)
        self.assertIn("versionName '0.5.2-r1c-d07-repost-experimental'",src)
        doc=(ROOT/'docs/R1B_HF03_VERSION_GATE.md').read_text()
        self.assertIn('497d7b4d4e896bf25338d4668ebb67387c3d6468',doc)
        self.assertIn('provisional',doc)

if __name__=='__main__':unittest.main()
