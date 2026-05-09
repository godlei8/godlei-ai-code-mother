import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  new URL('../../../components/app/AppCard.vue', import.meta.url),
  'utf8',
)

describe('AppCard action layout', () => {
  it('places preview and delete actions next to the creator profile area', () => {
    expect(source).toContain('profile-side-actions')
    expect(source).toContain('utilityActions.length')
  })

  it('reveals in-card primary actions only on hover or focus and keeps them centered', () => {
    expect(source).toContain('opacity: 0;')
    expect(source).toContain('justify-content: center;')
    expect(source).toContain('.app-card:hover .visual-actions')
    expect(source).toContain('.app-card:focus-visible .visual-actions')
  })
})
