# D14 HF06 — InstaEclipse source + actual APK and IG455 DEX audit

## Immutable builds examined

- **InstaEclipse 0.8.2 APK** (saved original): SHA256 `b1308bdf4edc2956671fe5a0ec04972ba77a25edd51e144072b9b157755d2dc5`, 15,621,572 bytes, three DEX files, Vector/Xposed API min 101 / target 102, scope `com.instagram.android`. Its `META-INF/version-control-info.textproto` identifies revision `ee1c81cadfbcd9fbbee4d01568ee5ca4b2653fbc`. The five inspected Java source file Git blobs (`ReelDownloadHook`, `SelectedMediaAdapter`, `StoryDownloadHook`, `DisableRepostHook`, `ReelsControlsHook`) match GitHub's `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d` exactly. This is file-level source congruence, not complete bytecode decompilation or build reproducibility.
- **Instagram 455.0.0.8.72 APK**: SHA256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`, 238,346,353 bytes, 31 DEX files; extracted from `InstaTaskManager_HF12_to_HF13_COMPLETE_HANDOFF.zip/reference/instagram.tar.zst`. Actual APK hash validated before DEX inspection.

## Source-backed findings

1. InstaEclipse `ReelDownloadHook.installReduceOptionsListPatch` searches for `ArrayList`-returning methods that **use both** `MediaOption$Option.PLAYBACK_CONTROLS` and `MediaOption$Option.UNSAVE`. It hooks the first match and may append native list entries when flags are enabled. **IG455 `classes5.dex` exact method** `LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;` contains the string `android_purge_26_q3_ClipsOrganicMediaItemViewMoreOptionsController_getReducedMenuOptions`, reads `PLAYBACK_CONTROLS` (code-unit offset 97), `UNSAVE` (offset 321), and has the correct method shape. The observed field uses establish its candidacy; a unique match/installed IE hook identity still requires actual runtime log.
2. The same compiled method reads `X.07EF.A08:ClipsViewerConfig` (code-unit offset 70) followed by `ClipsViewerConfig.A00:ClipsViewerSource` (offset 72). Its enum in `classes4.dex` defines `CLIPS_TAB`, `CLIPS_TAB_HOMECOMING`, `FEED_TIMELINE`, `REEL_FEED_TIMELINE`, plus other sources. A native source enum is a much stronger **menu-event origin hint** than a persistent selected-tab view. It does **not** establish the entry route for every viewer interaction or confirm the visible Media.
3. `SelectedMediaAdapter` upstream uses unique direct-owner Media and bounded traversal, rather than assuming the first View or nearest list is the active post. D06 unique visible-media binding remains `NOT_VERIFIED`.
4. `StoryDownloadHook` anchors `ReelViewerFragment.onCurrentActiveItemBound` and a per-item `ReelItem`. The IG455 static descriptor `ReelViewerFragment.FSZ(ReelItem,X.0NrB)` remains a candidate, not a verified action or safe untested extra hook.
5. `DisableRepostHook` distinguishes Reels/profile action handlers from the Feed `RepostsRepository$postRepost$2` path. Neither is a button-visibility enablement method. The existing D07 dedicated-tab lease is not changed.
6. `AccountLimitHook` finds `account_switcher_max_limit_reached` to relax an add-account limit. It is **not** a source-backed active-account identity provider and must not be used as one. Weak `UserSession` and Android lifecycle signals remain the only account-change evidence.

## D14 implementation and acceptance

D14 independently implements read-only source-category classification inside the **existing** D04 `X.07EF.A05` pass-through hook. It preflights exact owner/config/source classes and `A08/A00` field types after SHA-verified D04 installation; reads enum `.name()` only to bucket fixed code categories; retains no references, values, view strings, Media, usernames or account IDs. It records per-process and manual A/B buckets, screen-hint disagreement, absence and failures, and includes these in Passive, AUTO, Copy All and Export All. **No second Instagram method hook** and no native result/option modifications. Do not assume InstaEclipse's hook ordering or menu mutation state.

For physical qualification, browse Home Feed, Reels tab, Feed-opened Reel, Story, and account switch, opening the Reel `...` menu once per source as a controlled event. Compare D13 continuous view hints and D14 native menu-source buckets. With InstaEclipse ON, confirm the D04 hook still observes callbacks; optionally compare IE OFF in a separate manual test with re-enable afterward. A `CLIPS_TAB_SOURCE` or `REEL_FEED_TIMELINE_SOURCE` row is **source enum evidence only** until page ownership and account identity independently agree. No auto-Repost enablement based on these buckets.

## Sources

- ReSo7200/InstaEclipse `ReelDownloadHook.java`, `SelectedMediaAdapter.java`, `ReelsControlsHook.java`, `StoryDownloadHook.java`, `DisableRepostHook.java`, and `AccountLimitHook.java`, pinned at `ee1c81cadfbcd9fbbee4d01568ee5ca4b2653fbc`.
- User-supplied InstaEclipse 0.8.2 APK and IG455 archive (hashes above). Source methods and DEX instructions examined; no online Instagram behavior, account data, or private media inspected.
