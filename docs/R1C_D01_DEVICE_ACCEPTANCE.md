# R1C-D01 — source-backed media-candidate inventory

Status: **NOT_RUN** on device (source and CI tests are not acceptance).

## Known baseline

R1B HF05 SHA `530feede55917cd7d050e3dd89800608abe48710` passed hosted Android CI `42713725640` and repository contract `42713725548`. User-provided device capture on Instagram 455.0.0.8.72 showed stable `HOME_FEED`, `REELS`, `STORY`, and return `HOME_FEED`; no R1B sampling failures in the captured records. This qualifies *screen navigation only*.

## R1C-D01 inventory, not identity

This is a strictly read-only, one-time, background metadata inventory for the **exact** Instagram version `455.0.0.8.72`, with no Instagram internal method hooks. Class probes are source-backed by ReSo7200/InstaEclipse:

- `com.instagram.feed.media.Media` (declaration only; detect whether `getId(): String` shape exists, **never invoke** it)
- `com.instagram.feed.media.MediaExtKt`, `MutableMediaDictIntf`, `LiveTreeMediaDict`
- `com.instagram.ui.widget.framelayout.MediaFrameLayout`
- Bounded DexKit markers: `ReelViewerFragment.onCurrentActiveItemBound` (StoryDownloadHook), `ClipsOrganicMediaItemViewMoreOptionsController` (ReelDownloadHook), `MediaOptionsOverflowMenuCreator` (PostDownloadContextMenuHook)

All diagnostic records avoid account, media, caption, URL and message content. `UNIQUE_CANDIDATE` is a syntactic hint only, not an installed hook or working media binding. `identity_verified=false`, `binding_hooks=off`, `actions=off`. Original R1A DexKit version gate is unchanged.

## Device acceptance

1. Use the bundled Python+Rich device probe after hosted CI succeeds; it validates artifact SHA256SUMS, removes the previous debug-signature APK, installs and prompts for Vector scope.
2. Leave InstaEclipse enabled. Launch Instagram, open Feed / Reels / Story and return to Feed.
3. Confirm one `INA_R1C_SCHEDULED` per process, five `INA_R1C_MODEL` logs and up to three `INA_R1C_ANCHOR` logs, plus one `INA_R1C_SUMMARY` or an explicitly reported scan failure.
4. Verify `INA_R1B_NAVIGATION` behavior has not regressed, and check crashes/ANRs in full logcat (not only a filtered INA stream).
5. Unsupported Instagram versions must skip this inventory; no internal hooks or actions should appear.

**Completion gate:** record counts and candidate match distribution per exact APK, and separately verify view-to-media binding before promoting any identity. Not a release qualification.
