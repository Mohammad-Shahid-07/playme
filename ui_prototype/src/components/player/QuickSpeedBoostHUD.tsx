import React, { useRef, useCallback, type FC } from 'react';
import './QuickSpeedBoostHUD.css';

export interface QuickSpeedBoostHUDProps {
  isActive: boolean;
  boostSpeed: number;
  onSelectBoostSpeed: (speed: number) => void;
}

export const BOOST_PRESETS = [0.25, 0.5, 0.75, 1.5, 2.0, 3.0, 4.0];

export const formatBoostSpeed = (s: number): string => {
  return Number.isInteger(s) ? `${s}.0x` : `${s}x`;
};

export const QuickSpeedBoostHUD: FC<QuickSpeedBoostHUDProps> = ({
  isActive,
  boostSpeed,
  onSelectBoostSpeed,
}) => {
  const trackRef = useRef<HTMLDivElement>(null);

  const activeIndex = BOOST_PRESETS.reduce((closest, preset, idx) => {
    return Math.abs(preset - boostSpeed) < Math.abs(BOOST_PRESETS[closest] - boostSpeed)
      ? idx
      : closest;
  }, 3);

  const activeFraction = activeIndex / (BOOST_PRESETS.length - 1);

  const handleTrackPointerDown = useCallback(
    (e: React.PointerEvent<HTMLDivElement>) => {
      e.stopPropagation();
      if (!trackRef.current) return;
      const rect = trackRef.current.getBoundingClientRect();
      const fraction = Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width));
      const targetIdx = Math.round(fraction * (BOOST_PRESETS.length - 1));
      onSelectBoostSpeed(BOOST_PRESETS[targetIdx]);
    },
    [onSelectBoostSpeed]
  );

  if (!isActive) return null;

  return (
    <div
      className="speed-boost-hud-container"
      role="status"
      aria-live="polite"
      aria-label="Temporary playback speed boost"
    >
      <div className="speed-boost-hud-card">
        {/* Subtle top notch pointing upward */}
        <div className="speed-boost-notch" />

        {/* 1. Header: Lightning Bolt + Title */}
        <div className="speed-boost-header">
          <span className="material-symbols-rounded speed-boost-bolt-icon">bolt</span>
          <span className="speed-boost-title">Playback Speed</span>
        </div>

        {/* 2. Co-aligned Scale Container: Presets + Track */}
        <div className="speed-boost-scale-container">
          {/* Presets Row */}
          <div className="speed-boost-presets-wrapper">
            {BOOST_PRESETS.map((preset, idx) => {
              const isSelected = idx === activeIndex;
              const leftPercent = (idx / (BOOST_PRESETS.length - 1)) * 100;
              return (
                <button
                  key={preset}
                  type="button"
                  style={{ left: `${leftPercent}%` }}
                  className={`speed-boost-preset-btn ${isSelected ? 'is-active' : ''}`}
                  onClick={(e) => {
                    e.stopPropagation();
                    onSelectBoostSpeed(preset);
                  }}
                >
                  {formatBoostSpeed(preset)}
                </button>
              );
            })}
          </div>

          {/* Slider Track with Dots & Glowing Thumb */}
          <div
            ref={trackRef}
            className="speed-boost-track-zone"
            onPointerDown={handleTrackPointerDown}
          >
            <div className="speed-boost-track-line">
              {/* Active Blue Fill */}
              <div
                className="speed-boost-track-fill"
                style={{ width: `${activeFraction * 100}%` }}
              />

              {/* Inactive & Passed Tick Dots */}
              {BOOST_PRESETS.map((p, idx) => {
                const pos = (idx / (BOOST_PRESETS.length - 1)) * 100;
                const isPassed = idx <= activeIndex;
                return (
                  <div
                    key={p}
                    className={`speed-boost-dot ${isPassed ? 'is-passed' : ''}`}
                    style={{ left: `${pos}%` }}
                  />
                );
              })}
            </div>

            {/* Active Glowing Blue Thumb */}
            <div
              className="speed-boost-thumb"
              style={{ left: `${activeFraction * 100}%` }}
            />
          </div>
        </div>

        {/* 3. Footer: Status Indicator */}
        <div className="speed-boost-footer">
          <span className="material-symbols-rounded speed-boost-play-icon">play_arrow</span>
          <span className="speed-boost-status">
            {formatBoostSpeed(boostSpeed)} speed playing
          </span>
        </div>
      </div>
    </div>
  );
};

