# Hosted-only Android builds

Gradle is intentionally not invoked on Termux. GitHub Actions uses:

- JDK 17 / Android API 36 / Android build-tools 35.0.0
- AGP 8.13.2 / Gradle 8.13 (pinned in Actions, no wrapper required on phone)
- `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
- `tools/verify_r0_apk.py` for APK verification
- `dist/SHA256SUMS` and both APKs as one GitHub Actions artifact

Open the run and download the `InstaActions-R0-<SHA>` artifact.
Release signing is not configured for R0; it is a debug-signed personal test APK.
Instrumentation APK is built but **not executed** by the hosted job.
It may be executed on the user's device after installing both APKs.

To manually dispatch CI: `gh workflow run android-r0.yml --ref feature/ina-r0-api102-foundation-20261108`.
To inspect: `gh run list --repo Mikeyphw/InstaActions --workflow android-r0.yml -L 5`.
