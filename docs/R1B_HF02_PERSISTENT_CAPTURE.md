# R1B HF02 — persistent Vector callback log capture

Source pin: `675ae3bd16da812d9005534b3afb02b03290e075` (HF01, Android CI PASS).
GitHub branch: `feature/ina-r1b-screen-context-20261108`.

## Audit of uploaded r1b_2.zip

- Two APKs validated against SHA256SUMS; HF01 `InstaActionsEntry` diagnostic markers are present in the debug APK DEX.
- `logcat-diagnostics.log` was **empty**.
- `vector-diagnostics.log` includes VectorModuleManager loader messages but no callback markers; it was collected exclusively from `/data/adb/lspd/log/modules_*.log`.
- This cannot demonstrate that `onModuleLoaded`, `onPackageLoaded`, `onPackageReady`, or their hooks failed.

## Root cause in HF01's evidence collection

Vector 2.2 source at `JingMatrix/Vector`, `xposed/.../VectorContext.kt`, uses
`tag ?: "VectorContext"` for framework `log(int,String,String)` events. Vector's
`daemon/src/main/jni/logcat.cpp` routes the fixed `VectorContext` tag to the
modules journal. Custom `INA_DIAG` is not on that allowlist and is not necessarily
retained by either the modules or verbose journal. HF01 wrote that same custom
tag to both `android.util.Log` and framework `log(...)`, so its advertised dual
channel actually shared logcat's retention limitations. Heavy unrelated warning
output could displace early events before `logcat -d` ran.

HF02 retains `android.util.Log.i("INA_DIAG", ...)` for live capture, but uses
`log(Log.INFO, null, event)` for the VectorContext-tagged durable module journal.
It also ships a continuously running, tag-filtered logcat capture that starts
*before* launching Instagram. Uninstall-before-install handles signing changes.

No Instagram action hooks, UI changes, media/recipient IDs, URLs, or private data
are introduced. Device evidence must still determine whether the callbacks ran,
which guard was taken, and if the context observer works.

## Pass gates

1. Hosted Android CI / JVM / package checks all PASS for HF02 SHA.
2. `INA_DIAG_MODULE_LOADED` and `INA_DIAG_PACKAGE_READY` visible in VectorContext modules journal or captured live logcat, or explicit framework callback error demonstrated.
3. `INA_DIAG_HOOKS_INSTALLED`, `INA_DIAG_FIRST_ACTIVITY_RESUMED`, and `INA_R1B_CONTEXT` match expected screen transitions on device.
4. No new crashes/ANRs with InstaEclipse enabled; compare with it disabled if interference occurs.

Do not merge R1B or advance to R1C without this evidence. InstaEclipse's separate
`ReelsControlsHook` wrong-thread warnings remain outside this hotfix.
