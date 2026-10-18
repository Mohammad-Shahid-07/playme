import { useState, useMemo, type FC } from 'react';
import {
  type VideoItem,
  type VideoSubTab,
  type SortOption,
  type ViewMode,
  type ContinueWatchingItem,
} from '../../types/video';
import { MOCK_VIDEOS, CONTINUE_WATCHING_ITEMS, MOCK_FOLDERS } from '../../data/mockVideos';
import { VideoCard } from './VideoCard';
import { ContinueWatching } from './ContinueWatching';
import { VideoSubNav } from './VideoSubNav';
import { FolderList } from './FolderList';
import './VideoFeed.css';

export interface VideoFeedProps {
  onSelectVideo?: (video: VideoItem) => void;
}

export const VideoFeed: FC<VideoFeedProps> = ({ onSelectVideo }) => {
  const [subTab, setSubTab] = useState<VideoSubTab>('videos');
  const [sortOption, setSortOption] = useState<SortOption>('newest');
  const [viewMode, setViewMode] = useState<ViewMode>('list');

  // Sorted videos according to chosen sort option
  const sortedVideos = useMemo(() => {
    const list = [...MOCK_VIDEOS];
    switch (sortOption) {
      case 'name':
        return list.sort((a, b) => a.title.localeCompare(b.title));
      case 'size':
        return list.sort((a, b) => parseFloat(b.fileSize) - parseFloat(a.fileSize));
      case 'duration':
        return list.sort((a, b) => b.duration.localeCompare(a.duration));
      case 'newest':
      default:
        return list;
    }
  }, [sortOption]);

  const handleContinueWatchingPlay = (item: ContinueWatchingItem) => {
    // Find matching video or create video item representation
    const found = MOCK_VIDEOS.find((v) => v.title.toLowerCase().includes(item.title.toLowerCase()));
    if (found && onSelectVideo) {
      onSelectVideo(found);
    } else if (onSelectVideo) {
      onSelectVideo({
        id: item.id,
        title: item.title,
        year: 2024,
        fileSize: '1.4 GB',
        format: 'MP4',
        duration: item.duration,
        thumbnailUrl: item.thumbnailUrl,
        badges: [{ label: 'FHD', variant: 'gray' }],
        watchProgress: item.progress,
      });
    }
  };

  return (
    <div className="video-feed-container">
      {/* 0. App Brand Top Bar (Consistent with Player HUD Bar) */}
      <div className="home-top-bar">
        <div className="home-brand-group">
          <span className="home-brand-title">PlayMe</span>
          <span className="home-brand-dot" />
        </div>
        <div className="home-top-actions">
          <button type="button" className="home-action-btn" title="Search videos">
            <span className="material-symbols-rounded">search</span>
          </button>
          <button type="button" className="home-action-btn" title="Private Vault">
            <span className="material-symbols-rounded">lock</span>
          </button>
        </div>
      </div>

      {/* 1. Continue Watching Recent History Section */}
      <ContinueWatching
        items={CONTINUE_WATCHING_ITEMS}
        onPlayItem={handleContinueWatchingPlay}
      />

      {/* 2. Sub-Nav Bar: Videos / Folders tabs, Sort, View Toggle */}
      <VideoSubNav
        activeTab={subTab}
        onTabChange={setSubTab}
        sortOption={sortOption}
        onSortChange={setSortOption}
        viewMode={viewMode}
        onViewModeToggle={() => setViewMode((prev) => (prev === 'list' ? 'grid' : 'list'))}
      />

      {/* 3. Main Content: Videos List or Folders View */}
      {subTab === 'videos' ? (
        <div className={`video-card-list ${viewMode === 'grid' ? 'is-grid-layout' : ''}`}>
          {sortedVideos.map((video) => (
            <VideoCard
              key={video.id}
              video={video}
              viewMode={viewMode}
              onPlay={onSelectVideo}
              onMoreOptions={(_item, e) => {
                e.stopPropagation();
              }}
            />
          ))}
        </div>
      ) : (
        <FolderList
          folders={MOCK_FOLDERS}
          viewMode={viewMode}
          onSelectFolder={(folder) => {
            console.log('Selected folder:', folder.name);
          }}
        />
      )}
    </div>
  );
};

