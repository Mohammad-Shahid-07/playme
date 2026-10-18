# PlayMe Design System: Obsidian Violet Liquid Glass (v2)

> **Single Source of Truth** for PlayMe UI/UX styling across Web Prototypes, React Native, and Android Jetpack Compose implementations.
> Reference Implementation: [`app/.../PlayMeTheme.kt`](file:///d:/Web-Dev/react-native/playme/app/src/main/java/com/playme/player/core/designsystem/PlayMeTheme.kt) and [`ui_prototype.html`](file:///d:/Web-Dev/react-native/playme/ui_prototype.html).

---

## 1. Design Philosophy & Aesthetic Core

* **Theme Name**: Obsidian Violet & Liquid Dark Glass.
* **Mood**: Ultra-premium, tactile, futuristic, atmospheric, and welcoming.
* **Zero Harsh Borders**: Glass layers use soft rgba transparency with micro-borders (`1px rgba(255, 255, 255, 0.08)`) and high-saturate backdrop blur (28px–36px).
* **Atmospheric Ambient Mesh**: Background mesh layers glow with pulsating electric indigo (`#6366F1`), ultraviolet (`#8B5CF6`), and midnight cyan (`#3B82F6`) to eliminate flat, dead dark backgrounds.
* **Fluid Apple-style Motion**: Snappy easing curves (`cubic-bezier(0.16, 1, 0.3, 1)`) with scale feedback (`active:scale-95`).

---

## 2. Color Palette & Design Tokens

### A. Core Surfaces & Glass Tokens
| Token Name | Hex / RGBA | Jetpack Compose Token | Usage |
| :--- | :--- | :--- | :--- |
| **Obsidian Dark** | `#0C0E18` | `PlayMeDesign.ObsidianDark` | Deep app canvas background |
| **Obsidian Chassis** | `#0C0D16` | `PlayMeDesign.ObsidianChassis` | Outer device container / frame |
| **Dark Glass Background** | `rgba(12, 14, 24, 0.85)` | `PlayMeDesign.DarkGlassBackground` | Standard sheets, dialogs, overlays |
| **Dark Glass Elevated** | `rgba(12, 14, 24, 0.92)` | `PlayMeDesign.DarkGlassElevated` | Floating bottom bar, top app bar |
| **Frosted Card Base** | `rgba(14, 16, 28, 0.40)` | `PlayMeDesign.DarkGlassCard` | Video cards, list items, controls |
| **Frosted Card Hover** | `rgba(20, 23, 39, 0.80)` | `PlayMeDesign.DarkGlassCardHover` | Hover/active states on cards |
| **Glass Border Base** | `rgba(255, 255, 255, 0.08)`| `PlayMeDesign.DarkGlassBorder` | Subtle top/outer glass border |
| **Glass Border Hover**| `rgba(255, 255, 255, 0.15)`| `PlayMeDesign.DarkGlassBorderHover` | Highlighted/active border |

### B. Signature Electric Violet Palette (Brand Accents)
| Token Name | Hex | Usage |
| :--- | :--- | :--- |
| **Electric Indigo** | `#6366F1` | Primary brand accent, gradient start, seekbar progress |
| **Electric Violet** | `#757BF8` | PlayMe signature glow, toggle active states |
| **Deep Violet** | `#8B5CF6` | Gradient mid-tones, secondary button highlights |
| **Ultraviolet** | `#A855F7` | Gradient end, audio boost indicators |
| **Lavender Thumb** | `#9B9EFF` | Seekbar scrubbing head, slider knobs |
| **Lavender Glow** | `rgba(117, 123, 248, 0.60)`| Box shadow glows on active elements |

### C. Signature 3D Protruding Play Button (Center Dock)
| Token Name | Hex | Usage |
| :--- | :--- | :--- |
| **Ring Dark** | `#352F75` | Outer sweep-gradient ring shadow |
| **Ring Mid** | `#526DFF` | Outer sweep-gradient ring primary |
| **Ring Light** | `#8066FF` | Outer sweep-gradient highlight |
| **Inner Top** | `#181A32` | Play button inner dome top |
| **Inner Bottom** | `#0A0B14` | Play button inner dome bottom |

### D. Functional & Audio FX Feedback
| Feature | Color / Gradient | Usage |
| :--- | :--- | :--- |
| **Normal Volume** | `#6366F1` → `#A855F7` | 0% to 100% volume fill |
| **Super Boost (+20dB)**| `#F59E0B` → `#FF334B` (Amber to Red) | > 100% up to 200% volume boost warning |
| **Brightness HUD** | `#FCD34D` (Amber 300) | Gesture HUD vertical brightness bar & sun icon |
| **Sleep Timer** | `#FCD34D` (Amber 300) | Sleep timer active badge and icon |
| **Hardware Active** | `#00D287` (Neon Emerald) | Hardware acceleration, decoders active |
| **Nerd Stats / Codecs**| `#38BDF8` (Sky Cyan) | Real-time diagnostic overlays, bitrates |
| **Alert / Clipping** | `#FF334B` (Crimson) | Audio clipping danger, delete confirmation |

### E. Curated Media Spec Badges
| Badge Type | Background | Text | Border |
| :--- | :--- | :--- | :--- |
| **Indigo Badge** | `rgba(99, 102, 241, 0.15)` | `#A5B4FC` | `rgba(99, 102, 241, 0.30)` |
| **Violet Badge** | `rgba(168, 85, 247, 0.20)` | `#C084FC` | `rgba(168, 85, 247, 0.35)` |
| **Sky / Cyan Badge** | `rgba(2, 132, 199, 0.15)` | `#7DD3FC` | `rgba(2, 132, 199, 0.30)` |
| **Purple Badge** | `rgba(147, 51, 234, 0.15)` | `#D8B4FE` | `rgba(147, 51, 234, 0.30)` |
| **Codec Badge** | `rgba(255, 255, 255, 0.06)` | `#D4D4D8` | `rgba(255, 255, 255, 0.10)` |
| **NEW (Green)** | `#00D287` | `#000000` | None (Pill) |
| **NEW (Red)** | `#FF334B` | `#FFFFFF` | None (Pill) |
| **Highlight Last Played** | `#38BDF8` | — | Left border strip or duration pill |

### F. Category Folder Colors
* **Camera**: Bg `rgba(99, 102, 241, 0.15)`, Text `#A5B4FC`
* **Downloads**: Bg `rgba(139, 92, 246, 0.15)`, Text `#C084FC`
* **Movies**: Bg `rgba(236, 72, 153, 0.15)`, Text `#F472B6`
* **Records**: Bg `rgba(6, 182, 212, 0.15)`, Text `#22D3EE`

---

## 3. Typography Rules

| Role | Font Family | Weights | Usage |
| :--- | :--- | :--- | :--- |
| **Display & Headings** | `'Outfit', sans-serif` | 600 (SemiBold), 700 (Bold), 800 (ExtraBold) | App title, screen headers, sheet titles |
| **Body & UI Elements** | `'Plus Jakarta Sans', sans-serif` | 400 (Regular), 500 (Medium), 600 (SemiBold) | Video titles, metadata, buttons, labels |
| **Monospace / Diagnostics** | `'JetBrains Mono', monospace` | 400 (Regular), 600 (Bold) | Timestamps, durations, bitrates, codecs, EQ dB |
| **Iconography** | `Material Symbols Rounded` & `Lucide` | 400 weight, 20–24px | Media controls, navigation icons, indicators |

Google Fonts Import:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Outfit:wght@400;500;600;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
```

---

## 4. Glassmorphism & Shadow Utilities

### Standard CSS Classes (Ready for Tailwind or Vanilla CSS)

```css
:root {
  /* Surfaces */
  --bg-obsidian-dark: #0C0E18;
  --bg-obsidian-chassis: #0C0D16;
  --glass-surface: rgba(255, 255, 255, 0.05);
  --glass-surface-elevated: rgba(11, 13, 23, 0.92);
  --glass-border: rgba(255, 255, 255, 0.08);
  --glass-border-hover: rgba(255, 255, 255, 0.15);

  /* Brand Accents */
  --electric-indigo: #6366F1;
  --electric-violet: #757BF8;
  --deep-violet: #8B5CF6;
  --ultraviolet: #A855F7;
  --lavender-glow: rgba(117, 123, 248, 0.5);

  /* Functional */
  --amber-boost: #F59E0B;
  --amber-light: #FCD34D;
  --emerald-active: #00D287;
  --crimson-alert: #FF334B;
  --cyan-diagnostics: #38BDF8;
}

/* 1. Standard Frosted Glass Surface */
.cozy-glass {
  background: var(--glass-surface);
  backdrop-filter: blur(28px) saturate(185%);
  -webkit-backdrop-filter: blur(28px) saturate(185%);
  border: 1px solid var(--glass-border);
  box-shadow: 0 12px 32px -10px rgba(0, 0, 0, 0.65);
}

/* 2. Elevated Floating Navigation / Bottom Sheet Glass */
.cozy-glass-elevated {
  background: var(--glass-surface-elevated);
  backdrop-filter: blur(36px) saturate(195%);
  -webkit-backdrop-filter: blur(36px) saturate(195%);
  border: 1px solid var(--glass-border);
  box-shadow: 
    0 20px 50px -10px rgba(0, 0, 0, 0.9),
    0 0 28px rgba(117, 123, 248, 0.15);
}

/* 3. Interactive Pill */
.cozy-pill {
  background: rgba(255, 255, 255, 0.055);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  color: rgba(255, 255, 255, 0.78);
  transition: all 0.18s cubic-bezier(0.16, 1, 0.3, 1);
}
.cozy-pill:hover {
  background: rgba(255, 255, 255, 0.11);
  color: #ffffff;
  border-color: var(--glass-border-hover);
}
.cozy-pill-active {
  background: linear-gradient(135deg, #6366f1, #757bf8, #8b5cf6) !important;
  color: #ffffff !important;
  border: 1px solid rgba(255, 255, 255, 0.24) !important;
  box-shadow: 
    0 0 20px rgba(117, 123, 248, 0.5),
    0 4px 12px rgba(0, 0, 0, 0.45) !important;
  font-weight: 600;
}

/* 4. Signature Atmospheric Glow Background */
@keyframes atmosphericPulse {
  0%, 100% { transform: scale(1) translate(0, 0); opacity: 0.45; }
  50% { transform: scale(1.12) translate(12px, -12px); opacity: 0.70; }
}
.glow-ambient {
  animation: atmosphericPulse 14s ease-in-out infinite alternate;
}
```

---

## 5. Screen Hierarchy & Key Modules to Build in v2

1. **Player Screen**:
   * **Full-bleed video viewport** with ambient glow reflections.
   * **Dual-side vertical gesture HUD**: Left = Brightness (Amber), Right = Volume (Violet to Amber Super Boost).
   * **Double-tap seek indicator**: Smooth radial animated ripple (+10s / -10s).
   * **Interactive Seekbar**: Floating track, live time bubble, A-B repeat markers (`A` & `B`).
   * **Pro Action Bar**: Aspect ratio switch, Night Mode overlay, Speed slider (0.5x–4.0x), Audio FX trigger.
2. **Bottom Sheets & Modals**:
   * **Audio FX & Equalizer**: 5-band sliders, Bass Boost dial, Virtualizer, Super Boost (+20dB) toggle.
   * **Sleep Timer**: Minute selector, "Wait for active video to end" switch.
   * **Live Nerd Stats HUD**: Codec info, resolution, bitrate graph, dropped frame counter.
3. **Media Library View**:
   * **Header with Search & Quick Filter Pills** (Recent, Length, Quality).
   * **Toggleable View Mode**: Studio List Mode (frameless seamless rows) vs. 2-Column Editorial Grid.
   * **Bottom Floating Navigation Bar**: Tabs for Videos, Folders, Vault, and Settings with center protruding Play Button.
4. **Private Vault**:
   * **PIN Keypad Entry**: Glass digit circles with violet glow feedback and biometric quick-trigger.

---

## 6. Guidelines for Future AI Assistants

* **Always preserve token fidelity**: Never introduce arbitrary hex colors. Stick to the tokens listed in this document.
* **Component-first**: Build each piece as an isolated, typed React component (e.g., `Seekbar.tsx`, `GestureHUD.tsx`, `EqualizerSheet.tsx`).
* **Mirror Jetpack Compose**: Maintain data models and component names that align with Android's [`PlayMeTheme.kt`](file:///d:/Web-Dev/react-native/playme/app/src/main/java/com/playme/player/core/designsystem/PlayMeTheme.kt).
* **Smoothness first**: All buttons must include hover and active states (`active:scale-95`). All sheet modals must slide up from the bottom with glass backdrop blur.
