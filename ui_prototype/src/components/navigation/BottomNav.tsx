import { type FC } from 'react';
import { type NavTab } from '../../types/navigation';
import {
  VideoNavIcon,
  MusicNavIcon,
  BrowseNavIcon,
  SettingsNavIcon,
  PlayIcon,
} from '../icons/NavIcons';
import './BottomNav.css';

export interface BottomNavProps {
  activeTab: NavTab;
  onTabChange: (tab: NavTab) => void;
  onBigPlayClick?: () => void;
}

export const BottomNav: FC<BottomNavProps> = ({
  activeTab,
  onTabChange,
  onBigPlayClick,
}) => {
  return (
    <nav className="bottom-nav-dock" aria-label="Main Navigation">
      {/* Tab 1: Video */}
      <button
        type="button"
        className={`bottom-nav-tab ${activeTab === 'video' ? 'active' : ''}`}
        onClick={() => onTabChange('video')}
        aria-label="Video"
        aria-selected={activeTab === 'video'}
      >
        <div className="nav-tab-icon-wrapper">
          <VideoNavIcon active={activeTab === 'video'} size={21} />
        </div>
        <span className="nav-tab-label">Video</span>
      </button>

      {/* Tab 2: Music */}
      <button
        type="button"
        className={`bottom-nav-tab ${activeTab === 'music' ? 'active' : ''}`}
        onClick={() => onTabChange('music')}
        aria-label="Music"
        aria-selected={activeTab === 'music'}
      >
        <div className="nav-tab-icon-wrapper">
          <MusicNavIcon active={activeTab === 'music'} size={21} />
        </div>
        <span className="nav-tab-label">Music</span>
      </button>

      {/* Center Protruding Big Play Button (Standby) */}
      <div className="bottom-nav-center-slot">
        <button
          type="button"
          className="center-big-play-btn"
          onClick={onBigPlayClick}
          title="Play (Standby)"
          aria-label="Play Button"
        >
          <div className="play-btn-outer-ring">
            <div className="play-btn-inner-dome">
              <div className="play-btn-icon-wrapper">
                <PlayIcon size={24} />
              </div>
            </div>
          </div>
        </button>
      </div>

      {/* Tab 3: Browse */}
      <button
        type="button"
        className={`bottom-nav-tab ${activeTab === 'browse' ? 'active' : ''}`}
        onClick={() => onTabChange('browse')}
        aria-label="Browse"
        aria-selected={activeTab === 'browse'}
      >
        <div className="nav-tab-icon-wrapper">
          <BrowseNavIcon active={activeTab === 'browse'} size={21} />
        </div>
        <span className="nav-tab-label">Browse</span>
      </button>

      {/* Tab 4: Settings */}
      <button
        type="button"
        className={`bottom-nav-tab ${activeTab === 'settings' ? 'active' : ''}`}
        onClick={() => onTabChange('settings')}
        aria-label="Settings"
        aria-selected={activeTab === 'settings'}
      >
        <div className="nav-tab-icon-wrapper">
          <SettingsNavIcon active={activeTab === 'settings'} size={21} />
        </div>
        <span className="nav-tab-label">Settings</span>
      </button>
    </nav>
  );
};
