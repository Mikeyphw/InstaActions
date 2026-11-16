# RCA01 — Committed required-CI policy repair

## Verified trigger

RCA01's native Devtool v3.1 apply committed the diagnostic-only FAB overlay at
`193ab47cf78a80172f5fd7ccfe3043913606c2c0`, pushed it, and verified
[InstaActions PR #4](https://github.com/Mikeyphw/InstaActions/pull/4).
The subsequent `delivery session ci` command refused because the target HEAD
had **no** `config/delivery-required-ci.json`.

This is not an Android build failure or a failed overlay apply. The previous
session ID `b5ce884da3f448d19bb49d46cc773b50` stays intact and bound to
the original committed HEAD. Do **not** reapply that already committed overlay.

## Native Devtool CI policy

`config/delivery-required-ci.json` is a strict Devtool schema v1 policy for
`Mikeyphw/InstaActions` PRs into `main`. It requires both existing workflows,
identified from real GitHub Actions runs on the original PR head:

- `R0 / R1 Android API 102`, workflow ID `411517720`;
- `Repository contract`, workflow ID `411509546`.

The `pull_request` event is required; successful `push` checks cannot replace
PR checks. All checks must be source-bound to the exact new PR HEAD. Release
qualification remains separate.

The policy is **committed** in the same new Devtool native transaction as this
repair's regression test and note; an untracked/modified policy does not pass
Devtool's `delivery_ci_policy.load_policy` guard.

## Delivery and recovery

The HF02 source-pinned preparer uses the Devtool v3.1+ native builder,
`delivery adoption scaffold`, `validate overlay`, native apply/commit,
then a **new durable session bound to the new commit**. This is necessary
because the original session's HEAD is immutable. Its record is preserved,
not rewritten or retroactively declared CI-qualified.

Use the resulting native package's `status`, `deliver`/`ci` operations if CI
observation is incomplete; do not replay `apply` after a successful commit.
Review the native stage/return code and exact GitHub run URLs for failures.

## Truthful acceptance

This repair does not modify the RCA01 diagnostic hooks, UI, A/B payload, or
Instagram runtime. Existing security, Android/JVM, native Repost regression,
negative and source contract checks remain enabled. A successful CI observer
requires Devtool's additional GitHub branch-protection/ruleset and run-attempt
checks; a missing required status or unavailable rules inspection must remain
an explicit refusal, not a synthetic pass.

Physical Android 16 testing of RCA01 and its exact IG455 APK variants remains
`NOT_RUN` until recorded separately. No release or merge is authorized here.
