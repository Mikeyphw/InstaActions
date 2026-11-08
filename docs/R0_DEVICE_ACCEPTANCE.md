# R0 real-device acceptance (not yet executed)

Prerequisites: Instagram 451 (recommended baseline) or 453 (experimental), InstaEclipse
v0.8.2 enabled for `com.instagram.android`, Vector 2.2+ API102.

## Install and scope

Download both APKs and checksums from the same GitHub Actions run artifact.
Verify SHA-256 before installation. On the target Termux device:

```zsh
adb -s localhost:5555 install -r ./InstaActions-r0-debug.apk
adb -s localhost:5555 shell am force-stop com.instagram.android
adb -s localhost:5555 shell monkey -p com.instagram.android 1
adb -s localhost:5555 logcat -d -s INA_R0:I '*:S'
```

Enable InstaActions in Vector first, with **only** `com.instagram.android` scoped.
Keep InstaEclipse enabled. Both modules must appear independently in Vector.
Use `adb logcat -c` before the launch only if the existing device logs are not needed.

Pass only with:

- `INA_R0_PACKAGE_READY package=com.instagram.android process=main hooks=0` found in fresh logcat.
- Instagram remains functional with InstaEclipse enabled.
- Button and other Instagram UI remain unchanged (R0 must not inject anything).
- No additional crashes/ANRs attributable to either module.
- Companion shows installed version accurately and claims no live module status.

Record the exact Instagram version, InstaEclipse version, Vector version, build SHA,
Actions run URL and log excerpts. Do not log private account/message/media contents.

## Baseline / experimental

| Instagram | Status | Vector + InstaEclipse coexistence |
| --- | --- | --- |
| 451.0.0.20.80 | NOT_RUN | NOT_RUN |
| 453.0.0.0.56 | NOT_RUN | NOT_RUN |
| Lite 534.0.0.8.108 | OUT_OF_SCOPE | OUT_OF_SCOPE |
