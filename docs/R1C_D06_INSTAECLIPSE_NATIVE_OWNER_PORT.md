# InstaActions R1C D06 — InstaEclipse-derived native menu ownership

**Current status (VB01): SOURCE_PREPARED / VB01_CI_NOT_RUN / VB01_DEVICE_NOT_RUN / VISIBLE_REEL_IDENTITY_NOT_VERIFIED.**

**Historical HF01 status:** CI passed at `28490e7558c4b0ccd15607d8268f9d3e12cd8d32`;
user-reported FAB A1/A2/B1/B2 shows 4/4 `CONTEXT_DIRECT_MATCH`, with no
budget or ambiguity failures. These are direct-menu-context matches only.

**PORT01 outcome:** User FAB A1/A2/B1/B2 evidence with 5 Media-typed callbacks,
all 5 D06 `BUDGET_EXCEEDED`, while D05 reference continuity was stable. PORT01
hosted CI passed on `22a7a11b859e7709852628654ab0ce540265e9ee`
(repository `43112900092`, Android `43112900103`), but its owner resolution was
**INCONCLUSIVE**. This does not identify which budget was exhausted.

## Verified source pins

- InstaActions parent: `196d2de7a9b38bc26a40ee29a89c4321911b9e1f` (FAB01, Android + repository hosted CI success).
- InstaEclipse: `ReSo7200/InstaEclipse` `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d`.
- Relevant upstream algorithm: `app/src/main/java/ps/reso/instaeclipse/mods/media/SelectedMediaAdapter.java` methods `ownedDirectMedia`, `ownedMenuMedia`, and `uniqueDirectTypedField` (lines 131–170 at pinned upstream revision).
- Upstream GPL-3.0-only; adapted file and `third_party/InstaEclipse/NOTICE.md` retain attribution and license. If binaries are distributed, ensure **combined-work GPL compliance**; see notice.
- IG 455.0.0.8.72 APK SHA-256 `fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6`, size 238346353 bytes, `X.07EF.A05(X.03jS,Media):ArrayList` private final instance method. Strict D04 pin retained.

## What was actually ported and where

The `R1CD06MenuOwnership` utility translates **unique direct typed-field identity ownership** into a typed `Verdict`. It checks only exact declared `Media` fields of the native menu builder *receiver* and its explicit context parameter, using separate bounded class-depth (12), declared-layout (2048 total, 1024 per class), and exact-Media candidate (16) budgets; it rejects ambiguous, mismatched, inaccessible, and truncated observations. Like upstream, it doesn't choose arbitrary preloaded Media or accept an unrelated object graph's first item. No Media fields/methods are read or logged. `field.get(holder)` returns a reference only for equality against the menu callback's own exact `Media` argument. No references persist beyond the callback.

**Limits:** this is not a port of InstaEclipse's media ID, download/snapshot, playback, comment, or menu-mutation functions. `ReelDownloadHook.installReduceOptionsListPatch` is only a candidate overlap with `X.07EF.A05`, **not a proven descriptor match**. No independent view binder is installed and a direct owner match does *not* establish which Reel is visible. R2/R3 actions remain disabled.

`R1CD04PassiveProbe.Counter` reports bounded content-free `INA_R1C_D06_OWNER ordinal=N result=ENUM visible_media=unverified media_id=excluded actions=off`. It passes only verdict enum to the existing draggable `IA` FAB alongside legacy D05 enum counts. The report retains `INA_D05_FAB_REPORT_V1` for comparability, appends `INA_D06_MENU_OWNER_REPORT_V1`, and explicitly says `D06_VISIBLE_BINDING=NOT_VERIFIED`. `D06_OWNER_RESULT=FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY` is **menu-only evidence**, not a pass on real visible identity. Unknown outcomes remain inconclusive.

D05 A1 baseline is now reset **atomically with starting the FAB capture under the same Counter lock**. This addresses the prior A1 pre-capture `DIFFERENT_REFERENCE`, without rewriting the historical capture report.

## Device acceptance — no Termux/Instagram switching

1. After the new direct-child commit has both exact-SHA CI workflows green, use the ZIP's `install` command once. Leave Vector scope unchanged, with InstaActions and InstaEclipse **both enabled**.
2. Force-stop and relaunch Instagram. Confirm InstaEclipse's normal native Reel menu entries still work, and the draggable `IA` FAB is visible. Check `Hook: INSTALLED` on Instagram 455 only.
3. Tap `Start A1`; open Reel A menu once; use IA → Next. Reopen **same** Reel A (A2), advance. Swipe to a genuinely different Reel B (B1), open its menu and advance. Reopen same Reel B (B2) and Finish.
4. Copy safe report from the FAB. Expect `A1_BASELINE=1` in a clean session. Examine `A1/A2/B1/B2_D06_*` counts. `OWNER_DIRECT_MATCH`, `CONTEXT_DIRECT_MATCH`, `OWNER_AND_CONTEXT_MATCH` are positive **owner-only** signals. `MISMATCH`, `AMBIGUOUS`, `NO_DIRECT_OWNER`, `REFLECTION_FAILED` and `BUDGET_EXCEEDED` are failures or inconclusive evidence, not prompts to weaken the guard.
5. Verify no Instagram crashes/ANRs, no InstaEclipse duplicate/replaced menu entries and no action side effects. Report any ref-only vs owner result disagreement.

A **separate exact IG455 active-view-to-Media binding** is still required before D06's original media-identity outcome can be declared qualified. Source tests and hosted build alone do not qualify it.

## CI and ownership gates

- Devtool checks a clean exact FAB01 HEAD with an atomically applied native schema v2 overlay, hash-pinned additions/modifications, full repository Python tests, and a single direct-child commit. It refuses remote movement and doesn't force push, reset, stash, merge, or change Vector scope.
- Hosted Android Gradle builds are GitHub Actions only. APK verifier must see `R1CD06MenuOwnership`, `INA_R1C_D06_OWNER`, and `INA_D06_MENU_OWNER_REPORT_V1` in DEX.
- The launcher `status`/`install` only accepts *both* exact new SHA workflow successes and a checksum-verified APK. `adb install -r` only; never silently uninstalls.
- Rooted Android 16 API36 + libxposed API102 remain the target; no Android Lite or other package scope.

## D06 HF01: Bounded layout hotfix, InstaEclipse kept enabled

PORT01 treated **all** declared obfuscated fields as consuming one shared 64-field
budget, so a structurally wide menu owner could hit the bound even when there
were few or no direct Media fields. HF01 separates (a) total declared fields,
(b) per-class width, (c) superclass depth, and (d) **exact Media-typed instance
fields actually relevant** to the ownership rule. Each layer remains bounded.
No field values outside exact `Media` typed references are read. On any cap,
the full owner check is still refused: `OWNER_*_LIMIT` or `CONTEXT_*_LIMIT`
are content-neutral and specify the failing holder/limit without field names.

An `OWNER_DIRECT_MATCH`, `CONTEXT_DIRECT_MATCH` or `OWNER_AND_CONTEXT_MATCH`
on a single callback is never allowed to conceal a refusal in the same capture
stage: all callbacks in all four valid user-labeled windows must be positive to
report `FOUR_WINDOWS_DIRECT_OWNER_MATCH_ONLY`. Even that does not prove
independent visible-Reel identity; the action gate remains OFF.

**Procedure:** after HF01 commit passes exact-SHA hosted CI, use existing
Python/Rich ZIP `install` command; leave InstaEclipse enabled, restart Instagram,
repeat IA FAB A1/A2/B1/B2 and paste the safe report. No Vector scope toggle,
no Termux switching during captures. If any budget remains, its precise
`OWNER_*_LIMIT` / `CONTEXT_*_LIMIT` field identifies the next structural fix.


## VB01 follow-up (new revision; source only, CI/device NOT RUN)

HF01's on-device user report after the independent field-budget repair records
`CONTEXT_DIRECT_MATCH=1` in A1/A2/B1/B2, without mismatches or limit errors.
That is menu-context ownership only. Historical PORT01 failure remains in this
document for provenance. Both HF01 CI workflows passed at
`28490e7558c4b0ccd15607d8268f9d3e12cd8d32`.

VB01 adds a **separate**, read-only active-view candidate based on
`ReelsControlsHook.installScrollLock` (ClipsViewPagerImpl's ViewPager2) and
`ReelDownloadHook.findCarouselIndexFromView` (reject multiple matching visible
items). It locates exactly one large, centered **vertical** ViewPager2, confirms
exactly one centered RecyclerView page with adapter index equal to the pager's
`getCurrentItem()`, then compares the menu argument against the unique exact
Media-typed field found in visible page-owned Android View objects. It does
not search arbitrary Instagram controller graphs or inspect Media contents.

The `INA_D06_VISIBLE_PAGE_REPORT_V1` FAB section provides bounded count-only
verdicts and always includes `D06_ACTIVE_VIEW_BINDING=NOT_QUALIFIED` even if
all four windows report `VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE`. Lack of a
direct View Media field is an **inconclusive observation**, not proof the media
is absent or an instruction to weaken owner/ambiguity constraints.

Keep InstaEclipse ON throughout. For VB01 device capture use IA →
A1/A2/B1/B2. Test the stable A/A and B/B sequence first, then separately
rapidly swipe away and back, and verify an ambiguous/preloaded page never
gets a positive verdict. No UI/network/action mutation. R1C remains unmerged.


## D06 DD01 — single-capture deep failure localization (unqualified)

- Derived from VB01 source at exact HF01 head `28490e7558c4b0ccd15607d8268f9d3e12cd8d32`.
- The IA FAB takes up to **12 bounded content-free pager preflight snapshots per stage**, from the Android main thread. They are independent of menu callback threading and never carry a Media object, identifier, view class name, view text or URL.
- Each A/B window exports `INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1` with sample counts, reasons, maxima for visible-view/pager/recycler/page counts, and scan latency. This helps separate "no vertical pager exists" from "callback thread off main" and "Media not held on page views".
- VB01 synchronous media-match probe now distinguishes scan time, view count, class depth, declared fields and Media-field candidate budget failures. It still refuses ambiguous selection and does not use deferred media objects.
- **No proof of visible Media** from structural snapshots. All action hooks remain OFF and InstaEclipse remains enabled.
- Expected device workflow: ONEPASS build/install, force-stop/restart Instagram once, IA A1/A2/B1/B2, copy entire report. No Termux switching during the capture.
- Negative controls to perform after first report: rapid swipe A→B, scroll past preloaded neighboring Reel, back to A, Instagram orientation change if supported. The report must not confuse ambiguous or stale page evidence with identity.
