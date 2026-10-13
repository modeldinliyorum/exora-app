# DESIGN

Mode: **Operate** — the user completes a task (reading a number) at a glance.

## World
Pure black, pure white, nothing else. A currency board in the dark. The brief
pins this: black background, white text, no accent color. Restraint is the style.

## Tokens
| Token | Value |
|---|---|
| background | `#000000` |
| foreground | `#FFFFFF` |
| muted text | `#99FFFFFF` (60% white) |
| divider | `#1FFFFFFF` (12% white) |
| primary value size | 44sp, bold |
| secondary value size | 26sp, bold |
| label size | 18sp / 15sp |

Contrast: white on black = 21:1; muted on black ≈ 13:1 — both exceed WCAG AA.

## Layout
Single column, 24dp gutters, generous vertical rhythm (spacious dial).
1. **Primary block (top):** USD, then EUR. Largest type on screen.
2. Hairline divider.
3. **Secondary block:** GBP, CHF, JPY, AED, CAD, quieter.
4. Status line: "Updated HH:mm" / "Offline — showing last saved rates".

Each rate is one baseline-aligned line: muted `1 USD =` · bold white number ·
muted `TRY`. The number carries the hierarchy.

## Motion
None by design. Values update in place; the status line changes only on refresh.

## Anti-patterns avoided
No cards, shadows, gradients, icons-as-decoration, or color accents. No emoji.
