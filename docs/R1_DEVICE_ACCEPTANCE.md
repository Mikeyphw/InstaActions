# R1 read-only hook discovery — device acceptance

Status: **NOT_RUN**. A successful GitHub build does not verify these hooks.

Test scope: Android 16 / Vector 2.2+, Instagram 451 and 453 separately, InstaEclipse enabled and disabled.
Instagram Lite 534 and unknown Instagram versions must be skipped (fail closed).

## Steps

1. Install the R1 debug APK; enable only `com.instagram.android` in Vector.
2. Keep InstaEclipse enabled; force-stop Instagram before the cold-start test.
3. Run the commands below from Termux with ADB connected to `localhost:5555`.
4. Visit Feed, open a Reel, open a Story; confirm that no InstaActions button appears and no Instagram behavior changes.
5. Confirm `INA_R1_LIFECYCLE_HOOK_INSTALLED`, `INA_R1_SCAN_SCHEDULED`, exactly five `INA_R1_MARKER` results and `INA_R1_SUMMARY`.
6. Reopen Activities: confirm no additional query sweep for the same APK fingerprint.
7. Repeat with InstaEclipse disabled: compare startup, smooth scrolling, playback and crashes.
8. Unknown IG version must produce `INA_R1_SCAN_SKIP reason=unqualified_version` with no DexKit sweep.

```zsh
adb -s localhost:5555 shell am force-stop com.instagram.android
adb -s localhost:5555 logcat -c
adb -s localhost:5555 shell monkey -p com.instagram.android 1
# Manually visit Feed, Reels and Stories, then:
adb -s localhost:5555 logcat -d -s INA_R1:I '*:S' > "$HOME/ina-r1-device.log"
grep -E 'INA_R1_(LIFECYCLE|SCAN|MARKER|SUMMARY)' "$HOME/ina-r1-device.log"
```

## Evidence/decision

Record installed Instagram version, InstaEclipse installed/enabled version, Vector version, module APK checksum,
all five probe result classes (UNIQUE/MISSING/AMBIGUOUS/FAILED), lifecycle hook status, force-close/ANR result,
and whether the UI stayed unchanged. **Do not** store account names, DM contacts, media IDs, URLs or private contents.

Pass criteria for this R1A slice: candidate discoveries logged without new app crashes or competing Instagram method hooks.
Full R1 media-context/identity inference remains **NOT_IMPLEMENTED** until device findings drive R1B.
