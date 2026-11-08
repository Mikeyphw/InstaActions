# R0 execution checklist (feature branch)

This file is introduced on the first R0 feature branch to make it independently reviewable from main.

- [ ] Choose compatible AGP / Gradle / JDK versions and lock them for hosted Actions.
- [ ] Implement Android app manifest, companion launch activity and module branding.
- [ ] Wire libxposed API 102 via `compileOnly` dependency; avoid legacy module init accidentally loading.
- [ ] Package `META-INF/xposed/java_init.list`, `scope.list`, `module.prop` and verify them inside APK.
- [ ] Restrict target scope/process to `com.instagram.android` and add no-op hook lifecycle.
- [ ] Add safe capability/debug screen that does not collect sensitive content.
- [ ] Add JVM tests and hosted build workflow with APK/checksum artifact upload.
- [ ] Build instrumentation test APK in Actions and add emulator smoke where practical.
- [ ] Install APK on target Vector device with InstaEclipse also enabled and record results.
- [ ] Report SHA, workflow run ID, package/version checks and actual real-device PASS/FAIL.

Do not close R0 on repository-contract checks alone.
