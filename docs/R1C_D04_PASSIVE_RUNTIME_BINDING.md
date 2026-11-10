# R1C D04 — exact-build, read-only runtime Reels association

Status: SOURCE_PREPARED / HOSTED_CI_NOT_RUN / DEVICE_NOT_RUN. Source pinned to R1C D03 commit `9de5c417257c4fbbb0a22dfac432a6620f0bca3c`.

## Established evidence and implementation

D03 device evidence finds one exact descriptor for the modern Reels options list builder on Instagram 455.0.0.8.72. InstaEclipse `ReelDownloadHook.java` uses menu-building callbacks but warns that the legacy `void(Media, ButtonAdder)` method may be absent on recent builds. D04 observes the alternate list builder `X.07EF.A05(X.03jS, Media): ArrayList` (source APK SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`; byte length 238346353). Direct DEX metadata confirms it is an **instance** method with `access_flags=0x12` (private final), not static. The D04 guard explicitly refuses a static method. The declaring class has 38 methods (37 direct + 1 virtual).

`R1CD04PassiveProbe` runs a single worker after a valid Instagram Activity resume. It first requires the exact version and installed APK *byte length and SHA-256*. It does not load the obfuscated owner, resolve a Method, or install a hook before this gate passes. The worker scans no more than 256 declared methods, requires exactly one method with the exact private-final instance method signature, then installs one idempotently identified modern libxposed API 102 interceptor using protective mode. The interceptor calls `chain.proceed()` exactly once in a `try`, then only observes `chain.getArg(1)` type/nullness and `R1BContextMonitor.stableScreen()` from `finally`. Any observer exception is swallowed while original exceptions propagate unchanged. The `Counter` emits at most 64 `INA_R1C_D04_EVENT` records. No Media object reference is retained.

`R1BContextMonitor.stableScreen()` returns UNKNOWN unless precisely one active observer exists; lifecycle pause/destroy clears stable state. Only screen classification and Media argument type are recorded. These are not identity, account, author, post, URL or message data. No user data, media IDs, or media getter invocations are logged. InstaEclipse remains enabled and unmodified.

The original R1A supported-version allowlist (451/453) remains unchanged; D04 is a separate, passive, SHA-bound instrumentation path for Instagram 455 only. `ACTION_HOOKS_ENABLED=false`, `R1CDiscoveryPolicy.BINDING_HOOKS_ENABLED=false`, and `R1CD04Policy.ACTIONS_ENABLED=false` remain unchanged; the D04 observer hook is not an action/binding hook.

## Device acceptance

After both hosted workflows pass on the exact new commit, run the embedded probe. For each stage, open the selected Instagram screen and **open the three-dot options menu during `reels_overflow`** so the list builder can run. Stages: `feed`, `reels`, `reels_overflow`, `story`, `feed_return`. Confirm: one `INA_R1C_D04_GATE status=PASS` and one `INA_R1C_D04_HOOK status=INSTALLED`, with `INA_R1C_D04_EVENT` records showing `screen=REELS arg=MEDIA_TYPED` during the Reels menu stage (if the target method is on this path); no spurious association on Feed/Story, crashes or ANRs. If no events occur, mark `INCONCLUSIVE_METHOD_NOT_INVOKED`, **not success**. If APK SHA differs, mark gate REFUSED and do not bypass the check. Screens may transiently go UNKNOWN, so event correlation requires stable Reels state; navigation alone is not media identity.

Do NOT merge R1C or enable any action or stored media identity on this evidence. Subsequent work must prove an instance-to-on-screen association with invalidation and safety tests.
