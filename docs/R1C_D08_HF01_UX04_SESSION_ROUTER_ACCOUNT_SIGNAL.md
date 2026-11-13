# InstaActions D08 HF01 UX04 — single diagnostic and guarded session Repost

Exact parent: `741be30c1edaa0812196d94e76aef1e1df9eb1e9` on `feature/ina-r1c-media-discovery-20261109`.

## Mode workflow

1. Tap IA; select ONE diagnostic from the high-contrast dropdown: dedicated Reels recovery, manual A/B, manual Feed snapshot, D05/D06 media evidence or account-change diagnostic.
2. Press **Begin selected diagnostic**. Attempting to change the mode while RUNNING is refused; only its controls are visible.
3. Use the shown native diagnostic. **Finish** preserves its report and stops an active Reels A capture after recovery OFF; **Abort** disarms and cancels active captures.
4. Copy the single-mode report after Finish, before choosing a different mode.
5. Feed mode: select affected or unaffected manually *before* Begin; record multiple snapshots, Finish, switch accounts, start a separate Feed session under the other label. Reports retain separate cohort counts and session totals.

## Repost modes

The existing 90s/64-change mode is unchanged. The new `Keep Repost available · foreground session` switch removes the 90-second countdown only for the explicit current foreground Activity, with a 4096-modified-call guard. No persisted toggle, no automatic repost, no broader media surface. Native Boolean.FALSE only, verified D3r exact key, fresh manual A Reels evidence and ITM-OFF assertion remain mandatory. Loss of foreground Reels context, Activity lifecycle exit, observed UserSession reference change, changed native D3r result, hook failure or manual OFF disarms it.

**Important limitation:** no callback is not proof of a stable account. Same-Activity account switches may be missed. The user must turn OFF before switching accounts. This is deliberately NOT a never-expiring account-wide bypass.

## Account signal

The existing source-pinned `X.02xy.A04(UserSession,String):boolean` observer now compares only opaque weak `UserSession` references. It reads neither the String argument nor account IDs, usernames, tokens or fields. SAME, CHANGED, MISSING, GC-RECLAIMED are counted. A reference transition is a possible account/session change, not verified account identity. An observed change disarms session recovery. Account-diagnostic mode can record transitions without aborting itself.

## Other D08 improvements

The D08C ledger prints bounded enum-only composite reasons (owner/page/pre-menu). The D3r trace splits `receiver` from `method.getDeclaringClass()` categories; runtime classes are not assumed equal to static `X.02ib`. Feed cohorts are separately counted; custom icons without exact Repost text do not imply feature absence.

No Feed Repost action, Friend avatar mutation, auto-like, auto-comment, background sync, generic media-ID access, configuration key extraction, added permissions or auto-merge. Original R0–R8 retained. Source/hosted build are not device qualification.
