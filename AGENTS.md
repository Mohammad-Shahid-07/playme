# PlayMe AI Agent & Development Guidelines

> **CRITICAL DIRECTIVE FOR ALL AI AGENTS & CONTRIBUTORS**  
> Read and adhere strictly to these rules before writing, modifying, or refactoring code in this repository.

---

## 1. Zero Tolerance for Monolithic Files

* **Strict File Size Limits:**
  * **Target:** Under 200 lines per file.
  * **Hard Ceiling:** 300 lines max.
* **Never create god-files:**
  * Do NOT create monolithic components like the legacy `LibraryScreen.kt` (79 KB), `PlayerSettingsSidebar.kt` (70 KB), `PlayMePlayerController.kt` (50 KB), or `PlayerControlsHud.kt` (48 KB).
* **Decompose Aggressively:**
  * Every HUD indicator, drawer, settings tab, bottom sheet, gesture overlay, and control bar **must** live in its own dedicated, focused file with a single responsibility.
  * If a component starts growing past 200 lines, extract its sub-views and helpers into separate files immediately.

---

## 2. Open-Source Reference Standard: Consult VLC & Proven Repositories

Before implementing any complex media player logic, **always study and reference proven open-source implementations**, starting with our local VLC Android reference in:
```
vlc-android-master/vlc-android-master/application/vlc-android/
```

### Specific VLC Reference Implementations:
| Player Domain | VLC Reference File | What to Learn / Adopt |
| :--- | :--- | :--- |
| **Touch & Gestures** | [`org/videolan/vlc/gui/video/VideoTouchDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoTouchDelegate.kt) | Swipe scrub seek, left-side brightness, right-side volume, deadzones, pinch-to-zoom, and 2x speed boost. |
| **Aspect Ratio & Resizing** | [`org/videolan/vlc/gui/video/VideoPlayerResizeDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerResizeDelegate.kt) | Scaling modes (Best Fit, Fit Screen, Fill, 16:9, 4:3, Original) without video distortion. |
| **Audio & Subtitle Sync** | [`org/videolan/vlc/gui/video/VideoDelayDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoDelayDelegate.kt) | Real-time audio track switching, subtitle delay offsets (±50ms steps), and track parsing. |
| **Orientation & Sensors** | [`org/videolan/vlc/gui/video/VideoPlayerOrientationDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerOrientationDelegate.kt) | Sensor locking, auto-rotation handling, and layout stability during orientation changes. |
| **Overlay HUD Controls** | [`org/videolan/vlc/gui/video/VideoPlayerOverlayDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerOverlayDelegate.kt) | Timed auto-hide of controls (typically 3–4 seconds), lock states, and fade transitions. |
| **PiP & Floating Window** | [`org/videolan/vlc/gui/video/PopupManager.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/PopupManager.kt) | Android PiP lifecycle, draggable overlay window, and permission checks. |
| **Background Playback** | [`org/videolan/vlc/PlaybackService.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/PlaybackService.kt) | Foreground media service, Android 14+ `mediaPlayback` type, audio focus management, and media notification controls. |

* Also consult other gold-standard open-source Android players when relevant: **JustPlayer**, **NewPipe**, and official **AndroidX Media3 (ExoPlayer)** samples.

---

## 3. UI Fidelity Mandate: `ui_prototype` is the Single Source of Truth

* The React prototype in [`ui_prototype/`](file:///d:/Web-Dev/react-native/playme/ui_prototype) is the active design standard.
* **Never guess styling:**
  * Before building any Kotlin screen or component, inspect the corresponding prototype component in [`ui_prototype/src/components/`](file:///d:/Web-Dev/react-native/playme/ui_prototype/src/components).
  * Use the design tokens from [`ui_prototype/src/index.css`](file:///d:/Web-Dev/react-native/playme/ui_prototype/src/index.css):
    * **Surfaces:** Obsidian Dark (`#0C0E18`), Obsidian Chassis (`#0C0D16`), Workbench (`#080910`).
    * **Glass:** Elevated Glass (`rgba(11, 13, 23, 0.92)`), Glass Card (`rgba(14, 16, 28, 0.40)`), Glass Border (`rgba(255, 255, 255, 0.08)`).
    * **Accents:** Electric Violet (`#757BF8`), Electric Indigo (`#6366F1`), Deep Violet (`#8B5CF6`), Lavender Glow (`rgba(117, 123, 248, 0.5)`).
    * **Status:** Amber Boost (`#F59E0B`), Emerald Active (`#00D287`), Crimson Alert (`#FF334B`).
  * Never use default plain Material 3 colors or generic red/blue/green components. Everything must preserve the signature **Obsidian Violet Liquid Glass** aesthetic.

---

## 4. Architecture: Unidirectional Data Flow (MVI)

```
UI Composables ──(PlayerAction)──> PlayerViewModel ──(StateFlow<PlayerState>)──> UI Composables
                                          │
                                          └──(Command)──> Player Engine / Controller
```

* **Composables are Pure Views:**
  * Composables must receive immutable state (e.g. `PlayerState`) and emit typed actions (e.g. `PlayerAction`).
  * **Never** call ExoPlayer, ContentResolver, or system services directly inside UI composables.
  * **Never** mutate mutable state across files or create global singletons holding screen state.
* **State Logic Belongs in ViewModel:**
  * All state transitions, timers, and validations must reside in the ViewModel or isolated domain delegates.

---

## 5. Development Workflow & Safety

1. **Explain Before Executing:** Always explain planned actions in plain English before running terminal commands or modifying files.
2. **Use Bun for JS/TS:** When working inside `ui_prototype/`, always use `bun` (not `npm`, `yarn`, or `pnpm`).
3. **No Direct Environment File Access:** Never view or parse `.env` files.
4. **Git Hygiene:** Commit clean atomic changes. Never delete large swaths of code without archiving or committing the prior state to Git first.
