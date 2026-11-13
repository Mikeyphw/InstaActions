# InstaActions D11 HF03 — hardened diagnostic audit and controls

## Scope / provenance

Baseline: `Mikeyphw/InstaActions@ab01e1b2d2e6e3f91916d0751ec639295401c328`, exact R1C D10 branch. This phase changes **only diagnostics/reporting/UI text and tests**. The source-backed Instagram 455.0.0.8.72 Reels native-Repost recovery, 90-second/64-call mode, 4,096-change foreground mode, manual ITM-off claim, and account-switch safety requirements remain unchanged. InstaEclipse stays ON. No additional Instagram method hook, account-ID read, media-ID read, avatar modification, auto-Repost, Feed action, network call or Android permission.

## P0 audit finding: non-feature callbacks falsely imply completion

Observed device data had FEED `A_observations=112`, `B_observations=160`, yet *zero Feed snapshots*. D10's `R1CD10ComparisonFlow.sample()` counts general callbacks; A/B step completion is not content-specific evidence. **Fix:** keep these callback counters as background activity, add separate `featureScans[cohort][surface]` and `screenHintValid[cohort][surface]` derived from actual explicit snapshots (or scoped source callbacks). `FEATURE_COVERAGE` now reports `INCOMPLETE_FEATURE_SNAPSHOTS_NOT_QUALIFIED` if any required mode-specific A/B observation is missing; otherwise it reports the narrow evidence class, **never feature/Media/account identity**. Feed mismatched hints and truncated scans are explicitly downgraded. The captured quality survives Finish for Copy Selected and archives.

## P0 audit finding: avatar renderer not anchored

D10's avatar mode scans *all ImageViews in the whole visible Instagram Activity* and repeats the same scan under three user-supplied labels. The repeated counts are not three independent facepiles. **Fix:** explicit `SCAN_SCOPE=ENTIRE_VISIBLE_DECOR`, `surface_owner=NOT_FOUND`, and warning that same views may be reused across snapshots. Separate A/B per-surface completeness is counted; `PAIRED_VIEW_CANDIDATES_AVATAR_NOT_VERIFIED` is the strongest result. A real renderer-bound test requires further exact IG455 source/runtime evidence; this overlay **does not pretend to provide it**.

## P1 audit finding: Feed lacks exact owner and sample provenance

The Feed audit is a bounded whole-decor scan, not post-tree identification. Labels from unrelated UI could be counted, and label absence is not proof of button absence. Explicit per-surface A/B sample coverage is now separated from native method or passive call counts; false screen hints or scan caps are **untrusted**. The actual Repost button's ownership is still unqualified.

## P1 account-change trace

A reference change was observed on the user device, but aggregate counts did not show whether the event preceded/followed the manual marker. A bounded, ordinal-only timeline (up to 48 markers) now records `MANUAL_SWITCH_MARKER_NOT_IDENTITY_PROOF`, `REFERENCE_CHANGED_BEFORE_MARKER`, `REFERENCE_CHANGED_AFTER_MARKER`, `WEAK_REFERENCE_RECLAIMED` and `SESSION_MISSING`, with only A/B phase and no account identifiers, object values or timestamp. Its signal is explicitly **not** authenticated account identity; absent callbacks are not proof of no switch. The original fail-closed recovery controls remain unchanged.

## P1 Copy All / Export All honesty

D10's archive evicts records after 28 entries / 180k characters and clips individual records at 16k. HF03 exposes `clipped_records` and a prominent `ARCHIVE_INCOMPLETE` line if data was evicted/clipped. Copy/Export All remains **process-memory-only summaries**, never unlimited logcat or crash dumps; no persistence across Instagram process exit. A full historical export cannot be guaranteed after caps are reached.

## Not yet implemented / remains unqualified

- Exact current visible Media/pager ownership and Feed post binding remain unverified.
- Exact liked-by/reposted-by/Friends avatar container selection remains source-unqualified. **No facepile success claim.**
- Same-Activity account-change coverage is still imperfect, especially when the existing callback does not fire.
- True lossless 'all logs since app opened' is not provided; archive is deliberately bounded.
- Android hosted compilation and physical UI tests must run after application; source/JVM tests alone do not qualify the APK.

## Acceptance tests

- Generic callbacks with zero Feed snapshots => explicitly **incomplete**.
- Six distinct snapshots across A/B, with one screen mismatch => unqualified by mismatch.
- A missing avatar category => incomplete.
- Native callbacks before/after a manual account marker => ordered, opaque event signal.
- Truncated archive => strong visible `ARCHIVE_INCOMPLETE` warning.
- Previous D07 105-case native recovery test unchanged; all native safety constants and action scope remain unmodified.

## Snapshot transition/teardown race

Both manual Feed and Avatar snapshots now verify the diagnostic mode and active A/B cohort **again after** bounded view scanning, before storing data. Any aborted session or cohort change during a scan returns `REFUSED` and does not store or attribute that scan to the wrong cohort.
