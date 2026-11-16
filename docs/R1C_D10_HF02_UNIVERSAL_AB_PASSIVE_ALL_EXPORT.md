# D10 HF02 — shared A/B comparison, Passive and Export All

## Why passive wasn't visible
The previous UI exposed only five manual diagnostics. D04/D07 passive hooks existed, but their observations were spread across mode reports and account signals. This change adds a passive overview without new Instagram hooks.

## One active mode
Select Feed, Avatar, Reels recovery, Native A/B, Media, or Account change. Select Begin; then Start A, perform feature-specific observations, Stop A, switch account manually with Repost OFF, confirm the switch, Start B, repeat identical observations, Stop B, Finish. A/B labels are manual only. The Reels trial remains restricted to the dedicated Reels tab; never arm it on B. D05 A1/A2/B1/B2 mean Reels in one account and must be repeated on each account.

Passivity is automatic and nonexclusive. Switching an active diagnostic mode is refused until Finish or Abort; an incomplete A/B diagnostic must be Aborted, not falsely Finished.

## Reports
Copy All places a single text report in the clipboard. Export All opens Android's text/plain share sheet with the same content. It contains bounded module counters and completed/aborted/checkpoint summaries since Instagram process start, up to 28 archived records and 180,000 chars, truncation/loss counters disclosed. It is not full logcat, is not a disk backup, and is lost after process death. Sensitive account IDs, usernames, tokens, media IDs, captions, View contents and bitmap data are not collected by the new diagnostics.

## Avatar limitations
Three manually selected surfaces (Friends, liked-by, reposted-by) record count of visible ImageView and square-size candidates and exact fixed English/Portuguese labels. Generic small square imageviews are not proof of actual avatars, and counts aren't linked to a unique media item. No render overrides.

## Qualification
Legacy Reels-tab native Repost user-confirmed; formal D07C acceptance still pending. Feed Repost, avatar identity, D06 media identity, and automatic account-switch identification remain NOT VERIFIED. Always turn OFF before switching accounts; no callback means unverified.
