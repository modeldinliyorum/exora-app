# DESIGN

Mode: **Operate** — the user reads a number at a glance, then may switch base currency.

## World
A premium dark "market board". Pure black canvas, softly raised near-black cards,
one hairline stroke, a single live accent. Monochrome restraint with a green
liveness dot. Nothing decorative.

## Tokens
| Token | Value |
|---|---|
| background | `#000000` |
| surface (card) | `#0E0F11` |
| surface high | `#16171A` |
| stroke | `#1E2024` (1dp) |
| foreground | `#FFFFFF` |
| muted | `#8B9099` |
| faint | `#5B6068` |
| live accent | `#34D399` |

Contrast: white/black 21:1, muted/black ≈ 6.4:1 — WCAG AA for text.

## Typography
System sans (`sans-serif-medium`) at three tiers, with tabular figures
(`tnum`) on all numbers so digits align across cards.
- Rate value: **30sp**
- Currency code: 18sp
- Labels/units: 11–12sp, letter-spaced uppercase for section labels.

## Layout (edge-to-edge)
1. **Top bar** — `EXORA` wordmark + live dot + last-updated time.
2. **BASE CURRENCY** label + a horizontally scrollable **chip bar** (23 currencies).
   Selected chip = filled white pill, black text; unselected = near-black with a
   hairline stroke. This is the primary interaction.
3. **Rate list** — a `RecyclerView` of raised cards, one per currency:
   `1 CODE =` (left) … `value` + base unit (right). USD and EUR lead.
4. **Pull-to-refresh** — swipe down reloads; spinner tinted white.

System status and navigation bars are transparent, content drawn behind them, with
insets applied as padding to header and list.

## Motion
- Base switch and refresh animate through `DiffUtil` + a tuned `DefaultItemAnimator`
  (fade/move, 160–220ms).
- Chip selection animates via Material's own state change.
- No ambient or looping motion.

## Anti-patterns avoided
No gradients, glass blur, emoji icons, drop-shadow noise, or more than one accent.
