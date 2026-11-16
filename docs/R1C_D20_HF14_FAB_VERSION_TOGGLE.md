# D20 HF14 — Ignore Instagram version for FAB (UI only)

- User-controlled switch in the **standalone InstaActions app**. **Default OFF**, persist in private SharedPreferences (`commit()` before returning to Instagram).
- Instagram can read exactly one fixed Boolean via the **exported read-only provider**. Provider refuses unrecognized paths and caller UIDs; `insert`, `update`, `delete` are not implemented.
- Existing `R1CD05FabPanel` tests the exact `455.0.0.8.72` version first. On mismatch, if the app's switch is ON, attempt to attach **only the diagnostic FAB** on a main-screen-validated Instagram Activity. Fail closed if IPC is unavailable.
- The switch does **not** affect D04 APK SHA validation, D07 native method eligibility, Android Xposed scope, InstaEclipse, or account/media identity verification.
- Unsupported version forces Repost recovery OFF, disables its 90-second and foreground-session activation controls, and checks again in their callbacks.
- Debug marker `INA_FAB_VERSION_OVERRIDE_UI_ONLY`; mismatch and disabled switch produce `SKIPPED_VERSION_NOT_IG455_OVERRIDE_OFF`.
- Switching the setting back OFF takes effect on the next Activity resume. It does not promise to hide an already attached FAB immediately; close/reopen Instagram for a clean test.
- This **cannot fix a missing Vector hook**, a disabled module scope or an unresolved Instagram main Activity root.
- **Physical status: NOT RUN.** Source validation and hosted CI do not qualify actual device visibility.
