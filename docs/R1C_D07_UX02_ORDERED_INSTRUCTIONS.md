# D07 UX02 — ordered guided Repost controls

This is **UI-only**, not a native-hook or qualification change. Applies only to the exact InstaActions D07 UX01 source parent.

## Required order (the same in the IA panel and companion app)

1. **PREPARE.** Enable InstaActions in Vector/LSPosed for Instagram, keep InstaEclipse enabled, restart Instagram, use the manually selected affected account, turn InstaTaskManager's other Repost recovery OFF, open the Instagram **bottom Reels tab**. Verify native-hook readiness. Do **not** tap the in-app ITM-off confirmation yet.
2. **START A.** In the floating IA panel, press `Start A` exactly once. It is read-only capture and resets previous ITM-off acknowledgments. Do not press Start B or Stop A/B.
3. **BROWSE, RETURN.** Close IA, swipe through multiple Reels on that same bottom tab, reopen IA; if fresh A evidence not ready, repeat browsing. **Keep A recording.**
4. **CONFIRM, ENABLE.** Only once fresh A is ready, tap `4A · Confirm ITM Repost recovery is OFF`, then `4B · Enable native Repost`. The manual acknowledgment is not cross-module verification. `Enable` stays disabled unless existing evidence and lifecycle guards allow it.
5. **TEST, OFF, EXPORT.** Close panel; tap Instagram's existing native Repost button manually; reopen IA and press Recovery OFF before changing account or tab; copy the safe D07 report. Separate D07C acceptance confirms a real action on the device. The top status card has emergency OFF at all times.

## Scope and warnings

- Repost recovery is limited to the **dedicated Reels tab** where ITM HF12 succeeded; Reels embedded in feed/posts, profile Reels and normal posts are NOT qualified. A REELS screen hint is not identity proof.
- The existing 90-second / 64-modified-result lease and original Instagram click handler are unchanged. No automatic Repost.
- Optional manual B comparison and D05/D06 discovery are collapsed **after** Step 5 to prevent accidental reset. Starting B or stopping A/B invalidates fresh A and disables recovery.
- Same-Activity account switches cannot be reliably detected; manual OFF before leaving/switching is required.
- Device acceptance remains `NOT_RUN`; passing source tests and CI is not real Repost proof.

### HF01 — retain D05/D06 guidance contract

Step 1 now states **InstaEclipse stays ON**. This is the same operational
requirement as before UX02, restored in user-visible wording so inherited D05
and D06 guidance checks continue to protect the real workflow. No hook or
qualification state was modified.
