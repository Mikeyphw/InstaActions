# R1B HF05 — Hybrid navigation, separate media evidence

Baseline: `e59956c2481769eee1aca2d5796d527caf2f2fc2` (HF04) — all hosted CI passed.
Current device: Instagram `455.0.0.8.72`, Vector API 102 and InstaEclipse enabled.

Status: **NOT_QUALIFIED** until real-device acceptance of this APK. An offline
classifier test or Android-hosted build cannot prove selected-tab behavior.

## Why

The HF04 sample traversed ~110 visible Android Views, but Feed rows are built
using Litho, so `row_feed_button_like` and `media_group` were absent from the
ordinary View tree. Reels and Stories were separately detected with anchored
viewer markers. The `feed_tab` and `main_feed_action_bar` were present on every
screen: their *mere presence* must never be interpreted as an active Feed.

InstaEclipse's source confirms that Instagram's main Activity can be
`com.instagram.mainactivity.InstagramMainActivity`, with a legacy alias
`com.instagram.android.activity.MainTabActivity`. Its DistractionFreeUIHook
uses `feed_tab.isSelected()` to identify active navigation. See
`mods/ui/DistractionFreeUIHook.java` and `mods/ui/StartupTabHook.java` at
`https://github.com/ReSo7200/InstaEclipse`.

## Two separate, read-only outputs

- `INA_R1B_NAV_EVIDENCE`: only booleans indicating known Main Activity, Feed tab
  visible/selected, other tabs selected, Feed action bar visible, scan truncated.
  No texts, media objects, account identities or network requests.
- `INA_R1B_NAVIGATION surface=HOME_FEED|REELS|STORY|UNKNOWN`:
  a **screen-level preview**, requiring two matching snapshots >=1000 ms apart.
  A selected Home tab plus Feed action bar is only a *HOME_FEED* navigation hint;
  never a POST/media identity. REELS/STORY anchored viewer evidence overrides
  even a selected Home tab. Ambiguity, unrecognized Activity, other selected tab
  and truncated trees fail closed. A transition immediately invalidates the
  former stable screen and reports UNKNOWN until newly stable.
- `INA_R1B_CONTEXT surface=POST|REELS|STORY|UNKNOWN`: retained independent
  **media/viewer-level candidate**. POST now requires a visible
  `row_feed_button_like`; generic `feed_media*` and `post_container` resources
  cannot promote a POST without Feed-row evidence. Neither output provides a
  media ID. A HOME_FEED detection is not proof of a particular post.

Navigation is rechecked at a bounded 1.6 s interval because tab selected state
can change without Android layout events. Resource traversal still caps at
384 Views and depth 18; the old layout/scroll listeners remain. All timers,
listeners and pending callbacks are removed on Activity pause/destroy.

The R1A DexKit version allowlist is unchanged and continues to skip IG 455.
All mutating actions remain disabled. InstaEclipse is not modified or called.

## Device gate

1. Install the CI-produced HF05 debug APK, verifying SHA256SUMS. Uninstall the
   previous debug build first (per-install GitHub debug signing keys differ).
2. Enable InstaActions and Instagram scope in Vector; keep InstaEclipse enabled.
3. Cold-start Instagram. Visit **Feed → full-screen Reel → full-screen Story →
   Feed**, holding each screen long enough for at least two samples (~25 s).
4. Verify `INA_R1B_NAV_EVIDENCE` reports `feed_selected=true` on Home and no
   other tab selected. If the app does not select this View, HOME_FEED must
   remain UNKNOWN; report the evidence rather than guessing.
5. Expect stable navigation `HOME_FEED`, `REELS`, `STORY`, `HOME_FEED` with
   `UNKNOWN` permissible during transitions; no incorrect settled context.
6. Expect `INA_R1B_CONTEXT` to retain anchored Reels/Story results. POST may
   legitimately stay UNKNOWN if Litho hides Feed-row IDs.
7. Confirm no additional crashes/ANRs and observer attach/detach is balanced
   after closing the Activity; all diagnostics actions=off.

**Do not merge R1B** before this device gate. A mismatch requires an evidence-
based fix, not using `feed_tab` presence alone. Defer R1C media identity.
