import { useState, useRef, useEffect, type FC } from 'react';
import { type VideoSubTab, type SortOption, type ViewMode } from '../../types/video';
import { SortSlidersIcon, GridViewIcon, ListViewIcon, CheckmarkIcon } from '../icons/NavIcons';
import './VideoSubNav.css';

export interface VideoSubNavProps {
  activeTab: VideoSubTab;
  onTabChange: (tab: VideoSubTab) => void;
  sortOption: SortOption;
  onSortChange: (sort: SortOption) => void;
  viewMode: ViewMode;
  onViewModeToggle: () => void;
}

const SORT_LABELS: Record<SortOption, string> = {
  newest: 'Newest',
  name: 'Name',
  size: 'Size',
  duration: 'Duration',
};

export const VideoSubNav: FC<VideoSubNavProps> = ({
  activeTab,
  onTabChange,
  sortOption,
  onSortChange,
  viewMode,
  onViewModeToggle,
}) => {
  const [isSortMenuOpen, setIsSortMenuOpen] = useState(false);
  const sortMenuRef = useRef<HTMLDivElement>(null);

  // Close sort menu on click outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (sortMenuRef.current && !sortMenuRef.current.contains(e.target as Node)) {
        setIsSortMenuOpen(false);
      }
    };

    if (isSortMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isSortMenuOpen]);

  return (
    <div className="video-subnav-container">
      {/* 1. Main Navigation Bar */}
      <div className="video-subnav-bar">
        {/* Left: Videos / Folders Tabs */}
        <div className="subnav-tabs-group">
          <button
            type="button"
            className={`subnav-tab-btn ${activeTab === 'videos' ? 'is-active' : ''}`}
            onClick={() => onTabChange('videos')}
          >
            <span>Videos</span>
            {activeTab === 'videos' && <div className="subnav-active-line" />}
          </button>

          <button
            type="button"
            className={`subnav-tab-btn ${activeTab === 'folders' ? 'is-active' : ''}`}
            onClick={() => onTabChange('folders')}
          >
            <span>Folders</span>
            {activeTab === 'folders' && <div className="subnav-active-line" />}
          </button>
        </div>

        {/* Right: Sort and View Controls */}
        <div className="subnav-controls-group">
          {/* Sort Dropdown */}
          <div className="sort-dropdown-container" ref={sortMenuRef}>
            <button
              type="button"
              className="subnav-ctrl-btn sort-btn"
              onClick={() => setIsSortMenuOpen((prev) => !prev)}
              aria-expanded={isSortMenuOpen}
              title="Sort videos"
            >
              <SortSlidersIcon size={16} />
              <span className="sort-label-text">{SORT_LABELS[sortOption]}</span>
            </button>

            {isSortMenuOpen && (
              <div className="sort-menu-popover">
                {(Object.keys(SORT_LABELS) as SortOption[]).map((option) => (
                  <button
                    key={option}
                    type="button"
                    className={`sort-menu-item ${sortOption === option ? 'is-selected' : ''}`}
                    onClick={() => {
                      onSortChange(option);
                      setIsSortMenuOpen(false);
                    }}
                  >
                    <span>{SORT_LABELS[option]}</span>
                    {sortOption === option && (
                      <CheckmarkIcon size={14} />
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Grid / List View Toggle */}
          <button
            type="button"
            className="subnav-ctrl-btn view-toggle-btn"
            onClick={onViewModeToggle}
            title={viewMode === 'list' ? 'Switch to Grid View' : 'Switch to List View'}
          >
            {viewMode === 'list' ? (
              <GridViewIcon size={16} />
            ) : (
              <ListViewIcon size={16} />
            )}
          </button>
        </div>
      </div>

      {/* 2. Section Header: Consistent spacing across both tabs */}
      <div className="video-section-header">
        {activeTab === 'videos' ? 'TODAY' : 'DEVICE FOLDERS'}
      </div>
    </div>
  );
};
