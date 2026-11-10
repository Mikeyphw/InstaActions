# R1B HF05 — device evidence (2026-11-09)

Status: **PASS for stable navigation discovery**. Not proof of individual media ID/action behavior.

- GitHub head: `530feede55917cd7d050e3dd89800608abe48710`
- Android CI: `42713725640` PASS
- Repository contract: `42713725548` PASS
- User-provided capture: `r1b-hf05-navigation-capture.zip` on Instagram `455.0.0.8.72`
- 23:07:31 `HOME_FEED` stable during Feed stage 23:07:28–23:07:53
- 23:08:29 `REELS` stable during Reels stage 23:08:25–23:08:50
- 23:10:06 `STORY` stable during Stories stage 23:10:03–23:10:28
- 23:11:27 `HOME_FEED` stable during return stage 23:11:24–23:11:49
- R1B viewer evidence: Reels and Stories anchored, with `UNKNOWN` media context on Home Feed as expected
- Observer attach/detach 8/7; final observer still active at test end. No `INA_R1B_SAMPLE_FAILED` or `INA_DIAG_HOOKS_FAILED` in filtered sample. Full crash/ANR qualification outside the filtered capture remains separately recommended.
- `INA_R1_SCAN_SKIP reason=unqualified_version` on IG455 expected; no media IDs or mutating actions.

## Scope

R1B navigation is ready to merge via a normal merge commit. R1C must not infer a Post identity from Home Feed. The original R0-R8 roadmap and separate InstaEclipse coexistence contract remain unchanged.
