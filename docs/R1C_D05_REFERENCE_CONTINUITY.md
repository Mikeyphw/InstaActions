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

## Device probe

One uninterrupted Instagram-foreground 110-second `reels_ab_continuity` window:
1. On Reel A, open its three-dot menu, close, and reopen.
2. Swipe to a genuinely different Reel B; open its three-dot menu twice.
3. Do not return to Termux between A and B; the script captures continuously.

Verify all events are Media typed, within the window and only with REELS stable
or UNKNOWN during transitions; check for at least one BASELINE and whether
SAME_REFERENCE and DIFFERENT_REFERENCE appear. **Neither pattern qualifies
screen-to-media identity** without an independent visible-binding source.
R1C remains unmerged, action hooks disabled.

## R1C D05 HF02 — source/CI and coexistence qualification

The external Python/Rich probe must reject the **old D04 SHA** even when its two
CI runs pass, and require a **new direct-child SHA** with byte-for-byte canonical committed files, versionCode 13,
and D05 reference marker, plus both hosted workflows passing on that exact SHA.

Run the same A/B continuity sequence in two independent conditions:

1. **InstaEclipse ON** for Instagram, InstaActions ON; stop/restart Instagram.
2. **InstaEclipse OFF** for Instagram, InstaActions ON; stop/restart Instagram.
3. Restore **InstaEclipse ON** in Vector afterwards and force stop Instagram.

Each condition has a separate uninterrupted **110-second** A/B Reel test.
Vector scope is changed **only manually**. Typed confirmations are **user attestations, not programmatically verified Vector scope status**. If scope
cannot be confirmed, or the hook is not installed or does not emit callback
markers, device qualification is **INCONCLUSIVE**. Raw logcat remains local.

InstaEclipse upstream at e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d
has `ReelDownloadHook.installReduceOptionsListPatch`: it discovers an
`ArrayList` Reel option-list builder through field uses of
`MediaOption$Option.PLAYBACK_CONTROLS` and `UNSAVE`, then appends menu rows.
Its actual IG455 resolved method is NOT established by source alone.
Do not claim it equals `X.07EF.A05` without runtime descriptor or DEX proof.
The upstream source implementation is GPL-3.0; this phase ports no code.
See `docs/R1C_D05_INSTAECLIPSE_SOURCE_AUDIT.md` for the complete mapping.

### HF02 evidence and post-commit hardening

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
