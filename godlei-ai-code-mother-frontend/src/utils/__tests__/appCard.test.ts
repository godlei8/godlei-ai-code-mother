import { describe, expect, it } from 'vitest'
import { hydrateOwnedAppCreator, resolveAppCardProfile } from '../appCard'

describe('resolveAppCardProfile', () => {
  it('prefers the creator nickname and avatar from the app payload', () => {
    const profile = resolveAppCardProfile({
      appName: '旅行网站',
      userName: 'NoCode 官方',
      userAvatar: 'https://example.com/avatar.png',
    } as API.AppVO)

    expect(profile).toEqual({
      title: '旅行网站',
      creatorName: 'NoCode 官方',
      avatarUrl: 'https://example.com/avatar.png',
      initials: 'N',
    })
  })

  it('falls back to friendly defaults when creator info is missing', () => {
    const profile = resolveAppCardProfile({
      appName: '',
    } as API.AppVO)

    expect(profile).toEqual({
      title: '未命名应用',
      creatorName: '匿名用户',
      avatarUrl: '',
      initials: '匿',
    })
  })
})

describe('hydrateOwnedAppCreator', () => {
  it('fills the creator nickname and avatar for owned apps from the current login user', () => {
    const app = hydrateOwnedAppCreator(
      {
        id: '1',
        appName: '个人博客',
        userId: '99',
      } as API.AppVO,
      {
        id: '99',
        userName: '张三',
        userAvatar: 'https://example.com/me.png',
      } as API.LoginUserVO,
    )

    expect(app.userName).toBe('张三')
    expect(app.userAvatar).toBe('https://example.com/me.png')
  })

  it('keeps existing creator info when the app payload already has it', () => {
    const app = hydrateOwnedAppCreator(
      {
        id: '1',
        appName: '企业官网',
        userId: '99',
        userName: '平台官方',
        userAvatar: 'https://example.com/official.png',
      } as API.AppVO,
      {
        id: '99',
        userName: '张三',
        userAvatar: 'https://example.com/me.png',
      } as API.LoginUserVO,
    )

    expect(app.userName).toBe('平台官方')
    expect(app.userAvatar).toBe('https://example.com/official.png')
  })
})
