# R1C D03 — Independent modern Reels descriptor inventory

Status: DEVICE_STATIC_DESCRIPTOR_PASS (R1C D03 SHA `9de5c417257c4fbbb0a22dfac432a6620f0bca3c`; no runtime binding qualification). Source pinned to `5f87ef4ee637b1470d03ac49ad9bc406dbfd181e`.

## Why this exists

D02 at commit `5f87ef4ee637b1470d03ac49ad9bc406dbfd181e` passed repository and Android workflows and was inspected on Instagram 455.0.0.8.72 (device run 42750189318). Result: Story unique strong signature; Feed unique strong signature; Reels raw 32, 12 owners, one weak method but **zero modern list candidates**. No failures, no crash indicators in the diagnostic capture; Feed/Reels/Story/Feed navigation still stable.

D02's structural scan checks the *declaring classes of methods referencing* `ClipsOrganicMediaItemViewMoreOptionsController`. A modern list builder identified from the APK need not be among those classes. The static APK audit (SHA256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`) separately found `LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;`.

D03 makes an independent, bounded DexKit metadata query for exactly the **IG455 obfuscated owner** and `ArrayList` return with two parameters, then compares candidate descriptors against the pinned APK signature. It emits `INA_R1C_DIRECT_REELS` with **counts only**. A single exact metadata match is `IG455_EXACT_DESCRIPTOR_CANDIDATE`, not a live-media binder. A miss/incomplete scan never promotes capability.

## Safeguards

- Do not use `getMethodInstance()`, `Class.forName`, reflection, `invoke`, or Xposed hooks in the D03 direct query.
- Only process Instagram 455 via existing `R1CDiscoveryPolicy.mayInventory` guard.
- Limit returned descriptors to 64 inspected; explicit `INCOMPLETE` on overflow.
- Do not log media IDs, user content, media object values, obfuscated descriptors, captions, URLs, comments or account data.
- Existing R1A DexKit version allowlist remains unchanged; actions and binding hooks remain disabled.
- The independent probe intentionally does not change D02 `modern_list_methods`, which still measures its original string-owner cohort.

## Device acceptance

Run the Python/Rich probe after **the exact commit** passes Android CI. Expected one `INA_R1C_DIRECT_REELS` and unchanged `INA_R1C_SHAPE` Story/Feed/Reels behavior. Confirm no `FAILED`, no `BOUNDED_INCOMPLETE`, no nav regressions, and no app crash indicators. **Do not merge R1C or enable binding** on signature evidence alone; next phase must prove active view-to-Media association and lifecycle invalidation.
