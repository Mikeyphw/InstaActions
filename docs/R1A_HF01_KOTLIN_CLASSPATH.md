# INA R1A HF01 — DexKit Kotlin compile classpath

**Failed hosted build:** https://github.com/Mikeyphw/InstaActions/actions/runs/42544851603  
**Failed job:** 128927503702, `:app:compileDebugJavaWithJavac`.  
**Source pin:** `a0458560661e4b9550e9b611608e3e15936f97ce`.

## Root cause

`org.luckypray:dexkit:2.0.3` exposes Kotlin `kotlin.jvm.functions.Function1` and
`kotlin.Unit` types in its Java-visible matcher APIs. The Java-only Gradle project
has DexKit but not Kotlin's standard library on `debugCompileClasspath`.
Compiler diagnostics were `cannot access Function1` / `cannot access Unit`
(5 errors); numerous Kotlin deprecation annotations were secondary warnings.

## Fix

Declare `implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.25'` alongside
DexKit. `implementation` supplies both javac's compile classpath and Android's
runtime packaging. **Do not** add a Kotlin Gradle plugin solely for this Java project.
No Instagram hooks, UI behaviors, or DexKit query semantics are changed.

## Branch integration

The already-passing R0 commit must be merged into `main` before applying this
hotfix, preserving ancestry with a **merge commit**, not squash/rebase.
The R1 branch descends from R0 and must remain unmerged until hosted CI and
relevant R1 device acceptance pass.

## Acceptance gates

1. Offline Python tests, including stdlib compile-classpath regression.
2. Hosted `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`.
3. Hosted R0 APK Xposed metadata verifier and R1 native DexKit packaging verifier.
4. Device R1 discovery and InstaEclipse coexistence remain **NOT_RUN** until
   separately checked using `docs/R1_DEVICE_ACCEPTANCE.md`.

Only the hosted run can certify javac, Android packaging, and test APK outcomes.
