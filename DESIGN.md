---
name: Noscam
description: A clear “is this a scam?” check — calm, protective product UI
colors:
  harbor-slate: "oklch(0.218 0.008 223.9)"
  harbor-ink: "oklch(0.148 0.004 228.8)"
  surface: "oklch(1 0 0)"
  surface-muted: "oklch(0.963 0.002 197.1)"
  surface-sidebar: "oklch(0.987 0.002 197.1)"
  ink-muted: "oklch(0.56 0.021 213.5)"
  border: "oklch(0.925 0.005 214.3)"
  ring: "oklch(0.723 0.014 214.4)"
  on-primary: "oklch(0.987 0.002 197.1)"
  destructive: "oklch(0.577 0.245 27.325)"
  destructive-soft: "oklch(0.577 0.245 27.325 / 0.1)"
typography:
  page:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 600
    lineHeight: 1.25
    letterSpacing: "-0.01em"
  title:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 550
    lineHeight: 1.35
    letterSpacing: "-0.01em"
  body:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.45
    letterSpacing: "normal"
  label:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 500
    lineHeight: 1.25
    letterSpacing: "normal"
  meta:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 400
    lineHeight: 1.35
    letterSpacing: "normal"
  micro:
    fontFamily: "Geist Variable, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 500
    lineHeight: 1.2
    letterSpacing: "normal"
rounded:
  sm: "0.125rem"
  md: "0.25rem"
  lg: "0.375rem"
  xl: "0.5rem"
  # Linear-like: 2px / 4px / 6px / 8px. Controls use md (4px). Never pill.
spacing:
  xs: "0.25rem"
  sm: "0.5rem"
  md: "0.75rem"
  lg: "1rem"
  xl: "1.5rem"
  2xl: "2rem"
components:
  button-primary:
    backgroundColor: "{colors.harbor-slate}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
    padding: "0.25rem 0.625rem"
    height: "1.75rem"
    typography: "{typography.label}"
  button-primary-hover:
    backgroundColor: "oklch(0.218 0.008 223.9 / 0.8)"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
  button-outline:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.harbor-ink}"
    rounded: "{rounded.md}"
    padding: "0.25rem 0.625rem"
    height: "1.75rem"
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.harbor-ink}"
    rounded: "{rounded.md}"
    padding: "0.25rem 0.625rem"
    height: "1.75rem"
  button-destructive:
    backgroundColor: "{colors.destructive-soft}"
    textColor: "{colors.destructive}"
    rounded: "{rounded.md}"
    padding: "0.25rem 0.625rem"
    height: "1.75rem"
  breadcrumb:
    textColor: "{colors.ink-muted}"
    typography: "{typography.body}"
---

# Design System: Noscam

## 1. Overview

**Creative North Star: "The Quiet Second Opinion"**

Noscam’s visual system is a calm product surface for watching transaction risk. The UI should feel like a steady colleague: clear hierarchy, plain language, no drama. Density is **Linear-like** — compact chrome, issue-list rows, quiet toolbars — not marketing whitespace. Structure and typography do the work, not decoration.

Color is Harbor Slate — cool blue-gray ink on white — money-serious without bank-app theater. Elevation is flat: tonal layers and hairline borders, never lift for its own sake. Components are refined and restrained: tight corner radii, compact controls, quiet hover, focus rings that earn trust for keyboard users.

This system explicitly rejects loud cybersecurity / red-alert threat theater, bloated personal-finance dashboards, and generic SaaS marketing (purple gradients, feature-card grids, startup hype).

**Key Characteristics:**
- Single sans (Geist Variable) across UI — no display pairing
- Restrained cool neutrals; saturation reserved for risk/destructive
- Flat tonal depth; no decorative shadows
- Linear-like density: compact sidebar, toolbar page titles, dense list rows, sticky detail pane
- Tight radii (Linear-like 4px controls); no pill buttons; focus ring as the main interaction signal
- One job per screen — Connect sources, score transactions

## 2. Colors

Cool mist neutrals with Harbor Slate as the primary ink — serious, protective, never neon.

### Primary
- **Harbor Slate** (`oklch(0.218 0.008 223.9)`): Primary actions, key ink on light surfaces, the default “voice” of the product. Used sparingly as fill (buttons, strong emphasis), freely as text.

### Neutral
- **Harbor Ink** (`oklch(0.148 0.004 228.8)`): Default body/foreground text on light backgrounds.
- **Surface** (`oklch(1 0 0)`): Page and card backgrounds.
- **Surface Muted** (`oklch(0.963 0.002 197.1)`): Secondary fills, hover washes, quiet panels.
- **Surface Sidebar** (`oklch(0.987 0.002 197.1)`): App chrome / sidebar plane — a second neutral layer slightly off the content surface.
- **Ink Muted** (`oklch(0.56 0.021 213.5)`): Supporting labels, breadcrumbs, secondary copy. Must stay ≥4.5:1 on Surface; bump toward Harbor Ink if contrast slips.
- **Border** (`oklch(0.925 0.005 214.3)`): Hairline dividers and field strokes.
- **Ring** (`oklch(0.723 0.014 214.4)`): Focus-visible rings (`ring-3` at ~30% opacity).
- **On Primary** (`oklch(0.987 0.002 197.1)`): Text/icons on Harbor Slate fills.

### Semantic
- **Destructive** (`oklch(0.577 0.245 27.325)`): Risk, errors, scam-positive emphasis in content — never as ambient chrome.
- **Destructive Soft** (`oklch(0.577 0.245 27.325 / 0.1)`): Soft destructive button fill; text stays Destructive.

### Named Rules
**The Soft Signal Rule.** Saturation is for risk and error only. Harbor Slate and neutrals carry the UI; never paint inactive chrome in alert red or neon “security” accents.

**The One Voice Rule.** Primary fill (Harbor Slate) appears on ≤10% of any screen — primary CTA and selected emphasis. Its rarity is the point.

## 3. Typography

**Display Font:** Geist Variable (with `ui-sans-serif, system-ui, sans-serif`)
**Body Font:** Geist Variable (same stack)
**Label/Mono Font:** Geist Variable (no separate mono required)

**Character:** One technical-humanist sans, tuned for Linear-like product density. Quiet confidence — weight and size carry hierarchy, never a second display face. Prefer `text-sm` (0.875rem) and `text-xs` (0.75rem) in Tailwind; avoid large display headings in app chrome.

### Hierarchy
- **Page** (600, 0.875rem): Toolbar / view titles (“Transactions”, “Connect”). Same size as body; weight does the work.
- **Title** (550, 0.875rem): Detail pane headings, row primary text.
- **Body** (400, 0.875rem): Default UI copy; prose max ~65–75ch.
- **Label** (500, 0.875rem): Buttons, nav items, field labels.
- **Meta** (400, 0.75rem): Secondary row lines, timestamps, helper copy.
- **Micro** (500, 0.75rem): Badges, section eyebrows, compact counts.

### Named Rules
**The Fixed Scale Rule.** Product type uses fixed rem steps. No fluid `clamp()` headings in app chrome. No oversized page titles — Linear density means page = body size, semibold.

**The One Family Rule.** Geist Variable for everything. No serif display, no mono-for-flavor in labels.

**The Dense Chrome Rule.** Sidebar nav ~28px tall, toolbars ~44px, list rows compact with hairline dividers. Prefer full-bleed list + sticky detail over centered marketing layouts.

## 4. Elevation

Flat by default. Depth comes from tonal layering (Surface → Surface Muted → Surface Sidebar) and 1px Border strokes — not shadows. The current token set defines no `box-shadow` vocabulary; do not invent one for decoration.

### Shadow Vocabulary
None. If a future interaction needs lift, it must be state-driven and documented before use — never ambient card shadows.

### Named Rules
**The Flat-By-Default Rule.** Surfaces are flat at rest. No multi-layer shadows, no glassmorphism, no glow. Hierarchy = tone + border + spacing.

## 5. Components

Feel: **refined and restrained** — Linear-like density, tight corners, quiet hover, focus ring does the work.

### Buttons
- **Shape:** Linear-like (`rounded-md` → 4px / `--radius-md`). Product-tool, not pill candy.
- **Primary:** Harbor Slate fill, On Primary text; height ~1.75rem (`sm`), horizontal padding ~0.625rem; medium weight label. Prefer `size="sm"` in app chrome.
- **Hover:** Primary at 80% opacity; active presses `translateY(1px)` only when not a popup trigger.
- **Focus:** Border Ring + `ring-3` at Ring/30% — never skip for “clean” aesthetics.
- **Outline:** Surface fill, Border stroke, Harbor Ink text; muted wash on hover.
- **Ghost:** Transparent; muted wash on hover.
- **Destructive:** Soft destructive fill + Destructive text (not solid red blocks).
- **Disabled:** 50% opacity, no pointer events.

### Cards / Containers
- **Corner Style:** Linear-like fixed scale — `sm` 2px, `md` 4px (default controls), `lg` 6px, `xl` 8px. Prefer `rounded-sm` / `rounded-md`; never `rounded-xl`+.
- **Background:** Surface; optional Surface Muted for nested quiet zones.
- **Shadow Strategy:** None — Flat-By-Default.
- **Border:** 1px Border when a container needs an edge; prefer spacing over boxing.
- **Internal Padding:** Prefer `lg`–`xl` (1–1.5rem); avoid nested cards.

### Inputs / Fields
- **Style:** Border stroke on Surface; radius `md` (4px), matching buttons.
- **Focus:** Ring treatment matching buttons (`ring` / focus-visible).
- **Error:** Destructive border + soft destructive ring; never side-stripe accents.

### Navigation
- **Breadcrumb:** Body size, Ink Muted; current page steps toward Harbor Ink. Chevron separators, no heavy chrome.
- **App chrome:** Surface Sidebar for side/top planes; hairline Border separators. No purple accent rails.

### Signature: Scam result call (when built)
Lead with plain-language verdict and next action. Destructive/semantic color only on the finding itself — never on the whole page chrome. No shield icons as decoration, no panic motion.

## 6. Do's and Don'ts

### Do:
- **Do** keep every screen answering one job: “is this a scam?”
- **Do** use Harbor Slate + cool neutrals; reserve saturation for risk/error.
- **Do** meet WCAG 2.2 AA — body/placeholder contrast ≥4.5:1; large text ≥3:1; keyboard + `prefers-reduced-motion`.
- **Do** use tight-radius buttons and quiet hover; let the focus ring carry accessibility.
- **Do** convey depth with tonal layers and hairline borders.
- **Do** write plain-language next steps; urgency lives in the finding, not the chrome.

### Don't:
- **Don't** use loud “cybersecurity” / red-alert threat theater (shields, hacker aesthetics, panic UI).
- **Don't** build bloated personal-finance dashboards (charts everywhere, budgets, net-worth theater).
- **Don't** ship generic SaaS marketing (purple gradients, feature-card grids, startup hype).
- **Don't** use side-stripe borders (`border-left`/`border-right` >1px) as accent.
- **Don't** use gradient text, glassmorphism, or decorative glow.
- **Don't** invent shadow elevation for cards at rest.
- **Don't** use pill / fully-rounded buttons or large container radii (`rounded-xl`+); keep corners tight.
- **Don't** bury a real warning to feel polished — catch over comfort.
