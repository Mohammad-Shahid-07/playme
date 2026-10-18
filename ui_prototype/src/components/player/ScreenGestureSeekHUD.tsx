import React, { useRef, useState, useCallback, type FC } from 'react';
import './ScreenGestureSeekHUD.css';

export interface ScreenGestureSeekHUDProps {
  currentTime: number; // in seconds
  duration: number; // in seconds
  onSeek: (targetTime: number) => void;
  isLocked?: boolean;
  onLongPressStart?: () => void;
  onLongPressEnd?: () => void;
}

interface GestureState {
  isDragging: boolean;
  startX: number;
  startTime: number;
  deltaX: number;
  timeDelta: number;
  targetTime: number;
}

const formatTime = (seconds: number): string => {
  const s = Math.max(0, Math.floor(seconds));
  const mins = Math.floor(s / 60);
  const secs = s % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

const formatDelta = (deltaSec: number): string => {
  const abs = Math.abs(Math.round(deltaSec));
  const sign = deltaSec >= 0 ? '+' : '-';
  const mins = Math.floor(abs / 60);
  const secs = abs % 60;
  if (mins > 0) {
    return `${sign}${mins}m ${secs}s`;
  }
  return `${sign}${secs}s`;
};

export const ScreenGestureSeekHUD: FC<ScreenGestureSeekHUDProps> = ({
  currentTime,
  duration,
  onSeek,
  isLocked = false,
  onLongPressStart,
  onLongPressEnd,
}) => {
  const [gesture, setGesture] = useState<GestureState | null>(null);
  const [showLockedAlert, setShowLockedAlert] = useState(false);
  const lockedTimeoutRef = useRef<number | null>(null);
  const longPressTimerRef = useRef<number | null>(null);
  const isLongPressingRef = useRef(false);

  const handlePointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0) return;

    if (isLocked) {
      setShowLockedAlert(true);
      if (lockedTimeoutRef.current) window.clearTimeout(lockedTimeoutRef.current);
      lockedTimeoutRef.current = window.setTimeout(() => setShowLockedAlert(false), 1200);
      return;
    }

    try {
      e.currentTarget.setPointerCapture(e.pointerId);
    } catch {
      // pointer capture fallback
    }

    // Start Long-Press Detection (280ms hold)
    if (onLongPressStart) {
      isLongPressingRef.current = false;
      if (longPressTimerRef.current) window.clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = window.setTimeout(() => {
        isLongPressingRef.current = true;
        setGesture(null);
        onLongPressStart();
      }, 280);
    }

    setGesture({
      isDragging: true,
      startX: e.clientX,
      startTime: currentTime,
      deltaX: 0,
      timeDelta: 0,
      targetTime: currentTime,
    });
  };

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!gesture || !gesture.isDragging) return;

    const deltaX = e.clientX - gesture.startX;

    // If moved > 8px, cancel long-press so normal swipe seeking can happen
    if (Math.abs(deltaX) > 8 && longPressTimerRef.current) {
      window.clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }

    // If long-press is active, do not seek
    if (isLongPressingRef.current) {
      return;
    }

    const sensitivity = 0.35;
    const timeDelta = deltaX * sensitivity;
    const targetTime = Math.min(duration, Math.max(0, gesture.startTime + timeDelta));

    setGesture((prev) => {
      if (!prev) return null;
      return {
        ...prev,
        deltaX,
        timeDelta,
        targetTime,
      };
    });
  };

  const finishGesture = useCallback(() => {
    if (gesture && Math.abs(gesture.deltaX) >= 8) {
      onSeek(gesture.targetTime);
    }
    setGesture(null);
  }, [gesture, onSeek]);

  const handlePointerUp = (e: React.PointerEvent<HTMLDivElement>) => {
    try {
      e.currentTarget.releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }

    if (longPressTimerRef.current) {
      window.clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }

    if (isLongPressingRef.current) {
      isLongPressingRef.current = false;
      onLongPressEnd?.();
      setGesture(null);
      return;
    }

    finishGesture();
  };

  const handlePointerCancel = (e: React.PointerEvent<HTMLDivElement>) => {
    try {
      e.currentTarget.releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }

    if (longPressTimerRef.current) {
      window.clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }

    if (isLongPressingRef.current) {
      isLongPressingRef.current = false;
      onLongPressEnd?.();
    }

    setGesture(null);
  };

  const isSeekingActive = gesture && Math.abs(gesture.deltaX) >= 8 && !isLongPressingRef.current;
  const isForward = gesture ? gesture.timeDelta >= 0 : true;

  return (
    <div
      className={`gesture-seek-surface ${isSeekingActive ? 'is-active-drag' : ''}`}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerCancel}
      role="region"
      aria-label="Swipe horizontally to seek or press and hold for speed boost"
    >
      {/* Locked Alert Toast */}
      {showLockedAlert && (
        <div className="gesture-locked-toast">
          <span className="material-symbols-rounded">lock</span>
          <span>Controls Locked</span>
        </div>
      )}

      {/* Sleek Minimal Floating Pill HUD */}
      {isSeekingActive && gesture && (
        <div className="gesture-seek-pill">
          <span
            className={`material-symbols-rounded gesture-pill-icon ${
              isForward ? 'is-forward' : 'is-backward'
            }`}
          >
            {isForward ? 'fast_forward' : 'fast_rewind'}
          </span>

          <span
            className={`gesture-pill-delta ${
              isForward ? 'delta-forward' : 'delta-backward'
            }`}
          >
            {formatDelta(gesture.timeDelta)}
          </span>

          <div className="gesture-pill-divider" />

          <div className="gesture-pill-time-group">
            <span className="gesture-pill-target">{formatTime(gesture.targetTime)}</span>
            <span className="gesture-pill-duration">/ {formatTime(duration)}</span>
          </div>
        </div>
      )}
    </div>
  );
};
