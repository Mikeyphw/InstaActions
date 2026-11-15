# D18 HF11 — Automatic root diagnostic when Instagram is not currently running

Parent: InstaActions D17 `8736ccb4a98fabdf058f56767dc6d02fb4d423c1` (already applied). No earlier overlay needed.

## Why

Device report showed `root_uid_zero=true`, `instagram_main_pid_count=0`, and `other_pid_tag_lines_ignored=1`. D17's filter kept only logcat events whose PID matched **currently running** `pidof com.instagram.android`. That hides all startup evidence when the process has exited before the standalone InstaActions app opens. It does not establish a hook or FAB failure.

## D18 change

- Same automatic 25-second `su` worker on standalone app resume; no new consent prompt in the app. KernelSU remains in control of authorization.
- Cross-check current main-process PIDs from `pidof` and read-only `ps -A -o PID,NAME`; list auxiliaries separately, so `:push` is never treated as the main process.
- Preserve strong PID matching for trustworthy **live** event details (max 72).
- When PID is not live, show at most 16 **historical unverified marker category names**, with no PID, timestamp, `detail`, exception string, raw log line, or account/media content. These categories are explicitly not attributed to Instagram because logcat tags can be shared or forged.
- Reports continue in the existing in-app Copy FAB logs / Export FAB logs. Historical events are diagnostic hints, **not proof** of module installation or FAB attachment.
- No changes to FAB attachment, Repost recovery, Instagram processes or Xposed hooks. No local Gradle.

## Qualification

Physical acceptance: NOT RUN. Offline JVM and source-contract tests are informative but do not prove Android 16 root-process discovery or FAB visibility. The full repository suite is mandatory before Devtool, followed by exact-commit hosted GitHub Actions and device acceptance.
