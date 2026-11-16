# R1C D02 HF01 — IG455 APK-aware static evidence

Status: STATIC_APK_EVIDENCE_PASS; HOSTED_CI_AND_RUNTIME_NOT_RUN.

This replaces the **unapplied D02** overlay; apply it against original R1C D01 `fe18bbc92b3a75e7f1836ffd55df141b0441c4bc` only.

The user supplied a single `Instagram_455.0.0.8.72.apk` (238,346,353 bytes; SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`) inside `instagram.tar.zst`. Static DEX declaration and constant-string analysis covered all 31 DEX files, without executing the APK or reading user content.

## Findings and corrected behavior

- `com.instagram.feed.media.Media` and `MediaExtKt` are declared; `Media.getId():String` is declared, **not invoked**; `MediaFrameLayout` is present. The two legacy Mutable/LiveTree dictionary classes are absent, consistent with InstaEclipse's documented IG447+ changes.
- Story marker `ReelViewerFragment.onCurrentActiveItemBound`: exactly one const-string-referencing method, `Linstagram/features/stories/fragment/ReelViewerFragment;->FSZ(Lcom/instagram/model/reels/ReelItem;LX/0NrB;)V` in `classes6.dex`. The viewer class has 202 defined methods. D02's generic owner-reflection guard of 192 would misreport it as `BOUNDED_INCOMPLETE`. HF01 checks the matched DexKit method descriptor, not the whole owner; no reflection/invocation needed for this marker.
- Feed marker `MediaOptionsOverflowMenuCreator`: one reference, `LX/0bo8;-><clinit>()V` in `classes17.dex`. That is *not* a live click binder. The owner also declares two static `MediaOption$Option` + `ArrayList` methods. A stronger declaration hint has `MediaOption$Option` first and `int` last, but is **not** proof of correct runtime target.
- Reels marker `ClipsOrganicMediaItemViewMoreOptionsController`: 32 methods across 12 declaring classes, matching D01 runtime evidence. `LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;` is a plausible modern list-builder signature, not the old `void(Media,ButtonAdder)` shape. HF01 logs this separately as `modern_list_methods` and treats it as `MODERN_LIST_CANDIDATE_UNVERIFIED` when unique.
- The old D02 `MAX_METHODS_PER_OWNER=192` remains as a conservative fallback for class-level Feed/Reels inspection. The Story descriptor method is inspected separately, so the exception is narrow and justified, not a weakened bound.

## Security/acceptance boundaries

All findings are **static, version- and APK-hash-specific**. No media values, account data, text, captions, private messages, user identifiers or URLs were extracted. No methods are invoked; no Xposed hooks are installed on Instagram internals. Original R1A version allowlist unchanged. All R1C and action gates remain off. Candidate shape NEVER implies an identity or dispatch capability.

HF01 has offline tests only. Hosted CI and actual Android 455 `INA_R1C_SHAPE` values remain required before further work. A future R1C binding phase must separately prove active-media identity and lifecycle invalidation.
