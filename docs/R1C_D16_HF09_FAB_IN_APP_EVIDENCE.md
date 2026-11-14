# D16 HF09 — In-app FAB evidence and guarded rescue

Base: `Mikeyphw/InstaActions` branch `feature/ina-r1c-media-discovery-20261109`
commit `1605440d1ba078f811585ca6c4617a1965542dd4` (HF08).

## Observed problem and what remains unknown

User reports the IA floating button still absent after D14 and D15 HF08 despite
successful hosted API102 build and contract runs. No device-side FAB visibility
receipt has yet been captured. This patch **cannot claim physical restoration**.

## Changes

- `R1CD05FabPanel` logs early resume and all non-content guard outcomes; logs
  attach, failure type, retries, root selection, delayed attached/shown state,
  and detachment. All sampling remains bounded.
- `R1CD16MainSurface` checks IG-owned *resource entry names only* under a 384
  view budget. For unfamiliar Activity classes, both a visible main Feed action
  bar and a known bottom navigation tab must corroborate the main screen.
  Failure is `MAIN_ANCHOR_UNRESOLVED`, never generic unconditional attachment.
- `R1CD16FabEvents` sends fixed enum-like event categories and sanitized short
  details using an explicit broadcast from the Instagram process.
- The exported `R1CD16LogReceiver` uses Android 14+ `getSentFromUid()` and
  compares the UID against the actual installed Instagram package. Invalid
  sender UID, unknown category, malformed detail, or unavailable identity
  is dropped without recording data or invoking any action.
- App-private `R1CD16LogStore` persists the **last 180 event lines** with a
  24,000-character cap, even when the InstaActions UI is closed.
- The InstaActions app displays events without needing the FAB, with Refresh,
  Copy FAB logs, Export FAB logs, and Clear controls.

No `View` text, usernames, tokens, captions, media IDs, account IDs, private
Intent extras, or bitmap data are captured. An absence of app events may mean
Vector did not install lifecycle hooks, or Android blocked bridge delivery.
It is never reported as proof of one specific cause.

## Qualification

Offline source and pure-Java state/anchor checks: tested. Full inherited suite
and Android APK compilation: launcher / hosted GitHub Actions gated. Device FAB
visibility and UID-authenticated event delivery: NOT RUN.

## Device acceptance checklist

1. Install exact hosted APK and restart Instagram fully (stop/reopen process).
2. Open the standalone InstaActions app → FAB Diagnostics → Refresh FAB logs.
3. Open Instagram home, Reels, Feed-opened Reels; reopen InstaActions → Refresh.
4. Copy FAB logs and send them for diagnosis.
5. Confirm a *physically visible* IA chip separately; `FAB_VISIBLE SHOWN=true`
   is only an attachment observation, not proof it wasn't covered by a window.
6. Verify Copy, Export, and bounded retention even if the IA chip is missing.

If there are zero records, use `python3 ZIP diagnose` for a read-only ADB logcat
capture (`INA_DIAG`/`INA_FAB`). No `logcat` access permission is requested by app.

No local Gradle; CI must pass on the exact new Git commit. Native Reels Repost
recovery gates, InstaEclipse coexistence, and the original roadmap are unchanged.
