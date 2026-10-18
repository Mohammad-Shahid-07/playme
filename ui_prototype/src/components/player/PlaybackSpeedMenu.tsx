import React, { useEffect, useRef, useState, useCallback, type FC } from 'react';
import './PlaybackSpeedMenu.css';

export interface PlaybackSpeedMenuProps {
  isOpen: boolean;
  onClose: () => void;
  speed: number;
  onSpeedChange: (speed: number) => void;
}

// 8 benchmark speed presets
const ANCHORS = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 3.0, 4.0];
const NUM_SEGMENTS = ANCHORS.length - 1; // 7 intervals

export const formatSpeed = (s: number): string => {
  return `${s.toFixed(2)}×`;
};

// Map a speed value to a normalized position [0, 1] across the 7 segments
const speedToFraction = (speed: number): number => {
  if (speed <= ANCHORS[0]) return 0;
  if (speed >= ANCHORS[ANCHORS.length - 1]) return 1;

  for (let i = 0; i < NUM_SEGMENTS; i++) {
    const a = ANCHORS[i];
    const b = ANCHORS[i + 1];
    if (speed >= a && speed <= b) {
      const segFraction = (speed - a) / (b - a);
      return (i + segFraction) / NUM_SEGMENTS;
    }
  }
  return 0;
};

// Map a normalized position [0, 1] to a speed value (rounded to 0.05)
const fractionToSpeed = (fraction: number): number => {
  const clamped = Math.min(1, Math.max(0, fraction));
  const t = clamped * NUM_SEGMENTS;
  const i = Math.min(NUM_SEGMENTS - 1, Math.floor(t));
  const segFraction = t - i;
  const a = ANCHORS[i];
  const b = ANCHORS[i + 1];
  const rawSpeed = a + segFraction * (b - a);

  // Magnetic snap if very close to an anchor (within 0.03)
  for (const anchor of ANCHORS) {
    if (Math.abs(rawSpeed - anchor) < 0.03) {
      return anchor;
    }
  }

  // Round to 0.05
  return Math.round(rawSpeed * 20) / 20;
};

export const PlaybackSpeedMenu: FC<PlaybackSpeedMenuProps> = ({
  isOpen,
  onClose,
  speed,
  onSpeedChange,
}) => {
  const trackRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  const updateFromPointer = useCallback(
    (clientX: number) => {
      if (!trackRef.current) return;
      const rect = trackRef.current.getBoundingClientRect();
      const fraction = (clientX - rect.left) / rect.width;
      const newSpeed = fractionToSpeed(fraction);
      onSpeedChange(newSpeed);
    },
    [onSpeedChange]
  );

  const handlePointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0) return;
    setIsDragging(true);
    try {
      e.currentTarget.setPointerCapture(e.pointerId);
    } catch {
      // ignore
    }
    updateFromPointer(e.clientX);
  };

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!isDragging) return;
    updateFromPointer(e.clientX);
  };

  const handlePointerUp = (e: React.PointerEvent<HTMLDivElement>) => {
    setIsDragging(false);
    try {
      e.currentTarget.releasePointerCapture(e.pointerId);
    } catch {
      // ignore
    }
  };

  if (!isOpen) return null;

  const currentFraction = speedToFraction(speed);
  const fillPercent = currentFraction * 100;

  return (
    <div className="speed-dock-overlay" onClick={onClose}>
      <div
        className="speed-master-panel"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Playback Speed Studio"
      >
        {/* Header: Micro-Label + Prominent Hero Readout + Reset */}
        <div className="speed-master-header">
          <div className="speed-master-title-box">
            <span className="material-symbols-rounded speed-master-icon">speed</span>
            <span className="speed-master-caption">PLAYBACK SPEED</span>
          </div>

          <div className="speed-master-readout-box">
            {speed !== 1.0 && (
              <button
                type="button"
                className="speed-master-reset-btn"
                onClick={() => onSpeedChange(1.0)}
                title="Reset to 1.00× Normal"
              >
                Reset 1.0×
              </button>
            )}
            <span className="speed-master-hero-val">{formatSpeed(speed)}</span>
          </div>
        </div>

        {/* Geometrically Synchronized Preset Labels (Aligned 1-to-1 with stops) */}
        <div className="speed-ruler-labels-wrap">
          {ANCHORS.map((anchor, idx) => {
            const anchorFraction = idx / NUM_SEGMENTS;
            const isSelected = Math.abs(speed - anchor) < 0.02;
            const label =
              anchor === 1.0
                ? '1.0×'
                : Number.isInteger(anchor)
                ? `${anchor}×`
                : `${anchor}×`;

            return (
              <button
                key={anchor}
                type="button"
                className={`speed-ruler-btn ${isSelected ? 'is-active' : ''}`}
                style={{ left: `${anchorFraction * 100}%` }}
                onClick={() => onSpeedChange(anchor)}
                title={`Set speed to ${label}`}
              >
                <span>{label}</span>
                {isSelected && <div className="speed-ruler-active-dot" />}
              </button>
            );
          })}
        </div>

        {/* Custom Tactile Precision Track */}
        <div
          ref={trackRef}
          className={`speed-master-slider-zone ${isDragging ? 'is-dragging' : ''}`}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          onPointerCancel={handlePointerUp}
          role="slider"
          aria-valuemin={0.5}
          aria-valuemax={4.0}
          aria-valuenow={speed}
          aria-label="Drag or click to adjust speed"
        >
          {/* Groove Track */}
          <div className="speed-master-track-groove">
            {/* Illuminated Gradient Fill */}
            <div
              className="speed-master-track-fill"
              style={{ width: `${fillPercent}%` }}
            />

            {/* Benchmark Tick Notches */}
            {ANCHORS.map((anchor, idx) => {
              const tickPos = (idx / NUM_SEGMENTS) * 100;
              const isPassed = currentFraction >= idx / NUM_SEGMENTS - 0.005;
              return (
                <div
                  key={anchor}
                  className={`speed-master-notch ${isPassed ? 'is-passed' : ''}`}
                  style={{ left: `${tickPos}%` }}
                />
              );
            })}
          </div>

          {/* Glowing Tactile Thumb */}
          <div
            className="speed-master-thumb"
            style={{ left: `${fillPercent}%` }}
          >
            <div className="speed-master-thumb-core" />
          </div>
        </div>
      </div>
    </div>
  );
};
