# Architecture contract

## Components

1. **Companion UI** (`com.mikeyphw.instaactions`): actions, macros, recipient aliases, comment pools, logs and backups.
2. **Modern Xposed entry** (libxposed API 102): load only for `com.instagram.android` main process; prefer `onPackageReady` for a fully available classloader; detach from unrelated processes.
3. **Capability adapter**: DexKit-based discovery, per-Instagram-version cache and `SUPPORTED/UNSUPPORTED/UNVERIFIED` status.
4. **Context resolver**: current visible media, screen type, stable media identity, playback state and account identity (only where needed).
5. **UI host**: injected button and panel with lifecycle-aware attachment, touch exclusivity and placement constraints.
6. **Action dispatch**: small, independently guardable action handlers; native Instagram pathways preferred; no custom HTTP clients for Instagram.
7. **Macro engine**: guarded step execution, cancellation, idempotency keys, external side-effect records and failure policy.
8. **Diagnostics**: structured, privacy-safe events, source/build/version binding and optionally exported logs.

## Upstream compatibility

InstaActions must never hook InstaEclipse package methods, reuse its preferences or inject into its companion UI. Treat both as peers coexisting in Instagram. Detect potential duplicate controls for user messaging, but do not modify InstaEclipse runtime configuration automatically. Reserve the existing Explore long-press gesture for InstaEclipse.

## API 102 metadata implemented in R0 source

- `app/src/main/resources/META-INF/xposed/java_init.list`: fully-qualified modern `XposedModule` subclass.
- `app/src/main/resources/META-INF/xposed/module.prop`: `minApiVersion=102`, `targetApiVersion=102` and explicit options.
- `app/src/main/resources/META-INF/xposed/scope.list`: `com.instagram.android`.
- `android:label` and `android:description`: module name/description.
- `compileOnly('io.github.libxposed:api:102.0.0')`.

These paths are included in the R0 overlay. Verification inside the assembled APK is pending hosted CI.

## Hooking rules

- No blanket hook on every class/method; scope by precise host and capability.
- Do not prevent `chain.proceed()` except when a specific verified action requires interception.
- Bind/unbind UI on actual lifecycle transitions and use weak references where appropriate.
- No hardcoded view coordinates, obfuscated method identifiers as permanent constants or network calls from hot hooks.
- Include crash guard, hook kill switch, bounded DexKit work and rich `UNSUPPORTED` diagnostics.
