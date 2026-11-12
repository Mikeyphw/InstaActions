# INA R1C D07 UX01 — readable guided UI (SOURCE PREPARED)

## Purpose

The previous IA FAB was a 300dp-wide dark panel with indistinguishable unstyled buttons and a long technical dump ahead of any instructions. The separate companion MainActivity still described R1B rather than experimental D07. This change makes the *existing* feature understandable without adding an Instagram hook or silently declaring physical acceptance.

## Layout and accessibility

- Dark charcoal backdrop, slate cards and high-contrast white, blue, mint and amber text; minimum calculated panel text contrast > 7:1 except the primary button white-on-blue at > 5:1.
- 14sp normal-sized action text; 50dp minimum target; clear primary blue and safety-red action surfaces; touch ripple and persistent scroll bar.
- Adaptive overlay panel (max 432dp, remaining decor width, height responsive to actual decor, no fixed 400dp limit). Scrolling must be checked in portrait and landscape on the physical device.
- A short live state and next-action explanation appear first. Advanced D05/D06 technical metrics are collapsed by default.
- Companion app home screen now documents the D07 workflow, exact limitation and pending status rather than presenting legacy R1B as the current feature.

## Correct Repost interaction order

1. Use Instagram **455.0.0.8.72**, exact D04 gate, Instagram main process, InstaEclipse enabled.
2. Select the dedicated **Reels tab** on the affected account. Feed Reels, posts and profile Reels **are not qualified**.
3. Open **IA**, tap **Start A**, close IA and browse several Reels. Reopen IA and read the live next step. Do **not** Stop A before enabling: a fresh active A capture is part of the D07 authorization gate.
4. Confirm **InstaTaskManager's Repost recovery is OFF** (manual assertion only). Tap **Enable native Repost**, then close IA and use Instagram's original button yourself.
5. Tap **Recovery OFF** before any account/tab change. Recovery also expires after 90 seconds or 64 changed calls, but same-Activity account changes cannot be reliably detected.
6. Copy the **D07 safe Repost report**. Complete the separate D07C runner's *actual native action* checklist; neither source contracts nor a visible button alone constitute physical acceptance.

**B** is an optional passive comparison, not required for repeated A recovery. Switching to B invalidates the fresh-A trial. Restart A and observe again if needed.

## Non-goals / invariants

- No changes to native `X.0Ie0` / `X.0ApG.D3r` instrumentation, Boolean override, original click handlers, network requests, Vector permissions or other account features.
- D05/D06 diagnostic modes retained; not upgraded to verified visible-media identity.
- Source- and hosted-CI checks separate. D07C device status remains **NOT_RUN** until user evidence is evaluated.
- Do not mark normal posts or Reels outside the dedicated tab as qualified.
- No local Gradle; use GitHub Actions for Android compilation and instrumentation.

## Physical UI acceptance checklist

- Phone portrait and landscape: scroll the full panel to Recovery OFF, D07 export and Advanced tools, with no clipped/overlapping buttons or unreadable text.
- Check contrast, touch feedback, 50dp buttons and whether the new next-step instruction updates through Start A, evidence collection, manual confirmation, ON and OFF.
- Confirm the IA chip accurately indicates `IA`, `A`/`B`, or `ON` and remains draggable.
- Keep D05/D06 diagnostics collapsed by default; expand and check A1/A2/B1/B2 prompts and exports.
- Restart Instagram while ON and verify recovery de-arms. Test expiry and 64-call limit with source-qualified acceptance protocol; do not infer those from UI alone.
- Verify normal Instagram interactions and InstaEclipse coexistence remain unchanged.
