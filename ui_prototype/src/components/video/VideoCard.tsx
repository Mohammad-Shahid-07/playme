import { type FC, type MouseEvent } from 'react';
import { type VideoItem, type ViewMode } from '../../types/video';
import './VideoCard.css';

export interface VideoCardProps {
  video: VideoItem;
  viewMode?: ViewMode;
  onPlay?: (video: VideoItem) => void;
  onMoreOptions?: (video: VideoItem, e: MouseEvent) => void;
}

export const VideoCard: FC<VideoCardProps> = ({
  video,
  viewMode = 'list',
  onPlay,
  onMoreOptions,
}) => {
  const handleItemClick = () => {
    if (onPlay) {
      onPlay(video);
    }
  };

  const handleOptionsClick = (e: MouseEvent) => {
    e.stopPropagation();
    if (onMoreOptions) {
      onMoreOptions(video, e);
    }
  };

  // Cinematic 2-Column Grid Card Layout (Full-bleed media with text overlay on thumbnail)
  if (viewMode === 'grid') {
    const primaryBadge = video.badges && video.badges.length > 0 ? video.badges[0] : null;

    return (
      <div
        className="video-grid-card-item"
        onClick={handleItemClick}
        role="button"
        tabIndex={0}
        aria-label={`Play ${video.title}`}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleItemClick();
          }
        }}
      >
        {/* Full-bleed background thumbnail */}
        <img
          src={video.thumbnailUrl}
          alt={video.title}
          className="grid-thumb-img"
          loading="lazy"
        />

        {/* Top Badges (Quality on left, NEW on right) */}
        <div className="grid-top-badges">
          {primaryBadge ? (
            <span className={`video-spec-badge badge-${primaryBadge.variant} grid-top-spec`}>
              {primaryBadge.label}
            </span>
          ) : <span />}

          {(video.isNew || video.thumbnailBadge === 'NEW') && (
            <div className="thumb-badge-new grid-top-new">NEW</div>
          )}
        </div>

        {/* Bottom Scrim with Text Overlay on Top of Thumbnail */}
        <div className="grid-scrim-overlay">
          <div className="grid-title-text" title={video.title}>
            {video.title}
          </div>

          <div className="grid-meta-row">
            <span className="grid-meta-text">
              {video.year} • {video.fileSize}
            </span>
            <span className="grid-duration-badge">
              {video.duration}
            </span>
          </div>
        </div>

        {/* Bottom Watch Progress Bar on Thumbnail */}
        {typeof video.watchProgress === 'number' && video.watchProgress > 0 && (
          <div className="grid-progress-track">
            <div
              className="grid-progress-fill"
              style={{ width: `${Math.min(100, Math.max(0, video.watchProgress))}%` }}
            />
          </div>
        )}
      </div>
    );
  }

  // Standard Horizontal List Layout
  return (
    <div
      className="video-card-item"
      onClick={handleItemClick}
      role="button"
      tabIndex={0}
      aria-label={`Play ${video.title}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleItemClick();
        }
      }}
    >
      {/* 1. Left Thumbnail with Badges and Watch Progress */}
      <div className="video-thumb-container">
        <img
          src={video.thumbnailUrl}
          alt={video.title}
          className="video-thumb-img"
          loading="lazy"
        />

        {/* Top-Right Redesigned Solid NEW Badge */}
        {(video.isNew || video.thumbnailBadge === 'NEW') && (
          <div className="thumb-badge-new">
            NEW
          </div>
        )}

        {/* Bottom-Right Duration Badge */}
        <div className="thumb-badge-duration">
          {video.duration}
        </div>

        {/* Bottom Watch Progress Bar on Thumbnail ONLY */}
        {typeof video.watchProgress === 'number' && video.watchProgress > 0 && (
          <div className="thumb-progress-track">
            <div
              className="thumb-progress-fill"
              style={{ width: `${Math.min(100, Math.max(0, video.watchProgress))}%` }}
            />
          </div>
        )}
      </div>

      {/* 2. Middle Info Column (Height matches thumbnail, balanced top & bottom padding) */}
      <div className="video-info-container">
        {/* Natural text grouping: Title + Metadata */}
        <div className="video-text-group">
          {/* Video Title: Up to 2 lines worth of space */}
          <div
            className="video-title-text"
            title={video.title}
          >
            {video.title}
          </div>

          {/* Metadata: Year • FileSize • Format */}
          <div className="video-sub-meta">
            <span>{video.year}</span>
            <span className="video-meta-dot">•</span>
            <span>{video.fileSize}</span>
            <span className="video-meta-dot">•</span>
            <span>{video.format}</span>
          </div>
        </div>

        {/* Badges Row: Always displayed at bottom with balanced bottom padding */}
        {video.badges && video.badges.length > 0 && (
          <div className="video-badges-row">
            {video.badges.map((badge, index) => (
              <span
                key={`${badge.label}-${index}`}
                className={`video-spec-badge badge-${badge.variant}`}
              >
                {badge.label}
              </span>
            ))}
          </div>
        )}
      </div>

      {/* 3. Right Options Menu Button */}
      <div className="video-action-container">
        <button
          type="button"
          className="video-options-btn"
          onClick={handleOptionsClick}
          aria-label={`More options for ${video.title}`}
          title="More options"
        >
          <span className="material-symbols-rounded" style={{ fontSize: '19px' }}>
            more_vert
          </span>
        </button>
      </div>
    </div>
  );
};
