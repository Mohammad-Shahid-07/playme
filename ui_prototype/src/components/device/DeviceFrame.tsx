import { useState, useEffect, type FC, type ReactNode } from 'react';
import './DeviceFrame.css';

export type ScreenOrientation = 'portrait' | 'landscape';

export interface DeviceFrameProps {
  orientation: ScreenOrientation;
  onToggleOrientation: () => void;
  showFrame: boolean;
  onToggleFrame: () => void;
  scale?: number;
  onScaleChange?: (scale: number) => void;
  children: ReactNode;
}

export const DeviceFrame: FC<DeviceFrameProps> = ({
  orientation,
  onToggleOrientation,
  showFrame,
  onToggleFrame,
  scale = 1,
  onScaleChange,
  children,
}) => {
  const [timeStr, setTimeStr] = useState('9:41');
  const [islandExpanded, setIslandExpanded] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      let hours = now.getHours();
      const minutes = now.getMinutes().toString().padStart(2, '0');
      hours = hours % 12 || 12;
      setTimeStr(`${hours}:${minutes}`);
    };

    updateTime();
    const interval = setInterval(updateTime, 10000);
    return () => clearInterval(interval);
  }, []);

  const dimensionsLabel = orientation === 'portrait' ? '393 × 852' : '852 × 393';

  return (
    <div className="workbench-layout">
      {/* --- Left Obsidian Glass Control Sidebar --- */}
      <aside className={`workbench-sidebar ${sidebarCollapsed ? 'collapsed' : ''}`}>
        {/* Brand & Collapse Header */}
        <div className="sidebar-brand">
          {!sidebarCollapsed && (
            <div className="sidebar-logo-group">
              <div className="sidebar-logo-icon">PM</div>
              <div>
                <div className="sidebar-title">
                  Play<span>Me</span>
                </div>
                <div className="sidebar-subtitle">v2 Prototype</div>
              </div>
            </div>
          )}
          <button
            type="button"
            className="sidebar-collapse-btn"
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            title={sidebarCollapsed ? 'Expand Sidebar' : 'Collapse Sidebar'}
          >
            <span className="material-symbols-rounded" style={{ fontSize: '18px' }}>
              {sidebarCollapsed ? 'chevron_right' : 'chevron_left'}
            </span>
          </button>
        </div>

        {/* Orientation Section */}
        <div className="sidebar-section">
          {!sidebarCollapsed && <div className="sidebar-section-title">Orientation</div>}
          <button
            type="button"
            className={`sidebar-btn ${orientation === 'portrait' ? 'active' : ''}`}
            onClick={() => orientation !== 'portrait' && onToggleOrientation()}
            title="Portrait Mode (393 × 852)"
          >
            <span className="material-symbols-rounded" style={{ fontSize: '19px' }}>
              stay_current_portrait
            </span>
            {!sidebarCollapsed && (
              <span style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                <span>Portrait</span>
                <span className="sidebar-badge">19.5:9</span>
              </span>
            )}
          </button>

          <button
            type="button"
            className={`sidebar-btn ${orientation === 'landscape' ? 'active' : ''}`}
            onClick={() => orientation !== 'landscape' && onToggleOrientation()}
            title="Landscape Cinema Mode (852 × 393)"
          >
            <span className="material-symbols-rounded" style={{ fontSize: '19px' }}>
              stay_current_landscape
            </span>
            {!sidebarCollapsed && (
              <span style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                <span>Landscape</span>
                <span className="sidebar-badge">Cinema</span>
              </span>
            )}
          </button>
        </div>

        {/* Chassis & Display Section */}
        <div className="sidebar-section">
          {!sidebarCollapsed && <div className="sidebar-section-title">Device Frame</div>}
          <button
            type="button"
            className={`sidebar-btn ${showFrame ? 'active' : ''}`}
            onClick={onToggleFrame}
            title="Toggle realistic phone bezel chassis"
          >
            <span className="material-symbols-rounded" style={{ fontSize: '19px' }}>
              smartphone
            </span>
            {!sidebarCollapsed && <span>{showFrame ? 'Hardware Bezel' : 'Frameless View'}</span>}
          </button>
        </div>

        {/* Viewport Scale Section */}
        {onScaleChange && !sidebarCollapsed && (
          <div className="sidebar-section">
            <div className="sidebar-section-title">Height & Scale</div>
            <div style={{ display: 'flex', gap: '6px' }}>
              <button
                type="button"
                className={`sidebar-btn ${scale === 1 ? 'active' : ''}`}
                style={{ flex: 1, justifyContent: 'center' }}
                onClick={() => onScaleChange(1)}
              >
                100% Fit
              </button>
              <button
                type="button"
                className={`sidebar-btn ${scale === 0.9 ? 'active' : ''}`}
                style={{ flex: 1, justifyContent: 'center' }}
                onClick={() => onScaleChange(0.9)}
              >
                90%
              </button>
              <button
                type="button"
                className={`sidebar-btn ${scale === 0.8 ? 'active' : ''}`}
                style={{ flex: 1, justifyContent: 'center' }}
                onClick={() => onScaleChange(0.8)}
              >
                80%
              </button>
            </div>
          </div>
        )}

        {/* Specs & Hotkeys Footer */}
        {!sidebarCollapsed && (
          <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <div
              style={{
                padding: '8px 10px',
                borderRadius: '8px',
                background: 'rgba(255, 255, 255, 0.03)',
                border: '1px solid var(--glass-border)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                fontSize: '0.72rem',
                fontFamily: 'var(--font-mono)',
                color: '#94a3b8',
              }}
            >
              <span>Target Viewport</span>
              <span style={{ color: 'var(--lavender-text)', fontWeight: 600 }}>{dimensionsLabel}</span>
            </div>

            <div
              style={{
                fontSize: '0.68rem',
                color: '#64748b',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '0 4px',
              }}
            >
              <span>Tip: Press</span>
              <kbd
                style={{
                  padding: '1px 5px',
                  borderRadius: '4px',
                  background: 'rgba(255,255,255,0.08)',
                  fontFamily: 'var(--font-mono)',
                  color: '#ffffff',
                }}
              >
                R
              </kbd>
              <span>to rotate screen</span>
            </div>
          </div>
        )}
      </aside>

      {/* --- Main 100vh Stage Viewport --- */}
      <main className="workbench-stage">
        {/* Ambient Stage Glow Behind Phone */}
        <div
          className="stage-ambient-glow"
          style={{
            transform: orientation === 'landscape' ? 'scale(1.4, 0.8)' : 'scale(1, 1)',
          }}
        />

        {/* Outer Phone Wrapper (scales if requested, locked to 100vh) */}
        <div
          className="phone-wrapper"
          style={{
            transform: scale !== 1 ? `scale(${scale})` : undefined,
          }}
        >
          {/* Realistic Physical Buttons on Edge */}
          {showFrame && orientation === 'portrait' && (
            <>
              <div className="hardware-button button-action" title="Action Button" />
              <div className="hardware-button button-volume-up" title="Volume Up" />
              <div className="hardware-button button-volume-down" title="Volume Down" />
              <div className="hardware-button button-power" title="Power / Lock Button" />
            </>
          )}

          {/* Main 100vh Responsive Phone Chassis */}
          <div
            className={`phone-chassis ${showFrame ? 'has-frame' : 'no-frame'} orientation-${orientation}`}
          >
            {/* Screen Inner Viewport */}
            <div className="screen-viewport">
              {/* Atmospheric Background Mesh Glows */}
              <div className="mesh-glow-indigo glow-indigo" />
              <div className="mesh-glow-violet glow-violet" />
              <div className="mesh-glow-midnight glow-midnight" />

              {/* Dynamic Island Camera Punch-Hole */}
              <div
                className="dynamic-island"
                onClick={() => setIslandExpanded(!islandExpanded)}
                title="Dynamic Island (Click to expand)"
                style={{
                  width: islandExpanded ? '180px' : undefined,
                }}
              >
                <div className="island-camera-lens" />
                <div className="island-sensor" />

                {islandExpanded ? (
                  <span style={{ fontSize: '9.5px', color: '#a5b4fc', fontFamily: 'var(--font-mono)' }}>
                    PlayMe Audio
                  </span>
                ) : null}

                <div className="island-audio-bars">
                  <div className="island-bar" />
                  <div className="island-bar" />
                  <div className="island-bar" />
                </div>
              </div>

              {/* Realistic Mobile Status Bar */}
              <div className="device-status-bar">
                <span className="status-time">{timeStr}</span>
                <div className="status-indicators">
                  {/* Cellular 4-bar Signal Icon */}
                  <svg width="15" height="10" viewBox="0 0 17 11" fill="none">
                    <rect x="0" y="8" width="3" height="3" rx="0.75" fill="white" />
                    <rect x="4.5" y="5.5" width="3" height="5.5" rx="0.75" fill="white" />
                    <rect x="9" y="3" width="3" height="8" rx="0.75" fill="white" />
                    <rect x="13.5" y="0.5" width="3" height="10.5" rx="0.75" fill="white" />
                  </svg>

                  {/* 5G Badge */}
                  <span className="status-badge-5g">5G</span>

                  {/* Wi-Fi Icon */}
                  <svg width="13" height="10" viewBox="0 0 15 11" fill="none">
                    <path
                      d="M1.5 3.5C5.08985 0.5 9.91015 0.5 13.5 3.5"
                      stroke="white"
                      strokeWidth="1.5"
                      strokeLinecap="round"
                    />
                    <path
                      d="M3.75 6.2C6.0792 4.2 8.9208 4.2 11.25 6.2"
                      stroke="white"
                      strokeWidth="1.5"
                      strokeLinecap="round"
                    />
                    <circle cx="7.5" cy="9.5" r="1.25" fill="white" />
                  </svg>

                  {/* Battery Pill */}
                  <div className="battery-pill">
                    <div className="battery-level" />
                  </div>
                </div>
              </div>

              {/* Injected Content Slot (Scrolls internally inside phone, never scrolling the browser page) */}
              <div className="screen-content">{children}</div>

              {/* Mobile Home Gesture Indicator Bar */}
              <div className="home-indicator-wrapper">
                <div className="home-indicator" />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};
