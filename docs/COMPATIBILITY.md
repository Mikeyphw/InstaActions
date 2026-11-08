# Compatibility matrix

| Component | Version / state | Status at bootstrap |
| --- | --- | --- |
| Android | 16 (SDK 36) | TARGET / NOT_TESTED |
| KernelSU | rooted device | TARGET / NOT_TESTED |
| Vector | 2.2+, API 102 | TARGET / NOT_TESTED |
| InstaEclipse | 0.8.2 modern/API 102 | COEXISTENCE_REQUIRED / NOT_TESTED |
| Instagram | 451.0.0.20.80 | REFERENCE_BASELINE / NOT_TESTED_IN_INSTAACTIONS |
| Instagram | 453.0.0.0.56 | EXPERIMENTAL / NOT_TESTED |
| Instagram Lite | 534.0.0.8.108 | OUT_OF_SCOPE / RESEARCH_ONLY |

The initial 451 baseline is InstaEclipse's documented tested version, **not** evidence InstaActions works with it. No InstaActions runtime compatibility has been established yet. Keep per-version hook hashes/diagnostics in private test artifacts, never credentials or conversations.

## Evidence fields

Date, APK version/package, APK SHA-256, module version/commit, Vector version, InstaEclipse version, test action, expected result, actual result, crash/ANR check, CI run URL, result `PASS/FAIL/NOT_RUN/UNSUPPORTED`.
