# D12 — independent account-change correlation, not identity

Base `Mikeyphw/InstaActions@ab01e1b2d2e6e3f91916d0751ec639295401c328`,
consolidating D11 HF03 as an unpublished overlay.

## Two genuinely distinct observation sources

1. **Android framework:** `Application.ActivityLifecycleCallbacks` passively
counts resumed/paused/destroyed events after registration.
`Instrumentation.callActivityOnNewIntent(Activity, Intent)` and the Android
API 35 three-argument overload are hooked exactly once each, preserving the
original call, return value and exceptions. *No Intent arguments or ComponentCaller
contents are read*. An Intent delivery disables an active Repost recovery lease
without guessing the account. Optional hook failure is explicitly `UNAVAILABLE`.
2. **Instagram-specific, previously verified:** read-only
`X.02xy.A04(UserSession, String)` weak reference comparison; we do not read
fields or the String argument. Changed reference = a *possible* session
transition, not authenticated account identity.

Source: Android public API reference for Instrumentation and ActivityLifecycleCallbacks.
An account-provider getter on IG455 is **NOT** source-qualified; no guessed
Instagram class/method/field is hooked. No native Repost predicate modified.

## Observability / controls

- Process-volatile 64-event bounded ordered timeline with explicit dropped cap.
- Before/after manual-marker event bins, no timestamps, usernames or account IDs.
- D12 source status/coverage included in `ACCOUNT`, `PASSIVE`, `Copy ALL` and
  `Export ALL`, even if the Account mode was never opened.
- Android new Intent always disarms active Repost recovery, but never enables it.
- App same-Activity account changes can have no lifecycle/intent callbacks.
- No account-based whitelist/auto-arm, no new permission, no Feed action or
  avatar/Media identity promotion.

## Physical acceptance plan

A. Install exact-commit hosted APK with InstaEclipse ON; check D12 statuses.
B. Open Account diagnostic, Start A, gather callbacks and Stop A.
C. Turn Repost OFF, switch accounts *manually*, press Confirm marker, Start B
   and gather callback evidence, Stop B, Finish, Export ALL.
D. Compare native-ref and Android framework ordered events; do not label the
   account as verified when only a marker, intent or lifecycle event appears.
E. Repeat switching without leaving Instagram's Activity. If no signal, record
   a **coverage gap**, not a passing detector. Do not arm Repost automatically.
