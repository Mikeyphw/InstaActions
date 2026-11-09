# R1B — read-only screen-context device acceptance

Status: **NOT_RUN**. Hosted compilation/packaging is not runtime validation.

## Scope and safety

- Vector 2.2+ / API 102, Android 16, Instagram 451 or 453 qualified only.
- InstaEclipse enabled first, then disabled for comparison.
- View-tree observer is lifecycle-scoped and throttled to ≥800ms.
- No Instagram method hooks, taps, networking, content, media ID, contacts, captions or message collection.
- `ANCHORED` is a UI-resource classifier result, **not** a verified media context.
- Unknown/ambiguous/truncated trees fail closed and produce `UNKNOWN`.

## Device test (read this before running)

1. Install the R1B debug APK from Actions and verify `SHA256SUMS`.
2. Enable InstaActions only for `com.instagram.android` in Vector; keep InstaEclipse on.
3. Force-stop Instagram, clear logcat and start Instagram.
4. Visit Feed, a Reel, a Story, then return to Feed; scroll inside each screen.
5. Confirm no new button or changed Instagram behavior, and watch for crashes/ANRs.
6. Capture `INA_R1`, `INA_R1B` tags, and compare enabled/disabled InstaEclipse runs.
7. Repeat the cold-start test; ensure listener attach/detach lifecycle and no log flood.
8. Wrong Instagram version must cause no `INA_R1B_OBSERVER_ATTACHED`.

```zsh
adb -s localhost:5555 shell am force-stop com.instagram.android
adb -s localhost:5555 logcat -c
adb -s localhost:5555 shell monkey -p com.instagram.android 1
# Manually navigate Feed → Reel → Story → Feed, then:
adb -s localhost:5555 logcat -d -s INA_R1:I INA_R1B:I '*:S' > "$HOME/ina-r1b-device.log"
grep -E 'INA_R1B_(LIFECYCLE|OBSERVER|CONTEXT|SAMPLE|ATTACH)' "$HOME/ina-r1b-device.log"
```

Record only surface/score/confidence, whether expected transitions were observed,
crashes/ANRs and install version. Redact private data. Do not claim PASS for media
identity because this phase does not discover it. Unexpected `UNKNOWN` is a
valid fail-closed output and a useful sign that a better runtime anchor is needed.

## R1B HF02 capture correction

Use the live logcat capture and Vector module-journal collector in the HF02 delivery.
Never rely solely on a post-navigation `logcat -d` dump, or grep custom `INA_DIAG` tags in Vector `modules_*.log` when the framework does not route those tags there.
The HF02 entry uses the VectorContext framework tag for durable diagnostic events, while Android logcat remains `INA_DIAG`.
R1B acceptance remains NOT_RUN until callback and actual context transitions are recorded.

## Historical HF01 diagnostic addendum

Install the HF01 APK with **uninstall-before-install** because hosted debug
signatures can differ. Re-enable InstaActions in Vector if uninstall clears its
scope. Collect both `INA_DIAG` logcat entries and Vector `modules_*.log` entries,
which can show framework callback errors invisible in Android logcat. See
`docs/R1B_HF01_RUNTIME.md` and the delivery `INSTALL_AND_PROBE.zsh`.
