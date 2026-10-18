import { useState, useEffect } from 'react';
import { DeviceFrame, type ScreenOrientation } from './components/device/DeviceFrame';
import { BottomNav } from './components/navigation/BottomNav';
import { VideoPlayer } from './components/player/VideoPlayer';
import { LandscapeVideoPlayer } from './components/player/LandscapeVideoPlayer';
import { PortraitVideoPlayer } from './components/player/PortraitVideoPlayer';
import { type NavTab } from './types/navigation';
import { type VideoItem } from './types/video';
import './App.css';

export function App() {
  const [orientation, setOrientation] = useState<ScreenOrientation>('portrait');
  const [showFrame, setShowFrame] = useState(true);
  const [scale, setScale] = useState(1);
  const [isPlaying, setIsPlaying] = useState(false);
  const [activeTab, setActiveTab] = useState<NavTab>('video');
  const [activeVideo, setActiveVideo] = useState<VideoItem | null>(null);

  // Toggle orientation
  const handleToggleOrientation = () => {
    setOrientation((prev) => (prev === 'portrait' ? 'landscape' : 'portrait'));
  };

  const handleSelectVideo = (video: VideoItem) => {
    setActiveVideo(video);
    setIsPlaying(true);
  };

  const handleClosePlayer = () => {
    setActiveVideo(null);
    setIsPlaying(false);
  };

  // Keyboard shortcut: 'r' to toggle orientation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }
      if (e.key === 'r' || e.key === 'R') {
        handleToggleOrientation();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const isLandscape = orientation === 'landscape';

  return (
    <DeviceFrame
      orientation={orientation}
      onToggleOrientation={handleToggleOrientation}
      showFrame={showFrame}
      onToggleFrame={() => setShowFrame(!showFrame)}
      scale={scale}
      onScaleChange={setScale}
    >
      {/* Phone Viewport Inner Container */}
      <div style={{ position: 'relative', width: '100%', height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {isLandscape ? (
          <LandscapeVideoPlayer
            video={activeVideo}
            onClose={() => {
              setOrientation('portrait');
              handleClosePlayer();
            }}
            onToggleOrientation={handleToggleOrientation}
          />
        ) : isPlaying ? (
          <PortraitVideoPlayer
            video={activeVideo}
            onClose={handleClosePlayer}
            onEnterFullscreen={() => setOrientation('landscape')}
            onSelectVideo={(v) => setActiveVideo(v)}
          />
        ) : (
          <>
            {/* Active Tab Screen Content */}
            <VideoPlayer
              activeTab={activeTab}
              isPlaying={isPlaying}
              onTogglePlay={() => setIsPlaying(!isPlaying)}
              onSelectVideo={handleSelectVideo}
            />

            {/* Floating Capsule Bottom Navigation Dock */}
            <BottomNav
              activeTab={activeTab}
              onTabChange={setActiveTab}
              onBigPlayClick={() => {
                setIsPlaying(true);
              }}
            />
          </>
        )}
      </div>
    </DeviceFrame>
  );
}

export default App;
