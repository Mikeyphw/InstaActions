# InstaActions D15 HF07 — missing floating IA button rescue

## Incident, failure chain, and evidence

Following hosted D14 green CI at `053e07bb92992702220baaac896c4d38b276f501`, the user reports the floating InstaActions IA chip absent in Instagram 455. This is **user-reported runtime evidence**, not a reproduced device stack trace. Two concrete failure chains were found in D14 source:

1. `InstaActionsEntry.onResume` places `R1CD05FabPanel.onResumed` **after** independent resource/DexKit probes in a `finally` sequence. An optional observer throwing before reaching FAB can prevent attachment, and the FAB-specific exception was ignored. Source CI cannot detect on-device attachment visibility.
2. `R1CD05FabPanel.onResumed` assigns `current = next` **before** calling `next.attach()`, then assumes any matching Activity already has a live chip. A partial UI inflation exception can leave a false-positive `current`, so a later resume incorrectly skips repair.

Neither explanation has yet been confirmed with device logs. The patch closes both paths without enabling Repost or changing D14 detection.

## Repair contract

- Attempt FAB attachment first and independently of R1B/R1C/D04/D12/D13/D14 diagnostic initialization. Preserve the original `chain.proceed()` and Android lifecycle dispatch.
- Retry at 120, 350 and 900 milliseconds, at most four attempts in one resumed-Activity generation. Use a `WeakReference<Activity>` only; invalidate the generation on Activity leave, including pending retry callbacks. No background polling or persistent account/media data.
- Recover an incomplete attach (chip not parented) and unexpected decor replacement, remove orphaned UI, and keep the IA chip above subsequently added Instagram decor children. Fall back to Android's content FrameLayout only when the normal decor is unsuitable.
- Register an independent Android `onActivityResumed` callback as a second entry point for subsequent Activity resumes.
- Emit bounded privacy-safe `INA_FAB_ATTACH ATTACHED_MAIN_ACTIVITY`, `FAILED_ATTACH_<type>`, `SKIPPED_VERSION_*`, `DEFERRED_NO_FRAME_LAYOUT`, `REFUSED_RETRY_CAP_REACHED` and `DETACHED_ACTIVITY_LEFT` status lines. No usernames, media or account identities.
- Preserve D07 original native Repost 90-second/64-change and foreground-session/4096 limits, original version/build/scope, D08–D14 diagnostics, one D04 hook, passive detection and Copy/Export All. No native account-specific auto-rearm.

## Device acceptance is still required

1. After the exact-head hosted Android build and checksum verified APK installation, force-stop and reopen Instagram 455.
2. Confirm an `IA` chip visibly appears on the main Feed without waiting for DexKit discovery; tap, move and reopen it.
3. Navigate Home Feed → dedicated Reels → Reel from Feed → Story → back to main and verify no orphan or duplicate chip. Rotate/foreground/background and check Activity destruction/recreation.
4. With InstaEclipse ON, keep recovery OFF and verify all D14 passive reports/Copy All/Export All still work. Test temporary Repost recovery only in the qualified dedicated Reels tab.
5. Record `adb -s localhost:5555 logcat -d -s INA_FAB:I INA_DIAG:I '*:S'` when IA is still absent, and include the actual Instagram `versionName` and Vector scope status. Do not claim physical qualification if the chip is still invisible.

## Qualification

Source-only tests and static contracts can verify the recovery path, not its runtime visibility. Hosted CI and physical UI acceptance are independent gates. If GitHub CI passes but IA is missing, inspect the `INA_FAB` log lines before assuming the problem is fixed.
