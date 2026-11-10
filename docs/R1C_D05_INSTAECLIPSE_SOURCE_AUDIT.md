# InstaActions × InstaEclipse — upstream source audit and roadmap integration

Audit date: **2026-11-10**. Examined the **actual Java source**, not just README claims.

## Verified immutable anchors

| Repository | Ref | Status |
|---|---|---|
| [Mikeyphw/InstaActions](https://github.com/Mikeyphw/InstaActions) default `main` | `083e63051041bf96fed115a277f004dd8b468ec8` | R1B merged |
| InstaActions `feature/ina-r1c-media-discovery-20261109` | `b246b0f38b38623bdb77987d9fb11318f5c95709` | R1C D04, **four commits ahead of main**, no open PR |
| [ReSo7200/InstaEclipse](https://github.com/ReSo7200/InstaEclipse) `main` | `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d` | Upstream inspected |

For R1C D04 commit, hosted [Android build/run 42811396047](https://github.com/Mikeyphw/InstaActions/actions/runs/42811396047) and [Repository contract/run 42811396128](https://github.com/Mikeyphw/InstaActions/actions/runs/42811396128) both completed **success**. D05 is *not* in upstream InstaActions; it is an offline pending overlay, with no new CI or device qualification. No repository mutation was performed by this audit.

Upstream InstaEclipse README describes testing primarily on **Instagram 451.0.0.20.80**. InstaActions' verified target is **Instagram 455.0.0.8.72** with exact local APK SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6` and exact byte-size gate. Therefore **neither source-based structural similarity nor upstream tests prove behavior for our IG455 build**. Two Xposed modules can observe/intercept the same method with differing hook order.

### Highest-priority finding: Reel menu builder may overlap

[**ReelDownloadHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/ReelDownloadHook.java#L42-L241) implements at least **two separate Reel overflow mechanisms**:

1. **Legacy:** DexKit searches for `ClipsOrganicMediaItemViewMoreOptionsController`, then structurally finds a `void(Media, ButtonAdder)` method and inserts a button after that call. This is **not** InstaActions' D04 `ArrayList` method.
2. **Simplified/newer:** `installReduceOptionsListPatch` matches methods returning `java.util.ArrayList` **and using** `MediaOption$Option.PLAYBACK_CONTROLS` and `MediaOption$Option.UNSAVE`. It hooks the candidate, then appends `DOWNLOAD`, `COPY_LINK`, and optional carrier options to the **mutable returned list**. It caches its target as `ReelOptionsListBuilder`. This is a *credible candidate* for the exact InstaActions D04 observation point `X.07EF.A05(X.03jS, Media): ArrayList`.
3. **Separate native path:** Boolean download/restriction gates are found through MobileConfig numeric constants and have their results modified to unlock Instagram's native row. **Do not copy the gate overrides** into InstaActions.

**Source-level verdict: POSSIBLE SAME METHOD, NOT PROVEN.** ReelDownloadHook does **not** hardcode `X.07EF.A05`; its first matching method is resolved at runtime via field-use predicates. We must confirm that the installed **IG455 DEX** satisfies both predicates, or obtain the exact runtime log line `(IE|Reel) Options-list patch hooked: <class>.<method>`, and compare that descriptor, signature, and process to D04. Do not equate `ArrayList` return type with a method match. Independently inspect source for hook invocation, not just registration.

**D05 compatibility protocol:** After an exact-SHA D05 CI build, perform A/B menu continuity captures with **InstaEclipse ON** and **InstaEclipse OFF** in Vector, independently **keeping InstaActions ON**. Restart Instagram between cases. Each case: Reel A open/close/reopen its `⋯` menu, then different Reel B open/close/reopen its `⋯` menu, within an uninterrupted **110-second foreground** window. Re-enable InstaEclipse after comparison. Screenshots or a user confirmation of Vector scopes are *manual evidence only*, not instrumented proof. Capture only bounded diagnostic markers and crash indicators (raw logcat remains on device). Neither ON/OFF result alone proves visible media identity or method equivalence.

### R1C D06 — unique selected-visible-Reel binding (highest-value reuse)

[**PostDownloadContextMenuHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/PostDownloadContextMenuHook.java#L373-L629) resolves a Reel option click handler via stable `ClipsOrganicMoreOptionsHelper_handleOptionSelected`, with `void(MediaOption$Option)`, a unique instance method and private-analytics-method exclusion. It can swallow the click and prevent native behavior; **this is not a safe passive D05 hook**.

[**SelectedMediaAdapter.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/SelectedMediaAdapter.java#L120-L276) provides superior **selection ownership discipline**:

- `ownedMenuMedia`: find exactly one direct menu-creator reference on the tapped handler, then exactly one direct `Media` field on that creator; **reject ambiguity**.
- `ownedDirectMedia`: only accept a direct `Media` of the exact resolved Reel dispatcher class; reject ambiguous direct fields.
- `ownedStoryItem`: only a unique owner/explicit argument ReelItem, rejecting multiple or preloaded items before the `ReelItem→Media` getter.
- `uniqueAcross`: identity-based BFS, visited set bounded at **128 objects**, rejects multiple matches. Useful as a defensive *fallback* but must not replace verified current-view ownership.

**InstaActions D06 should independently implement** passive, unique, strongly typed ownership checks, correlate against UI's genuinely active Reel, invalidate on lifecycle/transition, fail closed on ambiguity, and keep no Media object in persistent state/logs. A stable same-reference pattern in D05 is **not** visible-Reel identity proof.

[**ReelDownloadHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/ReelDownloadHook.java#L383-L497) shows why carousel selection is tricky: the active index is trusted from view tree only when **exactly one matching carousel** is found. Uniqueness matters more than a convenient first hit.

### R2 — optional context UI: coexistence, not duplication

- [**UIHookManager.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/ui/UIHookManager.java#L35-L182): activity-owned dialog resolution, lifecycle hooks, view binding, weak per-activity listener tracking; Instagram's Reels/post action sheet can have Litho-rendered sections outside traditional menu builders.
- [**PostDownloadContextMenuHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/PostDownloadContextMenuHook.java#L278-L555): possible native menu row extension, per-menu de-duplication via WeakHashMap/ThreadLocal, and **separate allowlist** patch for `SimplifiedMediaOverflowBottomSheet`, matching `boolean→List` + `REPORT/HIDE_OPTIONS/GEN_AI_INFO`; do not confuse this with the Reel `PLAYBACK_CONTROLS/UNSAVE` ArrayList builder.
- [**MenuIcons.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/ui/MenuIcons.java): if using native rows, icon resource mismatches and styles matter. Avoid duplicated `Download`, `Media info`, `Playback speed`, `Copy link` rows when IE is enabled.

**R2 gate:** no duplicate rows, no gesture takeover, no stateful or sticky injection, no hidden UI, and a single opt-in InstaActions entry that does not change the menu when disabled. Verify both IE scope conditions. Any app-visible action requires explicit human input and later-phase qualification.

### R3 — single explicit actions, no inferred clicks

| Feature | Upstream evidence | Safe roadmap use |
|---|---|---|
| Media download/save/copy image/link | [FeedVideoDownloadHook](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/FeedVideoDownloadHook.java), [SelectedMediaSnapshot](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/SelectedMediaSnapshot.java), [PostDownloadContextMenuHook](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/PostDownloadContextMenuHook.java) | Prefer one user-confirmed action, native Instagram/IE functionality when already present; detach snapshot at click time, bound its size, reject unverified video source rather than silently downloading a cover photo |
| Reels quality/scrubber/speed | [PlaybackSpeedHook](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/PlaybackSpeedHook.java), [ReelsControlsHook](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/extras/ReelsControlsHook.java) | Already handled by IE; do not install overlapping player-speed interceptors or duplicate controls without demonstrated need |
| Like confirmation/undo | [LikeUndoHook.java](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/misc/LikeUndoHook.java#L155-L250) | Study exact Activity+Media sender entry and `REPLAYING` guard; no programmatic like until explicit R3 consent, per-action state/receipt, and two-module qualification |
| Story active item | [StoryDownloadHook.java](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/StoryDownloadHook.java#L396-L463) | `ReelViewerFragment.onCurrentActiveItemBound` identifies per-page ReelItem callback; D06 passive candidate, unique signature + IG455 validation required |
| Carousels / IG447+ Pando | [FeedVideoDownloadHook.java](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/FeedVideoDownloadHook.java#L1217-L1395) | Modern `Media` getters use Pando fields `carousel_media`, `video_versions`, `has_audio` instead of old `MutableMediaDictIntf` assumption; do not invoke before D06/ID permission |

### R4 — recipient-aware sharing

[**ShareSheetCleanupHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/ui/ShareSheetCleanupHook.java) changes share-sheet presentation, not a confirmed recipient-dispatch contract. [**ShareLinkDownloadHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/ShareLinkDownloadHook.java) accepts a shared post link for downloading, **not the same thing** as sending it to a chosen contact. No reusable verified R4 send implementation found. R4 must require explicit recipient choice and the native send confirmation; no guessed usernames, background sends, or cross-account recipients.

### R5 — comments

[**CommentCopyHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/misc/CommentCopyHook.java), [**CommentSearchHook.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/misc/CommentSearchHook.java), and [**CommentMedia.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/mods/media/CommentMedia.java) handle reading/searching/copying/downloading comment media. **They do not establish an authenticated comment-posting endpoint.** R5 remains a separate isolated, user-edited, explicit-confirmation, one-comment-only research phase. Avoid broad interaction automation or unsolicited mass comments.

### R6–R8 — safe orchestration, compatibility, delivery

- [**DexKitQueryCache.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/utils/core/DexKitQueryCache.java#L18-L115) caches method lookups by serialized query hash, and [**DexKitCache.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/utils/core/DexKitCache.java#L49-L105) invalidates by IG version. Good performance pattern, but InstaActions should continue **stronger fail-closed exact APK SHA256/byte size, selected method signature, and unique resolution**. Resist stale cache after same-version APK changes.
- [**ModernBackend.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/xposedcompat/ModernBackend.java#L1-L105) proves InstaEclipse's API102 backend is a real interceptor chain: before callback, `chain.proceed` (or skip), after callback. Shared hooks and priority can affect the result. InstaActions' `observation.proceed()` must stay exactly once and preserve outcome; measure coexistence rather than relying on assumed registration order.
- [**FeatureFlags.java**](https://github.com/ReSo7200/InstaEclipse/blob/e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d/app/src/main/java/ps/reso/instaeclipse/utils/feature/FeatureFlags.java) uses volatile toggles, most extras opt-in. Hook presence does not prove a feature is enabled or that it fired.
- R6: transaction/idempotence, per-action acknowledgements, bounded retries and cancellation, no hidden auto-like/share/comment. R7: hosted GitHub Actions on exact commit SHA + Android 16 and live UI test across IE ON/OFF. R8: release only after device-qualified, current-source tests, clear feature scope/permissions/license, and proof of no cross-module regressions.

### License and test-contract caveats

InstaEclipse's repository contains **GPL-3.0** licensing. This audit is a *design/reference review*. **Do not paste or adapt upstream implementation as proprietary/unlicensed code**; independently implement the concepts, or honor license obligations for any code reuse. Source references have been pinned to an immutable upstream SHA above.

Audit previous InstaActions tests *against the new source/manifest/version* before patching. Preserve real security guards, update stale version expectations only when necessary, and test under hosted CI. The R1C D05 source patch is still unqualified until a distinct new SHA passes both workflows and the controlled on/off device capture.

## Decision and next gate

1. **Complete D05**: WeakReference-only object continuity; source/hash/CI gate must reject D04's prior green SHA; on/off capture independently, explicit user scope confirmations, 110-second A/B foreground test, restore IE; no media IDs or actions.
2. **Do not merge** from D04 or D05 source/CI alone.
3. **Before D06**, acquire IG455 field-use evidence or InstaEclipse runtime log to establish whether `ReelOptionsListBuilder` is `X.07EF.A05`; then independently bind UI-active Reel to unique handler-owned Media, with ambiguity refusal.
4. Advance to R2 only after D06 user-visible identity acceptance. R3–R8 remain as scoped above; no unsolicited actions.
