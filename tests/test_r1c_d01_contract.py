"""R1C-D01 read-only candidate inventory. This is NOT live-media identity proof."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class R1CD01Contract(unittest.TestCase):
    def test_background_and_once_per_apk(self):
        s=(JAVA/'R1CDiscoveryController.java').read_text()
        self.assertIn('private static final Set<String> STARTED',s)
        self.assertIn('version + ":" + apk.length() + ":" + apk.lastModified()',s)
        self.assertIn('setDaemon(true)',s)
        self.assertIn('setPriority(Thread.MIN_PRIORITY)',s)
        self.assertIn('R1CDiscoveryPolicy.mayInventory(version)',s)
        self.assertIn('INA_R1C_SCHEDULED',s)
    def test_no_content_access_or_side_effect_hook(self):
        s=(JAVA/'R1CMediaCandidateProbe.java').read_text()
        self.assertIn('Class.forName(className, false, loader)',s)
        self.assertIn('getDeclaredMethods()',s)
        self.assertNotIn('.invoke(',s)
        self.assertNotIn('hook(',s)
        self.assertNotIn('getText(',s)
        self.assertNotIn('getOnClickListener(',s)
        self.assertNotIn('performClick(',s)
        self.assertNotIn('getSharedPreferences(',s)
        self.assertNotIn('getCookie(',s)
        self.assertNotIn('username',s.lower())
        self.assertIn('identity_verified=false binding_hooks=off actions=off',s)
    def test_source_backed_bounded_markers(self):
        s=(JAVA/'R1CMediaCandidateProbe.java').read_text()
        for marker in ('ReelViewerFragment.onCurrentActiveItemBound',
                'ClipsOrganicMediaItemViewMoreOptionsController',
                'MediaOptionsOverflowMenuCreator',
                'com.instagram.feed.media.Media'):
            self.assertIn(marker,s)
        self.assertIn('i < R1CDiscoveryPolicy.MAX_MARKER_QUERIES',s)
        self.assertIn('i < R1CDiscoveryPolicy.MAX_CLASS_PROBES',s)
    def test_existing_r1a_version_guard_unchanged(self):
        s=(JAVA/'R1ProbeController.java').read_text()
        self.assertIn('if (!R1ProbePolicy.supportedVersion(version))',s)
        e=(JAVA/'InstaActionsEntry.java').read_text()
        self.assertIn('R1CDiscoveryController.onActivityResumed',e)
        self.assertIn('R1ProbeController.onActivityResumed',e)
        self.assertIn('R1BContextMonitor.resumed',e)
        self.assertIn('chain.proceed()',e)
    def test_ci_and_version(self):
        g=(ROOT/'app/build.gradle').read_text()
        self.assertIn('versionCode 10',g)
        self.assertIn('0.4.1-r1c-d02',g)
        w=(ROOT/'.github/workflows/android-r0.yml').read_text()
        self.assertIn('tools/verify_r1c_apk.py',w)
        self.assertIn('InstaActions-R1C-',w)
    def test_qualification_is_not_assumed(self):
        doc=(ROOT/'docs/R1C_D01_DEVICE_ACCEPTANCE.md').read_text()
        self.assertIn('DEVICE_INVENTORY_PASS',doc)
        self.assertIn('identity_verified=false',doc)

if __name__=='__main__':unittest.main()
