# R1B — passive UI surface-state tracking

Base: R1A HF01 commit `ea635eb05d172a64a7bd92d5baadeb59a9ed1bb6`.

- Uses Android `Activity` lifecycle interceptors **only**, not Instagram business callbacks.
- Attaches one `OnGlobalLayoutListener` and one `OnScrollChangedListener` to the resumed Activity decor ViewTreeObserver; removes both on pause/destroy.
- Samples only visible views, with a cap of 384 nodes / 18 levels and 800 ms throttle.
- Uses Android resource package/name lookup, restricted to `com.instagram.android`.
- Does **not** read UI text, comments, media IDs, URLs, app class names, account names or DMs.
- Detects weak screen hints; ties, incomplete traversal, unknown IDs and unsupported builds resolve to UNKNOWN.
- Respects 'ReelViewer' = Instagram Stories, different from 'Clips' = Reels. Never maps 'story_tray' or bottom tabs to active content.
- Outputs only status enum and aggregate scores to logcat. No persistent collection.
- Mutating actions remain off and media IDs explicitly `unavailable`.

Hosted checks: JVM pure-Java classifier tests, Python privacy contract, APK verifier and R1 native DexKit packaging validator. R1B source is **not** acceptance until on-device observed transitions match screens without InstaEclipse clashes.

Next work (R1B2): from device evidence, qualify a non-mutating content binder/adapter to infer **current media identity** without leaking it into logs, then establish stale/rebind behavior. Only afterwards start R2 button injection.
