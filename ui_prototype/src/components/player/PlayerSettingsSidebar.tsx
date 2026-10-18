import { useState, type FC } from 'react';
import './PlayerSettingsSidebar.css';

export type SettingsSubView =
  | 'root'
  | 'audio-track'
  | 'audio-sync'
  | 'subtitles'
  | 'subtitle-sync'
  | 'speed'
  | 'decoder'
  | 'sleep-timer'
  | 'aspect';

export interface PlayerSettingsSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  initialTab?: string;
}

export const PlayerSettingsSidebar: FC<PlayerSettingsSidebarProps> = ({
  isOpen,
  onClose,
  initialTab = 'root',
}) => {
  // Navigation stack: 'root' or drill-down subviews
  const [currentView, setCurrentView] = useState<SettingsSubView>('root');

  // Player Settings State
  const [selectedAudio, setSelectedAudio] = useState('eng-5.1');
  const [audioDelay, setAudioDelay] = useState(0.0);
  const [dialogueBoost, setDialogueBoost] = useState(true);
  const [volumeBoost, setVolumeBoost] = useState(120); // 100% - 200%

  const [selectedSubtitle, setSelectedSubtitle] = useState('eng-cc');
  const [subtitleDelay, setSubtitleDelay] = useState(0.0);
  const [subtitleSize, setSubtitleSize] = useState('Normal');

  const [playbackSpeed, setPlaybackSpeed] = useState('1.0x');
  const [decoder, setDecoder] = useState('HW+');
  const [sleepTimer, setSleepTimer] = useState('Off');
  const [backgroundPlay, setBackgroundPlay] = useState(false);

  const [aspectRatio, setAspectRatio] = useState('Fit to Screen');
  const [ambientGlow, setAmbientGlow] = useState(true);
  const [brightness, setBrightness] = useState(85);

  // Sync initial tab when drawer opens
  const [prevIsOpen, setPrevIsOpen] = useState(isOpen);
  if (isOpen !== prevIsOpen) {
    setPrevIsOpen(isOpen);
    if (isOpen) {
      if (initialTab === 'subtitles') {
        setCurrentView('subtitles');
      } else if (initialTab === 'audio') {
        setCurrentView('audio-track');
      } else {
        setCurrentView('root');
      }
    }
  }

  if (!isOpen) return null;

  // Labels helper
  const getAudioLabel = (id: string) => {
    if (id === 'eng-5.1') return 'English 5.1';
    if (id === 'hin-stereo') return 'Hindi Stereo';
    if (id === 'jap-dts') return 'Japanese DTS';
    return id;
  };

  const getSubtitleLabel = (id: string) => {
    if (id === 'off') return 'Off';
    if (id === 'eng-cc') return 'English [CC]';
    if (id === 'spa') return 'Spanish';
    return id;
  };

  return (
    <div className="settings-sidebar-overlay">
      {/* Clickable Backdrop */}
      <div className="settings-sidebar-backdrop" onClick={onClose} aria-label="Close Settings" />

      {/* Right Mobile Drawer Panel */}
      <aside className="settings-sidebar-panel" aria-label="Player Settings">
        {/* ======================================================== */}
        {/* 1. ROOT VIEW: NATIVE GROUPED INSET LIST                  */}
        {/* ======================================================== */}
        {currentView === 'root' && (
          <>
            {/* Header */}
            <div className="settings-nav-header">
              <div className="settings-header-title-box">
                <span className="settings-header-title">Settings</span>
              </div>
              <button
                type="button"
                className="settings-header-close-btn"
                onClick={onClose}
                title="Done"
              >
                <span className="material-symbols-rounded">close</span>
              </button>
            </div>

            {/* Scrollable Grouped Content */}
            <div className="settings-scroll-content">
              {/* GROUP 1: AUDIO & SOUND */}
              {/* GROUP 1: AUDIO */}
              <div className="settings-group-section">
                <span className="settings-group-title">AUDIO</span>
                <div className="settings-mobile-card">
                  {/* Audio Track Row */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('audio-track')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">headphones</span>
                      </div>
                      <span className="row-label">Audio Track</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{getAudioLabel(selectedAudio)}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Audio Sync Delay Row */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('audio-sync')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">sync</span>
                      </div>
                      <span className="row-label">Audio Sync</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">
                        {audioDelay === 0 ? '0.0s' : audioDelay > 0 ? `+${audioDelay.toFixed(1)}s` : `${audioDelay.toFixed(1)}s`}
                      </span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Dialogue Clarity Boost Switch */}
                  <div className="settings-mobile-row is-action">
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">graphic_eq</span>
                      </div>
                      <span className="row-label">Dialogue Clarity</span>
                    </div>
                    <div
                      className={`mobile-switch ${dialogueBoost ? 'is-on' : ''}`}
                      onClick={() => setDialogueBoost(!dialogueBoost)}
                    >
                      <div className="mobile-switch-thumb" />
                    </div>
                  </div>

                  {/* Hardware Super Volume Slider */}
                  <div className="settings-slider-row">
                    <div className="slider-label-row">
                      <div className="row-left">
                        <div className="row-icon-pill">
                          <span className="material-symbols-rounded">volume_up</span>
                        </div>
                        <span className="row-label">Volume Boost</span>
                      </div>
                      <span className="slider-current-val">{volumeBoost}%</span>
                    </div>
                    <div className="slider-track-wrap">
                      <input
                        type="range"
                        min="100"
                        max="200"
                        value={volumeBoost}
                        onChange={(e) => setVolumeBoost(parseInt(e.target.value, 10))}
                        className="mobile-range-slider"
                        style={{
                          background: `linear-gradient(to right, #6366f1 0%, #818cf8 ${((volumeBoost - 100) / 100) * 100}%, rgba(255, 255, 255, 0.08) ${((volumeBoost - 100) / 100) * 100}%, rgba(255, 255, 255, 0.08) 100%)`
                        }}
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* GROUP 2: SUBTITLES */}
              <div className="settings-group-section">
                <span className="settings-group-title">SUBTITLES</span>
                <div className="settings-mobile-card">
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('subtitles')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">subtitles</span>
                      </div>
                      <span className="row-label">Subtitles</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{getSubtitleLabel(selectedSubtitle)}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('subtitle-sync')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">tune</span>
                      </div>
                      <span className="row-label">Subtitle Delay</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">
                        {subtitleDelay === 0 ? '0.0s' : `${subtitleDelay.toFixed(1)}s`}
                      </span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* GROUP 3: PLAYBACK & ENGINE */}
              <div className="settings-group-section">
                <span className="settings-group-title">PLAYBACK & ENGINE</span>
                <div className="settings-mobile-card">
                  {/* Playback Speed */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('speed')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">speed</span>
                      </div>
                      <span className="row-label">Playback Speed</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{playbackSpeed}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Hardware Decoder */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('decoder')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">bolt</span>
                      </div>
                      <span className="row-label">Decoder Engine</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{decoder}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Sleep Timer */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('sleep-timer')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">timer</span>
                      </div>
                      <span className="row-label">Sleep Timer</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{sleepTimer}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Background Play Switch */}
                  <div className="settings-mobile-row is-action">
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">picture_in_picture_alt</span>
                      </div>
                      <span className="row-label">Background Audio</span>
                    </div>
                    <div
                      className={`mobile-switch ${backgroundPlay ? 'is-on' : ''}`}
                      onClick={() => setBackgroundPlay(!backgroundPlay)}
                    >
                      <div className="mobile-switch-thumb" />
                    </div>
                  </div>
                </div>
              </div>

              {/* GROUP 4: DISPLAY */}
              <div className="settings-group-section">
                <span className="settings-group-title">DISPLAY</span>
                <div className="settings-mobile-card">
                  {/* Aspect Ratio */}
                  <div
                    className="settings-mobile-row"
                    onClick={() => setCurrentView('aspect')}
                  >
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">aspect_ratio</span>
                      </div>
                      <span className="row-label">Aspect Ratio</span>
                    </div>
                    <div className="row-right">
                      <span className="row-value">{aspectRatio}</span>
                      <span className="material-symbols-rounded row-chevron">chevron_right</span>
                    </div>
                  </div>

                  {/* Ambient Glow Switch */}
                  <div className="settings-mobile-row is-action">
                    <div className="row-left">
                      <div className="row-icon-pill">
                        <span className="material-symbols-rounded">flare</span>
                      </div>
                      <span className="row-label">Ambient Edge Glow</span>
                    </div>
                    <div
                      className={`mobile-switch ${ambientGlow ? 'is-on' : ''}`}
                      onClick={() => setAmbientGlow(!ambientGlow)}
                    >
                      <div className="mobile-switch-thumb" />
                    </div>
                  </div>

                  {/* Brightness Slider */}
                  <div className="settings-slider-row">
                    <div className="slider-label-row">
                      <div className="row-left">
                        <div className="row-icon-pill">
                          <span className="material-symbols-rounded">brightness_medium</span>
                        </div>
                        <span className="row-label">Screen Brightness</span>
                      </div>
                      <span className="slider-current-val">{brightness}%</span>
                    </div>
                    <div className="slider-track-wrap">
                      <input
                        type="range"
                        min="10"
                        max="100"
                        value={brightness}
                        onChange={(e) => setBrightness(parseInt(e.target.value, 10))}
                        className="mobile-range-slider"
                        style={{
                          background: `linear-gradient(to right, #6366f1 0%, #818cf8 ${((brightness - 10) / 90) * 100}%, rgba(255, 255, 255, 0.08) ${((brightness - 10) / 90) * 100}%, rgba(255, 255, 255, 0.08) 100%)`
                        }}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}

        {/* ======================================================== */}
        {/* 2. SUB-VIEW: AUDIO TRACK SELECTOR                         */}
        {/* ======================================================== */}
        {currentView === 'audio-track' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Audio Track</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {[
                  { id: 'eng-5.1', title: 'English', desc: 'Dolby Digital 5.1 (Surround) · Default' },
                  { id: 'hin-stereo', title: 'Hindi', desc: 'Stereo AAC 320 kbps' },
                  { id: 'jap-dts', title: 'Japanese', desc: 'Original DTS 5.1 Surround' },
                ].map((track) => {
                  const isSelected = selectedAudio === track.id;
                  return (
                    <div
                      key={track.id}
                      className="settings-choice-row"
                      onClick={() => setSelectedAudio(track.id)}
                    >
                      <div className="choice-text-col">
                        <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>
                          {track.title}
                        </span>
                        <span className="choice-desc">{track.desc}</span>
                      </div>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">
                          check
                        </span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 3. SUB-VIEW: AUDIO SYNC DELAY                            */}
        {/* ======================================================== */}
        {currentView === 'audio-sync' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Audio Sync</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              {/* Big Visual Sync Stepper Box */}
              <div className="sync-stepper-hero-box">
                <span className="sync-hero-label">CURRENT DELAY</span>
                <span className="sync-hero-value">
                  {audioDelay === 0 ? '0.0 s' : audioDelay > 0 ? `+${audioDelay.toFixed(1)} s` : `${audioDelay.toFixed(1)} s`}
                </span>
                <span className="sync-hero-hint">
                  {audioDelay === 0
                    ? 'Audio & Video are in perfect sync'
                    : audioDelay > 0
                    ? 'Audio is delayed behind video'
                    : 'Audio plays ahead of video'}
                </span>

                <div className="sync-tap-steppers-row">
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setAudioDelay((v) => Math.max(-5.0, v - 0.5))}
                  >
                    -0.5s
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setAudioDelay((v) => Math.max(-5.0, v - 0.1))}
                  >
                    -0.1s
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn sync-reset-btn"
                    onClick={() => setAudioDelay(0.0)}
                  >
                    Reset (0.0s)
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setAudioDelay((v) => Math.min(5.0, v + 0.1))}
                  >
                    +0.1s
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setAudioDelay((v) => Math.min(5.0, v + 0.5))}
                  >
                    +0.5s
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 4. SUB-VIEW: SUBTITLES SELECTOR                           */}
        {/* ======================================================== */}
        {currentView === 'subtitles' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Subtitles</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {[
                  { id: 'off', title: 'Off', desc: 'Captions disabled' },
                  { id: 'eng-cc', title: 'English [CC]', desc: 'Embedded SubRip' },
                  { id: 'spa', title: 'Spanish', desc: 'Embedded SubRip' },
                ].map((item) => {
                  const isSelected = selectedSubtitle === item.id;
                  return (
                    <div
                      key={item.id}
                      className="settings-choice-row"
                      onClick={() => setSelectedSubtitle(item.id)}
                    >
                      <div className="choice-text-col">
                        <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>
                          {item.title}
                        </span>
                        <span className="choice-desc">{item.desc}</span>
                      </div>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">
                          check
                        </span>
                      )}
                    </div>
                  );
                })}
              </div>

              {/* Subtitle Font Size Group */}
              <div className="settings-group-section" style={{ marginTop: '1.25rem' }}>
                <span className="settings-group-title">SUBTITLE FONT SIZE</span>
                <div className="settings-mobile-card">
                  {['Normal', 'Large', 'Extra Large'].map((sz) => {
                    const isSelected = subtitleSize === sz;
                    return (
                      <div
                        key={sz}
                        className="settings-choice-row"
                        onClick={() => setSubtitleSize(sz)}
                      >
                        <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>{sz}</span>
                        {isSelected && (
                          <span className="material-symbols-rounded choice-check-icon">check</span>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 5. SUB-VIEW: SUBTITLE SYNC DELAY                         */}
        {/* ======================================================== */}
        {currentView === 'subtitle-sync' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Subtitle Sync</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="sync-stepper-hero-box">
                <span className="sync-hero-label">CURRENT SUBTITLE SYNC</span>
                <span className="sync-hero-value">
                  {subtitleDelay === 0 ? '0.0 s' : `${subtitleDelay.toFixed(1)} s`}
                </span>

                <div className="sync-tap-steppers-row">
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setSubtitleDelay((v) => v - 0.2)}
                  >
                    -0.2s
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn sync-reset-btn"
                    onClick={() => setSubtitleDelay(0.0)}
                  >
                    Reset
                  </button>
                  <button
                    type="button"
                    className="sync-tap-btn"
                    onClick={() => setSubtitleDelay((v) => v + 0.2)}
                  >
                    +0.2s
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 6. SUB-VIEW: PLAYBACK SPEED                              */}
        {/* ======================================================== */}
        {currentView === 'speed' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Playback Speed</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {[
                  { val: '0.5x', label: '0.5x' },
                  { val: '0.75x', label: '0.75x' },
                  { val: '1.0x', label: '1.0x (Normal)' },
                  { val: '1.25x', label: '1.25x' },
                  { val: '1.5x', label: '1.5x' },
                  { val: '1.75x', label: '1.75x' },
                  { val: '2.0x', label: '2.0x' },
                ].map((s) => {
                  const isSelected = playbackSpeed === s.val;
                  return (
                    <div
                      key={s.val}
                      className="settings-choice-row"
                      onClick={() => setPlaybackSpeed(s.val)}
                    >
                      <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>{s.label}</span>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">check</span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 7. SUB-VIEW: HARDWARE DECODER                            */}
        {/* ======================================================== */}
        {currentView === 'decoder' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Hardware Decoder</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {[
                  { id: 'HW+', title: 'HW+ (Hardware Acceleration Plus)', desc: 'Custom optimized GPU rendering · Recommended' },
                  { id: 'HW', title: 'HW (Standard Hardware)', desc: 'Android native MediaCodec acceleration' },
                  { id: 'SW', title: 'SW (Software Rendering)', desc: 'CPU software fallback for rare unsupported codecs' },
                ].map((d) => {
                  const isSelected = decoder === d.id;
                  return (
                    <div
                      key={d.id}
                      className="settings-choice-row"
                      onClick={() => setDecoder(d.id)}
                    >
                      <div className="choice-text-col">
                        <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>
                          {d.title}
                        </span>
                        <span className="choice-desc">{d.desc}</span>
                      </div>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">check</span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 8. SUB-VIEW: SLEEP TIMER                                 */}
        {/* ======================================================== */}
        {currentView === 'sleep-timer' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Sleep Timer</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {['Off', '15 Minutes', '30 Minutes', '45 Minutes', '60 Minutes', 'End of Video'].map((t) => {
                  const isSelected = sleepTimer === t;
                  return (
                    <div
                      key={t}
                      className="settings-choice-row"
                      onClick={() => setSleepTimer(t)}
                    >
                      <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>{t}</span>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">check</span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* ======================================================== */}
        {/* 9. SUB-VIEW: ASPECT RATIO                                */}
        {/* ======================================================== */}
        {currentView === 'aspect' && (
          <div className="settings-subview-wrapper">
            <div className="settings-nav-header">
              <button
                type="button"
                className="subview-back-btn"
                onClick={() => setCurrentView('root')}
              >
                <span className="material-symbols-rounded">arrow_back</span>
                <span>Settings</span>
              </button>
              <span className="subview-header-title">Aspect Ratio</span>
              <div style={{ width: 32 }} />
            </div>

            <div className="settings-scroll-content">
              <div className="settings-mobile-card">
                {[
                  { id: 'Fit to Screen', desc: 'Preserves original aspect ratio with black bars' },
                  { id: 'Crop to Fill 16:9', desc: 'Zooms and crops to fill widescreen display' },
                  { id: 'Stretch 100%', desc: 'Stretches video to touch all screen edges' },
                  { id: '1:1 Original Size', desc: 'Displays exact pixel-to-pixel native resolution' },
                ].map((mode) => {
                  const isSelected = aspectRatio === mode.id;
                  return (
                    <div
                      key={mode.id}
                      className="settings-choice-row"
                      onClick={() => setAspectRatio(mode.id)}
                    >
                      <div className="choice-text-col">
                        <span className={`choice-title ${isSelected ? 'is-active' : ''}`}>
                          {mode.id}
                        </span>
                        <span className="choice-desc">{mode.desc}</span>
                      </div>
                      {isSelected && (
                        <span className="material-symbols-rounded choice-check-icon">check</span>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </aside>
    </div>
  );
};
