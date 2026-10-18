# VLC Gesture Disambiguation & Touch Controls Reference

Reference source: [`VideoTouchDelegate.kt`](file:///d:/Web-Dev/react-native/playme/vlc-android-master/vlc-android-master/application/vlc-android/src/org/videolan/vlc/gui/video/VideoTouchDelegate.kt)

---

## 1. Constants & Flags

```kotlin
const val TOUCH_FLAG_AUDIO_VOLUME    = 1
const val TOUCH_FLAG_BRIGHTNESS      = 1 shl 1
const val TOUCH_FLAG_DOUBLE_TAP_SEEK = 1 shl 2
const val TOUCH_FLAG_PLAY            = 1 shl 3
const val TOUCH_FLAG_SWIPE_SEEK      = 1 shl 4
const val TOUCH_FLAG_SCALE           = 1 shl 6
const val TOUCH_FLAG_FASTPLAY        = 1 shl 7

// Gesture States
private const val TOUCH_NONE       = 0
private const val TOUCH_VOLUME     = 1
private const val TOUCH_BRIGHTNESS = 2
private const val TOUCH_MOVE       = 3 // Horizontal Scrub Seek
private const val TOUCH_TAP_SEEK   = 4
private const val TOUCH_FASTPLAY   = 7
```

---

## 2. Touch Arbitration Algorithm

When `ACTION_MOVE` events fire, VLC arbitrates gesture direction using slope calculation:

```kotlin
val xChanged = event.x - touchX
val yChanged = event.y - touchY

// Ratio of vertical movement to horizontal movement
val coef = (yChanged / xChanged).absoluteValue

// Deadzone checks
val deltaY = (((initTouchY - event.y).absoluteValue / screenConfig.metrics.xdpi + 0.5f) * 2f).coerceAtLeast(1f)
```

### Decision Rules:
1. **Vertical Gesture (Volume or Brightness):**
   * Condition: `coef > 2.0` (more than 2x as vertical as horizontal) AND `deltaY` exceeds deadzone threshold (5% screen height).
   * Left side of screen (`event.x < screenWidth / 2`): Adjust **Brightness**.
   * Right side of screen (`event.x >= screenWidth / 2`): Adjust **Audio Volume**.

2. **Horizontal Gesture (Swipe Scrub Seek):**
   * Condition: `coef <= 2.0` AND horizontal displacement > `8dp` to `16dp`.
   * Sensitivity: `val timeDelta = deltaX * sensitivity` (typically `0.35` multiplier).

3. **Temporary 2x Speed Boost (`TOUCH_FASTPLAY`):**
   * In `ACTION_DOWN`, start a `250ms` timer (`fastPlayRunnable`).
   * If movement exceeds deadzone before timer fires, cancel the timer.
   * If timer fires without significant movement:
     - Record `savedRate = currentSpeed`.
     - Set `currentSpeed = 2.0f`.
     - Hide overlay HUD controls to maximize viewing area.
     - Show floating "2.0x Boost" indicator.
   * On `ACTION_UP` or `ACTION_CANCEL`:
     - Restore `currentSpeed = savedRate`.
     - Dismiss boost indicator.

4. **Pinch-to-Zoom:**
   * Handled by Android's `ScaleGestureDetector`.
   * When `scaleGestureDetector.isInProgress` is true, touchAction is set to `TOUCH_IGNORE` so single-finger scrub/volume logic does not trigger.
