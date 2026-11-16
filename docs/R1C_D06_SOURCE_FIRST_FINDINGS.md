# D06 SF01 — InstaEclipse-first conformance and consolidated findings

## Source pins

- InstaEclipse `ReSo7200/InstaEclipse@e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d` (GPL-3.0-only).
- InstaActions baseline `Mikeyphw/InstaActions@28490e7558c4b0ccd15607d8268f9d3e12cd8d32`.
- Instagram 455.0.0.8.72 APK SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6` / bytes 238346353.
- `ReelDownloadHook.installReduceOptionsListPatch`: `java.util.ArrayList` return, `MediaOption$Option.PLAYBACK_CONTROLS` and `.UNSAVE` field-use fingerprint. Source-confirmed.
- `ReelsControlsHook.installScrollLock`: marker `android_purge_26_q3_ClipsViewPagerImpl_getViewAtIndex`; its declaring class has an AndroidX `ViewPager2`-typed field. Source-confirmed.
- `SelectedMediaAdapter.ownedDirectMedia` and `uniqueDirectTypedField`: exact type + reference uniqueness + ambiguity rejection. Already adapted as D06 menu-context ownership.

## Verified vs unverified

| Boundary | Evidence | Status |
|---|---|---|
| D04 exact IG455 menu callback | Physical pass in earlier R1C runs | VERIFIED at menu observation only |
| D05 A/A B/B weak-reference continuity | User's four-window report | VERIFIED for temporal continuity |
| D06 direct context owner | All four `CONTEXT_DIRECT_MATCH` and zero failure categories | VERIFIED for menu context ownership only |
| Reel option-list builder equals D04 method | Source describes compatible fingerprint; runtime DexKit comparison in SF01 | NOT_RUN until new exact APK scan |
| Native Reels binder string on IG455 | Upstream marker from ReelControls; SF01 checks APK and one ViewPager2 field | NOT_RUN until scan |
| Visible-page direct Media | DD01 candidate probe + FAB structural preflight | NOT_RUN / UNVERIFIED |
| User-visible Reel identity, action safety | No independent physical proof | NOT_VERIFIED / ACTIONS_OFF |

## Why this is more targeted than DD01

SF01 checks two exact *upstream algorithm signatures* against the SHA-gated IG455 APK, instead of inventing alternative Reel models. Both checks run once in a low-priority DexKit worker **after D04 exact hash verification** and have no Instagram mutation hooks. The native menu match uses exactly the same two upstream enum-field-use keys; it refuses multiple matches rather than trusting upstream's first hit. It then compares a unique descriptor against `R1CModernReelsPolicy.EXACT_DESCRIPTOR` (the verified D04 `X.07EF.A05` signature). The pager check uses the exact upstream deobfuscation marker and checks the declaring-class direct ViewPager2 field.

The IA FAB shows `menu=` and `pager=` statuses and appends `INA_D06_INSTAECLIPSE_SOURCE_CONFORMANCE_V1` to the **existing combined report**. No more A1/A2/B1/B2 repetition is required merely to diagnose upstream method equivalence. A four-window capture is only relevant to a subsequent on-screen association test.

## Reporting and acceptance

- `MATCH_EXACT_D04` = InstaEclipse source fingerprint resolves uniquely to the very same D04 method in this exact IG455 APK; **does not prove InstaEclipse currently hooked it**.
- `DIFFERENT_METHOD` = the menu fingerprint resolves uniquely but to another method; no method overlap can be claimed.
- `UNIQUE_WITH_VIEWPAGER2_FIELD` = one native pager marker candidate with the upstream field shape; **does not prove it is the current visible pager**.
- `AMBIGUOUS`, `NO_MATCH`, `QUERY_FAILED`, or `REFLECTION_FAILED` = conformance unverified; no budget relaxation or guessing.
- `TARGET_APK_EXACT_HASH_GATE=true` is D04's observed exact-version/hash/shape and installed-hook gate, not proof InstaEclipse is enabled.
- No account data, Media fields/getters/IDs, URL, captions, class/method descriptor dump, or raw logcat in report.
- No automatic APK download on failed CI, no force push, no auto merge or scope change.
- Prior device capture is retained as historical evidence, not silently reclassified as source-first device qualification.

## Findings-first report layout

`INA_D06_CONSOLIDATED_FINDINGS_V1` appears **before** the historical D05 and full DD01 counters. It lists the exact IG455 hook gate, upstream source/Dex conformance, the current capture's D05/D06 owner/viewer verdicts, one `NEXT_REQUIRED_CONFIRMATION`, and explicit unverified identity / disabled actions. The legacy report sections remain available for forensic analysis. If the native source scan has not completed, the summary says so instead of inventing a positive finding.


## HF02: confirmed D06 callback-vs-preflight disagreement

User-supplied SF01 device report: 39/39 main-thread structural samples saw
one eligible vertical ViewPager2 and one centered page (2 pagers total), but
all four menu callbacks returned NO_VERTICAL_PAGER. D05 was baseline/same/
different/same and D06 context owner matched all 4. This **does not** show a
missing pager; it is consistent with the menu transiently obscuring the pager
from the callback's visibility-filtered traversal. The source confirms both
scans use isShown() and getGlobalVisibleRect(), whereas the upstream native
binder's ViewPager2 declaration was already exact-IG455-DEX qualified.

HF02 adds bounded, stage-local weak references to the **pre-menu** page's
AndroidX pager/recycler/page. The callback independently verifies same
activity, stage, age <=3000ms, same currentItem and adapter position and
same page child. It optionally compares direct typed Media fields on that
page. It does not read a Media ID, remember Media, assert live visibility or
enable any action. An absent field is NO_DIRECT_MEDIA, not proof of mismatch.

Fail-closed: stale/index-changed/recycled/ambiguous/scan-limited evidence is
unqualified. Any apparent candidate remains NOT_VERIFIED until independent
active-page lifecycle/selector binding is established. The original fresh
menu-time view scan and all D05/D06 owner evidence remain separately reported.

Only new field-device test needed: one A1 window and one Reel menu callback;
copy IA report. A2/B1/B2 are unnecessary for diagnosing the visibility
transition, though remain available for regression. InstaEclipse stays enabled.
