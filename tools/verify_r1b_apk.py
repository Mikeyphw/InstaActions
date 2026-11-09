#!/usr/bin/env python3
"""Hosted proof of packaged R1B observer, never evidence of real Instagram detection."""
import argparse
import zipfile
from pathlib import Path

def check(apk: Path):
    with zipfile.ZipFile(apk) as z:
        names = [n for n in z.namelist() if n.endswith('.dex')]
        assert names, 'R1B: APK has no DEX'
        payload = b''.join(z.read(n) for n in names)
        for token in (b'R1BContextMonitor', b'R1BContextPolicy',
                      b'INA_R1B_CONTEXT', b'media_id=unavailable actions=off'):
            assert token in payload, 'R1B: missing DEX marker ' + token.decode()
        assert 'META-INF/xposed/scope.list' in z.namelist()
        assert z.read('META-INF/xposed/scope.list').decode().strip() == 'com.instagram.android'
    print('INA_R1B_HOSTED_PACKAGING_PASS (device context detection NOT_RUN)')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk', required=True, type=Path)
    args = parser.parse_args()
    check(args.apk)
