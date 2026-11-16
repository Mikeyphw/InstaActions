# InstaEclipse attribution and redistribution requirements

This InstaActions D06 adaptation ports the **unique direct typed-field / native menu-owned Media selection** strategy from:

- Project: InstaEclipse, https://github.com/ReSo7200/InstaEclipse
- Source: `app/src/main/java/ps/reso/instaeclipse/mods/media/SelectedMediaAdapter.java`
- Fixed upstream revision: `e19ab0def2dc1ac197b7a2d4f8763f701ef68b1d`
- Upstream license: **GPL-3.0-only**; see `LICENSE-GPL-3.0.txt`.
- That upstream file itself notes field-key fingerprint inspiration from HushGram MediaBridges.

The adapted `R1CD06MenuOwnership.java` uses GPL-3.0-only with a source attribution header. It preserves the fail-closed exact-field-type, identity uniqueness and no ambient last-item semantics, but was modified for a strict bounded, API102-compatible, read-only event result. **It does not import or call InstaEclipse implementation classes**, modify its settings, or require switching off its Vector scope.

If distributing binaries that include this GPL-derived component, comply with GPL-3.0 obligations for the **combined work**, including making the corresponding source available under a GPL-compatible license and retaining the required copyright/license notices. Private use is distinct from distribution. Do not describe this module as permissively licensed without reviewing all dependencies and existing rights.

This adaptation is not a claim that menu ownership by itself proves the currently visible Reel. Full active view-to-media identity association requires independent runtime evidence before actions can be enabled.
