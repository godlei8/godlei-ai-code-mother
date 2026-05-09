import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(
  new URL('../../../components/app/AppChatMessageList.vue', import.meta.url),
  'utf8',
)

describe('AppChatMessageList user avatar', () => {
  it('renders a user avatar next to user messages', () => {
    expect(source).toContain('v-if="item.role === \'user\'"')
    expect(source).toContain('class="user-avatar"')
    expect(source).toContain('{{ userAvatarText }}')
  })

  it('falls back to the user initial when no avatar url is available', () => {
    expect(source).toContain("props.userName?.slice(0, 1).toUpperCase() || 'U'")
  })
})
