# Testing and evidence plan

## Bootstrap checks

- `python3 -m unittest discover -s tests -v`: validates doc/metadata contract only.
- No Gradle or instrumentation test is part of the bootstrap. These are R0 deliverables.

## Hosted CI after R0

- Static/lint/unit tests on PR and push; debug APK assembled only on GitHub-hosted runners.
- Inspect generated APK for `META-INF/xposed` metadata, scope, min SDK and package IDs.
- Instrumentation test APK built in Actions; emulator jobs run where compatible.
- Upload checksums, APK, test XML, redacted logcat and manifest evidence.
- Refuse release if host workflow is missing or silently skips tests.

## Physical Android acceptance

- Confirm correct device/package and Vector API; reject emulator/physical mismatch.
- Install InstaActions and InstaEclipse; enable both for Instagram; force-stop/reopen Instagram.
- Test InstaEclipse Explore long press before and after enabling InstaActions.
- Run tests per surface (Reels, home posts, opened posts, stories), per Instagram version.
- Track crash-free startup, background/foreground, back navigation, rotation and loss of media context.
- Test duplicate taps, network interruption, stale media, account switch and unsupported hook fallback.
- For contacts/groups, verify destination identity and content; for comments, verify at most one per trigger.

## Never label tests as passed unless observed

Use `NOT_RUN` for unexecuted tests, `INCONCLUSIVE` for unavailable device functionality, `UNSUPPORTED` for intentionally unimplemented behavior. Compilation != runtime acceptance.
