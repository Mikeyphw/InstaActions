# R1C D17 HF10 — automatic read-only root FAB diagnostics

Parent: `Mikeyphw/InstaActions` branch `feature/ina-r1c-media-discovery-20261109` at `820f69c339138735d4b262336c40b33d4725a810` (D16 HF09). This is a source-prepared overlay, not physical acceptance.

## Reason for change

The D16 receiver depends on an emitted Instagram-side event and cross-UID broadcast delivery. If hooks never loaded, the app may show no events. To recover evidence independently of the in-process broadcast bridge, D17 automatically starts a bounded background root probe whenever the standalone InstaActions app resumes. No extra in-app opt-in control is presented. KernelSU remains the system's authority and may request the user's root grant; the app neither bypasses nor modifies its permissions.

## Read-only command and verification

D17 launches the fixed literal `su -c` command, with no user-supplied shell input. It checks `id -u`, enumerates the main `com.instagram.android` PID, and reads at most 1000 recent logcat lines from InstaActions-specific tags `INA_FAB`, `INA_DIAG`, `INA_R1B`, and `INA_R1C`. The command does not modify packages, Android settings, modules, files or the Instagram process. It runs off the main thread, with a 25-second process deadline, bounded 160k-character in-memory capture and a 20-second scheduling interval.

The pure-Java parser only retains allowlisted, sanitized event names with optional bounded detail/type fields, and only accepts logcat records attributed to a main Instagram PID observed by the same root command. Foreign-process markers are counted but discarded. The full raw capture is not persisted or exported. The app-private report is kept until replaced or explicitly cleared.

The report distinguishes root denied/unavailable, timeout, Instagram not running, missing/mismatched logcat events and a positive read-only capture. Even a successful root probe cannot conclusively prove hook absence from an empty logcat window or guarantee FAB visibility: log rotation and missed Activity transitions remain possible.

## User experience

- Open Instagram and navigate its Home Feed; then open the standalone InstaActions app.
- Root capture starts **automatically** on resume. No separate app opt-in button. If KernelSU presents an authorization prompt, it must be granted by the user in that trusted system UI.
- The **Automatic Root FAB Diagnostics** card displays status and sanitized records.
- Existing **Copy FAB logs** and **Export FAB logs** include both the D16 broadcast history and the D17 root report. The former can be empty while the latter shows startup/hook markers.
- Denied, missing and timeout states remain visible; standard non-root diagnostics still work.

## Safety and qualification

No Instagram account IDs, private media, captions, URLs, cookies, tokens, full logcat or mutable root commands are written to the report. No change to existing native Repost recovery, account-switch safeguards, Instagram version gate, Vector API102 module scope, or InstaEclipse coexistence. Root is for diagnosis only and grants no action eligibility. Source/contract tests do not prove Android compilation, root access on the real device or FAB restoration.

Device acceptance remains **NOT RUN** until exact-commit hosted Actions succeed, APK is installed and a real capture is exported from the standalone app after navigating Instagram. Check whether `root_uid_zero=true`, `instagram_main_pid_count>0`, and whether `INA_FAB_*` markers were seen from that process.
