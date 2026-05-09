import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  new URL('../../../components/app/AppCard.vue', import.meta.url),
  'utf8',
)

describe('AppCard compact layout', () => {
  it('uses smaller primary action buttons and tighter overlay spacing', () => {
    expect(source).toContain('height: 36px;')
    expect(source).toContain('inset: 12px;')
    expect(source).toContain('width: min(252px, 100%);')
  })

  it('keeps the content area visually compact with reduced internal spacing', () => {
    expect(source).toContain('padding: 12px 12px 14px;')
    expect(source).toContain('gap: 10px;')
    expect(source).toContain(':size="40"')
  })
})
