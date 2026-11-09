# R1B — read-only screen-context device acceptance

Status: **NOT_RUN**. Hosted compilation/packaging is not runtime validation.

## Scope and safety

- Vector 2.2+ / API 102, Android 16; DexKit discovery qualified for IG 451/453 only.
- R1B passive View-tree **diagnostics** may run on other Instagram versions; any classification is provisional and cannot authorize actions.
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
8. On unqualified Instagram versions expect `INA_R1_SCAN_SKIP` and `INA_R1B_VERSION qualified=false`, plus passive observer events.
9. Confirm DexKit is never scheduled on unqualified versions and all action hooks stay off.

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

## R1B HF03 version-policy separation

HF02 live logs verified all Vector callbacks and the first Activity resume. The
DexKit scan correctly emitted `INA_R1_SCAN_SKIP reason=unqualified_version`.
HF02's passive observer also silently refused unsupported versions due to a
shared guard; HF03 removes **only that observer guard**. It logs the package's
non-private version, marks `qualified=false`, and attaches a bounded read-only
Android ViewTreeObserver. R1A DexKit still refuses unqualified versions; no
Instagram business class hook, media identity lookup or action dispatch is enabled.

Expected device flow: `INA_R1B_VERSION` -> `INA_R1B_OBSERVER_ATTACHED` ->
`INA_R1B_CONTEXT`, on Feed/Reels/Stories. `UNKNOWN` is a safe classification,
not a successful determination of screen identity. Capture failure/ANR diagnostics
without collecting app text or media identifiers. Do not merge without valid
screen transitions and no regression in InstaEclipse coexistence.

## HF04: unknown Feed resource diagnostics

See `docs/R1B_HF04_FEED_HINTS.md`. `INA_R1B_RESOURCE_HINTS` contains only
static resource/view type names, capped and sanitized. Capture labeled Feed,
Reels, Stories, Feed-return evidence. Transient `REELS` on a Story boundary is
not sufficient for acceptance. No action capabilities may be enabled.

## HF04 source-backed Feed qualifier

Source: InstaEclipse FeedPhotoZoomHook / FeedVideoDownloadHook.
`row_feed_button_like` provides anchored Feed-row evidence;
`row_feed_button_save` and `media_group` are only supporting hints.
Verify Feed → Reels → Stories → Feed on device, checking for transient
misclassification and ensuring no mutating action is ever enabled.
