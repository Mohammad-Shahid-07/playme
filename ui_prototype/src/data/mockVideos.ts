import { type VideoItem, type ContinueWatchingItem, type FolderItem } from '../types/video';

export const MOCK_VIDEOS: VideoItem[] = [
  {
    id: 'the-batman',
    title: 'The Batman',
    year: 2022,
    fileSize: '7.4 GB',
    format: 'MKV',
    duration: '2:56:11',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg',
    isNew: true,
    badges: [
      { label: '4K', variant: 'gold' },
      { label: 'HDR', variant: 'blue' },
      { label: '5.1', variant: 'gray' },
    ],
  },
  {
    id: 'money-heist-s01e01',
    title: 'Money Heist S01E01',
    year: 2017,
    fileSize: '1.1 GB',
    format: 'MP4',
    duration: '47:22',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg',
    watchProgress: 24, // Matches the blue watch progress on thumbnail and inline bar
    badges: [
      { label: 'FHD', variant: 'gray' },
      { label: 'AAC', variant: 'gray' },
    ],
  },
  {
    id: 'demon-slayer-s03e05',
    title: 'Demon Slayer S03E05',
    year: 2023,
    fileSize: '350 MB',
    format: 'MP4',
    duration: '23:40',
    thumbnailUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&auto=format&fit=crop&q=80',
    badges: [
      { label: 'FHD', variant: 'gray' },
      { label: 'AAC', variant: 'gray' },
    ],
  },
  {
    id: 'salaar',
    title: 'Salaar',
    year: 2023,
    fileSize: '4.8 GB',
    format: 'MKV',
    duration: '2:55:10',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/zXuGXX6IjWRVjHQqMaxaobqzD8O.jpg',
    badges: [
      { label: '4K', variant: 'gold' },
      { label: 'HDR', variant: 'blue' },
      { label: '5.1', variant: 'gray' },
    ],
  },
  {
    id: 'interstellar',
    title: 'Interstellar',
    year: 2014,
    fileSize: '12.8 GB',
    format: 'MKV',
    duration: '2:49:03',
    thumbnailUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&auto=format&fit=crop&q=80',
    badges: [
      { label: '4K', variant: 'gold' },
      { label: 'HDR', variant: 'blue' },
      { label: '5.1', variant: 'gray' },
    ],
  },
];

export const CONTINUE_WATCHING_ITEMS: ContinueWatchingItem[] = [
  {
    id: 'cw-concert',
    title: 'Concert in the Park',
    duration: '14:32',
    timeLeft: '5m left',
    progress: 68,
    thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'cw-retro-gaming',
    title: 'Retro Gaming Memories',
    duration: '06:18',
    timeLeft: '3m left',
    progress: 52,
    thumbnailUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'cw-cosmos',
    title: 'Cosmos: A SpaceTime Odyssey',
    duration: '45:10',
    timeLeft: '2m left',
    progress: 92,
    thumbnailUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'cw-cyberpunk',
    title: 'Cyberpunk 2077 Night City Walk',
    duration: '18:45',
    timeLeft: '8m left',
    progress: 45,
    thumbnailUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80',
  },
];

export const MOCK_FOLDERS: FolderItem[] = [
  {
    id: 'folder-camera',
    name: 'Camera',
    videoCount: 18,
    totalSize: '4.2 GB',
    thumbnailUrl: 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'folder-downloads',
    name: 'Downloads',
    videoCount: 42,
    totalSize: '6.1 GB',
    thumbnailUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'folder-movies',
    name: 'Movies',
    videoCount: 12,
    totalSize: '28.5 GB',
    thumbnailUrl: 'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg',
  },
  {
    id: 'folder-recordings',
    name: 'Screen Recordings',
    videoCount: 7,
    totalSize: '1.2 GB',
    thumbnailUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop&q=80',
  },
  {
    id: 'folder-whatsapp',
    name: 'WhatsApp Video',
    videoCount: 35,
    totalSize: '2.8 GB',
    thumbnailUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80',
  },
];

