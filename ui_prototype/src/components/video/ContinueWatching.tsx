import { useState, type FC } from 'react';
import { type ContinueWatchingItem } from '../../types/video';
import './ContinueWatching.css';

export interface ContinueWatchingProps {
  items: ContinueWatchingItem[];
  onPlayItem?: (item: ContinueWatchingItem) => void;
  onClear?: () => void;
}

export const ContinueWatching: FC<ContinueWatchingProps> = ({
  items: initialItems,
  onPlayItem,
  onClear,
}) => {
  const [items, setItems] = useState<ContinueWatchingItem[]>(initialItems);

  const handleClear = () => {
    setItems([]);
    if (onClear) {
      onClear();
    }
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <div className="continue-watching-section">
      {/* 1. Header with Count Badge and Clear Action */}
      <div className="continue-watching-header">
        <div className="cw-title-group">
          <h2 className="cw-title">Continue Watching</h2>
          <span className="cw-count-badge" aria-label={`${items.length} items`}>
            {items.length}
          </span>
        </div>
        <button
          type="button"
          className="cw-clear-btn"
          onClick={handleClear}
          title="Clear history"
        >
          Clear
        </button>
      </div>

      {/* 2. Horizontal Carousel List */}
      <div className="cw-carousel-track">
        {items.map((item) => (
          <div
            key={item.id}
            className="cw-card"
            onClick={() => onPlayItem?.(item)}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onPlayItem?.(item);
              }
            }}
          >
            {/* Thumbnail with Gradient Progress Bar and Duration */}
            <div className="cw-thumb-container">
              <img
                src={item.thumbnailUrl}
                alt={item.title}
                className="cw-thumb-img"
                loading="lazy"
              />
              <div className="cw-duration-badge">
                {item.duration}
              </div>
              <div className="cw-progress-track">
                <div
                  className="cw-progress-fill"
                  style={{ width: `${Math.min(100, Math.max(0, item.progress))}%` }}
                />
              </div>
            </div>

            {/* Video Title */}
            <div className="cw-card-title" title={item.title}>
              {item.title}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
