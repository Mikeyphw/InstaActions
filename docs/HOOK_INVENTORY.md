# Hook discovery ledger

Do not record private conversations, cookies or Instagram credentials.

| ID | Screen | Action | IG version | DexKit signature / description | Probe type | Confidence | Hook clash? | Result |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| media-context-01 | Reels | Read visible media identity | TBD | TBD | Read-only | UNVERIFIED | TBD | NOT_RUN |
| ui-action-01 | Reels | Attach quick button | TBD | TBD | UI | UNVERIFIED | TBD | NOT_RUN |
| native-send-01 | Share | Locate verified native dispatcher | TBD | TBD | Read-only | UNVERIFIED | TBD | NOT_RUN |

Record a separate row per Instagram build and a short redacted failure diagnostic when lookup is ambiguous.

## R1 discovery candidate inventory (not yet device-verified)

The five markers below originate from InstaEclipse upstream; they identify likely
methods/classes but do **not** establish safe action dispatchers or media identity.
No network calls, sharing, posting, UI injection or Instagram method hooks are installed.

| Probe ID | Version qualification | Marker | Kind | Status |
| --- | --- | --- | --- | --- |
| reel_options | 451/453 | ClipsOrganicMediaItemViewMoreOptionsController | method | NOT_RUN |
| story_item_bound | 451/453 | ReelViewerFragment.onCurrentActiveItemBound | method | NOT_RUN |
| story_menu | 451/453 | [INTERNAL] Pause Playback | method | NOT_RUN |
| feed_video_player | 451/453 | IgFeedVideoPlayer: Persistent Visible Cover Image | class | NOT_RUN |
| playback_started | 451/453 | Playback started | method | NOT_RUN |

A UNIQUE DexKit result alone does not prove runtime functionality.
The hook inventory must be populated from actual device logs, and collisions
assessed with InstaEclipse enabled before R2.
