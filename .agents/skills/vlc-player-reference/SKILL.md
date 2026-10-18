---
name: vlc-player-reference
description: >-
  Provides a architectural cheat-sheet and index of the local VLC Android source code
  located in vlc-android-master/. Activate this skill whenever implementing or debugging
  video player logic, touch gestures (scrub, volume, brightness, 2x boost), aspect ratio
  scaling, audio/subtitle sync delays, Picture-in-Picture (PiP), background playback services,
  audio focus, or MediaSession handling.
---

# VLC Android Architecture & Implementation Reference

This skill documents the proven media player architecture from the local VLC Android reference codebase located at:
```
vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/
```

Use this reference to find exactly which file implements a given player feature, how the algorithms work, and how to adapt them cleanly into PlayMe's Jetpack Compose MVI architecture.

---

## 1. Quick File Map: Where to Look for What

| Player Domain | VLC Reference File Path | Key Concepts & Functions to Learn |
| :--- | :--- | :--- |
| **Touch Gestures & Seek** | [`gui/video/VideoTouchDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoTouchDelegate.kt) | Gesture disambiguation, deadzones, swipe scrub seek, left/right vertical split (brightness vs. volume), long-press 2x fastplay, pinch-to-zoom. |
| **Aspect Ratio & Resizing** | [`gui/video/VideoPlayerResizeDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerResizeDelegate.kt) | `ScaleType` modes (Best Fit, Fit Screen, Fill, 16:9, 4:3, Original), aspect ratio cycling without distortion. |
| **Audio & Subtitle Sync** | [`gui/video/VideoDelayDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoDelayDelegate.kt) | Delay offset steps (±50ms steps), audio/subtitle track switching, repeat touch listeners, Bluetooth audio compensation. |
| **Overlay Auto-Hide & Lock** | [`gui/video/VideoPlayerOverlayDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerOverlayDelegate.kt) | Auto-hide timer (typically 3–4 seconds timeout), locked controls state, fade-in/fade-out HUD transitions. |
| **PiP & Floating Window** | [`gui/video/PopupManager.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/PopupManager.kt) | Android PiP lifecycle, draggable floating overlay window (`SYSTEM_ALERT_WINDOW`), surface detaching and reattaching. |
| **Orientation & Sensors** | [`gui/video/VideoPlayerOrientationDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerOrientationDelegate.kt) | Sensor locking, auto-rotation handling, configuration change persistence. |
| **Background Playback Engine** | [`PlaybackService.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/PlaybackService.kt) | Foreground media service (`mediaPlayback` type), audio focus management, headset disconnect pause, media notification controls. |
| **MediaSession Integration** | [`MediaSessionCallback.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/MediaSessionCallback.kt) | MediaSessionCompat actions (play, pause, seekTo, skipToNext, custom speed actions). |
| **Queue & Playlist Manager** | [`media/PlaylistManager.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/media/PlaylistManager.kt) | Shuffling, repeat modes, track progression, queue persistence. |

---

## 2. Core Implementation Patterns to Adopt

### A. Gesture Disambiguation Algorithm (from `VideoTouchDelegate.kt`)
Never let horizontal scrub seek and vertical brightness/volume gestures conflict:
1. **Deadzone:** Ignore initial movement until delta > `8dp` (VLC uses `gestureSafetyMargin = 24dp` at edges).
2. **Direction Slope (`coef`):**
   ```kotlin
   val coef = abs(deltaY / deltaX)
   // If coef > 2.0: Pure vertical gesture
   // If coef <= 2.0: Pure horizontal seek gesture
   ```
3. **Screen Halves for Vertical Gestures:**
   * **Left Half (`x < screenWidth / 2`):** Brightness adjustment (`0.0f .. 1.0f`).
   * **Right Half (`x >= screenWidth / 2`):** Audio volume adjustment (`0% .. 100%`, or up to `200%` with software boost).
4. **Hold for 2x Speed Boost (`TOUCH_FASTPLAY`):**
   * Long-press hold timer (250ms–280ms).
   * Save original rate, temporarily boost to `2.0f`.
   * On release (`ACTION_UP`), restore saved original rate.
5. **Double Tap Seek:**
   * Left third: rewind 10s. Right third: forward 10s.
   * Timeout window: `750ms`.

### B. Aspect Ratio Modes (from `VideoPlayerResizeDelegate.kt`)
VLC defines 6 primary scaling behaviors:
* `BEST_FIT`: Letterbox/pillarbox to fit entire video within viewport preserving original aspect ratio.
* `FIT_SCREEN`: Crop edges to fill full screen without black bars.
* `FILL`: Stretch to exact viewport dimensions (ignores aspect ratio).
* `16:9` & `4:3`: Force specific standard aspect ratios.
* `ORIGINAL`: 1:1 pixel mapping with no scaling.

### C. Audio & Subtitle Sync Delays (from `VideoDelayDelegate.kt`)
* Stepping granularity: **`50ms` per step** (VLC uses microsecond value `50000L`).
* Range: typically `-5000ms` to `+5000ms`.
* Provide a "Reset to 0ms" button and a hold-to-repeat increment button.

### D. Overlay Auto-Hide (from `VideoPlayerOverlayDelegate.kt`)
* Reset a countdown timer (typically **3 to 4 seconds**) on any user touch or playback change.
* When timer fires, fade out HUD controls.
* If controls are locked (`isLocked`), block all actions and display a transient "Controls Locked" toast pill.

---

## 3. Adapting VLC to PlayMe's Jetpack Compose MVI Architecture

VLC uses Android Views, XML, and Delegates (`VideoPlayerActivity`). In PlayMe, we translate these into **clean, unidirectional Compose**:

```
UI Composables ──(PlayerAction)──> PlayerViewModel ──(StateFlow<PlayerState>)──> UI Composables
                                          │
                                          └──(Command)──> PlayerController (ExoPlayer)
```

1. **Keep UI Composables Pure:** UI composables only render `PlayerState` and emit `PlayerAction`.
2. **Extract Delegates:** Keep logic out of the composables; use isolated ViewModel delegates or use cases (e.g., `GestureDelegate`, `DelayDelegate`).
3. **Respect File Limits:** Maximum 200–300 lines per file. Never recreate god-composables.
