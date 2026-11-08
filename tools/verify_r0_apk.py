#!/usr/bin/env python3
"""Hosted APK proof: metadata, package, permissions and instrumentation artifact.
Not a substitute for real Vector+InstaEclipse device acceptance.
"""
import argparse
import subprocess
import zipfile
from pathlib import Path

EXPECTED_META = {
    'META-INF/xposed/java_init.list': 'com.mikeyphw.instaactions.InstaActionsEntry',
    'META-INF/xposed/scope.list': 'com.instagram.android',
}

def verify(apk, test_apk):
    with zipfile.ZipFile(apk) as z:
        for name, content in EXPECTED_META.items():
            assert name in z.namelist(), f'missing from APK: {name}'
            assert z.read(name).decode().strip() == content, name
        prop = z.read('META-INF/xposed/module.prop').decode()
        assert 'minApiVersion=102' in prop
        assert 'targetApiVersion=102' in prop
        assert any(n.endswith('.dex') for n in z.namelist()), 'no dex in APK'
        assert 'AndroidManifest.xml' in z.namelist()
    with zipfile.ZipFile(test_apk) as z:
        assert 'AndroidManifest.xml' in z.namelist()
        assert any(n.endswith('.dex') for n in z.namelist())
    # aapt is provided by the hosted Android SDK in GitHub Actions.
    import os
    sdk = Path(os.environ['ANDROID_HOME'])
    aapt = sdk / 'build-tools' / '35.0.0' / 'aapt'
    info = subprocess.check_output([str(aapt), 'dump', 'badging', str(apk)], text=True)
    assert "package: name='com.mikeyphw.instaactions'" in info
    assert "sdkVersion:'28'" in info
    assert "targetSdkVersion:'36'" in info
    required = ['android.permission.INTERNET', 'android.permission.READ_CONTACTS',
                'android.permission.READ_SMS', 'android.permission.QUERY_ALL_PACKAGES']
    for perm in required:
        assert f"uses-permission: name='{perm}'" not in info, perm
    print('INA_R0_HOSTED_APK_METADATA_PASS')

if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('--apk', type=Path, required=True)
    p.add_argument('--test-apk', type=Path, required=True)
    opts = p.parse_args()
    verify(opts.apk, opts.test_apk)
