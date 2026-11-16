# InstaActions D08 — source-grounded audit: Feed Repost and Friends avatars

**Audit scope:** prior exact IG455 APK static-DEX investigation and prior native Repost trace, source in the supplied InstaTaskManager HF12/HF13 handoff, plus InstaActions D06/D07 source. No new hook, device trace or native intervention for either target was added in this audit. This document records candidate code owners, critical mismatches and test plan; it is NOT a working implementation.

## Product objective remains R0–R8

InstaActions is an independent Vector/libxposed API 102 module for **personal, explicitly invoked, context-aware Instagram quick actions** on Feed, Reels and Stories, coexisting with InstaEclipse. R1C current-visible-Media identity is unresolved; R2 contextual UI, R3 like/save/repost/download/playback, R4 explicit sharing, R5 one confirmed user-edited comment, R6 idempotence/retries/cancellation, R7 exact-commit CI+Android acceptance and R8 release remain planned. Do not duplicate InstaEclipse Download/Copy/Info/Speed UI or introduce mass/background engagement.

## D07 verified result, exact scope

Manual Account A, dedicated bottom Reels tab, exact Instagram `455.0.0.8.72` APK SHA `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`: native hooks 2/2 and optional 3/3, 41 selector-4 objects (31 singleton `X.09j4`, 10 payload `X.09ZC`), 10 bounded D3r modifications, manual OFF. **User reported a visible native Repost button for 90 seconds and a successful self-initiated Repost.** This validates an actual Reels-tab action in the reported environment but is not the formal D07C acceptance record. Reels opened as posts, profiles and Feed posts remain NOT QUALIFIED.

## Feed Repost: distinct code path, not an extension of D07

The prior IG455 static source audit identifies:

- Reels/profile action: `X.06GI.EIk(...)` and `X.06GI.A00(...)`; Reels UI eligibility `X.0Ie0.invoke()` selector 4, exact `X.0ApG.D3r(0xdf82522e)` data predicate. These support the **existing Reels-tab** recovery only.
- Home Feed action: `com.instagram.reposts.data.RepostsRepository$postRepost$2` / `invokeSuspend(Object):Object` with an asynchronous repository commit. InstaEclipse `DisableRepostHook` suppresses this path separately; disabling its suppression does **not** reveal a hidden Feed button.
- IG455 `can_repost` and `isRepostButtonEnabled` are separate model/UI clues. Presence of names or a negative Boolean does not prove that the user/account/media may repost or that a Feed renderer is enabled.

**Critical missing proof:** actual Feed row Media ownership and visible row binding, Feed UI-state renderer visibility, Feed-specific original eligibility and UI gate for the exact viewed post, native click handler, and whether the backend accepts a user-triggered action. R1B `HOME_FEED` is screen evidence, not media identity. D06 Reels menu ownership cannot be used as a Feed identity substitute. The 90-second D07Reels override MUST NOT be routed to Feed.

### Proposed Feed investigation (read-only first)

1. On exact IG455 with InstaEclipse ON and its suppress-Repost option OFF, compare one public ordinary Feed post, one Feed-opened Reel, and the same Reel in the dedicated Reels tab. Note visible button state manually without capturing IDs or names.
2. Resolve Feed-specific native renderer and eligibility method by exact source/DEX signature and method uniqueness. Count original result Boolean/null/object classes, visibility gate and `can_repost` presence only. Do not hook action executors to force optimistic Reposted state or network calls.
3. Bind a unique, foreground visible Feed row to its Media using independent D06/R1C evidence; handle carousels, scroll/recycle, navigation, media change and stale callback negatives.
4. Only after separate physical proof, consider `Show Repost button · Feed`, default OFF and reversible; never conflate ordinary post vs Feed-opened Reel. Original Instagram click and server permissions remain authoritative.

## Friends avatars: three distinct features, not a single switch

Prior static IG455 anchors (source candidates, **not runtime-verified**):

| Feature | Static anchor | Evidence missing |
|---|---|---|
| Liked-by facepile on Reels | `LikedBySocialContextRenderer_getFacepile`, `X.0G4F.A03(...)`, `X.0G4I.A0q(...)` | Underlying nonempty actual social-context list and exact visible media |
| Reposted-by facepile | `RepostedBySocialContextActionHandler_handleRepostedBySocialContextTap`, `X.05NL.EIn(...)` | This is a tap handler, not an avatar data source; find native view and list |
| Reels Friends/friendly avatars | `ClipsFriendlyViewerFacepileComponent_render`, `ClipsProfilePictureFacepileComponent_render`, `X.0R3f.A0q(...)`, `X.0G9s.A0q(...)`, `ClipsActionBarFacepileUiState(isFriendsTabFacepileDisabled=`, `resolveShouldDisableFacepile` | Distinguish rendered/suppressed state, Friends-tab context and real source-list presence |

`friendly_bubble_with_repost`, `friendly_system_without_repost`, `liked_by_bubble`, and `facepile_top_likers` are experiment/social-context identifiers, **not evidence that avatars exist for a particular post**. Composite renderer `X.0G4F.A03` takes many inputs; overriding its whole result risks replacing unrelated UI. Do not fabricate friends, create fake profile icons, fetch private graph/social data, or force nonempty placeholders when original data are empty or unavailable.

### Proposed avatar audit (read-only first)

1. Capture user-identified target surface separately: Friends tab, Reel liked-by, Reel reposted-by, and optionally Feed social context. No account names/screenshots are required for aggregate telemetry; a user-supplied screenshot may help choose visual target later.
2. For exact source-backed renderer candidates, hook only a single unique method and call `chain.proceed()` exactly once; count invoked/not-invoked, native list `NULL/EMPTY/NONEMPTY` (never contents), native suppression/disabled Boolean, rendered avatar View count, UI `UNKNOWN`, and conflicting surfaces.
3. Verify presence and count agreement across content with/without genuine friends/social proof. No generated identities, extra network requests, profile URLs, account IDs or hidden scraping; preserve server-provided empty lists.
4. Only then consider independently toggled `Show liked-by avatars`, `Show reposted-by avatars`, `Show Friends-tab avatars` default OFF, with source-data and visible-media proof. Any test with no source data is NOT OBSERVED, not a failure of UI rendering.

## Acceptance and refusal policy

- Keep D07 native Repost recovery only on the dedicated Reels tab, manual ON/OFF with 90s/64-change limit, no automatic repost.
- Feed rendering and friend avatars remain read-only research; `r1c_d08_feed_repost_enabled=false` and `r1c_d08_avatar_render_modification_enabled=false`.
- No Gradle in Termux; use GitHub Actions exact-commit build; no auto-merge. Test API 36 physical device, original user-triggered actions, screen changes, rapid scroll, insets, account switch, module coexistence and restoration.
- Preserve InstaActions R0–R8 and existing D06 evidence/guards. Do not raise `MEDIA_IDENTITY=NOT_VERIFIED` to qualified without independent source and device proof.

**Source provenance:** `InstaTaskManager_HF12_to_HF13_COMPLETE_HANDOFF.zip/reference/Instagram455_Repost_Facepiles_ForceEnable_Audit_2026-11-10.md` and `InstaActions_D06_HF02_COMPLETE_HANDOFF_20261112.zip/04_FULL_ROADMAP.md` and `03_FINDINGS_AND_DEVICE_EVIDENCE.md`.
