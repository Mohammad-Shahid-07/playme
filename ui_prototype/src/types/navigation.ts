export type NavTab = 'video' | 'music' | 'browse' | 'settings';

export interface NavItemConfig {
  id: NavTab;
  label: string;
  icon: string;
}
