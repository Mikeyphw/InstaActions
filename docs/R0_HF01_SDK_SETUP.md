# INA R0 HF01 — SDK bootstrap repair

Source run: https://github.com/Mikeyphw/InstaActions/actions/runs/42531826230

The failed job stopped in `android-actions/setup-android@v3` before installing Android platform 36 or starting Gradle.
Google's SDK repository no longer contains the legacy `tools` package; the v3 action attempted to install it.

Changes:
- Update to `android-actions/setup-android@v4`, explicitly installing only `platform-tools`.
- Move to Node 24 native actions: checkout v7, setup-java v6, setup-gradle v6.
- Use the open-source `basic` caching provider for the private repository.
- Pin Ubuntu 24.04 instead of using the changing `ubuntu-latest` image.
- Execute Python workflow contract tests early, before Android SDK setup.
- Add a regression test for the deprecated SDK tools package and actions versions.

No Android app source, Xposed hooks, APK signing or native dependencies are modified.

Acceptance: after source-pinned Devtool apply and autocommit, the *new GitHub Actions run* must pass SDK setup, Gradle tests/build and APK verifier. The old run remains failed.
