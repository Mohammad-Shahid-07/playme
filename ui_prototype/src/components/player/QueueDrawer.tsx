import { useState, useRef, type FC, type DragEvent } from 'react';
import { type VideoItem } from '../../types/video';
import './QueueDrawer.css';

export interface QueueItem {
  id: string;
  title: string;
  duration: string;
  fileSize?: string;
  resolution?: string;
  format?: string;
  thumbnailUrl: string;
  channel?: string;
}

export interface QueueDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  orientation: 'portrait' | 'landscape';
  currentVideo?: VideoItem | null;
  onSelectQueueItem?: (item: QueueItem) => void;
}

const DEFAULT_QUEUE: QueueItem[] = [
  {
    id: 'cw-retro-gaming',
    title: 'Retro Gaming Memories',
    duration: '06:18',
    fileSize: '210 MB',
    resolution: '1080p',
    format: 'MKV',
    thumbnailUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=400&auto=format&fit=crop&q=80',
    channel: 'Arcade Classics',
  },
  {
    id: 'cw-cosmos',
    title: 'Cosmos: A SpaceTime Odyssey',
    duration: '45:10',
    fileSize: '1.8 GB',
    resolution: '4K HDR',
    format: 'MKV',
    thumbnailUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&auto=format&fit=crop&q=80',
    channel: 'National Geographic',
  },
  {
    id: 'cw-cyberpunk',
    title: 'Cyberpunk 2077 Night City Walk',
    duration: '18:45',
    fileSize: '850 MB',
    resolution: '4K 60fps',
    format: 'MP4',
    thumbnailUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=400&auto=format&fit=crop&q=80',
    channel: 'RayTracing Ambience',
  },
  {
    id: 'the-batman',
    title: 'The Batman — Official Extended Scene',
    duration: '12:30',
    fileSize: '740 MB',
    resolution: '4K HDR',
    format: 'MKV',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg',
    channel: 'Warner Bros. Pictures',
  },
  {
    id: 'money-heist-s01e01',
    title: 'Money Heist S01E01 — Bella Ciao',
    duration: '47:22',
    fileSize: '1.1 GB',
    resolution: 'FHD',
    format: 'MP4',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg',
    channel: 'Netflix Series',
  },
];

export const QueueDrawer: FC<QueueDrawerProps> = ({
  isOpen,
  onClose,
  orientation,
  currentVideo,
  onSelectQueueItem,
}) => {
  const [queue, setQueue] = useState<QueueItem[]>(DEFAULT_QUEUE);
  const [isShuffle, setIsShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState<'off' | 'all' | 'one'>('all');
  const [autoplay, setAutoplay] = useState(true);
  const [draggedIdx, setDraggedIdx] = useState<number | null>(null);
  const [dragOverIdx, setDragOverIdx] = useState<number | null>(null);

  const dragNodeRef = useRef<HTMLDivElement | null>(null);

  if (!isOpen) return null;

  // Drag & Drop handlers
  const handleDragStart = (index: number, e: DragEvent<HTMLDivElement>) => {
    setDraggedIdx(index);
    dragNodeRef.current = e.currentTarget;
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', `${index}`);
  };

  const handleDragOver = (index: number, e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (dragOverIdx !== index) {
      setDragOverIdx(index);
    }
  };

  const handleDrop = (index: number, e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (draggedIdx === null || draggedIdx === index) {
      setDraggedIdx(null);
      setDragOverIdx(null);
      return;
    }

    const updated = [...queue];
    const [moved] = updated.splice(draggedIdx, 1);
    updated.splice(index, 0, moved);
    setQueue(updated);
    setDraggedIdx(null);
    setDragOverIdx(null);
  };

  const handleDragEnd = () => {
    setDraggedIdx(null);
    setDragOverIdx(null);
  };



  const handleRemove = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setQueue((prev) => prev.filter((item) => item.id !== id));
  };

  const handleClearQueue = () => {
    setQueue([]);
  };

  const handleCycleRepeat = () => {
    if (repeatMode === 'off') setRepeatMode('all');
    else if (repeatMode === 'all') setRepeatMode('one');
    else setRepeatMode('off');
  };

  const currentTitle = currentVideo?.title || 'Concert in the Park';
  const currentDuration = currentVideo?.duration || '14:32';
  const currentResolution = currentVideo?.badges?.[0]?.label || '1080p FHD';
  const currentThumb =
    currentVideo?.thumbnailUrl ||
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80';

  return (
    <div className={`queue-overlay is-left-side ${orientation === 'landscape' ? 'is-landscape' : 'is-portrait'}`}>
      {/* Clickable Backdrop Dimmer */}
      <div className="queue-backdrop" onClick={onClose} aria-label="Close Queue" />

      {/* Main Glass Drawer sliding from the Left Hand Side */}
      <aside className="queue-drawer-panel" aria-label="Playback Queue">
        {/* Top Header */}
        <div className="queue-top-bar">
          <div className="queue-top-title-group">
            <button
              type="button"
              className="queue-back-btn"
              onClick={onClose}
              title="Close Queue"
              aria-label="Back"
            >
              <span className="material-symbols-rounded">arrow_back</span>
            </button>
            <div className="queue-title-meta">
              <div className="queue-heading-line">
                <span className="queue-main-title">Queue</span>
                <span className="queue-pill-count">{queue.length}</span>
              </div>
              <span className="queue-subtext">Drag items to rearrange</span>
            </div>
          </div>

          <div className="queue-top-tools">
            <button
              type="button"
              className={`queue-icon-tool ${isShuffle ? 'is-active' : ''}`}
              onClick={() => setIsShuffle(!isShuffle)}
              title={isShuffle ? 'Shuffle Active' : 'Shuffle Off'}
            >
              <span className="material-symbols-rounded">shuffle</span>
            </button>

            <button
              type="button"
              className={`queue-icon-tool ${repeatMode !== 'off' ? 'is-active' : ''}`}
              onClick={handleCycleRepeat}
              title={`Repeat: ${repeatMode}`}
            >
              <span className="material-symbols-rounded">
                {repeatMode === 'one' ? 'repeat_one' : 'repeat'}
              </span>
            </button>
          </div>
        </div>

        {/* Scrollable Queue Content */}
        <div className="queue-scroll-area">
          {/* NOW PLAYING HERO SECTION */}
          <section className="queue-nowplaying-section">
            <div className="queue-section-header">
              <div className="queue-nowplaying-tag">
                <span className="queue-pulse-dot" />
                <span>NOW PLAYING</span>
              </div>
              <div className="queue-equalizer-bars">
                <span className="eq-bar bar-1" />
                <span className="eq-bar bar-2" />
                <span className="eq-bar bar-3" />
              </div>
            </div>

            <div className="queue-nowplaying-card">
              <div className="queue-hero-thumb-box">
                <img src={currentThumb} alt={currentTitle} className="queue-hero-thumb-img" />
                <div className="queue-hero-playing-badge">
                  <span className="material-symbols-rounded">equalizer</span>
                </div>
                <span className="queue-chip-duration">{currentDuration}</span>
              </div>

              <div className="queue-hero-info">
                <span className="queue-hero-title">{currentTitle}</span>
                <div className="queue-hero-specs">
                  <span>DJ Live Set</span>
                  <span className="queue-dot">·</span>
                  <span>{currentResolution}</span>
                </div>
              </div>
            </div>
          </section>

          {/* UP NEXT SECTION */}
          <section className="queue-upnext-section">
            <div className="queue-upnext-header-bar">
              <div className="queue-upnext-label-group">
                <span className="queue-upnext-label">UP NEXT</span>
                {queue.length > 0 && (
                  <button
                    type="button"
                    className="queue-clear-text-btn"
                    onClick={handleClearQueue}
                  >
                    Clear All
                  </button>
                )}
              </div>

              <div
                className="queue-autoplay-switch-wrap"
                onClick={() => setAutoplay(!autoplay)}
                title="Toggle Autoplay next video"
              >
                <span className="queue-autoplay-text">Autoplay</span>
                <div className={`queue-switch-track ${autoplay ? 'is-on' : ''}`}>
                  <div className="queue-switch-thumb" />
                </div>
              </div>
            </div>

            {/* Drag & Drop Reorderable List */}
            {queue.length === 0 ? (
              <div className="queue-empty-box">
                <span className="material-symbols-rounded queue-empty-icon">playlist_add</span>
                <span className="queue-empty-title">Queue is empty</span>
                <span className="queue-empty-subtitle">Videos you add will appear here</span>
              </div>
            ) : (
              <div className="queue-reorder-list">
                {queue.map((item, index) => {
                  const isDragging = draggedIdx === index;
                  const isDragOver = dragOverIdx === index;

                  return (
                    <div
                      key={item.id}
                      draggable
                      onDragStart={(e) => handleDragStart(index, e)}
                      onDragOver={(e) => handleDragOver(index, e)}
                      onDrop={(e) => handleDrop(index, e)}
                      onDragEnd={handleDragEnd}
                      className={`queue-row ${isDragging ? 'is-dragging' : ''} ${isDragOver ? 'is-drag-over' : ''}`}
                      onClick={() => {
                        if (onSelectQueueItem) {
                          onSelectQueueItem(item);
                        }
                      }}
                    >
                      {/* Insertion indicator line when dragging */}
                      {isDragOver && <div className="queue-drop-indicator" />}

                      {/* Video Thumbnail (Left) */}
                      <div className="queue-row-thumb-wrap">
                        <img src={item.thumbnailUrl} alt={item.title} className="queue-row-thumb-img" />
                        <span className="queue-chip-duration">{item.duration}</span>
                        <div className="queue-hover-play-btn">
                          <span className="material-symbols-rounded">play_arrow</span>
                        </div>
                      </div>

                      {/* Video Title and Clean Typographic Metadata (Center) */}
                      <div className="queue-row-details">
                        <span className="queue-row-title">{item.title}</span>
                        <div className="queue-row-meta-line">
                          <span>{item.channel || 'Video'}</span>
                          <span className="queue-dot">·</span>
                          <span>{item.resolution || '1080p'}</span>
                        </div>
                      </div>

                      {/* Right Side: Quick Remove on Hover & Drag Handle */}
                      <div className="queue-row-right">
                        <button
                          type="button"
                          className="queue-row-remove"
                          onClick={(e) => handleRemove(item.id, e)}
                          title="Remove from queue"
                        >
                          <span className="material-symbols-rounded">close</span>
                        </button>
                        <div
                          className="queue-drag-grip"
                          title="Drag to rearrange"
                          aria-label="Drag Handle"
                        >
                          <span className="material-symbols-rounded queue-grip-icon">drag_handle</span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </div>

        {/* Bottom Bar: Clean Add Button */}
        <div className="queue-bottom-bar">
          <button
            type="button"
            className="queue-add-videos-btn"
            onClick={() => {
              // Demo feedback
              const demoVideo: QueueItem = {
                id: `demo-${Date.now()}`,
                title: 'Neon Drift — Tokyo After Dark',
                duration: '08:42',
                resolution: '4K',
                fileSize: '410 MB',
                thumbnailUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400&auto=format&fit=crop&q=80',
                channel: 'CyberVision',
              };
              setQueue((prev) => [...prev, demoVideo]);
            }}
          >
            <span className="material-symbols-rounded">add</span>
            <span>Add Video to Queue</span>
          </button>
        </div>
      </aside>
    </div>
  );
};
