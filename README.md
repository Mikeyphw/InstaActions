# InstaActions

A private, standalone Android Xposed module for **personal** Instagram shortcuts,
designed to coexist with [InstaEclipse](https://github.com/ReSo7200/InstaEclipse).

**Status: R0 source implemented; hosted Gradle and device acceptance pending.**
No Instagram action hooks, UI injection, DMs, comments or repost modifications exist yet.

## Architecture and compatibility

- Android 16 target, minSdk 28; package `com.mikeyphw.instaactions`.
- Modern libxposed API **102** / Vector **2.2+**.
- Default scope: `com.instagram.android` main process only.
- InstaEclipse independently installed/enabled; no shared preferences, classes or
  UI gestures are modified by InstaActions.
- Instagram 451.0.0.20.80 baseline, 453.0.0.0.56 experimental until real testing.
- Instagram Lite is out of scope for R0.

## R0 deliverables

- Companion app with installed-version indicators, precise scope and honest module-status labeling.
- No-op `InstaActionsEntry` with logcat `INA_R0_PACKAGE_READY` (only if Vector invokes it).
- Modern `META-INF/xposed` manifest/scope/entry metadata, no legacy API 82 entry.
- JVM tests, instrumentation APK, hosted CI, APK metadata validator and SHA-256 output.
- No permissions for internet, contacts, storage, messages or arbitrary package enumeration.

## Build and acceptance

Android Gradle builds happen exclusively in GitHub Actions. Do **not** run Gradle
locally on Termux. See [docs/BUILD.md](docs/BUILD.md) and
[docs/R0_DEVICE_ACCEPTANCE.md](docs/R0_DEVICE_ACCEPTANCE.md).

Documentation:
[ROADMAP](ROADMAP.md) · [Architecture](docs/ARCHITECTURE.md) ·
[Compatibility](docs/COMPATIBILITY.md) · [Security](docs/SECURITY.md) ·
[R0 execution](docs/R0_EXECUTION.md).

Independent from Meta, Instagram and InstaEclipse. Not a general mass-automation tool.
