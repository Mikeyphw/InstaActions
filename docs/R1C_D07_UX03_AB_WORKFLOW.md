# D07 UX03 — visible A/B comparison without interrupting native Repost recovery

This change only updates the capture controls and informational UI. No additional Instagram hook, native eligibility override or automatic action is added.

## Recovery mode (existing five-step path)

Prepare on manually selected Account A and the dedicated Reels tab → Start A → browse until fresh A evidence → confirm ITM recovery OFF → enable native Repost → user taps Instagram's original Repost → Recovery OFF → copy D07 safe report. Keep A recording throughout this recovery path. The 90-second/64-change bound is unchanged.

## Optional account A/B comparison (separate visible card after Step 5)

1. Start A, browse Reels, then reopen IA. Recovery OFF.
2. Tap **A2 · Stop A · KEEP observations**. This stops only live recording, retaining original A counters.
3. Manually switch to Account B. The app cannot verify identities, account selection or a same-Activity switch; **do not activate recovery while switching**. If Instagram process restarts, the volatile A counters disappear; export an A-only report first if a restart is likely.
4. Tap **Optional · Start B read-only comparison**, browse Reels, then tap **B3 · Stop B · KEEP both samples**.
5. Tap **B4 · Copy combined A/B report**. Both cohorts and original primitive results are retained within the same process. Do not press Reset A/B before exporting.

Start B is guarded: no recording in progress, recovery OFF, core hooks ready and at least one recorded A event. Stop A / Stop B are guarded by the corresponding current cohort. A and B labels remain manual and do not establish account identity.

Reset A/B is confined to Advanced and now actually clears both TraceSummary and ExactGateTrial aggregate counters. Stopping either cohort does NOT reset evidence; a new Start A invalidates the earlier fresh-A recovery window as before.

## Observed D07 milestone vs qualification

The user confirmed the native Repost button remained available for the intended 90-second window and a native Repost succeeded on the dedicated Reels tab. This is a valid **user-reported functional result**. Formal D07C multi-check acceptance, independent active media identity, account switching protection, Feed Repost, embedded Reels and avatar rendering remain unqualified.
