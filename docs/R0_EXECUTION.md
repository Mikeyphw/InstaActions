# R0 execution — API 102 foundation

## Implemented in R0 overlay (still subject to hosted proof)

- [x] Select AGP 8.13.2, Gradle 8.13, JDK 17 for hosted builds.
- [x] Implement Android app manifest, companion activity and branding.
- [x] Wire `compileOnly` libxposed API 102 with modern entry only.
- [x] Package `META-INF/xposed` metadata and add hosted APK verification.
- [x] Restrict entry to Instagram main process and install no hooks.
- [x] Display truthful package diagnostics without exposing private content.
- [x] Add pure JVM unit tests and instrumentation test APK build.
- [x] Add GitHub Actions build, SHA-256 inventory and debug artifact retention.

## Pending evidence (DO NOT mark R0 complete yet)

- [ ] Hosted Gradle compile/unit tests/assemble debug pass.
- [ ] APK verified by `aapt` and metadata verifier in Actions.
- [ ] Commit SHA and hosted workflow run ID recorded.
- [ ] APK installed in Vector 2.2+ on target real device.
- [ ] Both InstaActions and InstaEclipse enabled on same Instagram scope.
- [ ] `INA_R0_PACKAGE_READY` observed after Instagram launch; Instagram remains stable.
- [ ] Compatibility matrix updated with actual device results.

Strict completion gate: hosted CI **and** device acceptance evidence.
