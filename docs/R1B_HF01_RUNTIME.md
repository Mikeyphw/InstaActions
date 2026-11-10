# R1B HF01 — Vector lifecycle diagnostics

Source: `8bd86313cb788550aa5799d0a39b1eec9a4d50e5`.
Previous hosted CI: [42551130228](https://github.com/Mikeyphw/InstaActions/actions/runs/42551130228) PASS.
Device: Instagram starts and Vector declares InstaActions loaded, with module enabled,
scope correct, and stored APK path matching the installed package. Android logcat
contains **zero INA_R1/INA_R1B records**, so R1B acceptance is **NOT_VERIFIED**.

## Why this is instrumentation, not a claimed root-cause fix

Vector's loader success proves the module class is loaded, not that its callbacks
passed the package/process guard or that Android logcat displayed their messages.
The previous entry did not emit logs until after the hooks were installed.

HF01 logs the earliest lifecycle boundaries to **two independent channels**:
Android logcat (`INA_DIAG`) and libxposed framework logging. No account names,
contacts, media identifiers, URLs, captions or messages are recorded.

- `INA_DIAG_MODULE_LOADED`: callback reached; main-process guard result.
- `INA_DIAG_MODULE_INFO`: native library metadata available or failure type.
- `INA_DIAG_PACKAGE_LOADED`: package load callback, with only boolean matches.
- `INA_DIAG_PACKAGE_READY`: hook install callback reached, with guard booleans.
- `INA_DIAG_GUARD_SKIP`: explains package/process mismatch.
- `INA_DIAG_TARGET_APK_PRESENT`: sufficient target identity available.
- `INA_DIAG_HOOKS_INSTALLED` / `INA_DIAG_HOOKS_FAILED`: lifecycle hook result.
- `INA_DIAG_FIRST_ACTIVITY_RESUMED`: first intercepted Activity resume.
- Existing `INA_R1*` and `INA_R1B*` markers still report DexKit and view context.

R1B's UI classifier and DexKit probes are otherwise unchanged; this patch does
not invoke Instagram action dispatchers, change UI widgets, or send network calls.

## Expected decision tree

1. Loader message but no `INA_DIAG_MODULE_LOADED` in either log: check framework
   callback dispatch/exception stack, and whether the injected target is Instagram.
2. Module loaded but no package callback: investigate Vector package lifecycle.
3. Package callback with `main=false`: processName mismatch, not missing scope.
4. Hooks failed: use exception **type** and framework stack trace for a targeted fix.
5. Hooks installed but no first Activity resume: framework lifecycle interception.
6. Resume seen but no observer attached: check installed Instagram version, Activity
   visibility, view observer attachment, and the R1A/R1B version gate.

## R1B merge gate

Do not merge into `main` until the Android CI is green and the operator confirms
runtime context transitions and no new crash/ANR. InstaEclipse's separate Reels
viewpager thread violations must be diagnosed independently; do not modify its
sources or disable its hooks as a side effect of this patch.
