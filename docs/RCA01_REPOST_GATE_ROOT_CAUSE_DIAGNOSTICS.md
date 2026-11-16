# RCA01 — Why Repost differs between Account A and Account B

## Scope and provenance

Working repository: `Mikeyphw/InstaActions`, branch
`feature/ina-r1c-media-discovery-20261109`, starting HEAD
`59129a571d9342a0da0d4a56a06d1c0cf4fe8e9f`.

This milestone **temporarily disables the legacy D05/D07 multi-feature floating
panel** and attaches **one RCA01 diagnostic-only FAB** through the existing
activity hook and the Android lifecycle fallback. Legacy source is retained for
regression and potential future use, but its `onResumed` and `onLeaving`
entry points are not called. No new UI triggers a Repost action or enables
Repost recovery; any old in-memory recovery lease is disarmed on RCA01 attach.

The native Repost observers are qualified only after D04 verifies one of two
exact Instagram 455.0.0.8.72 `base.apk` hashes and a 238,346,353-byte size:

- Reference HF12 APK: `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`
- User's APKM base APK: `4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a`

The full APKM container SHA is NOT a substitute for base.apk SHA. This static
whitelist does not prove runtime/device eligibility.

## Source-backed meaning of the field

The original IG455 bytecode has an `X.02ib.D3r(int):Boolean` path for exact
integer key `0xdf82522e` in Repost selector-4, via GraphQL `TypeModelData`.
Independently, `com.instagram.feed.media.Media.A3x():Boolean` reads the same
hash in LiveTreeJNI (with a cache/fast path) and `Media.AFV(Boolean):void`
updates a cached value or its LiveTree model. These are **distinct receivers
and data-model paths**. Sharing a key is NOT proof of identical source objects,
GraphQL schema fields, a server-supplied `can_repost` flag, or a single A/B rollout.
The `can_repost` JSON token has not been causally linked to that exact key.

RCA01 observes these separately and only during manually labeled A/B capture.
For `AFV` it records the argument **after successful return**, not proof of a
persisted update; exceptions remain native. It records getter Boolean category,
nullable state, exact D3r result (call ordinals remain in the existing D08 report), outer UI-shape category and
existing first MobileConfig gate. No media IDs, usernames, account IDs, raw
objects, configuration arguments, network payload, URLs or captions are saved.

## Operator workflow (real device)

1. In Vector, enable `InstaActions` for the Instagram main process. Keep
   InstaEclipse enabled and turn off all old ITM Repost recovery. Restart IG.
   Confirm the **only floating chip says `RCA01`**; the old D05/D07 FAB must
   not be visible. If no chip appears, inspect `INA_RCA01` / `INA_DIAG` logs
   and check the selected APK/version/hash first.
2. Select the affected Account A, open the bottom Reels tab and open **the
   same public Reel** chosen for both accounts. Tap RCA01 → **Start A**, close
   panel and browse a controlled set of Reels; reopen RCA01 and **Stop A**.
3. Switch to Account B manually with recovery OFF. Reopen the same Reel in
   the same native surface. Tap **Start B** and then **Stop B** after matching
   observations. Neither A nor B is an authenticated account identity.
4. Tap **Copy RCA01 A/B report** (explicit only). Preserve original results.
   Repeat A→B→A with a **fresh capture** on another session; avoid mixing
   different Reel populations and session changes in a single claim.
5. Compare site-wise Boolean distributions, earliest sampled event order,
   missing/null outcomes, successful `AFV` calls, UI state and source hints.
   Correlation does not prove origin. Check Friends-tab/facepile separately
   before asserting a common rollout gate.

## Safety / acceptance

- RCA01 FAB is host-Activity-owned, not `SYSTEM_ALERT_WINDOW` and not an
  always-on Android overlay. Explicit buttons only start/stop/clear/copy.
- Each A/B cohort stores <=4,096 primitive site observations and a <=96-item
  bounded ordered timeline (no user data). App process death loses samples.
- Probe result logging is category-only. No automatic clipboard copying.
- No second account-binding provider, no network interception, no Repost
  mutation, no persistent enablement, no Friends UI fabrication.
- **Not qualified:** real-device RCA01 FAB visibility, accurate per-media
  binding, `can_repost` mapping, server source, combined rollout, device
  performance, InstaEclipse coexistence, or hosted CI until corresponding
  evidence exists. A successful local JVM smoke is NOT physical acceptance.
- Exact Android test APK and unit tests are built through GitHub Actions.

## Existing test gates

Historical D05/D15 UI source tests remain as regression checks for dormant
classes, but runtime attachment expectations now assert the new RCA01 FAB in
both entry and lifecycle fallback paths. No source or test is removed to hide
failures. Reconsider deleting obsolete D05 UI cases only after equivalent
current-runtime coverage is proven and reviewed separately.
