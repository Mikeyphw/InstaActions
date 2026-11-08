# InstaActions

A private, standalone Instagram quick-actions module intended to coexist with InstaEclipse.

**Status:** Repository bootstrap only. No installable APK or functional hooks exist yet.

## Target platform

- Android 16; rooted device with KernelSU and Vector 2.2+ (libxposed API 102).
- Default scope: `com.instagram.android` only.
- InstaEclipse v0.8.2 coexistence is a required acceptance test, **not yet verified**.
- Instagram 451.0.0.20.80 is InstaEclipse's documented tested baseline; version 453 requires explicit qualification.
- Instagram Lite is a separate research track and is not initially supported.
- All Android Gradle builds and instrumentation builds happen via GitHub Actions, not on Termux.

## Repository documents

- [ROADMAP.md](ROADMAP.md) — milestones, gates and scope.
- [Architecture](docs/ARCHITECTURE.md) — module boundaries, permissions and interaction model.
- [Compatibility](docs/COMPATIBILITY.md) — precise supported/experimental status.
- [Test Plan](docs/TEST_PLAN.md) — hosted and device acceptance.
- [Hook Inventory](docs/HOOK_INVENTORY.md) — hook discovery ledger template.
- [Security](docs/SECURITY.md) — constraints around contacts, comment submission and logs.

## Development branches

`main` is the baseline branch. Milestones are implemented on isolated `feature/ina-rN-*` branches, followed by a PR, Actions evidence and device acceptance. Consolidated Devtool overlays may be used as the delivery mechanism, but overlays must be source-bound and contain tests and a complete manifest.

## Current CI

The repository bootstrap CI verifies documentation and project configuration **only**. The Android module and its hosted Gradle workflows are delivered in R0; bootstrap CI passing must not be described as APK qualification.

## Upstream reference

- https://github.com/ReSo7200/InstaEclipse
- https://libxposed.github.io/api/io/github/libxposed/api/package-summary.html

Not affiliated with Instagram, Meta or InstaEclipse. No upstream source should be copied into this private repository without a deliberate license review.
