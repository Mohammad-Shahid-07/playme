import { useState, useEffect, type FC } from 'react';
import { type VideoItem } from '../../types/video';
import { QueueDrawer, type QueueItem } from './QueueDrawer';
import { PlayerSettingsSidebar, type SettingsTabType } from './PlayerSettingsSidebar';
import { PlaybackSpeedMenu, formatSpeed } from './PlaybackSpeedMenu';
import { ScreenGestureSeekHUD } from './ScreenGestureSeekHUD';
import { QuickSpeedBoostHUD } from './QuickSpeedBoostHUD';
import './LandscapeVideoPlayer.css';

export interface LandscapeVideoPlayerProps {
  video?: VideoItem | null;
  onClose?: () => void;
  onToggleOrientation?: () => void;
}

const TOTAL_DURATION = 734; // 12m 14s (00:03 current, -12:11 remaining at initial start)

const formatPlayerTime = (seconds: number): string => {
  const s = Math.max(0, Math.floor(seconds));
  const mins = Math.floor(s / 60);
  const secs = s % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

export const LandscapeVideoPlayer: FC<LandscapeVideoPlayerProps> = ({
  video,
  onClose,
  onToggleOrientation,
}) => {
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(false);
  const [isLocked, setIsLocked] = useState(false);
  const [speed, setSpeed] = useState(1.0);
  const [isSpeedMenuOpen, setIsSpeedMenuOpen] = useState(false);
  const [isSpeedBoosting, setIsSpeedBoosting] = useState(false);
  const [boostSpeed, setBoostSpeed] = useState(1.5);
  const [currentTime, setCurrentTime] = useState(3);
  const [abActive, setAbActive] = useState(false);
  const [isQueueOpen, setIsQueueOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [settingsTab, setSettingsTab] = useState<SettingsTabType>('audio');

  const effectiveSpeed = isSpeedBoosting ? boostSpeed : speed;

  // Playback timer progression based on effective speed (normal vs temporary boost)
  useEffect(() => {
    if (!isPlaying) return;
    const interval = setInterval(() => {
      setCurrentTime((prev) => {
        if (prev >= TOTAL_DURATION) {
          setIsPlaying(false);
          return TOTAL_DURATION;
        }
        return Math.min(TOTAL_DURATION, prev + 0.25 * effectiveSpeed);
      });
    }, 250);
    return () => clearInterval(interval);
  }, [isPlaying, effectiveSpeed]);

  // Desktop keyboard convenience: press and hold 'B' to temporarily boost speed
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.key === 'b' || e.key === 'B') && !e.repeat && !isSpeedBoosting) {
        setIsSpeedBoosting(true);
      }
    };
    const handleKeyUp = (e: KeyboardEvent) => {
      if (e.key === 'b' || e.key === 'B') {
        setIsSpeedBoosting(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [isSpeedBoosting]);

  // Defaults matching the exact reference screenshot
  const title = video?.title || 'Concert in the Park';
  const resolution = video?.badges?.[0]?.label || '1080p';
  const fileSize = video?.fileSize || '485 MB';
  const format = video?.format || 'MP4';
  const posterUrl =
    video?.thumbnailUrl ||
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1400&auto=format&fit=crop&q=80';

  const progressPercent = (currentTime / TOTAL_DURATION) * 100;

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const nextPercent = parseFloat(e.target.value);
    setCurrentTime((nextPercent / 100) * TOTAL_DURATION);
  };

  return (
    <div className="landscape-player-root select-none font-sans">
      {/* 1. TOP HUD BAR */}
      <div className="player-hud-top">
        <div className="player-top-row">
          {/* Left: Back Button + Title & Inline Metadata */}
          <div className="player-title-col">
            <button
              type="button"
              className="player-back-btn"
              onClick={onClose}
              title="Back to Library"
            >
              <span className="material-symbols-rounded">arrow_back</span>
            </button>

            <div className="player-title-meta-group">
              <h3 className="player-video-title">{title}</h3>
              <div className="player-video-meta">
                <span className="meta-res-badge">{resolution}</span>
                <span className="meta-dot">·</span>
                <span>{fileSize}</span>
                <span className="meta-dot">·</span>
                <span>{format}</span>
              </div>
            </div>
          </div>

          {/* Right: Liquid Glass Top Utility Capsule */}
          <div className="player-top-utility-capsule">
            <button
              type="button"
              className="player-utility-btn"
              onClick={() => setIsQueueOpen(true)}
              title="Playback Queue"
            >
              <span className="material-symbols-rounded">playlist_play</span>
            </button>
            <button
              type="button"
              className="player-utility-btn"
              onClick={() => {
                setSettingsTab('subtitles');
                setIsSettingsOpen(true);
              }}
              title="Subtitles & Captions"
            >
              <span className="material-symbols-rounded">closed_caption</span>
            </button>
            <button
              type="button"
              className="player-utility-btn"
              onClick={() => {
                setSettingsTab('audio');
                setIsSettingsOpen(true);
              }}
              title="Audio Tracks & Sync"
            >
              <span className="material-symbols-rounded">headphones</span>
            </button>
            <button
              type="button"
              className="player-utility-btn"
              onClick={() => {
                setSettingsTab('playback');
                setIsSettingsOpen(true);
              }}
              title="Player Studio Settings"
            >
              <span className="material-symbols-rounded">tune</span>
            </button>
          </div>
        </div>
      </div>

      {/* 2. FULL-BLEED VIDEO CANVAS WITH DYNAMIC AMBIENT GLOW */}
      <div className="player-stage-area">
        {/* Ambient Cinema Glow */}
        <div className="player-ambient-glow">
          <div className="ambient-glow-orb" />
        </div>

        {/* Video Canvas / Stream */}
        <img
          src={posterUrl}
          alt={title}
          className="player-video-canvas"
        />

        {/* Interactive Screen Horizontal Swipe-to-Seek Micro-Component */}
        <ScreenGestureSeekHUD
          currentTime={currentTime}
          duration={TOTAL_DURATION}
          onSeek={(newTime) => setCurrentTime(newTime)}
          isLocked={isLocked}
          onLongPressStart={() => setIsSpeedBoosting(true)}
          onLongPressEnd={() => setIsSpeedBoosting(false)}
          onLongPressSlide={(targetSpeed) => setBoostSpeed(targetSpeed)}
          boostSpeed={boostSpeed}
        />

        {/* Temporary Speed Booster HUD (appears when pressing & holding on screen) */}
        <QuickSpeedBoostHUD
          isActive={isSpeedBoosting}
          boostSpeed={boostSpeed}
          onSelectBoostSpeed={setBoostSpeed}
        />

        {/* LIVE ON-SCREEN SUBTITLES OVERLAY (Matching screenshot text exactly) */}
        <div className="player-subtitle-box">
          <span className="player-subtitle-pill">
            [ Atmospheric synth swells — Prepare for orbital jump ]
          </span>
        </div>
      </div>

      {/* 3. RIGHT FLOATING VERTICAL ACTION DOCK */}
      <div className="player-side-dock">
        <div className="player-side-capsule">
          <button
            type="button"
            className={`player-side-btn ${isMuted ? 'is-active' : ''}`}
            onClick={() => setIsMuted(!isMuted)}
            title="Mute / Unmute"
          >
            <span className="material-symbols-rounded">
              {isMuted ? 'volume_off' : 'volume_up'}
            </span>
          </button>

          <div className="player-side-divider" />

          <button
            type="button"
            className={`player-side-btn ${isLocked ? 'is-active' : ''}`}
            onClick={() => setIsLocked(!isLocked)}
            title="Lock Controls"
          >
            <span className="material-symbols-rounded">
              {isLocked ? 'lock' : 'lock_open'}
            </span>
          </button>

          <div className="player-side-divider" />

          <button
            type="button"
            className="player-side-btn"
            title="Capture Screenshot"
          >
            <span className="material-symbols-rounded">photo_camera</span>
          </button>

          <div className="player-side-divider" />

          <button
            type="button"
            className={`player-side-btn player-side-btn-ab ${abActive ? 'is-active' : ''}`}
            onClick={() => setAbActive(!abActive)}
            title="A-B Loop Repeat"
          >
            A-B
          </button>

          <div className="player-side-divider" />

          <button
            type="button"
            className="player-side-btn"
            onClick={onToggleOrientation}
            title="Rotate Orientation"
          >
            <span className="material-symbols-rounded">screen_rotation</span>
          </button>
        </div>
      </div>

      {/* 4. BOTTOM TIMELINE & TRANSPORT DOCK */}
      <div className="player-hud-bottom">
        {/* Scrubber Row */}
        <div className="player-scrubber-row">
          <span className="player-time-current">{formatPlayerTime(currentTime)}</span>

          <div className="player-seekbar-track-wrap">
            <div className="player-seekbar-bg">
              <div className="player-seekbar-buffer" style={{ width: '65%' }} />
              <div
                className="player-seekbar-progress"
                style={{ width: `${progressPercent}%` }}
              />
            </div>
            <div
              className="player-seekbar-thumb"
              style={{ left: `${progressPercent}%` }}
            />
            <input
              type="range"
              min="0"
              max="100"
              value={progressPercent}
              step="0.1"
              className="player-seekbar-slider"
              onChange={handleSeek}
              aria-label="Seek video"
            />
          </div>

          <span className="player-time-remaining">
            -{formatPlayerTime(TOTAL_DURATION - currentTime)}
          </span>
        </div>

        {/* 3-Slot Landscape Transport Deck */}
        <div className="player-bottom-deck">
          {/* SLOT 1 (LEFT): Up Next Queue Pill */}
          <div className="player-upnext-slot">
            <div
              className="player-upnext-pill"
              title="Open Playing Queue"
              onClick={() => setIsQueueOpen(true)}
            >
              <div className="player-upnext-thumb-wrap">
                <img
                  src="https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=200&auto=format&fit=crop&q=80"
                  alt="Up Next"
                  className="player-upnext-thumb"
                />
              </div>
              <div className="player-upnext-info">
                <div className="player-upnext-label-row">
                  <span className="player-upnext-label">UP NEXT</span>
                  <span className="player-upnext-tag">1/5</span>
                </div>
                <span className="player-upnext-title">Retro Gaming Memories</span>
              </div>
              <span className="material-symbols-rounded player-upnext-arrow">
                chevron_right
              </span>
            </div>
          </div>

          {/* SLOT 2 (CENTER): Transport Controls Capsule */}
          <div className="player-transport-slot">
            <div className="player-transport-capsule">
              <button
                type="button"
                className="player-ctrl-btn"
                title="Previous Track"
              >
                <span className="material-symbols-rounded">skip_previous</span>
              </button>

              {/* Signature Center Play/Pause Button */}
              <button
                type="button"
                className="player-play-btn"
                onClick={() => setIsPlaying(!isPlaying)}
                title={isPlaying ? 'Pause' : 'Play'}
              >
                <span className="material-symbols-rounded">
                  {isPlaying ? 'pause' : 'play_arrow'}
                </span>
              </button>

              <button
                type="button"
                className="player-ctrl-btn"
                title="Next Track"
              >
                <span className="material-symbols-rounded">skip_next</span>
              </button>
            </div>
          </div>

          {/* SLOT 3 (RIGHT): Right Tools Capsule */}
          <div className="player-tools-slot">
            <div className="player-tools-capsule">
              <button
                type="button"
                className={`player-speed-btn ${isSpeedBoosting ? 'is-boosting' : ''}`}
                onClick={() => setIsSpeedMenuOpen(true)}
                title="Playback Speed (click for menu, hold video or press 'B' to boost)"
              >
                <span>{formatSpeed(effectiveSpeed)}</span>
              </button>

              <button
                type="button"
                className="player-tool-btn"
                title="Cycle Aspect Ratio"
              >
                <span className="material-symbols-rounded">fit_screen</span>
              </button>

              <button
                type="button"
                className="player-tool-btn"
                onClick={onToggleOrientation}
                title="Toggle Fullscreen"
              >
                <span className="material-symbols-rounded">fullscreen</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Slide-in Playback Queue Drawer */}
      <QueueDrawer
        isOpen={isQueueOpen}
        onClose={() => setIsQueueOpen(false)}
        orientation="landscape"
        currentVideo={video}
        onSelectQueueItem={(_item: QueueItem) => {
          setIsQueueOpen(false);
        }}
      />

      {/* Right-Hand Mobile Settings Sidebar Drawer */}
      <PlayerSettingsSidebar
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        initialTab={settingsTab}
      />

      {/* Dedicated Playback Speed Popover Micro-Component */}
      <PlaybackSpeedMenu
        isOpen={isSpeedMenuOpen}
        onClose={() => setIsSpeedMenuOpen(false)}
        speed={speed}
        onSpeedChange={setSpeed}
      />
    </div>
  );
};
