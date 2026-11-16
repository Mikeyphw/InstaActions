# D15 HF08 — repair FAB staged validation (direct D14 parent)

## Root cause
D15 introduced `decor.getId()` in `healthyCurrentRoot()` to distinguish the
Activity decor view from the content view. Two inherited pre-existing source
contracts reject view-ID access inside the InstaActions FAB controller:
`tests/test_r1c_d05_fab01_contract.py` and
`tests/test_r1c_d07_ux01_contract.py`. This caused the full 173-test source
suite to refuse *before* Devtool. No repository mutation happened.

## Correction
Use reference equality `root == decor || content == decor`, where `content`
is returned by Android's `activity.findViewById(android.R.id.content)`.
This neither reads the View's identifier nor logs Instagram UI contents.
The four bounded retries, stale button cleanup, independent Activity lifecycle
rescue, source-hash gates, and native Repost safeguards are unchanged.

## Observability
On a new full-suite failure, report test IDs and final assertion messages
as a concise dedicated diagnostic section, then refuse. No tests suppressed.

## Acceptance
- Run D15 HF08 ZIP `--self-test`.
- Launcher runs all inherited tests from the *real* checkout in a disposable
  staging clone before Devtool.
- After successful Devtool/push, require both SHA-matched GitHub Actions
  workflows and checksum verified APK before install.
- FAB must be physically visible on Android 16; hosted success alone isn't
  proof of the UI fix.
- Parent SHA: 053e07bb92992702220baaac896c4d38b276f501.
