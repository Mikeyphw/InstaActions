# INA R1C D05 FAB01 — Instagram-native passive diagnostic panel

Status: SOURCE_PREPARED. NEW exact-commit hosted Android build, physical-device testing and Instagram runtime qualification **NOT_RUN**.

## Scope and compatibility

- Source base: InstaActions feature-branch HEAD `06dad42f68ac2e3a39db7af2bf5fd75225634899` (HF03, both CI workflows passed).
- Model: independent activity-owned draggable IA chip inspired by the *pattern* in `Mikeyphw/InstaTaskManager/TaskPanel.java`; no InstaTaskManager or InstaEclipse code imported or copied.
- Scope stays exactly `com.instagram.android` main process (Vector API 102). `InstaEclipse` remains **enabled** throughout the current capture. The panel never accesses or modifies Vector scope, InstaEclipse settings or menu hooks.
- Exact Instagram 455.0.0.8.72 SHA-256/size and D04 method signature gate are retained. The chip is absent on unknown Instagram versions; the panel can begin capture only after that exact observer is installed and at least four events remain under its global 64-event limit.
- The original instrumentation executes `observation.proceed()` exactly once. No new Instagram method hooks or action hooks; all Instagram action policies stay disabled.
- UI uses the active Instagram Activity's own `FrameLayout` decor view, never `SYSTEM_ALERT_WINDOW`; view is removed on pause/destroy and a running capture is cancelled. Chip drag uses vertical placement and starts below InstaTaskManager's upper-right chip.

## On-device procedure (no Termux switching during capture)

1. Install the **new** FAB01 CI-debug APK after both workflows pass on its *exact* pushed commit. Keep InstaActions and InstaEclipse enabled in Vector; restart Instagram once for module loading.
2. Tap the draggable **IA** chip in Instagram. Verify `Hook: INSTALLED`. If it says `VERIFYING / REFUSED`, do not claim a capture; verify IG455 SHA and Vector module activation. The old device probe isn't needed for this test.
3. Tap **Start A1**. On Reel A, open three-dot menu, close it, tap **IA → Next stage**.
4. During **A2**, reopen Reel A's menu, close, tap **IA → Next stage**.
5. During **B1**, swipe to a genuinely different Reel B, open its menu, close, tap **IA → Next stage**.
6. During **B2**, reopen Reel B's menu, close, tap **IA → Finish capture**.
7. Use **Copy safe report** or **Share safe report**. The report contains only allowlisted per-stage integer counts and bounded elapsed durations. No original logcat, Instagram media values, IDs, links, captions, account identifiers or interaction contents.

Every stage has a 60-second maximum and a 16-callback window cap. Expired or empty stable-Reels windows produce `INCONCLUSIVE_MISSING_OR_EXPIRED_WINDOW`; `REFERENCE_COUNTS_ONLY` means four non-empty bounded stages, **not** visible media identity. UNKNOWN/other-screen events cannot qualify a window. Activity exit cancels a running session. If the global D04 log limit is nearly exhausted, restart Instagram (not InstaEclipse) for a new 64-event budget.

## Build and acceptance gates

- Native Devtool overlay requires clean worktree on the feature branch at exact SHA `06dad42f68ac2e3a39db7af2bf5fd75225634899`, hashes every modified source, validates repository Python tests, and commits atomically. Launcher verifies changed-file manifest and single expected parent before a normal non-force push. Fail closed on mismatch.
- All Gradle and Android/JUnit builds run **only via GitHub Actions**, not Termux.
- `tools/verify_r1c_apk.py` checks D04/D05 passive markers and the new FAB/session/report tokens in built DEX.
- Only exact-new-SHA success for `Repository contract` and `R0 / R1 Android API 102` authorizes downloading and installing the APK. Device acceptance is still **NOT_RUN** until actual Vector/IG455 tests with InstaEclipse enabled prove no crashes/ANRs and a meaningful counter sequence.
- R1C remains unmerged. This feature is a diagnostic UI bridge, **not** roadmap R2 acceptance, UI/macro/send capability or a media-identity binding.

## Safety/privacy constraints

- No overlay permission, instrumentation of InstaEclipse, action endpoints, network calls, background sender, user account data, Media object retention or extraction.
- On the hooked method thread, pass only `R1BNavigationPolicy.Screen`, `R1CD05ReferenceContinuity.Result`, and `boolean` to session capture; all UI operations stay on the main thread.
- The report's `INSTA_ECLIPSE=user_kept_enabled_not_programmatically_verified` field is truthful: enablement is assumed from the user setting, not proven via private Vector state.
- User-selected sharing happens only when the person taps **Share safe report**; it opens the system chooser and does not send to a recipient on its own.
- Because there is no media identity, the A1/A2/B1/B2 labels are **human-declared time windows**; comparison outcomes cannot prove which Reel or account was visible.


## D06 superseding follow-up (source prepared; do not confuse with D05 evidence)

D06 keeps the existing four-window A/B FAB and adds *menu-owned direct-typed-field* verdicts adapted from InstaEclipse's GPL-3.0-only `SelectedMediaAdapter.java`. The D05 `A1_DIFFERENT_REFERENCE` seen before capture occurred because the previous reference was not reset; D06 atomically resets it when starting the FAB. This does not retroactively change the earlier device report.

An owner verdict means only that the native menu builder's receiver/context holds precisely one exact-typed `Media` reference equal to the callback argument. It does **not** verify a distinct visible-Reel binding. Unknown and ambiguous cases are explicit. Full D06 device acceptance and active-view binding remain NOT_RUN until examined.
