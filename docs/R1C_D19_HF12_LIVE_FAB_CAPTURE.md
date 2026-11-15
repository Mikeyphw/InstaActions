# D19 HF12 — Automatic live FAB capture while opening Instagram

Parent: D18 `c13deed81385904d80ee71f8bf51fdc034ab3150` on `feature/ina-r1c-media-discovery-20261109`.

## Why

D18 on Android 16 reported root UID 0 and no main Instagram PID, no historical marker, and no events in the cross-process broadcast journal. Its single on-resume capture occurs while the standalone InstaActions app is foregrounded; if Instagram was killed when switching activities, it loses evidence. The lack of a process or marker in a snapshot cannot prove the module is disabled.

## D19 design

- Starts automatically when the user opens InstaActions. NO additional in-app opt-in; KernelSU still controls root.
- Android foreground service (`specialUse`) with visible status notification keeps capture alive when the user opens Instagram next. Up to 150 seconds, then stops automatically; restart rate-limited at 210 seconds. The app does not auto-start or force-stop Instagram.
- A finite root script reads only `pidof`, `ps` process names and `INA_FAB` / `INA_DIAG` / `INA_R1B` / `INA_R1C` logcat tags. A transient stream is parsed in memory; only bounded fixed-category event names and main-process presence counters are stored.
- Main PID seen during the window correlates qualifying events even if the process exits before returning to the app. Unknown or early PIDs are explicitly `UNVERIFIED_PID`, never attributed to Instagram.
- The standalone app's existing `Copy FAB logs`, `Export FAB logs`, `Refresh FAB logs` include D19. If no Instagram process appears, the report explicitly directs the user to open Instagram during the capture.
- No changes to Instagram injection, FAB attachment logic, verified Reel Repost guards, other user data or account/media identity. **D19 does not claim the FAB is fixed.**

## Device acceptance

**NOT RUN**. GitHub-hosted Android compilation and a real Android 16 user journey (open InstaActions → automatic capture notification → open Instagram Home Feed → return → Copy FAB logs) must be performed. An app killed despite a foreground service yields only partial evidence, not acceptance. No Gradle on Termux; full test suite on real checkout is mandatory before Devtool. For personal diagnostics only; not a claim of Play policy suitability.
