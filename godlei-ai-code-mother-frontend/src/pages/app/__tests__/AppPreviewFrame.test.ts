import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  new URL('../../../components/app/AppPreviewFrame.vue', import.meta.url),
  'utf8',
)

describe('AppPreviewFrame generating state', () => {
  it('renders a dedicated generating state when preview is still unavailable', () => {
    expect(source).toContain('v-if="loading && !src"')
  })

  it('keeps a standalone generation animation shell for the preview area', () => {
    expect(source).toContain('preview-generation-state')
    expect(source).toContain('generation-orbit')
    expect(source).toContain('generation-code-line')
  })
})
