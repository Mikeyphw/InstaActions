# InstaActions — Project Roadmap

Version: 1.0 (2026-11-08)  
Status: R0 hosted CI passed; R1A read-only probe source prepared, device qualification pending  
Repository target: `Mikeyphw/InstaActions` (private)

## Vision

Add a configurable, context-aware quick-actions button to Instagram Reels and posts, later extending to stories and other surfaces as supported. A short tap runs a selected single action or macro; a long press opens an action panel. A separate companion app manages macros, destinations, comment pools and diagnostics. InstaActions is independently installable and must coexist with InstaEclipse without overwriting its menu or relying on its internal classes.

## Constraints and non-goals

- Vector 2.2+ and libxposed API 102 first; classic API 82 compatibility is not in initial scope.
- Android 16 rooted real-device qualification; compatibility with Android 9+ may be added after validation.
- Scope only `com.instagram.android` unless the user explicitly opts into other packages later.
- Instagram 451.0.0.20.80 is a baseline; Instagram 453 is experimental pending tests.
- Instagram Lite 534 requires a separate feasibility assessment and is **not** implicitly supported.
- Never hardcode UI coordinates, Instagram credentials or DM session cookies.
- No background mass likes, unsolicited bulk DMs, comment flooding, scraping or unattended posting.
- No local Gradle builds: GitHub Actions owns all Android builds and instrumentation test APKs.
- No mandatory translations, larger fonts or accessibility redesign.
- Each feature needs a kill switch, compatible fallback and structured status reporting.

## Release and quality contract

Each milestone requires: reviewable source changes; consolidated and source-pinned Devtool overlay (if using Devtool); deterministic unit tests; hosted Actions checks; compatibility reports; user-facing notes; and a branch/commit/CI evidence trail. Hooked behavior requires device evidence rather than an APK build alone. Fail closed on unknown Instagram versions, missing method matches, incompatible UI trees and conflicting hooks. Avoid duplicate execution and record why actions are unavailable.

### R0 — Foundation and repository qualification

Deliver: private repo, bootstrap docs, CI contract checks, minimal Android app/module skeleton, companion launch activity, API 102 entry (`META-INF/xposed/java_init.list`), `module.prop`, `scope.list`, build settings, version info, encrypted-free local settings prototype, safe non-hooking `onPackageReady` diagnostics, hosted debug APK and host-side JVM tests.

Acceptance: Actions compile/assemble produces checksummed installable APK; package/scope metadata verified from APK; module visible in Vector; Instagram main process load logged without altering Instagram UI; InstaEclipse loads separately; diagnostics avoid private user content. No runtime write hooks yet.

### R1 — Read-only Instagram capability discovery

Deliver: DexKit-driven per-version adapter registry, cached method probes, process/package guard, context model (post/reel/story and media identity), lifecycle tracking, feature-status screen, read-only hook inventory. Probes must be bounded and must not run on unrelated processes.

Acceptance: reliable detection and media-context transitions for tested surfaces; no crash/ANR with InstaEclipse enabled; unknown versions fall back to no-op. Document matches and negative findings per IG version.

### R2 — UI foundation

Deliver: configurable floating action button, tap/hold gestures, action panel, screen-specific show/hide conditions, button position/opacity, collision avoidance with Instagram/InstaEclipse overlays, dynamic layout compatibility and settings persistence.

Acceptance: no touch interception outside button/panel, restored state after recreation, correct visibility on Reels/posts, no menu conflict when long-pressing Instagram Explore.

### R3 — Basic single actions

Deliver: like/unlike, copy post/reel URL, save/unsave, configurable speed cycling and native repost trigger (where available). Prefer Instagram's existing action dispatch; avoid reimplementing networking. Respect InstaEclipse's settings and disable duplicate controls rather than fighting them.

Acceptance: verify all actions, idempotence and error reporting on test content; no automatic network replay after failure. Speed correctly restores/updates on media transitions. Repost explicitly marked unsupported on versions without native action.

### R4 — Saved quick-send destinations

Deliver: contact/group chooser with account-specific saved aliases, native share sheet fallback, recipient identity confirmation, optional confirmation before send and last-used destination. Do not store tokens or access DM databases if native UI can be used.

Acceptance: verified intended recipient and media before initiating each send; repeated button taps cannot duplicate a send. If direct-send hook is unverified, open native share UI prefiltered where possible, or open unfiltered share UI without claiming direct-send capability.

### R5 — Configurable random comments

Deliver: user-defined editable comment pools, weighted or uniform random selection, preview or single-tap mode, per-media suppression of repeat posts, action cooldown and edit/import/export. Explicitly separate `draft chosen` from `comment submitted` in UI/logs.

Acceptance: one user tap creates at most one comment; no unattended loops, bulk posting, cross-account leakage or spam-oriented scheduling. Unsupported direct submit falls back to prefilling native comment UI.

### R6 — Macro engine

Deliver: sequential action graph (simple ordered steps first), per-step failure policy (stop/skip), action timeout and cancellation, contextual filters (Reels vs posts), reusable profiles, local backup/restore and an execution history without message bodies. Distinguish local actions from external irreversible actions.

Acceptance: deterministic unit tests for ordering, guard conditions and duplicate suppression; failures do not trigger replay of already-completed sends/comments/reposts; user sees completed/failed/skipped results.

### R7 — Compatibility and regression qualification

Deliver: hosted emulator + physical device acceptance, Instagram 451 baseline and Instagram 453 experimental matrix, InstaEclipse enabled/disabled parity checks, orientation/lifecycle/reentrancy tests, instrumentation test APK and failure artifact collection. Research Insta Lite separately without implying support.

Acceptance: documented PASS/FAIL/UNSUPPORTED per action per version; no bootloop, crash regressions, UI obstruction or mutual-hook interference. No release based solely on synthetic emulator results.

### R8 — Private release and maintenance

Deliver: private release APK, GitHub Actions provenance/checksums, versioned changelog, rollback instructions, export format version, hook inventory snapshot, debug-log redaction and upgrade regression checklist.

Acceptance: install, enable, scope, relaunch and smoke-test steps confirmed on target device; version/commit match release; rollback works; both modules remain enabled.

## Optional backlog (not part of completion gates)

- Instagram Lite after a dedicated hook inventory.
- Classic LSPosed API 82 / older Android support.
- Contextual widgets and optional Tasker entry point (local-only and explicit).
- UI presets per Instagram account and configurable toolbar placement.

## Initial feature matrix

| Feature | R-target | Expected execution model | Fallback |
| --- | --- | --- | --- |
| Like/unlike | R3 | Instagram native action | Disabled with explanation |
| Speed cycle | R3 | Existing player controller | No-op |
| Copy link | R3 | Current media context / share intent | Native copy/share |
| Save/unsave | R3 | Instagram native action | Disabled |
| Repost | R3 | Instagram native repost | Disabled |
| Direct send to saved contact/group | R4 | Verified native share dispatch | Native share sheet |
| Random comment | R5 | Select one user-authored string | Prefill comment composer |
| Multi-action macro | R6 | Synchronous guarded state machine | Stop / skip per policy |

## Recommended branch sequence

- `main`: bootstrap baseline only.
- `feature/ina-r0-api102-foundation-20261108`: Android project / modern Xposed skeleton.
- `feature/ina-r1-hook-discovery-YYYYMMDD` etc., per phase.
- Consolidated hotfix branches only when needed, e.g. `fix/ina-r3-hf01-YYYYMMDD`.

## Definition of 'done'

No milestone closes without a named commit SHA, successful required Actions run IDs, a compatibility matrix update and the relevant on-device test record. Tests that cannot be run must be labeled `NOT_RUN` (not `PASS`).

## R1A implementation note (2026-11-08)

R1A is read-only background DexKit candidate discovery for five upstream markers.
R1B remains responsible for confirmed visible media context and ID tracking.
No R1 acceptance gate closes until device evidence is recorded with InstaEclipse active.
