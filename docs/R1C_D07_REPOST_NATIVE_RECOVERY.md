# InstaActions R1C D07A+D07B — Native Repost diagnostic and bounded recovery

## Exact provenance and scope

- Source: InstaTaskManager HF13 commit `3c16c9ab4a836b1378fad5b1350c0c859c8be367` (HF12 user demonstrated original button + user-initiated Repost on Web). NOT a guarantee of recovery on InstaActions.
- Target: InstaActions R1C D06 HF02 `4a7d8363e7c953e74608401ea0210460d0388f6c` **unmerged branch**; this overlay is a direct-child source candidate, not hosted or phone qualified.
- Only the **dedicated Reels tab** has user-observed positive evidence in InstaTaskManager. Reels shown as posts, normal posts, Stories, Friends tab and avatars remain NOT QUALIFIED. R1B Screen.REELS is merely a supporting viewer hint, not proof of tab origin.
- Exact installed Instagram version `455.0.0.8.72`, base APK 238346353 bytes, SHA256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`; D07 *reuses existing D04 verified-hook flag*, no second 238 MB hash.

## D07A (read-only)

`X.0Ie0.invoke` selector `$t == 4` maps native Repost UI; `X.0F2B.invoke(Object)` selectors 11 and 12 map UFI state/count and are always read-only. Original `chain.proceed()` called exactly once, exceptions/returns preserved. `X.0Ie0.A03 → X.0FWn.A01` resolves actual `X.0ApG` concrete receiver and `D3r(int)` method; do not use a guessed class `X.02ib`. Optional factory, mobile-config and session-check hooks are read-only. Caller deoptimization restricted to `X.0Ie0.invoke` using libxposed API102.

Manually labeled cohort A/B counters record only return type/shape/true/false/null, exact native-data presence, selector and install status, no usernames, Media references, account identity, keys or IDs. D06 callback/menu/media evidence remains a wholly independent read-only channel with `MEDIA_IDENTITY=NOT_VERIFIED`.

## D07B (manual, short lease)

Default **OFF**. User opens IA FAB, waits for D04 gate and D07 observers, starts A while viewing dedicated Reels tab, captures at least five fresh false/singleton native frames with data present, explicitly asserts **ITM Repost recovery OFF**, and presses `Enable native Repost · Reels TAB (90 s)`. Reels navigation must be anchored; this is NOT independently proven dedicated-tab identity. A/B labels are not authenticated accounts. If any B evidence exists, HF12 stricter B qualifier is mandatory (one <=5% false/singleton outlier permissible). No forced repeat of B if fresh A alone qualifies using prior HF12 *reference evidence*.

Only original `Boolean.FALSE` from exact `D3r(0xdf82522e)` inside selector-4 stack with actual native data present may return `Boolean.TRUE`. Preserve true, null, other keys, nested frames, other classes/callers and all original Instagram actions. No Repost action is called; user must tap Instagram's existing native button. Server authorization and visibility remain outside this module.

OFF on manual disable, D05 capture transition, new A/B label, Activity pause/destroy, hook failure, navigation no longer anchored as REELS (sampled each second while Activity lives), 90-second expiry or 64 changed native checks. In-flight operations have epoch invalidation; full same-Activity account identity switching cannot be verified. **Manually press Recovery OFF before account switch or leaving the tab.** InstaTaskManager may remain installed as a profiler but its own Repost recovery MUST be OFF (manual confirmation only; not programmatically enforced).

## Qualification matrix

1. Hosted Repository contract + Android API102 CI must pass for the new exact D07 commit. Source smoke (105 synthetic assertions), D07 tests, D06 nonregression tests; APK DEX inventory, package/scope, SHA256SUMS and exact-head artifact.
2. On physical Android16 IG455, keep InstaEclipse enabled; verify D04 exact SHA; verify D07 native observer shapes, safe A-only capture, original Repost button on dedicated Reels tab, user-initiated action and optional Instagram Web check. Then disable and verify native behavior.
3. Negative controls: B account, Feed ordinary post, Reel-as-post, rapid swipe, lifecycle exit, null/native true/wrong key, nested callback, data unavailable, deopt error, time/call limit, another mutator ON, changed installed IG build, and unexpected installer errors.
4. D06 HF02 A1 report is still pending separately. Never claim `MEDIA_IDENTITY=VERIFIED` based on D07.

## Rollback

`python3 <delivery.zip> status` verifies the branch and CI; to undo **before merge**, revert the single source commit deliberately via Git and ordinary new PR/commit. Do not use `git reset --hard`, force push or silently uninstall/disable InstaEclipse or ITM.
