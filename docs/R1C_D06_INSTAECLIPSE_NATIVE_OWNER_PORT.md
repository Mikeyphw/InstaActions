# InstaActions R1C D06 — InstaEclipse-derived native menu ownership

**Status: SOURCE_PREPARED / NEW_HOSTED_CI_NOT_RUN / DEVICE_NOT_RUN / VISIBLE_REEL_IDENTITY_NOT_VERIFIED.**

## Verified source pins

- InstaActions parent: `196d2de7a9b38bc26a40ee29a89c4321911b9e1f` (FAB01, Android + repository hosted CI success).
- InstaEclipse: `ReSo7200/InstaEclipse` `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d`.
- Relevant upstream algorithm: `app/src/main/java/ps/reso/instaeclipse/mods/media/SelectedMediaAdapter.java` methods `ownedDirectMedia`, `ownedMenuMedia`, and `uniqueDirectTypedField` (lines 131–170 at pinned upstream revision).
- Upstream GPL-3.0-only; adapted file and `third_party/InstaEclipse/NOTICE.md` retain attribution and license. If binaries are distributed, ensure **combined-work GPL compliance**; see notice.
- IG 455.0.0.8.72 APK SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`, size 238346353 bytes, `X.07EF.A05(X.03jS,Media):ArrayList` private final instance method. Strict D04 pin retained.

## What was actually ported and where

The `R1CD06MenuOwnership` utility translates **unique direct typed-field identity ownership** into a typed `Verdict`. It checks only exact declared `Media` fields of the native menu builder *receiver* and its explicit context parameter, up to six classes and 64 declared fields each; it rejects ambiguous, mismatched, inaccessible, and truncated observations. Like upstream, it doesn't choose arbitrary preloaded Media or accept an unrelated object graph's first item. No Media fields/methods are read or logged. `field.get(holder)` returns a reference only for equality against the menu callback's own exact `Media` argument. No references persist beyond the callback.

**Limits:** this is not a port of InstaEclipse's media ID, download/snapshot, playback, comment, or menu-mutation functions. `ReelDownloadHook.installReduceOptionsListPatch` is only a candidate overlap with `X.07EF.A05`, **not a proven descriptor match**. No independent view binder is installed and a direct owner match does *not* establish which Reel is visible. R2/R3 actions remain disabled.

`R1CD04PassiveProbe.Counter` reports bounded content-free `INA_R1C_D06_OWNER ordinal=N result=ENUM visible_media=unverified media_id=excluded actions=off`. It passes only verdict enum to the existing draggable `IA` FAB alongside legacy D05 enum counts. The report retains `INA_D05_FAB_REPORT_V1` for comparability, appends `INA_D06_MENU_OWNER_REPORT_V1`, and explicitly says `D06_VISIBLE_BINDING=NOT_VERIFIED`. `D06_OWNER_RESULT=FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY` is **menu-only evidence**, not a pass on real visible identity. Unknown outcomes remain inconclusive.

D05 A1 baseline is now reset **atomically with starting the FAB capture under the same Counter lock**. This addresses the prior A1 pre-capture `DIFFERENT_REFERENCE`, without rewriting the historical capture report.

## Device acceptance — no Termux/Instagram switching

1. After the new direct-child commit has both exact-SHA CI workflows green, use the ZIP's `install` command once. Leave Vector scope unchanged, with InstaActions and InstaEclipse **both enabled**.
2. Force-stop and relaunch Instagram. Confirm InstaEclipse's normal native Reel menu entries still work, and the draggable `IA` FAB is visible. Check `Hook: INSTALLED` on Instagram 455 only.
3. Tap `Start A1`; open Reel A menu once; use IA → Next. Reopen **same** Reel A (A2), advance. Swipe to a genuinely different Reel B (B1), open its menu and advance. Reopen same Reel B (B2) and Finish.
4. Copy safe report from the FAB. Expect `A1_BASELINE=1` in a clean session. Examine `A1/A2/B1/B2_D06_*` counts. `OWNER_DIRECT_MATCH`, `CONTEXT_DIRECT_MATCH`, `OWNER_AND_CONTEXT_MATCH` are positive **owner-only** signals. `MISMATCH`, `AMBIGUOUS`, `NO_DIRECT_OWNER`, `REFLECTION_FAILED` and `BUDGET_EXCEEDED` are failures or inconclusive evidence, not prompts to weaken the guard.
5. Verify no Instagram crashes/ANRs, no InstaEclipse duplicate/replaced menu entries and no action side effects. Report any ref-only vs owner result disagreement.

A **separate exact IG455 active-view-to-Media binding** is still required before D06's original media-identity outcome can be declared qualified. Source tests and hosted build alone do not qualify it.

## CI and ownership gates

- Devtool checks a clean exact FAB01 HEAD with an atomically applied native schema v2 overlay, hash-pinned additions/modifications, full repository Python tests, and a single direct-child commit. It refuses remote movement and doesn't force push, reset, stash, merge, or change Vector scope.
- Hosted Android Gradle builds are GitHub Actions only. APK verifier must see `R1CD06MenuOwnership`, `INA_R1C_D06_OWNER`, and `INA_D06_MENU_OWNER_REPORT_V1` in DEX.
- The launcher `status`/`install` only accepts *both* exact new SHA workflow successes and a checksum-verified APK. `adb install -r` only; never silently uninstalls.
- Rooted Android 16 API36 + libxposed API102 remain the target; no Android Lite or other package scope.
