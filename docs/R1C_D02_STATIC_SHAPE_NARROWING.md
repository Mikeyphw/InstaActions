# R1C-D02 — Source-backed, read-only signature narrowing

Status: **APK-AWARE_HF01_SOURCE_AND_OFFLINE_TESTS_PASS; HOSTED_CI_AND_DEVICE_NOT_RUN**.

See `docs/R1C_D02_HF01_APK455_STATIC_EVIDENCE.md` for verified APK hash, 202-method Story owner fix, and the modern Reels candidate distinction.

Verified R1C-D01 device baseline: Instagram `455.0.0.8.72`, model classes `3/5`, story-page and feed-options each one raw marker candidate, Reel-options string marker **32** matches, scan failures **0**, screen navigation Feed -> Reels -> Story -> Feed. No media IDs observed or captured.

## InstaEclipse source references

Source: `ReSo7200/InstaEclipse` commit `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d`:
- `mods/media/ReelDownloadHook.java`: use the controller string to find declaring classes; structurally identify `void(Media, ButtonAdder)` where the ButtonAdder declares `(Context, OnClickListener, String, int)` (no naive assumption that each string reference is a binder).
- `mods/media/PostDownloadContextMenuHook.java`: menu creator method has a `MediaOption$Option` and `ArrayList` shape; D02 only checks declaration shape, never invokes or hooks it.
- `mods/media/StoryDownloadHook.java`: `ReelViewerFragment.onCurrentActiveItemBound` identifies a candidate per-page binder; declaration shape is weak until live page events are proven.
- `mods/media/FeedVideoDownloadHook.java`: Instagram 446+/447+ removed the split `MutableMediaDictIntf` / `LiveTreeMediaDict`; missing both on Instagram 455 is expected.

## Boundaries

- Only `Class.forName(name, false, loader)` and Java declared-method reflection; no constructors or getters invoked and no Xposed hooks on Instagram-internal methods.
- Bound 64 raw DexKit refs, 48 distinct declaring classes, and 192 methods per class. A truncated scan reports `BOUNDED_INCOMPLETE`, not success.
- Counts only: no classes, obfuscated method names, captions, usernames, URLs or actual media identifiers in logs.
- New marker `INA_R1C_SHAPE id=... raw=... owners=... candidate_methods=... strong_methods=... result=... truncated=... media_id=unavailable binding_hooks=off actions=off`.
- `UNIQUE_SIGNATURE_CANDIDATE` is **not identity verification** and does not authorize binding/action hooks.
- DexKit R1A version allowlist unchanged. R1C inventory is strictly limited to IG455.

## Device acceptance

After hosted CI, capture `INA_R1C_MODEL`, `INA_R1C_ANCHOR`, `INA_R1C_SHAPE`, `INA_R1C_SUMMARY` plus R1B stable navigation markers for Feed/Reels/Story/Feed. Ensure no read-only regressions or crashes. Do not merge R1C until D02's shape evidence is reviewed and actual media instance binding is independently qualified.
