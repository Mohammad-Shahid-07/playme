# PlayMe AI Agent & Development Guidelines

See full instructions and reference guide in [AGENTS.md](file:///d:/Web-Dev/react-native/playme/AGENTS.md).

### Summary Directives
1. **Never create monolithic files:** Strict ceiling of 200–300 lines per file. All HUDs, drawers, and overlays must be atomic.
2. **Consult Open-Source References:** Always reference the local VLC Android source code in `vlc-android-master/` for gesture handling, audio/subtitle sync, background services, and scaling modes.
3. **UI Fidelity Mandate:** `ui_prototype` is the single source of truth for design tokens, animations, and layouts.
4. **Unidirectional Data Flow:** Use pure MVI (`PlayerAction` -> `PlayerViewModel` -> `PlayerState`). Never mutate player state directly inside composables.
