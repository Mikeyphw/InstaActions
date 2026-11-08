# Privacy and safety constraints

- Private repo and local-first settings by default.
- Do not ship Instagram credentials, access tokens, cookies, private messages, recipient IDs in logs or test fixtures.
- Do not create custom Instagram authentication or direct HTTP APIs.
- Saved recipient aliases and comment pools are per Instagram account; purge them or require explicit migration when switching accounts.
- One-tap quick send or comment requires a deliberate user gesture; add an optional confirmation toggle and a strong duplicate-submission guard.
- Random comments are chosen from user-authored lists; no unattended bulk posting or network retry after an uncertain outcome.
- Diagnostic export is opt-in and redacted; retain only minimum metadata needed for debugging.
- Release APKs require checksum/provenance verification. Never embed signing private keys in repo.
- Source copied from GPL-3.0 InstaEclipse must be separately reviewed for license implications; architecture is intended to remain independent.
