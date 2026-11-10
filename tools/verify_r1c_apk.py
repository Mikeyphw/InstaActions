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
                b'R1CMediaCandidateProbe', b'R1CStaticShapeProbe', b'R1CModernReelsPolicy', b'R1CD04Policy', b'R1CD04PassiveProbe', b'INA_R1C_D04_HOOK', b'INA_R1C_D04_EVENT', b'INA_R1C_DIRECT_REELS', b'INA_R1C_MODEL',
                b'INA_R1C_ANCHOR', b'INA_R1C_SHAPE', b'INA_R1C_SUMMARY',
                b'identity_verified=false binding_hooks=off actions=off',
                b'R1BNavigationPolicy'):
            assert token in blob, f'R1C missing DEX marker {token!r}'
        assert z.read('META-INF/xposed/scope.list').decode().strip() == 'com.instagram.android'
    print('INA_R1C_D04_HOSTED_PACKAGING_PASS (live media identity NOT_RUN)')

if __name__=='__main__':
    p=argparse.ArgumentParser()
    p.add_argument('--apk', type=Path,required=True)
    check(p.parse_args().apk)
