import { useState, type FC } from 'react';
import { type NavTab } from '../../types/navigation';
import { type VideoItem } from '../../types/video';
import { VideoFeed } from '../video/VideoFeed';
import './VideoPlayer.css';

export interface VideoPlayerProps {
  activeTab: NavTab;
  isPlaying: boolean;
  onTogglePlay: () => void;
  onSelectVideo?: (video: VideoItem) => void;
}

export const VideoPlayer: FC<VideoPlayerProps> = ({
  activeTab,
  isPlaying: _isPlaying,
  onTogglePlay: _onTogglePlay,
  onSelectVideo,
}) => {
  const [selectedVideo, setSelectedVideo] = useState<VideoItem | null>(null);

  return (
    <div className="player-screen-container">
      {/* 1. Video Tab: Renders the Video List Feed matching your screenshot */}
      {activeTab === 'video' && (
        <VideoFeed
          onSelectVideo={(video) => {
            setSelectedVideo(video);
            if (onSelectVideo) {
              onSelectVideo(video);
            }
          }}
        />
      )}

      {/* 2. Music Tab View */}
      {activeTab === 'music' && (
        <div className="tab-content-view">
          <div className="tab-icon-large">
            <span className="material-symbols-rounded" style={{ fontSize: '30px' }}>
              headphones
            </span>
          </div>
          <div className="tab-content-title">Music Player</div>
          <div className="tab-content-desc">
            Hi-Res Lossless Audio renderer with 5-band Equalizer and hardware Bass Boost.
          </div>
          <div className="tab-indicator-pill" style={{ marginTop: '0.5rem' }}>
            <span>Active Screen: Music</span>
          </div>
        </div>
      )}

      {/* 3. Browse Tab View */}
      {activeTab === 'browse' && (
        <div className="tab-content-view">
          <div className="tab-icon-large">
            <span className="material-symbols-rounded" style={{ fontSize: '30px' }}>
              folder_open
            </span>
          </div>
          <div className="tab-content-title">Media Library</div>
          <div className="tab-content-desc">
            Browse videos, folders, internal storage, DLNA/SMB shares, and Private Vault.
          </div>
          <div className="tab-indicator-pill" style={{ marginTop: '0.5rem' }}>
            <span>Active Screen: Browse</span>
          </div>
        </div>
      )}

      {/* 4. Settings Tab View */}
      {activeTab === 'settings' && (
        <div className="tab-content-view">
          <div className="tab-icon-large">
            <span className="material-symbols-rounded" style={{ fontSize: '30px' }}>
              tune
            </span>
          </div>
          <div className="tab-content-title">Settings & Audio FX</div>
          <div className="tab-content-desc">
            Configure decoders, +20dB Super Volume Boost, subtitle styling, and gestures.
          </div>
          <div className="tab-indicator-pill" style={{ marginTop: '0.5rem' }}>
            <span>Active Screen: Settings</span>
          </div>
        </div>
      )}

      {/* Selected Video Toast Feedback if tapped */}
      {selectedVideo && (
        <div
          style={{
            position: 'fixed',
            bottom: '96px',
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'rgba(12, 15, 26, 0.94)',
            border: '1px solid rgba(99, 102, 241, 0.4)',
            borderRadius: '999px',
            padding: '6px 14px',
            fontSize: '11px',
            color: '#c7d2fe',
            fontFamily: 'var(--font-mono)',
            zIndex: 100,
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.7), 0 0 16px rgba(99, 102, 241, 0.2)',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
          }}
          onClick={() => setSelectedVideo(null)}
        >
          <span>Selected: {selectedVideo.title}</span>
          <span style={{ cursor: 'pointer', opacity: 0.7 }}>✕</span>
        </div>
      )}
    </div>
  );
};
