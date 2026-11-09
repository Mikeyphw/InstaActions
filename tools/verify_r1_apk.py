#!/usr/bin/env python3
"""Hosted R1 proof: DexKit JNI packaged and debug/test APK available.
Runtime hook discovery and coexistence need device evidence separately.
"""
import argparse
from pathlib import Path
import zipfile


def verify(apk: Path, test_apk: Path) -> None:
    with zipfile.ZipFile(apk) as package:
        names = package.namelist()
        native = [name for name in names if name.startswith('lib/')
                  and name.endswith('/libdexkit.so')]
        assert native, 'DexKit native library missing from debug APK'
        assert any('arm64-v8a' in name for name in native), 'ARM64 DexKit library missing'
        assert any(n.endswith('.dex') for n in names), 'module dex missing'
        prop = package.read('META-INF/xposed/module.prop').decode()
        assert 'targetApiVersion=102' in prop
        assert package.read('META-INF/xposed/scope.list').decode().strip() == 'com.instagram.android'
    with zipfile.ZipFile(test_apk) as test:
        assert any(n.endswith('.dex') for n in test.namelist()), 'test apk dex missing'
    print('INA_R1_HOSTED_DEXKIT_PACKAGING_PASS')
    print('Native ABIs:', ', '.join(sorted(set(p.split('/')[1] for p in native))))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk', type=Path, required=True)
    parser.add_argument('--test-apk', type=Path, required=True)
    args = parser.parse_args()
    verify(args.apk, args.test_apk)
