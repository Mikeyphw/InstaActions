#!/usr/bin/env python3
"""Hosted proof of packaged R1C-D01 discovery only, not runtime acceptance."""
import argparse
from pathlib import Path
import zipfile

def check(apk:Path):
    with zipfile.ZipFile(apk) as z:
        dexes=[n for n in z.namelist() if n.endswith('.dex')]
        assert dexes, 'R1C: APK has no DEX'
        blob=b''.join(z.read(n) for n in dexes)
        for token in (b'R1CDiscoveryPolicy', b'R1CDiscoveryController',
                b'R1CMediaCandidateProbe', b'R1CStaticShapeProbe', b'R1CModernReelsPolicy', b'R1CD04Policy', b'R1CD04PassiveProbe', b'R1CD05ReferenceContinuity', b'R1CD05FabPanel',
                b'R1CD05FabSession', b'INA_D05_FAB_REPORT_V1', b'R1CD06MenuOwnership', b'INA_R1C_D06_OWNER', b'R1CD06VisiblePageBinding', b'R1CD06DirectViewMedia', b'INA_R1C_D06_VISIBLE_PAGE', b'INA_D06_VISIBLE_PAGE_REPORT_V1', b'R1CD06ViewPreflight', b'INA_D06_DEEP_DIAGNOSIS_V1', b'INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1', b'R1CD06PreMenuReconciliation', b'R1CD06PreMenuPolicy', b'INA_D06_PREMENU_RECONCILIATION_V1', b'INA_R1C_D06_PREMENU', b'PREMENU_MEDIA_MATCH_CANDIDATE', b'R1CD06UpstreamConformance', b'INA_D06_INSTAECLIPSE_SOURCE_CONFORMANCE_V1', b'ClipsViewPagerImpl_getViewAtIndex', b'PLAYBACK_CONTROLS', b'UNSAVE', b'INA_D06_MENU_OWNER_REPORT_V1', b'INA_R1C_D04_HOOK', b'INA_R1C_D04_EVENT', b'INA_R1C_D05_REFERENCE', b'INA_R1C_DIRECT_REELS', b'INA_R1C_MODEL',
                b'INA_R1C_ANCHOR', b'INA_R1C_SHAPE', b'INA_R1C_SUMMARY',
                b'identity_verified=false binding_hooks=off actions=off',
                b'R1BNavigationPolicy', b'R1CD07Recovery', b'R1CD07GateTrace',
                b'R1CD07PredicateTrace', b'R1CD07ActualDataTarget',
                b'R1CD07UxGuide', b'R1CD07ComparisonFlow', b'CURRENT STATUS',
                b'STEP 1 OF 5', b'STEP 5 OF 5',
                b'INA_D07_REPOST_EXACT_APK_A_B_TRACE',
                b'ina-d07-repost-ui-selector4', b'ina-d07-verified-d3r-'):
            assert token in blob, f'R1C missing DEX marker {token!r}'
        assert z.read('META-INF/xposed/scope.list').decode().strip() == 'com.instagram.android'
    print('INA_R1C_D04_HOSTED_PACKAGING_PASS (live media identity NOT_RUN)')

if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--apk', type=Path,required=True)
    check(p.parse_args().apk)
