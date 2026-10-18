import { type FC, type MouseEvent } from 'react';
import { type FolderItem, type ViewMode } from '../../types/video';
import { FolderGlyphIcon } from '../icons/NavIcons';
import './FolderList.css';

export interface FolderListProps {
  folders: FolderItem[];
  viewMode?: ViewMode;
  onSelectFolder?: (folder: FolderItem) => void;
  onMoreOptions?: (folder: FolderItem, e: MouseEvent) => void;
}

export const FolderList: FC<FolderListProps> = ({
  folders,
  viewMode = 'list',
  onSelectFolder,
  onMoreOptions,
}) => {
  return (
    <div className={`folder-list-container ${viewMode === 'grid' ? 'is-grid-layout' : ''}`}>
      {folders.map((folder) => (
        <div
          key={folder.id}
          className="folder-card-item"
          onClick={() => onSelectFolder?.(folder)}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              onSelectFolder?.(folder);
            }
          }}
        >
          {/* Folder Icon / Thumbnail Preview */}
          <div className="folder-icon-preview">
            <FolderGlyphIcon size={22} className="folder-glyph" />
          </div>

          {/* Folder Details */}
          <div className="folder-info-column">
            <div className="folder-name-text">{folder.name}</div>
            <div className="folder-meta-text">
              <span>{folder.videoCount} videos</span>
              <span className="folder-meta-dot">•</span>
              <span>{folder.totalSize}</span>
            </div>
          </div>

          {/* More Options */}
          <button
            type="button"
            className="folder-options-btn"
            onClick={(e) => {
              e.stopPropagation();
              onMoreOptions?.(folder, e);
            }}
            title={`Options for ${folder.name}`}
          >
            <span className="material-symbols-rounded" style={{ fontSize: '19px' }}>
              more_vert
            </span>
          </button>
        </div>
      ))}
    </div>
  );
};
