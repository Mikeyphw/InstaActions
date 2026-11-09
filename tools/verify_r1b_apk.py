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
                      b'R1BResourceDiagnostics', b'INA_R1B_RESOURCE_HINTS', b'row_feed_button_like', b'row_feed_button_save',
                      b'INA_R1B_CONTEXT', b'media_id=unavailable actions=off', b'INA_DIAG_MODULE_LOADED', b'INA_DIAG_PACKAGE_READY', b'INA_DIAG_HOOKS_INSTALLED', b'INA_DIAG_FIRST_ACTIVITY_RESUMED'):
            assert token in payload, 'R1B: missing DEX marker ' + token.decode()
        assert 'META-INF/xposed/scope.list' in z.namelist()
        assert z.read('META-INF/xposed/scope.list').decode().strip() == 'com.instagram.android'
    print('INA_R1B_HOSTED_PACKAGING_PASS (device context detection NOT_RUN)')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk', required=True, type=Path)
    args = parser.parse_args()
    check(args.apk)
