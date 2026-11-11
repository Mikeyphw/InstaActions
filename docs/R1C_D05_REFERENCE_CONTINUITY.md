# INA R1C D05: Reels menu Media-reference continuity (read-only)

Status: SOURCE_CHANGE_ONLY; HOSTED_CI_REQUIRES_NEW_SHA; DEVICE_NOT_RUN; MEDIA_IDENTITY_UNVERIFIED.
Source: R1C D04 SHA `b246b0f38b38623bdb77987d9fb11318f5c95709`.

## D04 verified device evidence, 2026-11-10

User capture `r1c-d04-passive-capture.zip` header pinned Android run `42811396047`, SHA `b246b0f38b38623bdb77987d9fb11318f5c95709`.
- Installed Instagram 455.0.0.8.72 SHA256 and APK-size gating: PASS, observer installed once.
- Four `INA_R1C_D04_EVENT` entries, all `MEDIA_TYPED`, at 09:52:04, 09:52:08,
  09:52:12, and 09:52:17 (America/Sao_Paulo); all within `reels_overflow` test.
- First event screen `UNKNOWN` while navigation re-stabilized; next three `REELS`.
- No events during Feed, plain Reels, Story, Feed-return windows; screen sequence retained.
- `crash-indicators.txt` is empty, hook errors absent; Android CI 42811396047
  and repository checks 42811396128 passed.
- These observations establish a **menu callback with a Media-typed parameter**,
  not that the argument's media ID matches a displayed Reel.

## D05 changes

- Strict D04 APK SHA and method signature guard retained. libxposed interceptor
  calls `proceed()` exactly once, preserves return value/exception, and logs no
  media object contents.
- After D04 type/screen check, compare only reference equality of consecutive
  Media arguments **while R1B confirms REELS**, keeping the last reference via
  `WeakReference` (not a strong retention). No `identityHashCode`, no `getId`,
  no `toString`, no IDs, URLs, text or account data; logs only outcome words.
- On `UNKNOWN`/non-Reels, wrong type, Activity pause or destroy, invalidate
  immediately. A GC-cleared WeakReference is `BASELINE_COLLECTED`, not a mismatch.
- Bounded by the existing D04 maximum 64 events per process. No new Xposed
  target hooks, no actions, no media identity assertion.
- `BASELINE`, `SAME_REFERENCE`, `DIFFERENT_REFERENCE` are **object continuity
  diagnostics only**; a rebuilt Media wrapper may differ for the same Reel.

## Current device qualification — FAB01 (InstaEclipse enabled throughout)

**FAB01 replaces the prior HF02/HF03 Termux-driven ON/OFF Vector-scope protocol.**
Leave InstaEclipse and InstaActions enabled throughout this test. Do not switch
Vector scope to perform the current D05 acceptance.

After the NEW FAB01 hosted Android and repository workflows both pass on the
exact FAB01 commit, install that debug APK and restart Instagram. Open the
floating **IA** chip on Instagram's main Activity. Wait for the **exact IG455
hook to show INSTALLED**; it is bound to the existing SHA-256 and method gate.

1. Start **A1**. On Reel A open its three-dot menu, close, tap IA > Next.
2. **A2**: reopen the same Reel A menu, close, tap IA > Next.
3. **B1**: swipe to a different Reel B, open menu, close, tap IA > Next.
4. **B2**: reopen the same Reel B menu, close, tap IA > Finish.
5. Copy or share **safe report**: only bounded per-window enum counts, never
   raw logcat, media data, URLs, captions, account identifiers or content.

Each window is at most 60 seconds; empty/expired windows are **INCONCLUSIVE**.
Stage letters are manually chosen, not proof of visible-media identity.
An unexpected Activity pause/destroy cancels an active capture, and no UI
survives outside Instagram's main Activity. The module itself never modifies
InstaEclipse, changes Vector scope, installs an action hook or grants media IDs.

The **old** 110-second Termux `reels_ab_continuity` method with manual
InstaEclipse ON/OFF/restore attestations is *superseded for current acceptance*;
previous records remain useful historical evidence only.
A scope-toggle diagnosis is optional later *only if independently established
interference makes it necessary*.

## Historical HF02/HF03 delivery and provenance notes

HF02 originally hardened the external Python/Rich probe (strict new-commit
validation and sanitized `adb logcat` parsing). HF03 fixed the Termux Hermes
`tools` namespace collision and was successfully pushed and hosted-CI qualified
at commit `06dad42f68ac2e3a39db7af2bf5fd75225634899`.
Those older probes are **not** the current FAB01 device qualification route.
VersionCode 14 identifies the NEW FAB01 debug APK; both workflows must pass on
its exact commit before installation. No old D04 or HF03 CI run can qualify FAB01.

InstaEclipse upstream at e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d
has `ReelDownloadHook.installReduceOptionsListPatch`: it discovers an
`ArrayList` Reel option-list builder through field uses of
`MediaOption$Option.PLAYBACK_CONTROLS` and `UNSAVE`, then appends menu rows.
Its actual IG455 resolved method is NOT established by source alone.
Do not claim it equals `X.07EF.A05` without runtime descriptor or DEX proof.
The upstream source implementation is GPL-3.0; this phase ports no code.
See `docs/R1C_D05_INSTAECLIPSE_SOURCE_AUDIT.md` for the complete mapping.

### HF02 historical evidence and post-commit hardening — superseded

- Delivery is **not applied** by archive creation. The user explicitly executes
  the self-contained outer Python ZIP, which contains a separately validated
  Devtool overlay for *D04* `b246b0f38b38623bdb77987d9fb11318f5c95709`.
- `python3 delivery.zip check` runs fail-closed parser and remote-source
  regression simulations offline. GitHub Actions unit tests execute the
  **identical** `tools/r1c_d05_evidence_policy.py` parser source.
- Once the exact changed-file list, parent, committed payload SHA-256 and clean
  worktree all match the overlay, the launcher is allowed to push the head.
  Old D04 CI does not qualify D05; remote SHA verification happens again before
  the probe installs an exact-CI APK.
- `adb logcat -v epoch` capture stages are tied to host/device-clock checked
  epoch boundaries; **only** `reels_ab_continuity` records contribute to A/B
  counts. Sub-windows A1/A2/B1/B2 are **time buckets with manual attestation**,
  not verified visible-content identity. Unknown fields, marker suffixes and
  malformed records are rejected by exact parser grammars. Raw logcat stays
  local; exported crash evidence is counts only.
- Distinct InstaEclipse-only row must appear uniquely ON, be absent OFF, then
  reappear uniquely after Vector scope restoration and full Instagram relaunch.
  Manual menu checks are not proof of Vector's internal scope DB or method
  identity; inconclusive results always fail closed.
- `adb install -r` first; uninstall after signature mismatch requires a second
  explicit `UNINSTALL` confirmation and may reset module settings. No automated
  Instagram or InstaEclipse uninstall; all actions remain off.

### D05 HF03 — Devtool import collision recovery

The HF02 apply failed **before commit**: Python unittest imported an unrelated
`tools` package from the Termux Hermes installation and raised `ImportError`.
Devtool reported a successful rollback, restoring the D04 branch worktree.
HF03 loads `tools/r1c_d05_evidence_policy.py` from this repository using an
absolute path and adds a separate foreign-`tools` import regression. Its exact
source/CI/device gates, reference-only semantics, and manual InstaEclipse
coexistence checks remain unchanged. HF03 is **NOT_RUN** on device until
application and exact new-commit hosted CI evidence.


### D05 FAB01 — Instagram-native read-only control surface (2026-11-11)

The InstaActions activity-local IA chip is now draggable and opens a compact
read-only diagnostic panel. No system overlay permission, action hook, internal
Instagram method invocation, automated scope change, or InstaEclipse modification.
The chip lives in Instagram's *main* Activity only and is removed at pause/destroy.
The user manually advances A1/A2 on one Reel, B1/B2 on a different Reel.
Counters accept only pre-existing D05 comparison enum + screen/type signals.
Each stage expires after 60 seconds; missing callbacks or timeout results in
INCONCLUSIVE, never a claimed visible-media identity. Raw logcat is not exported.
A user may copy/share only the bounded, value-free count report.
InstaEclipse stays enabled throughout. Coexistence is assumed, not read from
Vector's private scope state. If actual interference is found later, an optional
independent ON/OFF investigation may be designed, but is NOT part of FAB01.

Hosted Android/JVM, installation, and physical callback acceptance are NOT_RUN
until a direct-child commit of HF03 and its exact-SHA GitHub Actions pass.
Do not merge or enable any action based on this diagnostic evidence.
