# R1B HF03 — separate discovery qualification from read-only screen diagnostics

Source commit: `497d7b4d4e896bf25338d4668ebb67387c3d6468`.
Hosted R1B HF02 run `42558417190`: PASS. Uploaded `r1b_3.zip` APKs match
SHA256SUMS. Device log captured in primary Instagram process:

- `INA_DIAG_MODULE_LOADED main=true`
- `INA_DIAG_PACKAGE_LOADED instagram=true main=true`
- `INA_DIAG_PACKAGE_READY instagram=true main=true`
- `INA_DIAG_HOOKS_INSTALLED ... actions=off`
- `INA_DIAG_FIRST_ACTIVITY_RESUMED actions=off`
- `INA_R1_SCAN_SKIP reason=unqualified_version actions=off`

No `INA_R1B_OBSERVER_ATTACHED` or `INA_R1B_CONTEXT` was captured. Both R1A
and R1B had relied on the same exact-version allowlist (451/453), so an
unqualified Instagram build skipped passive observation before attach.

HF03 retains strict version gating for R1A native DexKit discovery and every
mutating action (disabled). Only the Android framework View-tree observer is
allowed to collect read-only UI surface hints on an unqualified build. This
contains no Instagram-method hooks, view text, URLs, media IDs or accounts.
The observer remains throttled (800 ms), bounded (384 visible nodes / depth 18),
and removed on Activity pause/destroy. It cannot authorize actions.

Diagnostic output:

- `INA_R1B_VERSION version=... qualified=false mode=read_only_preview`
- `INA_R1B_OBSERVER_ATTACHED ... qualified=false`
- `INA_R1B_CONTEXT surface=UNKNOWN|POST|REELS|STORY ... qualified=false`
- `INA_R1_SCAN_SKIP reason=unqualified_version` must remain on the device.

The version name is public package metadata and sanitized to <=64 characters.
Non-qualified results are **provisional**. `UNKNOWN` is fail-closed and valid;
it is not proof that a context detector works. Complete the on-device screen
transition and performance acceptance before R1B merge or R1C.

The earlier InstaEclipse `CalledFromWrongThreadException` findings are unrelated
to this version gate and are not modified here.
