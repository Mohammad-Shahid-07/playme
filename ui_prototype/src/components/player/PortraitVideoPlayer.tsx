import { useState, type FC } from 'react';
import { type VideoItem } from '../../types/video';
import { QueueDrawer, type QueueItem } from './QueueDrawer';
import { PlayerSettingsSidebar, type SettingsTabType } from './PlayerSettingsSidebar';
import './PortraitVideoPlayer.css';

export interface PortraitVideoPlayerProps {
  video?: VideoItem | null;
  onClose: () => void;
  onEnterFullscreen: () => void;
  onSelectVideo?: (video: VideoItem) => void;
}

const SPEED_OPTIONS = ['1.0x', '1.25x', '1.5x', '2.0x', '0.5x'];
type AspectFitMode = 'contain' | 'cover';

export const PortraitVideoPlayer: FC<PortraitVideoPlayerProps> = ({
  video,
  onClose,
  onEnterFullscreen,
}) => {
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(false);
  const [isLocked, setIsLocked] = useState(false);
  const [speedIndex, setSpeedIndex] = useState(0);
  const [progress, setProgress] = useState(28);
  const [fitMode] = useState<AspectFitMode>('contain');
  const [abActive, setAbActive] = useState(false);
  const [isQueueOpen, setIsQueueOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [settingsTab, setSettingsTab] = useState<SettingsTabType>('audio');
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Defaults matching the video item
  const title = video?.title || 'Concert in the Park';
  const resolution = video?.badges?.[0]?.label || '1080p';
  const fileSize = video?.fileSize || '485 MB';
  const format = video?.format || 'MP4';
  const posterUrl =
    video?.thumbnailUrl ||
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1400&auto=format&fit=crop&q=80';

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 2000);
  };

  const handleCycleSpeed = () => {
    const nextIdx = (speedIndex + 1) % SPEED_OPTIONS.length;
    setSpeedIndex(nextIdx);
    showToast(`Speed: ${SPEED_OPTIONS[nextIdx]}`);
  };


  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProgress(parseFloat(e.target.value));
  };

  return (
    <div className="unified-portrait-player-root select-none font-sans">
      {/* 1. TOP HUD BAR (Matches Landscape Top HUD & App DNA) */}
      <div className="portrait-hud-top">
        <div className="portrait-top-row">
          {/* Left: Back Button + Title & Inline Metadata */}
          <div className="portrait-title-col">
            <button
              type="button"
              className="portrait-back-btn"
              onClick={onClose}
              title="Back to Library"
            >
              <span className="material-symbols-rounded">arrow_back</span>
            </button>

            <div className="portrait-title-meta-group">
              <h3 className="portrait-video-title">{title}</h3>
              <div className="portrait-video-meta">
                <span className="meta-res-badge">{resolution}</span>
                <span className="meta-dot">·</span>
                <span>{fileSize}</span>
                <span className="meta-dot">·</span>
                <span>{format}</span>
              </div>
            </div>
          </div>

          {/* Right: Liquid Glass Top Utility Capsule (Same as Landscape) */}
          <div className="portrait-top-utility-capsule">
            <button
              type="button"
              className="portrait-utility-btn"
              onClick={() => setIsQueueOpen(true)}
              title="Playback Queue"
            >
              <span className="material-symbols-rounded">playlist_play</span>
            </button>
            <button
              type="button"
              className="portrait-utility-btn"
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
              className="portrait-utility-btn"
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
              className="portrait-utility-btn"
              onClick={() => {
                setSettingsTab('playback');
                setIsSettingsOpen(true);
              }}
              title="Settings"
            >
              <span className="material-symbols-rounded">tune</span>
            </button>
          </div>
        </div>
      </div>

      {/* 2. FULL-BLEED MOBILE VIDEO STAGE (Natively handles 16:9 & 9:16 vertical) */}
      <div className="portrait-stage-area">
        {/* Dynamic Atmospheric Ambilight Glow */}
        <div className="portrait-ambient-glow">
          <div className="portrait-glow-orb" />
        </div>

        {/* Video Canvas Element */}
        <img
          src={posterUrl}
          alt={title}
          className={`portrait-video-canvas fit-${fitMode}`}
        />

        {/* Top & Bottom Cinematic Gradient Scrims */}
        <div className="portrait-scrim-top" />
        <div className="portrait-scrim-bottom" />

        {/* LIVE ON-SCREEN SUBTITLE PILL */}
        <div className="portrait-subtitle-box">
          <span className="portrait-subtitle-pill">
            [ Atmospheric synth swells — Prepare for orbital jump ]
          </span>
        </div>
      </div>

      {/* 3. FLOATING VERTICAL ACTION DOCK (Single Sleek Liquid Glass Capsule on Right) */}
      <div className="portrait-side-dock">
        <div className="portrait-side-capsule">
          <button
            type="button"
            className={`portrait-side-btn ${isMuted ? 'is-active' : ''}`}
            onClick={() => {
              setIsMuted(!isMuted);
              showToast(isMuted ? 'Unmuted' : 'Muted');
            }}
            title={isMuted ? 'Unmute' : 'Mute'}
          >
            <span className="material-symbols-rounded">
              {isMuted ? 'volume_off' : 'volume_up'}
            </span>
          </button>

          <div className="portrait-side-divider" />

          <button
            type="button"
            className={`portrait-side-btn ${isLocked ? 'is-active' : ''}`}
            onClick={() => {
              setIsLocked(!isLocked);
              showToast(isLocked ? 'Controls Unlocked' : 'Controls Locked');
            }}
            title={isLocked ? 'Unlock Screen' : 'Lock Screen'}
          >
            <span className="material-symbols-rounded">
              {isLocked ? 'lock' : 'lock_open'}
            </span>
          </button>

          <div className="portrait-side-divider" />

          <button
            type="button"
            className="portrait-side-btn"
            onClick={() => showToast('Screenshot Saved')}
            title="Capture Snapshot"
          >
            <span className="material-symbols-rounded">photo_camera</span>
          </button>

          <div className="portrait-side-divider" />

          <button
            type="button"
            className={`portrait-side-btn portrait-side-btn-ab ${abActive ? 'is-active' : ''}`}
            onClick={() => {
              setAbActive(!abActive);
              showToast(abActive ? 'A-B Loop Off' : 'A-B Loop Active');
            }}
            title="A-B Loop Repeat"
          >
            A-B
          </button>

          <div className="portrait-side-divider" />

          <button
            type="button"
            className="portrait-side-btn"
            onClick={onEnterFullscreen}
            title="Rotate to Landscape Cinema Mode"
          >
            <span className="material-symbols-rounded">screen_rotation</span>
          </button>
        </div>
      </div>

      {/* 4. BOTTOM HUD: CLEAN TIMELINE + SINGLE UNIFIED LIQUID GLASS TRANSPORT DOCK */}
      <div className="portrait-hud-bottom">
        {/* Timeline Scrubber Row (Electric Violet Progress Bar) */}
        <div className="portrait-scrubber-row">
          <span className="portrait-time-current">00:03</span>

          <div className="portrait-seekbar-track-wrap">
            <div className="portrait-seekbar-bg">
              <div className="portrait-seekbar-buffer" style={{ width: '65%' }} />
              <div
                className="portrait-seekbar-progress"
                style={{ width: `${progress}%` }}
              />
            </div>
            <div
              className="portrait-seekbar-thumb"
              style={{ left: `${progress}%` }}
            />
            <input
              type="range"
              min="0"
              max="100"
              value={progress}
              step="0.1"
              className="portrait-seekbar-slider"
              onChange={handleSeek}
              aria-label="Seek timeline"
            />
          </div>

          <span className="portrait-time-remaining">-12:11</span>
        </div>

        {/* Full-Width Balanced Bottom Controls Deck */}
        <div className="portrait-bottom-deck">
          {/* Left Utility: Speed Badge Pill */}
          <div className="portrait-deck-side portrait-deck-left">
            <button
              type="button"
              className="portrait-speed-badge"
              onClick={handleCycleSpeed}
              title="Playback Speed"
            >
              <span>{SPEED_OPTIONS[speedIndex]}</span>
            </button>
          </div>

          {/* Center: Hero Transport Controls (Dead-Center) */}
          <div className="portrait-deck-center">
            <button
              type="button"
              className="portrait-transport-btn"
              onClick={() => showToast('Previous Track')}
              title="Previous Track"
            >
              <span className="material-symbols-rounded">skip_previous</span>
            </button>

            {/* Signature Obsidian Violet Glowing Play/Pause Circle Button */}
            <button
              type="button"
              className="portrait-hero-play-btn"
              onClick={() => setIsPlaying(!isPlaying)}
              title={isPlaying ? 'Pause' : 'Play'}
            >
              <span className="material-symbols-rounded">
                {isPlaying ? 'pause' : 'play_arrow'}
              </span>
            </button>

            <button
              type="button"
              className="portrait-transport-btn"
              onClick={() => showToast('Next Track')}
              title="Next Track"
            >
              <span className="material-symbols-rounded">skip_next</span>
            </button>
          </div>

          {/* Right Utility: Fullscreen / Landscape Mode */}
          <div className="portrait-deck-side portrait-deck-right">
            <button
              type="button"
              className="portrait-deck-btn"
              onClick={onEnterFullscreen}
              title="Rotate to Landscape Cinema Mode"
            >
              <span className="material-symbols-rounded">fullscreen</span>
            </button>
          </div>
        </div>
      </div>

      {/* Floating Feedback Toast */}
      {toastMessage && (
        <div className="portrait-toast-pill">
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Slide-up Playback Queue Bottom Sheet */}
      <QueueDrawer
        isOpen={isQueueOpen}
        onClose={() => setIsQueueOpen(false)}
        orientation="portrait"
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
    </div>
  );
};
