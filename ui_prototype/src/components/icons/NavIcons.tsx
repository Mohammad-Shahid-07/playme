import { type FC, type SVGProps } from 'react';

export interface IconProps extends SVGProps<SVGSVGElement> {
  active?: boolean;
  size?: number;
}

/**
 * Modern Video Icon (Clean outline for inactive, solid fill for active)
 */
export const VideoNavIcon: FC<IconProps> = ({ active = false, size = 22, ...props }) => {
  if (active) {
    return (
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        {...props}
      >
        <rect
          x="2.5"
          y="4.5"
          width="19"
          height="15"
          rx="4"
          fill="#38bdf8"
        />
        <polygon points="10,8.5 16,12 10,15.5" fill="#0c0e18" />
      </svg>
    );
  }

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect x="2.5" y="4.5" width="19" height="15" rx="4" />
      <polygon points="10,8.5 16,12 10,15.5" fill="currentColor" stroke="none" />
    </svg>
  );
};

/**
 * Modern Music Headphones Icon (Clean minimalist studio headset)
 */
export const MusicNavIcon: FC<IconProps> = ({ active = false, size = 22, ...props }) => {
  const color = active ? '#38bdf8' : 'currentColor';
  const fill = active ? '#38bdf8' : 'none';

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path d="M3 14C3 9.02944 7.02944 5 12 5C16.9706 5 21 9.02944 21 14" strokeWidth="2" />
      <rect x="2" y="13" width="5" height="8" rx="2.5" fill={fill} />
      <rect x="17" y="13" width="5" height="8" rx="2.5" fill={fill} />
    </svg>
  );
};

/**
 * Modern Browse / Library Folder Icon (Exact style to reference screenshot)
 */
export const BrowseNavIcon: FC<IconProps> = ({ active = false, size = 22, ...props }) => {
  if (active) {
    return (
      <svg
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        {...props}
      >
        <path
          d="M2.5 7.5C2.5 5.84315 3.84315 4.5 5.5 4.5H9.37868C10.1743 4.5 10.9374 4.81607 11.5 5.37868L12.5 6.37868C13.0626 6.94129 13.8257 7.25736 14.6213 7.25736H18.5C20.1569 7.25736 21.5 8.60051 21.5 10.2574V16.5C21.5 18.1569 20.1569 19.5 18.5 19.5H5.5C3.84315 19.5 2.5 18.1569 2.5 16.5V7.5Z"
          fill="#38bdf8"
        />
      </svg>
    );
  }

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path d="M2.5 7.5C2.5 5.84315 3.84315 4.5 5.5 4.5H9.37868C10.1743 4.5 10.9374 4.81607 11.5 5.37868L12.5 6.37868C13.0626 6.94129 13.8257 7.25736 14.6213 7.25736H18.5C20.1569 7.25736 21.5 8.60051 21.5 10.2574V16.5C21.5 18.1569 20.1569 19.5 18.5 19.5H5.5C3.84315 19.5 2.5 18.1569 2.5 16.5V7.5Z" />
    </svg>
  );
};

/**
 * Modern Settings / Tools Grid & Sliders Icon
 */
export const SettingsNavIcon: FC<IconProps> = ({ active = false, size = 22, ...props }) => {
  const color = active ? '#38bdf8' : 'currentColor';
  const fill = active ? '#38bdf8' : 'none';

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke={color}
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect x="3.5" y="3.5" width="6.5" height="6.5" rx="2" fill={fill} />
      <rect x="14" y="3.5" width="6.5" height="6.5" rx="2" />
      <rect x="3.5" y="14" width="6.5" height="6.5" rx="2" />
      <rect x="14" y="14" width="6.5" height="6.5" rx="2" fill={fill} />
    </svg>
  );
};

/**
 * Sleek Modern Play Triangle Icon (Solid crisp shape, no neon blur)
 */
export const PlayIcon: FC<IconProps> = ({ size = 22, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="#ffffff"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path
        d="M6 4.75C6 3.9686 6.8587 3.4913 7.5255 3.9015L19.3094 11.1515C19.9421 11.5408 19.9421 12.4592 19.3094 12.8485L7.5255 20.0985C6.8587 20.5087 6 20.0314 6 19.25V4.75Z"
      />
    </svg>
  );
};

/**
 * Modern Sort Sliders Icon (Matching 1.8px rounded stroke language)
 */
export const SortSlidersIcon: FC<IconProps> = ({ size = 16, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <line x1="4" y1="7" x2="20" y2="7" />
      <circle cx="8" cy="7" r="2.5" fill="#0c0e18" />
      <line x1="4" y1="17" x2="20" y2="17" />
      <circle cx="16" cy="17" r="2.5" fill="#0c0e18" />
    </svg>
  );
};

/**
 * Modern Grid View Icon (4 rounded square tiles)
 */
export const GridViewIcon: FC<IconProps> = ({ size = 16, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <rect x="3.5" y="3.5" width="6.5" height="6.5" rx="2" />
      <rect x="14" y="3.5" width="6.5" height="6.5" rx="2" />
      <rect x="3.5" y="14" width="6.5" height="6.5" rx="2" />
      <rect x="14" y="14" width="6.5" height="6.5" rx="2" />
    </svg>
  );
};

/**
 * Modern List View Icon (Horizontal bars)
 */
export const ListViewIcon: FC<IconProps> = ({ size = 16, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <line x1="8.5" y1="6" x2="20.5" y2="6" />
      <line x1="8.5" y1="12" x2="20.5" y2="12" />
      <line x1="8.5" y1="18" x2="20.5" y2="18" />
      <circle cx="4.5" cy="6" r="1.2" fill="currentColor" />
      <circle cx="4.5" cy="12" r="1.2" fill="currentColor" />
      <circle cx="4.5" cy="18" r="1.2" fill="currentColor" />
    </svg>
  );
};

/**
 * Clean Checkmark Icon
 */
export const CheckmarkIcon: FC<IconProps> = ({ size = 14, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
};

/**
 * Modern Folder Glyph Icon
 */
export const FolderGlyphIcon: FC<IconProps> = ({ size = 22, ...props }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      xmlns="http://www.w3.org/2000/svg"
      {...props}
    >
      <path d="M2.5 7.5C2.5 5.84315 3.84315 4.5 5.5 4.5H9.37868C10.1743 4.5 10.9374 4.81607 11.5 5.37868L12.5 6.37868C13.0626 6.94129 13.8257 7.25736 14.6213 7.25736H18.5C20.1569 7.25736 21.5 8.60051 21.5 10.2574V16.5C21.5 18.1569 20.1569 19.5 18.5 19.5H5.5C3.84315 19.5 2.5 18.1569 2.5 16.5V7.5Z" />
    </svg>
  );
};

