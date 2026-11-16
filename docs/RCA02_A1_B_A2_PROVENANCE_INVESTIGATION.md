# RCA02 — Where does the Repost Boolean originate?

## Change scope

One consolidated, read-only milestone on `Mikeyphw/InstaActions`, branch
`feature/ina-r1c-media-discovery-20261109`, starting from
`e708b82d1867d3a5461e604a450275faf25ae5e2` (RCA01 + committed HF02 CI policy).
This change does not enable Repost, alter GraphQL/model values, fabricate Friends,
or change Instagram APK acceptance rules. The legacy multi-feature D05/D07 FAB
remains disabled. The **only active floating chip** is now **RCA02**.

## Physical RCA01 evidence motivating this work

The tester's manually labeled A/B capture reported:

| Observer | A | B |
| --- | --- | --- |
| `Media.A3x()` original | 68 false, 1 null | 66 true |
| `Media.AFV(Boolean)` successful call arguments | 34 false | 30 true |
| `X.02ib.D3r(0xdf82522e)` original | 67 false, 1 null | 66 true |
| First native MobileConfig | 75 false | 74 false |
| Native UI singleton/payload | 75 / 0 | 8 / 66 |

Six A frames and eight B frames had missing early `A03` data; the report did
not prove identical Media objects, an authenticated account binding, a server
rollout, or the root cause of the Friends tab. RCA01's B singleton outliers
coincided with absent Repost data rather than an observed false `D3r`.

## Static exact-source investigation

An offline pass over **all 31 DEX files** in the provided 238,346,353-byte
Instagram 455.0.0.8.72 `base.apk` (SHA-256
`4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a`)
identified **two direct** invokes of `Media.AFV(Boolean)`:

1. `classes.dex`, `LX/06nv;->A03(LX/06ck;Z)Lcom/instagram/feed/media/Media;`,
   code unit offset **6138**. The method operates on a keyed update map;
   the immediate source can be a keyed Boolean or an existing getter fallback.
   This path is labelled `X06NV_DELTA_MERGE_OR_FALLBACK`, **not** server-origin.
2. `classes6.dex`, `LX/043F;->A00(LX/043F;)V`, offset **1615**.
   Immediately before the invoke, the method reads
   `LX/06xq;->A3f:Ljava/lang/Boolean;`, copying this legacy field into Media.
   This path is labelled `X043F_LEGACY_FIELD_COPY`.

Both are exact static methods in this APK. The reproducible source audit is
`python3 -B tools/rca02_verify_afv_callers.py --base-apk /path/to/base.apk`.
Its failure on a different base hash is **intentional**, and its successful
result is a **direct invoke inventory only**, not a proof of all dynamic
call paths or source-server causality. The separate older, exact-approved
Instagram base APK has **not** been re-audited by this particular script.

## Source-bound hooks and evidence safety

After D04 exact-build attestation, RCA02 installs optional, pass-through hooks
on those two method signatures using `getDeclaredMethod`, exact return type,
`static` checks, original `proceed()` and mandatory caller-scope cleanup in
`finally`. Media `AFV`'s existing original pass-through observer records a
**successful** setter argument together with a code-only caller-scope bucket.
Missing hooks produce `UNAVAILABLE` or partial status, never a fake source.
The keyed delta scope includes a possible getter fallback; RCA02 cannot assert
that every argument in the scope came from the update map.

A separate bounded, weak-identity ledger correlates Media getter/setter events
only when `getThisObject()` was the **same in-process Java object** (`==`) in
one manual pass. Maximum **256 weak slots per pass**, **4,096 primitive events
per pass**, and a **112-entry timeline per pass**; evicted and capped evidence
is explicitly reported. No strong Media references are retained, and no
account/media IDs, usernames, captions, tokens, URLs, raw GraphQL values,
stack traces or raw object strings are exported.

`X.02ib.D3r` and UI selector-4 are correlated within the existing original
same-thread UI frame, but **are not declared the same Media object**.
Singleton UI results are now classified separately as:

- source-confirmed early `A03` data missing;
- early `A03` present and original exact `D3r` false;
- unresolved/unknown original path.

If the observer is missing, absent data cannot be inferred and the result
stays unresolved rather than being counted as `false`. The original native
result is always returned unmodified.

## RCA02 FAB operator workflow

1. Keep InstaEclipse enabled and all older Repost recovery mechanisms OFF.
   Verify the Instagram main UI shows one chip reading **RCA02**, not RCA01 or
   the old multi-feature FAB. Use exact verified Instagram 455 build.
2. Start **A1** on the affected account, observe the same public Reel and
   capture several Repost UI computations; **Stop A1** to preserve its native
   report snapshot. Do not force Repost on.
3. Switch to working account B manually; tap **Confirm manual switch to B**;
   Start B, observe the same public Reel, Stop B.
4. Return to affected account A manually; tap **Confirm manual return to A**;
   Start A2, observe the same Reel, Stop A2.
5. Tap **Copy RCA02 A1/B/A2 report** (explicit clipboard action); the report
   contains separate three-pass counters, source attribution and bounded
   original native snapshots. Process death loses all captures.

**The app never automatically confirms an Instagram account identity or the
same Reel**. The two confirmation buttons record only a tester's assertion.
A reported A1(false) / B(true) / A2(false) may indicate reproducible
account-associated differences, **not** the server feature rollout mechanism.

## Validation and unresolved acceptance

- Source contract regressions, original D07 safety checks and RCA02 39-case
  standalone JVM test must pass. Hosted Android build and signed/release checks
  are a distinct concern; no local Termux Gradle is required.
- Source-gate and native Devtool validation must use the latest verified
  compatible `Mikeyphw/devtool` main v3.1+ with exact Git blob pins and the
  repository's committed `config/delivery-required-ci.json`. No manual patch,
  copy-over worktree or ad-hoc commit workflow is authorized.
- Android runtime, InstaEclipse coexistence, real Media-object binding,
  GraphQL-to-Media linkage, `can_repost` JSON mapping, origin of `A3f`,
  server-side rollout, and shared Friends eligibility remain **unverified**
  until separate physical/source evidence establishes them.
- Any failed native Devtool validation, hosted CI, or follow-up should retain
  the **complete** native transcript and Devtool session receipt; do not
  reapply a successfully committed immutable package.

## Legacy test cleanup

The obsolete UX02 assertion that the previous five-step Repost enabling
instructions are **visible** is replaced by current RCA02-visible-guide and
old-cards-hidden checks. The old D07 pure safety helper and permanent OFF
semantics retain explicit regression coverage. D05/D15 tests still guard dormant
classes and their native safety invariants, not claims of visible RCA02 UI.
