# InstaActions

A private, standalone Android Xposed module for **personal** Instagram shortcuts,
designed to coexist with [InstaEclipse](https://github.com/ReSo7200/InstaEclipse).

**Status: R1 read-only probe source prepared; R1 hosted CI and device acceptance pending.**
No Instagram action hooks, UI injection, DMs, comments or repost modifications exist yet. R1 adds only a transparent Activity lifecycle interceptor and bounded background DexKit discovery.

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

## R1 read-only discovery

- R1 probes five **candidate** method/class markers from InstaEclipse upstream with DexKit 2.0.3.
- The only installed runtime interceptor observes `Activity.onResume`, preserves the original return and exceptions, and schedules a low-priority worker.
- **Only exact** Instagram versions 451.0.0.20.80 and 453.0.0.0.56 qualify; unknown builds do nothing.
- Probe results are diagnostic hints, not verified method mappings or detected media identities. No username, media ID, DM, URL or payload is logged.
- Use `adb -s localhost:5555 logcat -d -s INA_R1:I '*:S'` for device proof.
- See [R1 device acceptance](docs/R1_DEVICE_ACCEPTANCE.md) for the complete checklist.

## R1B read-only surface observation

R1A hosted CI passed at `ea635eb`. R1B adds bounded, throttled UI-resource
observation on qualified Instagram versions only. It logs **candidate** screen
transitions (Reels, Stories, Posts, Unknown) without reading text, media IDs,
usernames or messages. It installs no Instagram-internal action hook and does
not change InstaEclipse UI. Run `adb -s localhost:5555 logcat -d -s INA_R1B:I "*:S"`
for observations. **Media identity remains unavailable**, and R1B is incomplete
until actual device observations establish reliable surface detection.
