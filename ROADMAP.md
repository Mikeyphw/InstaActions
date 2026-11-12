# InstaActions — canonical R0–R8 roadmap and current source status

Last source baseline: `Mikeyphw/InstaActions` R1C D04 commit
`b246b0f38b38623bdb77987d9fb11318f5c95709`.
R1B PR #3 is merged. R1C is unmerged. **D05 HF02 is a candidate
source change; it must pass exact-new-SHA GitHub Actions and on-device
manual InstaEclipse ON/OFF acceptance before being qualified.**
Repository visibility is public as checked on 2026-11-10; private personal
use and a possible private APK release are separate from repository visibility.

Important status: D04 runtime proof confirms Media *type* during the Reel
menu only. D05 weak-reference continuity is NOT visual Reel identity, and
D06 remains mandatory. No action dispatch has been enabled.

---

# Full InstaActions roadmap — from foundation to release

## Product vision and constraints

**InstaActions** is an independent libxposed API102/Vector module for **personal, explicitly invoked, context-aware Instagram quick actions**. It should coexist with InstaEclipse rather than replacing it or duplicating its already available options. Intended targets are Home Feed posts, Reels and Stories, with precise media-identity and active-screen gating. Android user device: Android 16/API 36 on rooted Termux + KernelSU + Vector/LSPosed; `adb -s localhost:5555` is the known working target (do not use failed `emulator-5554`).

No mass automation, no autonomous engagement, no background comment spam, no account content in diagnostic logs, no unrestricted/guess-based Instagram-internal hooks, no auto-merge before acceptance. Source docs and tests must stay aligned with implementation.

## R0 — Foundation [MERGED, COMPLETED]

- Android application/module skeleton; `com.mikeyphw.instaactions` package; Instagram scope `com.instagram.android`; libxposed API 102; Vector 2.2+.
- Hosted GitHub Actions Android build, artifact SHA-256, metadata and APK contract checks, baseline instrumentation.
- Debug install/reinstall workflow handles signing changes. R0 PR #1 merged.

## R1A — Read-only hook discovery [MERGED, COMPLETED]

- Version-gated DexKit marker inventory and lifecycle hooks; original allowlist IG 451/453 remains independent of R1B/R1C.
- Hook discovery and Vector logging diagnostics, no mutation. PR #2 merged.

## R1B — Screen and viewer detection [MERGED, DEVICE-ACCEPTED]

- R1B HF01–HF03: stable Vector diagnostics, detach/attach evidence, passive scan decoupled from strict R1A DexKit allowlist for read-only screen hints.
- HF04: source-backed Feed row ID checks; diagnostic evidence showed Feed `UNKNOWN` while Reels/Stories were detected; Instagram uses Litho.
- HF05: separate stable navigation (`HOME_FEED`, `REELS`, `STORY`, `UNKNOWN`) from actual media/viewer inference (`REELS`, `STORY`, `POST`, `UNKNOWN`). Explicit selected Feed tab, main Feed bar, two samples >=1000ms; fail closed on contradictory/truncated signal, invalidate on Activity pause/destroy.
- Device test successfully observed Feed → Reels → Story → Feed; CI green; PR #3 merged. **Screen recognition is not media ID verification**.

## R1C — Source-backed Media model and runtime association [IN PROGRESS, UNMERGED]

### D01 — Candidate inventory [DEVICE DISCOVERY PASS]
- IG455 version-only, read-only class metadata + 3 DexKit markers; 3 of 5 model classes found; Story and Feed markers each unique, Reels marker raw 32; no failures. No live binding.

### D02 HF01/HF02 — APK-aware static shape qualification [DEVICE SHAPE PASS]
- Static APK says Story target has 202 methods; fixed incorrect bounded-incomplete interpretation.
- Story unique strong signature; Feed menu unique strong signature; raw Reels marker 32 across 12 owner classes, one weak candidate, no modern-list candidate by that route.
- Packaging repaired HF02: executable Python ZIP must contain **separate nested canonical Devtool ZIP**, never be passed directly to Devtool.

### D03 — Exact modern Reels descriptor [DEVICE STATIC PASS]
- `LX/07EF;->A05(LX/03jS;Lcom/instagram/feed/media/Media;)Ljava/util/ArrayList;`; exact=1, raw=1. This method is **PRIVATE FINAL INSTANCE** (DEX flags `0x12`), not static.
- No Media ID/binding claims.

### D04 — Passive runtime callback [DEVICE ACCEPTANCE PASS FOR TYPE/SCREEN ONLY]
- Installed APK SHA `fb6ca8...` and size 238346353 gated before installing libxposed observer.
- One read-only interceptor on method `X.07EF.A05(X.03jS, Media)`; `proceed()` once, preserve return/exception, observe parameter type and stable R1B screen, bounded <=64 events per process.
- Actual device during Reels overflow: **4 callbacks, all Media-typed; 3 on stable REELS, 1 on transitional UNKNOWN**. No reported crash indicators. No callbacks in Feed/Story test stages. D04 exact-SHA Android+repository CI passed.
- **No current visible media verification**.

### D05 — Weak-reference continuity [SOURCE PACKAGE PREPARED, NOT APPLIED]
- Add `WeakReference`-only previous Media object comparison while stable REELS; outcomes `BASELINE`, `BASELINE_COLLECTED` (GC), `SAME_REFERENCE`, `DIFFERENT_REFERENCE`, `SKIPPED_UNSTABLE`, `SKIPPED_WRONG_TYPE`.
- No IDs/fields/hashCodes/toString; no strong retention; invalidation on Activity pause/destroy or non-Reels; existing <=64-event bound.
- Test Reel A menu twice, then genuinely different Reel B menu twice in one uninterrupted foreground window (actual probe uses 110 seconds; old doc says 90 seconds and should be corrected as stale).
- **NEW PRIORITY:** controlled InstaEclipse-enabled vs InstaEclipse-disabled comparison, with Instagram restarted, scope checked and InstaEclipse restored. See `06_INSTAECLIPSE_COEXISTENCE.md`.
- Hosted CI, phone acceptance and proof of on-screen identity pending. DO NOT merge R1C.

### D06 (or follow-on) — True active view-to-Media association [NOT STARTED]
- Identify per-surface current binding method with source-backed signatures, e.g. Story `ReelViewerFragment.onCurrentActiveItemBound`, Feed actual row-bind and Reels modern builder/context.
- Differentiate menu-reference continuity from **currently displayed** media. A/B test with actual swipe/change, timing and independent visible state, and content-neutral diagnostics.
- Promote identity only when separate independent evidence binds an exact Media object to the foreground surface and changes/invalidation are correct. Do not log plaintext IDs; consider short-lived opaque per-process tokens only if necessary and reviewed for leakage (avoid persistence).
- Gate on exact APK/version/method shape; zero ambiguous candidates; avoid interfering with InstaEclipse's hooks; test with coexistence modes and multiple entry paths (tab, feed→Reel, story tray).
- Verify lifecycle invalidation, rapid navigation, rotation/process restart, menu close/reopen, GC, performance and crash-free operation.
- R1C PR only after explicit runtime acceptance for each claimed surface (or limited scoped capability claims). No auto-merge.

## R2 — Contextual quick-actions UI [NOT STARTED]

- Design unobtrusive context-aware button/panel with clean visual design; observe status/navigation bars, screen insets, Android 16 Material3 where appropriate. Avoid 'debug-y' UI, huge fonts or unnecessary accessibility changes.
- Display only actions genuinely available and justified for the current verified media type; grey/disable or hide when screen/media `UNKNOWN`.
- Avoid duplicating InstaEclipse Reel/post menu options; provide optional native toolbar or action sheet if useful.
- Settings: enable/disable each action, choose UI placement, coexistence behavior, debug diagnostics off by default, no PT-BR requirement for module if project uses English UI.
- UI tests: Feed, Reels, Story, overlay dismissal, navigation, rotation, split-screen, IME, theme, bars and collision with InstaEclipse. No action dispatch until R3 gate.

## R3 — User-triggered core quick actions [NOT STARTED]

- Like/unlike (explicit click, accurate current state, idempotence, rollback/confirmation), Save/unsave, Copy link/metadata (bounded and consented), Repost/share when supported, playback-speed control.
- Each action: verified Media identity, single explicit user intent, exact signature, version/ABI and UX safety gate; report unsupported as unavailable.
- Never duplicate Instagram/InstaEclipse native entries unnecessarily; prefer existing stable official intents/options when possible.
- Test actual post/Reel/Story variants, carousel slide identity, video vs photo, inaccessible/private content, transient errors, network offline, retries.

## R4 — Sharing to contacts/groups [NOT STARTED]

- Human-selected recipients and explicit confirmation; use Android/Instagram native sharing/intent or safe integration where feasible.
- Avoid harvesting contacts, private messages, group membership or arbitrary follower lists; show recipient scope clearly.
- Support one media share at a time; guard stale media context, confirm destinations and avoid accidental sends.

## R5 — Explicit single-comment workflow [NOT STARTED]

- A **single human-triggered**, contextual reply/comment from a small user-authored or user-approved candidate set, optionally randomly selected, with preview and confirm before posting.
- No automatic posting, mass engagement, hidden background scheduling, spamming or evasion of Instagram controls; block duplicate/replay.
- Never read comment threads/user messages unnecessarily; conservative error handling and accurate completion receipts.

## R6 — Safe macros / idempotence [NOT STARTED]

- User-defined sequences of supported individually qualified actions, each with clear preview, granularity, confirmation and cancellation.
- Single-target atomicity/idempotence, duplicate prevention, no unbounded loops, rate limits, temporal expiry and on-screen context validation before each operation.
- Resumable receipts without sensitive content, fail-closed on navigation or identity change, undo where semantics permit.

## R7 — Full device and CI acceptance [NOT STARTED]

- Hosted CI on exact SHA; source/test/doc staleness audit; APK provenance and signature; emulator/physical device behavior at Android 16/API 36; Vector/API102 compatibility.
- Regression matrix for Feed/Reels/Stories, Instagram version change, startup/detach, performance/memory, blacklisted apps, InstaEclipse enabled/disabled, crash-free logs and UI functional acceptance.
- App privacy/security and installation/upgrade/cold-start/reboot handling. Separate device acceptance for specific hooks from broad app/release qualification.

## R8 — Release / maintenance [NOT STARTED]

- Versioned signed APK with hashes, reproducible CI workflow, changelog, tested capabilities matrix and known Instagram build compatibility.
- Clear experimental flags and fail-closed unsupported-version path. Build/installation instructions, rollback, diagnostics privacy, no automatic unsafe update to unsupported Instagram.
- Release only after required runtime evidence and explicit user decision; no auto-merge or automatic full-gate run just because targeted CI passes.

## Success gate for any new capability

`static source candidate` → `exact-build proof` → `runtime callback observation` → `independent active view-to-media correlation` → `lifecycle invalidation/coexistence` → `UI acceptance` → `human-triggered action validation` → `release qualification`. Never skip gates or conflate them.

## D05 HF02 — pre-application replacement and fail-closed qualification

HF02 replaces the never-applied D05 HF01. The ZIP delivery is **not** deployed
by its generation; it must be explicitly invoked at D04. Its Python/Rich
launcher checks exact D04, source hashes, a clean repository, and expected
branch before applying Devtool. After Devtool it verifies the direct-parent
commit, exact file delta and SHA-256 of every committed file before pushing.
No force push, no local Gradle, no automatic merge.

The device probe first verifies the new direct-child commit's exact canonical
contents and BOTH hosted CI workflows (not the old D04 runs). It checks the
CI artifact checksums and requires manual Vector ON / OFF / restored scope
attestations. Each condition uses a separate 110-second A1/A2/B1/B2 timestamped
window; only exact allowlisted markers leave the device. InstaEclipse-only
menu rows must be observed distinctly ON, OFF and restored after relaunch.
No media IDs, usernames, captions, full logcat, or content URLs are exported.
All UI/state observations remain *manually attested*, and no hook overlap or
actual visible Media identity is claimed. D06 cannot be skipped.


### D05 HF03 — Hermetic test-import recovery (2026-11-10)

HF02 validation failed on Termux because `tests/test_r1c_d05_evidence_policy.py`
imported an unrelated Hermes `tools` package. Devtool verified rollback to D04.
HF03 replaces unapplied HF02, imports the repository policy by exact file path,
and adds a subprocess regression with `sys.modules["tools"]` shadowed.
No D05 runtime hook behavior changes; no CI/device qualification is implied.

### R1C D05 FAB01 — Instagram in-app diagnostics (source prepared 2026-11-11)

Adapt the independent, activity-local floating chip pattern from
Mikeyphw/InstaTaskManager to an InstaActions-only diagnostic panel.
Maintain InstaEclipse enabled for all tests; no mandatory Vector scope flips.
Four explicit user-controlled A1/A2/B1/B2 windows, safe live counters,
clipboard/share report and activity-lifecycle invalidation; no action or
media-identity qualification. Pending hosted CI and physical device acceptance.


## R1C D06 — InstaEclipse source-derived native menu ownership (new source stage)

- Source parent: FAB01 commit `196d2de7a9b38bc26a40ee29a89c4321911b9e1f`.
- Adapt `SelectedMediaAdapter.uniqueDirectTypedField`/`ownedDirectMedia` (GPL-3.0-only, attributed in `third_party/InstaEclipse/NOTICE.md`) for native Instagram menu builder *receiver* and explicit context argument, rejecting ambiguity, mismatch and reflection failures. No reuse of InstaEclipse's installed runtime or switching its Vector scope.
- Preserve Android API102, installed Instagram 455 SHA-256 pin, exactly one `chain.proceed()`, D04/D05 64-event cap, A/B FAB, content-free report. New status: `MENU_OWNER=UNVERIFIED` until device evidence, and **visible Media still unverified**.
- Fix D05 FAB first event by atomically invalidating its old weak-reference baseline at capture start.
- Run hosted CI on the exact next commit, physical A1/A2/B1/B2 with InstaEclipse ON, and verify NO crashes, ANRs or disappearing menu buttons; none of those gates can be preclaimed.
- A confirmed native menu owner is a *necessary*, not sufficient, condition for active Reel-to-Media binding. Actual visible item source must be independently bound before enabling R2/R3 actions.
- Keep all Gradle work in GitHub Actions, no automatic merge, no background actions, no extra module scope.

### D06 HF01 — bounded owner-layout diagnosis (2026-11-11)

PORT01 exact-SHA hosted CI passed on `22a7a11b859e7709852628654ab0ce540265e9ee`,
but user-supplied FAB evidence showed **5/5 BUDGET_EXCEEDED** ownership checks.
D05 reference continuity was healthy; independent visible-media binding remains unverified.
HF01 preserves all passive action guards and InstaEclipse ON, differentiates
owner vs context and field/depth/Media-candidate budgets, and requires every
callback in a capture window to be owner-verified before reporting a positive
four-window menu-ownership result. Device/CI qualification of HF01 is pending.


### D06 VB01 — independent visible-page candidate (2026-11-11)

- New diagnostic-only vertical ViewPager2/current-item candidate based on InstaEclipse
  `ReelsControlsHook` pager source and `ReelDownloadHook` unique-view selection.
- Menu-owned Media must match a unique exact typed field on *the currently visible
  page's view tree*, or the result is a typed, content-free refusal.
- Retain D05 four-stage FAB and D06 HF01 4/4 user-reported context-owner evidence.
- No automatic InstaEclipse toggles, additional Instagram method hooks,
  action dispatch, media field getters, IDs, URLs or stored Media objects.
- Candidate-match results are explicitly **not qualified visible-media identity**.
  CI and physical device negative cases (rapid swipes, adjacent/preloaded pages,
  return to Reel A, ambiguous pager) must be collected before promoting D06.


## D06 DD01 — single-capture deep failure localization (unqualified)

- Derived from VB01 source at exact HF01 head `28490e7558c4b0ccd15607d8268f9d3e12cd8d32`.
- The IA FAB takes up to **12 bounded content-free pager preflight snapshots per stage**, from the Android main thread. They are independent of menu callback threading and never carry a Media object, identifier, view class name, view text or URL.
- Each A/B window exports `INA_D06_STRUCTURAL_PREFLIGHT_REPORT_V1` with sample counts, reasons, maxima for visible-view/pager/recycler/page counts, and scan latency. This helps separate "no vertical pager exists" from "callback thread off main" and "Media not held on page views".
- VB01 synchronous media-match probe now distinguishes scan time, view count, class depth, declared fields and Media-field candidate budget failures. It still refuses ambiguous selection and does not use deferred media objects.
- **No proof of visible Media** from structural snapshots. All action hooks remain OFF and InstaEclipse remains enabled.
- Expected device workflow: ONEPASS build/install, force-stop/restart Instagram once, IA A1/A2/B1/B2, copy entire report. No Termux switching during the capture.
- Negative controls to perform after first report: rapid swipe A→B, scroll past preloaded neighboring Reel, back to A, Instagram orientation change if supported. The report must not confuse ambiguous or stale page evidence with identity.

## D06 SF01 — upstream source conformance (pending CI/device)

One-pass source-gated IG455 conformance using the exact `ReelDownloadHook` PLAYBACK_CONTROLS/UNSAVE list-builder fingerprint and `ReelsControlsHook` ClipsViewPagerImpl_getViewAtIndex marker, beside DD01. D05/D06 menu ownership qualified only within their original scope; no more repeat four-window test for source-shape confirmation. See `docs/R1C_D06_SOURCE_FIRST_FINDINGS.md`.


### D06 HF02 — callback-overlay / pre-menu reconciliation

Resolved ambiguity from SF01: the UI pager is found in 39/39 preflight
samples but absent in 4/4 menu-time view scans. Add weak stage-bound pre-menu
pager+page evidence and exact AndroidX adapter-position consistency, with
separate bounded direct-Media corroboration, all actions disabled. No new
Instagram hooks. No new upstream discovery. Physical qualification pending.


---

## R1C D07A + D07B — Native Repost evidence port from InstaTaskManager [SOURCE IMPLEMENTED, HOSTED + PHYSICAL PENDING]

- **D07A:** Transfer exact IG455 `X.0Ie0` selector-4 + `X.0F2B` 11/12 read-only counters, actual `X.0ApG.D3r(0xdf82522e)` receiver discovery, read-only factory/config/session diagnostics, original return and exception preservation. Reuse existing D04 APK attestation without another full hash. Keep D06 HF02 identity **NOT VERIFIED**.
- **D07B:** Manual A/B labels, explicit Reels-tab assertion, explicit ITM Repost recovery-OFF acknowledgment, fresh A/strict optional B gating, default-OFF 90 s/64 changed-check lease, one true/false eligibility change ONLY inside original selector-4. Native Android Repost action remains controlled by Instagram and user's tap. All other actions OFF. No auto-account identity or tab-origin proof; same-Activity account switch needs manual OFF.
- **D07C:** exact-new-commit Android16/Vector API102 hosted CI + physical-device coexistence/rollback/Feed negative-control acceptance REQUIRED before claiming working or merging.
- **D08:** ordinary posts and Reel-as-post parity separately, not extrapolated from dedicated Reels tab.
- **D09:** Friends tab and avatar/facepile real data and UI investigation, entirely read-only until device-qualified.
- **R2–R8:** ALL existing screen-local action, confirmation, privacy, automation restrictions, release gates, and InstaEclipse coexistence requirements retained unchanged.

### D07 UX01 — readable UI and guided Repost workflow (source overlay; NOT device qualified)

- Redesign IA panel with high-contrast status, adaptive scrolling, distinct safety/primary actions and collapsed D05/D06 tools; rewrite companion MainActivity's stale R1B-only onboarding for D07.
- Add an Android-independent guidance state model and regression tests. No native Repost method hooks, eligibility or activity-lifecycle changes.
- Keep the exact IG455 / Reels TAB / manual account and ITM OFF guard. **A must remain actively recording when recovery is armed**. B remains optional read-only comparison.
- Status: source overlay prepared. **No new exact-commit hosted CI or Android physical UI acceptance yet**. No D07C qualification, D06 media identity, posts parity or account isolation promoted.
- Next: run exact-head hosted Android/API102 build; verify portrait/landscape scrolling, contrast, status transitions, D05/D06 expand/collapse and native Repost workflow; then D07C physical acceptance.
