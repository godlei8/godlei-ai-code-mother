import { readFileSync } from 'node:fs'
import { describe, expect, it } from 'vitest'

const source = readFileSync(new URL('../AppChatPage.vue', import.meta.url), 'utf8')

describe('AppChatPage deploy action', () => {
  it('renders a visible deploy button label next to the preview actions', () => {
    expect(source).toContain('{{ deployButtonText }}')
  })

  it('switches the deploy button label when the app already has a deploy url', () => {
    expect(source).toContain("return deployedUrl.value ? '重新部署' : '部署应用'")
  })

  it('passes the current login user avatar into the chat message list', () => {
    expect(source).toContain(':user-avatar="loginUser?.userAvatar"')
    expect(source).toContain(":user-name=\"loginUser?.userName || loginUser?.userAccount || '用户'\"")
  })

  it('keeps the preview loading animation tied to the sending state', () => {
    expect(source).toContain(':loading="sending"')
    expect(source).not.toContain(':loading="sending && !previewReady"')
  })
})
