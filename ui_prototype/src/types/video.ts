export type BadgeVariant = 'gold' | 'blue' | 'gray' | 'green';

export interface VideoBadge {
  label: string;
  variant: BadgeVariant;
}

export interface VideoItem {
  id: string;
  title: string;
  year: number | string;
  fileSize: string;
  format: 'MKV' | 'MP4' | 'WEBM' | 'AVI' | string;
  duration: string;
  thumbnailUrl: string;
  isNew?: boolean; // When true, displays "NEW" badge on the thumbnail
  thumbnailBadge?: string; // Optional custom badge text e.g. "NEW"
  badges: VideoBadge[];
  watchProgress?: number; // 0 to 100 percentage
}

export interface ContinueWatchingItem {
  id: string;
  title: string;
  duration: string;
  timeLeft: string;
  progress: number; // 0 to 100 percentage
  thumbnailUrl: string;
}

export interface FolderItem {
  id: string;
  name: string;
  videoCount: number;
  totalSize: string;
  thumbnailUrl?: string;
}

export type VideoSubTab = 'videos' | 'folders';
export type SortOption = 'newest' | 'name' | 'size' | 'duration';
export type ViewMode = 'list' | 'grid';
