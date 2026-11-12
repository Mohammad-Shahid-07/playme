# PlayMe

**Obsidian Violet Liquid Glass** Android media player built with Kotlin, Jetpack Compose, Media3 (ExoPlayer), LibVLC, and FFmpegKit.

> **Status:** `v0.01 (Pre-release)` (`versionCode = 1`, `minSdk = 26` / Android 8.0+, `targetSdk = 35`)  
> **Repository:** [`Mohammad-Shahid-07/playme`](https://github.com/Mohammad-Shahid-07/playme)

### Screenshots (`v0.01` on Device)

| Library Home | Auto-Grouped Series | Series Episodes |
| :---: | :---: | :---: |
| <img src="docs/screenshots/library-home.png" width="220" alt="Library Home" /> | <img src="docs/screenshots/series-tab.png" width="220" alt="Series Tab" /> | <img src="docs/screenshots/series-detail.png" width="220" alt="Series Detail" /> |

| Player HUD | Playback Speed Studio | Settings Sidebar | Queue Sheet |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/player-hud.png" width="180" alt="Player HUD" /> | <img src="docs/screenshots/player-speed-studio.png" width="180" alt="Speed Studio" /> | <img src="docs/screenshots/player-settings.png" width="180" alt="Player Settings" /> | <img src="docs/screenshots/player-queue.png" width="180" alt="Queue Bottom Sheet" /> |

---

## Overview

PlayMe pairs a dark-glass Jetpack Compose UI (**Obsidian Violet Liquid Glass**, defined in [`DESIGN.md`](DESIGN.md) and [`ui_prototype/`](ui_prototype/)) with a dual-engine native playback stack. It scans device storage progressively, groups multi-episode shows deterministically into series, extracts thumbnails even for streams skipped by platform decoders (such as 10-bit HEVC), and keeps playback responsive across Android 8.0 through Android 15 (including Android 9 / API 28 and display-cutout devices).

---

## Verified Capabilities (`v0.01`)

### Dual-Engine Playback (`UniversalPlayerEngine`)
* **Primary Hardware Engine ([`PlayMePlayerEngine`](app/src/main/java/com/playme/player/core/player/PlayMePlayerEngine.kt)):** AndroidX Media3 ExoPlayer (`1.5.1`) configured with `DefaultRenderersFactory` (`EXTENSION_RENDERER_MODE_ON` and decoder fallback enabled).
* **Universal Fallback & Software Engine ([`VlcPlayerEngine`](app/src/main/java/com/playme/player/core/player/VlcPlayerEngine.kt)):** LibVLC (`3.6.5`) powers `SW` decoder mode, automatic fallback if ExoPlayer encounters a playback error, and upfront routing for `.ts` and `.m2ts` transport streams.
* **MPEG-TS PTS Normalization:** [`UniversalPlayerEngine`](app/src/main/java/com/playme/player/core/player/UniversalPlayerEngine.kt) detects large non-zero initial PTS offsets (`> 5,000 ms`) on `.ts`/`.m2ts` captures and normalizes timeline position and duration back to `00:00`.
* **Audio Focus & Headset Disconnect Protection ([`PlayerAudioFocusEffect`](app/src/main/java/com/playme/player/feature/player/ui/PlayerAudioFocusEffect.kt)):** Acts as the single audio-focus owner (`AudioFocusRequest` on API 26+ with main-thread `Handler`) across both engines, pausing on focus loss/transient loss (resuming on `AUDIOFOCUS_GAIN` when paused by focus) and pausing on `ACTION_AUDIO_BECOMING_NOISY`.

### FFmpegKit Thumbnails & Seek-Bar Frame Previews
* **Four-Tier Cover Pipeline ([`ThumbnailDecoder`](app/src/main/java/com/playme/player/core/scanner/ThumbnailDecoder.kt), [`ThumbnailManager`](app/src/main/java/com/playme/player/core/scanner/ThumbnailManager.kt)):**
  1. App disk cache (`cacheDir`) + in-memory `LruCache` + Coil 3 (`coil-video`).
  2. System thumbnails (`ContentResolver.loadThumbnail` on API 29+, `MediaStore.Video.Thumbnails.getThumbnail` on API 26–28).
  3. `MediaMetadataRetriever` at 10% duration (with dark-frame detection and `.nomedia` path fallback).
  4. Software frame extraction via [`FfmpegFrameExtractor`](app/src/main/java/com/playme/player/core/scanner/FfmpegFrameExtractor.kt) (`ffmpeg-kit-lts-min-16kb` `6.1.4`) for streams platform decoders reject, such as 10-bit HEVC.
* **On-Demand Seek Previews ([`SeekPreviewFrames`](app/src/main/java/com/playme/player/core/scanner/SeekPreviewFrames.kt), [`SeekPreviewBubble`](app/src/main/java/com/playme/player/feature/player/ui/SeekPreviewBubble.kt)):** Splits the timeline into `24` buckets and decodes `160×90` letterboxed preview frames on demand using per-bucket locking and atomic temp-file writes.

### Progressive Two-Stage Library & Deterministic Series Grouping
* **Two-Stage Discovery ([`VideoRepository`](app/src/main/java/com/playme/player/core/repository/VideoRepository.kt), [`MediaStoreScanner`](app/src/main/java/com/playme/player/core/scanner/MediaStoreScanner.kt), [`DeepStorageScanner`](app/src/main/java/com/playme/player/core/scanner/DeepStorageScanner.kt)):**
  * Emits cached snapshots immediately (`LibrarySnapshotStore`) while stripping deleted files.
  * Queries `MediaStore.Video.Media` (with `RESOLUTION` fallback on API 28 rows missing `WIDTH`/`HEIGHT`) and watches for media changes via a debounced `ContentObserver`.
  * Recursively walks storage (up to depth `7`, files `>= 500 KB` across `mp4`, `mkv`, `webm`, `avi`, `mov`, `flv`, `3gp`, `m4v`, `ts`, `wmv`, `mpg`, `mpeg`) to index videos inside `.nomedia` folders such as Telegram and WhatsApp, memoizing metadata in `MediaMetadataStore`.
* **Cross-Version Storage Access ([`StorageAccess`](app/src/main/java/com/playme/player/core/permissions/StorageAccess.kt), [`MainActivity`](app/src/main/java/com/playme/player/MainActivity.kt)):** Uses `MANAGE_EXTERNAL_STORAGE` on Android 11+ (API 30+) and runtime `READ_EXTERNAL_STORAGE` (with rationale/App Info fallback and `requestLegacyExternalStorage="true"` on API 29) on Android 8.0–10 (API 26–29).
* **Deterministic Series Grouping ([`TitleNormalizer`](app/src/main/java/com/playme/player/core/grouping/TitleNormalizer.kt), [`EpisodeParser`](app/src/main/java/com/playme/player/core/grouping/EpisodeParser.kt), [`SeriesGrouper`](app/src/main/java/com/playme/player/core/grouping/SeriesGrouper.kt)):** Recognizes `S01E02`, `S01EP02`, `1x02`, `Season X Episode Y`, `[01-13]`, `EP02`, `Episode 2`, `Part 1`, `CD1`, `Lecture 05`, and bare trailing numbers (excluding camera prefixes like `VID 001` and grade labels like `Class 10`). Groups by normalized title stem *after* stripping markers and release tags, requires at least 2 episodes per series, isolates remakes by year, and prevents shared-prefix false merges (`S01_ShowA_E01` vs `S01_ShowB_E01`).
* **Library Views & History:** `Videos`, `Folders`, and `Series` tabs in a `HorizontalPager`, List/2-column Grid toggle, sorting (`Newest`, `Name`, `Size`, `Duration`), real-time search by title or folder, and a `Continue Watching` carousel persisted asynchronously in [`WatchProgressStore`](app/src/main/java/com/playme/player/core/history/WatchProgressStore.kt).

### Player HUD, Gestures & Controls
* **Gesture Layer ([`PlayerGestureDetector`](app/src/main/java/com/playme/player/feature/player/ui/PlayerGestureDetector.kt), [`PlayerGestureHandlers`](app/src/main/java/com/playme/player/feature/player/ui/PlayerGestureHandlers.kt), [`GestureOverlayHUD`](app/src/main/java/com/playme/player/feature/player/ui/GestureOverlayHUD.kt)):**
  * Single tap toggles HUD (auto-hides after `3.5s` during playback); double-tap outer thirds (`30%`) seeks `-10s` / `+10s`, center toggles play/pause.
  * Left vertical swipe adjusts screen brightness; right vertical swipe adjusts device `STREAM_MUSIC` volume (bidirectionally synced via `ContentObserver` without double-attenuating engine output); horizontal swipe scrubs the timeline. Includes diagonal slope guards and system-edge deadzones.
  * Long-press (`280 ms` hold) activates hold-to-boost playback (`2.0x` default), and dragging horizontally selects across `0.25x`, `0.5x`, `0.75x`, `1.5x`, `2.0x`, `3.0x`, and `4.0x` ([`QuickSpeedBoostHUD`](app/src/main/java/com/playme/player/feature/player/ui/QuickSpeedBoostHUD.kt)).
* **Speed Studio Dock ([`PlaybackSpeedStudioDock`](app/src/main/java/com/playme/player/feature/player/ui/PlaybackSpeedStudioDock.kt), [`PlaybackSpeedSlider`](app/src/main/java/com/playme/player/feature/player/ui/PlaybackSpeedSlider.kt)):** Tactile speed ruler and precision slider from `0.5x` to `4.0x` (plus `0.25x–4.0x` hold-boost presets) with a `1.0x` reset pill.
* **Settings Sidebar ([`PlayerSettingsSidebar`](app/src/main/java/com/playme/player/feature/player/ui/PlayerSettingsSidebar.kt), [`SettingsRootView`](app/src/main/java/com/playme/player/feature/player/ui/SettingsRootView.kt), [`PlayerSettingsSubViews`](app/src/main/java/com/playme/player/feature/player/ui/PlayerSettingsSubViews.kt)):**
  * **Audio:** Track switching, Audio Sync delay (`±5000ms` range in `±0.1s` / `±0.5s` steps with independent Reset), and Volume Boost (`100%–200%`, up to `8,000 mB` / `+80 dB` target gain via `LoudnessEnhancer` on ExoPlayer or `0–200` software volume on LibVLC).
  * **Subtitles:** Track switching, Font Size (`Normal`, `Large`, `Extra Large`), Subtitle Sync delay (`±0.2s` steps with independent Reset), and on-screen subtitle pill.
  * **Playback & Display:** `HW` (Media3) / `SW` (LibVLC) engine switch, Sleep Timer (`Off`, `15m`, `30m`, `45m`, `60m`), Aspect Ratio (`Best Fit`, `Fit Screen`, `Fill`, `16:9`, `4:3`, `Original`), Ambient Edge Glow (API 31+ `Modifier.blur` with API 26–30 `Canvas` radial fallback), and Screen Brightness (`10%–100%` / `Auto`).
* **Queue, Side Dock & Fullscreen ([`QueueBottomSheet`](app/src/main/java/com/playme/player/feature/player/ui/QueueBottomSheet.kt), [`PlayerSideDock`](app/src/main/java/com/playme/player/feature/player/ui/PlayerSideDock.kt), [`PlayerScreen`](app/src/main/java/com/playme/player/feature/player/ui/PlayerScreen.kt)):**
  * Queue drawer with Now Playing, Up Next list, remove / Clear All, Shuffle (preserving/restoring pre-shuffle order), Repeat (`off`, `all`, `one`), and Autoplay.
  * Side dock with Mute, Controls Lock, A-B Loop (`+5s` segment loop from marked start), and Rotate / Fullscreen toggle (`WindowInsetsControllerCompat` + `values-v28` `shortEdges` cutout support).

### Architecture
* **Modular MVI:** Immutable `StateFlow` UI states (`LibraryState`, `PlayerState`) and typed `LibraryAction` / `PlayerAction` intents routed through `LibraryViewModel` and `PlayerViewModel` to focused delegates (`PlayerTransportController`, `PlayerQueueController`, `PlayerSessionEffects`, `PlayerSettingsDelegate`).
* **File-Size Discipline:** Every Kotlin file under `app/src/main/java` stays within [`AGENTS.md`](AGENTS.md)'s 300-line ceiling (`<= 296` lines).

---

## v0.01 Pre-release Scope & Known Limitations

PlayMe `v0.01` is an early pre-release focused on local video discovery and playback. The following areas are **not yet implemented** or are UI placeholders:

* **Navigation Tabs & Center Play Button:** Only the **Video** tab (`LibraryScreen`) is active; **Music**, **Browse**, and **Settings** render placeholder cards ([`MainTabPlaceholderView`](app/src/main/java/com/playme/player/feature/navigation/MainTabPlaceholderView.kt)), and the center dock Play button in [`BottomNavBar`](app/src/main/java/com/playme/player/feature/navigation/BottomNavBar.kt) is currently unwired.
* **Vault, Overflow Menu & Frame Snapshot:** The Private Vault lock button in [`LibraryTopBar`](app/src/main/java/com/playme/player/feature/library/ui/LibraryTopBar.kt) and item overflow menus are not yet wired, and the Frame Snapshot button in [`PlayerSideDock`](app/src/main/java/com/playme/player/feature/player/ui/PlayerSideDock.kt) currently displays a toast without saving an image.
* **Background Playback, PiP & Floating Window:** While permissions and `supportsPictureInPicture="true"` are declared in [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml), `PlaybackService`, `MediaSession` media notifications, Picture-in-Picture lifecycle handling, and floating pop-up windows are planned for future milestones (the `Background Audio` and `Dialogue Clarity` toggles in Player Settings currently update UI state/toasts only).
* **External File Intents:** Launching PlayMe from external file managers via `ACTION_VIEW` intents is planned for a future milestone.
* **Signing:** Pre-release `assembleRelease` APKs are signed with the `localRelease` debug-keystore configuration in [`app/build.gradle.kts`](app/build.gradle.kts) (`~/.android/debug.keystore`) rather than a production release key.

---

## Installation (`v0.01` Pre-release)

PlayMe splits APKs by ABI because LibVLC and FFmpegKit bundle native libraries per architecture (avoiding a ~115 MB universal APK; `x86`/`x86_64` emulator ABIs are excluded).

### Which APK Should I Download?

| APK File | ABI | Size (bytes) | SHA-256 Checksum | Device Compatibility |
| :--- | :--- | ---: | :--- | :--- |
| `PlayMe-v0.01-arm64-v8a.apk` | `arm64-v8a` | `33,882,401` | `7a2a40c25067c33609ca20dc04cd687826e7bc1c7e521e83dc27270dd8a6e4d8` | **Recommended** for most modern 64-bit ARM phones & tablets (Android 8.0+). |
| `PlayMe-v0.01-armeabi-v7a.apk` | `armeabi-v7a` | `30,846,902` | `e510ff194bfded85c2db076b8d025208e9b1e1edaa8120daa14bf20d5f116f81` | Older 32-bit ARM devices or 32-bit Android OS builds. |

### Verify Checksums Before Installing

On Linux / macOS:
```bash
sha256sum PlayMe-v0.01-*.apk
```

On Windows PowerShell:
```powershell
Get-FileHash .\PlayMe-v0.01-*.apk -Algorithm SHA256
```

Enable **Install unknown apps** for your browser or file manager if prompted, then install the APK matching your device ABI.

---

## Building from Source

### Android App (`:app`)

**Requirements:**
* Android SDK (`compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`) configured in `local.properties` (or `ANDROID_HOME`)
* JDK 21 (`JavaVersion.VERSION_21` in [`app/build.gradle.kts`](app/build.gradle.kts); [`gradle.properties`](gradle.properties) configures `org.gradle.java.home` for Android Studio's JBR on Windows)
* A keystore at `~/.android/debug.keystore` (`storePassword = "android"`, `keyAlias = "androiddebugkey"`, `keyPassword = "android"`) for the `localRelease` signing configuration in [`app/build.gradle.kts`](app/build.gradle.kts)

**Build Release APKs (R8 minified + ABI-split):**

* **macOS / Linux:**
  ```bash
  ./gradlew assembleRelease
  ```
* **Windows (PowerShell / CMD):**
  ```powershell
  .\gradlew.bat assembleRelease
  ```

Split release APKs are written to `app/build/outputs/apk/release/` (`app-arm64-v8a-release.apk` and `app-armeabi-v7a-release.apk`).

**Run Android Lint:**
```powershell
.\gradlew.bat lintRelease
```
*(Use `./gradlew lintRelease` on macOS/Linux.)*

> **Note on Release Signing:** [`app/build.gradle.kts`](app/build.gradle.kts) currently configures `signingConfigs.create("localRelease")` using `~/.android/debug.keystore` for local release benchmarking. Replace this with your own keystore configuration in `app/build.gradle.kts` before distributing production builds.

### Design Prototype (`ui_prototype/`)

The interactive React + TypeScript design prototype in [`ui_prototype/`](ui_prototype/) uses [`bun`](https://bun.sh) (see [`ui_prototype/package.json`](ui_prototype/package.json)):

```bash
cd ui_prototype
bun install
bun run dev      # Start Vite dev server
bun run build    # Type-check (tsc -b) and build production bundle
bun run lint     # Run oxlint
bun run preview  # Preview production build
```
