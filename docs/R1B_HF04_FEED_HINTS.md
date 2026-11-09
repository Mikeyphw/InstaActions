# InstaActions R1B HF04 — bounded, in-process Feed anchor evidence

Base commit: `b2453613ff8a7adf332d49d852a2ad4ba8d618a1`.
Previous hosted CI run `42560201450` passed. Device evidence in
`r1b-surface-evidence.zip` showed `REELS / ANCHORED` (score 12), `STORY /
ANCHORED` (score 27–45), and `UNKNOWN` (0/0/0) on Feed and after returning.
A short-lived REELS decision on the way into Stories does **not** qualify
stable screen identity. UIAutomator had the same 38-node, zero Instagram-ID
accessibility hierarchy for all four screens, which is not suitable for
source-level Feed anchor discovery.

## What changed

`R1BContextMonitor` now produces `INA_R1B_RESOURCE_HINTS` only when the
screen classifier returns UNKNOWN. It collects only **Android view type names**
and **static Instagram-owned resource entry names** from the already bounded
in-process hierarchy (maximum 384 views, depth 18). It logs:

- total visited/visible nodes, with-ID nodes, unresolved resource IDs,
  number of distinct Instagram static view IDs, and truncation status;
- up to 24 sanitized, deterministic static Instagram view resource names,
  preferentially showing possible feed/layout/media anchors;
- up to 12 sanitized view class names with bounded per-class counts.

There are at most three hint messages per Activity session, separated by
at least 8 seconds. A single UI-thread timer requests later samples only
while UNKNOWN; it is detached and canceled on pause/destroy. Ordinary
R1B_CONTEXT logs, views sampled, and version gate semantics are otherwise
unchanged. All actions remain OFF; R1A DexKit discovery retains strict
451/453 version qualification. No view text, captions, media IDs, account IDs,
URLs, messages, accessibility text, contacts, taps, click listeners or network
calls are collected. Do **not** use this result to authorize actions.

## Acceptance

After Android CI passes, run `INSTALL_AND_PROBE_HF04.zsh` on the device with
InstaActions + InstaEclipse enabled. The script verifies GitHub artifact
checksums, uninstalls the differently signed debug build, installs the new
one, and captures live filtered logcat starting before Instagram launches.
Remain on Feed long enough for at least two UNKNOWN samples before moving to
Reels and Stories. Repeat on return to Feed; share the zipped capture.

Do **not** merge R1B until Feed classifications are grounded in real
resource evidence, transient classifications are understood, runtime
performance is acceptable, and coexistence is verified. If static IDs are
absent even in-process, prefer a safer activity/fragment lifecycle anchor
rather than guessing from obfuscated names.

## InstaEclipse source-backed anchors (included in HF04)

Source-verified against ReSo7200/InstaEclipse `main`:

- `FeedPhotoZoomHook.java`: `row_feed_button_like` is the classic Feed-row-only
  anchor distinguishing Feed images from shared Instagram image views.
- `FeedVideoDownloadHook.java`: distinguishes Feed `row_feed_button_like`
  from Reels `like_button` under `clips_ufi_component`; also uses
  `row_feed_button_save` for Feed media lookup.
- `RoundedFeedPostsHook.java`: `MediaFrameLayout`/`media_group` and
  `video_container` are supporting media-container evidence, **not**
  standalone screen identity anchors.

R1B HF04 treats `row_feed_button_like` as an ANCHORED Post/Feed-row hint only
when no strong Reels or Stories viewer anchors are visible; save/media-group
IDs add score only when that Feed-row like button is present. Navigation tabs,
`video_container` alone, and `media_group` alone cannot qualify a Feed
surface. This still needs acceptance on Instagram 455; source evidence does
not guarantee that every UI version exposes the anchor in the observer tree.

Upstream references:
- https://github.com/ReSo7200/InstaEclipse/blob/main/app/src/main/java/ps/reso/instaeclipse/mods/feed/FeedPhotoZoomHook.java
- https://github.com/ReSo7200/InstaEclipse/blob/main/app/src/main/java/ps/reso/instaeclipse/mods/media/FeedVideoDownloadHook.java
- https://github.com/ReSo7200/InstaEclipse/blob/main/app/src/main/java/ps/reso/instaeclipse/mods/feed/RoundedFeedPostsHook.java
