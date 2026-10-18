# Video Player Semi-Transparency Rule

## Mandatory Rule for all Video Player Overlays

Any component, modal, drawer, popup, HUD, capsule, button, or overlay that renders inside or on top of the Video Player canvas **MUST be semi-transparent** matching the Queue drawer and Settings drawer standard. Never use opaque or high-opacity backgrounds (such as `0.85+` or solid colors).

### Liquid Glass Standard Specifications:
- **Background**: `rgba(10, 13, 24, 0.65)` (or `var(--player-glass-bg)`)
- **Backdrop Filter**: `blur(28px) saturate(190%)` (or `var(--player-glass-blur)`)
- **Webkit Backdrop Filter**: `blur(28px) saturate(190%)`
- **Border**: `1px solid rgba(255, 255, 255, 0.1)` (or `var(--player-glass-border)`)
- **Shadow**: `0 16px 45px rgba(0, 0, 0, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.08)` (or `var(--player-glass-shadow)`)

### Applies to:
- `QuickSpeedBoostHUD` (temporary hold speed booster)
- `PlaybackSpeedMenu` (precision speed menu / studio)
- `ScreenGestureSeekHUD` (seek pill, locked alert toast)
- `QueueDrawer` (playing queue drawer)
- `PlayerSettingsSidebar` (player settings drawer)
- Top bar elements (`.player-back-btn`, `.player-top-utility-capsule`)
- Bottom deck elements (`.player-upnext-pill`, `.player-transport-capsule`, `.player-tools-capsule`)
- Floating docks (`.player-side-capsule`)
- Subtitle pills (`.player-subtitle-pill`)
- Any future video player micro-components or overlays
