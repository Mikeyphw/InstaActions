# InstaActions D13 HF05 — automatic screen discovery, consolidated D11 HF03 + HF04 lifecycle

## Source provenance and scope

Direct overlay parent: D10 `ab01e1b2d2e6e3f91916d0751ec639295401c328` on
`feature/ina-r1c-media-discovery-20261109`. This overlay includes all D11 HF03
hardened diagnostics and the subsequent D12/HF04 independent Android lifecycle
work in one atomic application. Nothing is applied to a standalone D11 or D12
checkout, and Reels native Repost recovery logic is byte-for-byte unchanged.

## Problem from physical A/B evidence

Explicit Feed/Avatar manual snapshots have two weaknesses: the user must label
screen locations individually, and broad screen scans cannot tie a UI widget to
one verified Media or facepile owner. D13 introduces a passive, bounded, UI-only
navigation observation. It is NOT a new Instagram entitlement, user identification,
Feed Repost workaround, or media binding.

## Automatic monitoring

The sampler is attached to existing Instagram Activity resume/pause hooks. It
uses `R1BContextMonitor.stableScreen()`, which already independently stabilizes
HOME_FEED, REELS, STORY and UNKNOWN using view IDs and anchored surfaces.
Every 1.5 s on a resumed Instagram Activity it checks only a bounded UI subtree
(max 512 shown views, 24 ms). It reads *only* selected/activated/checked bits
for Instagram-owned IDs `feed_tab` and `clips_tab`, never view texts, Intent
contents, model fields, usernames, session tokens, images, or Media references.
A 2-sample debounce rejects one-frame transients.

- Stable HOME_FEED and selected feed tab -> `HOME_FEED`.
- Stable STORY -> `STORY` (R1B screen anchor only).
- Stable REELS, Clips tab explicitly selected -> `DEDICATED_REELS_TAB_HINT`.
- Stable REELS, Feed tab explicitly selected -> `FEED_OPENED_REEL_CANDIDATE`.
- Stable REELS with hidden or unselected tabs -> `REELS_ORIGIN_UNKNOWN`; a
  preceding HOME_FEED is merely an origin-route hint, not qualification.
- Contradictory tabs, truncated scan, unsupported screen, background/paused
  Activity -> `UNKNOWN` or skipped, never affirmative evidence.

A recent Home→Reels transition is never treated as a verified Feed-opened Reel.
This requires an independently verified Instagram navigation origin owner.

## Account changes

The existing D09 `UserSession` reference observer and D12 framework lifecycle
(new Intent + Activity resumed/paused/destroyed) remain active. Their event
ordering is included in AUTO and Passive reports and in Copy All/Export All.
An opaque reference change is a *suspected* session/account transition and can
turn native Repost recovery OFF; it cannot identify the active Instagram account.
D13 does not automatically start, re-arm, or expand Repost to Feed/Stories.

## UI and reports

Passive discovery is automatic with no Begin action. Open INA -> Passive Overview
for the latest surface and bounded transitions. Choose **AUTO discovery** for a
controlled per-account comparison: Begin, Start A, navigate surfaces (no snapshot
buttons), Stop A, turn recovery OFF, manually switch, acknowledge, Start B,
repeat, Stop B, Finish. Generic callbacks do NOT constitute feature evidence.
Unobserved surfaces stay unobserved. Evidence remains `NOT_VERIFIED` even when
both A and B contain screen hints. This experiment does not require A/B account
labels for passive navigation detection.

Copy All and Export All include the D11 report archive (with loss warnings),
D12 lifecycle/session-reference log and the new D13 passive transitions. Data
is content-free, bounded and exists only in the Instagram process's memory.
Copy All does not provide Android logcat or persist across process termination.

## Required acceptance

1. `python3 overlay.zip --self-test` must check the package, source pins,
   refusal behavior and full-source preflight before Devtool is invoked.
2. D10/D11/D12/D13 JVM safety and source contracts must pass in the actual
   checkout. No local Gradle build; hosted GitHub Actions must build APKs.
3. Physically test HOME_FEED -> dedicated Reels -> Stories -> Reel FROM Feed,
   including routes that hide navigation tabs, Activity pause/resume, and
   same-Activity A/B account switches.
4. Demonstrate that uncertain Reel origin stays unresolved, the detected
   account reference change disarms recovery, no auto-rearm occurs, and
   ordinary Feed posts do NOT gain Repost actions.
5. Export All and compare event sequence with physical navigation.

**Device qualification: NOT_RUN.** GitHub-hosted qualification: NOT_RUN.
Original R0–R8 InstaActions goals remain intact.
