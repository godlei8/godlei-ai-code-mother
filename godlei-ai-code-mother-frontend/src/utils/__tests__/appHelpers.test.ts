import { describe, expect, it } from 'vitest'
import {
  buildAppNameFromPrompt,
  formatAppRelativeTime,
  resolveAppDetailLoadMode,
  resolveAppEditorMode,
} from '../appHelpers'

describe('buildAppNameFromPrompt', () => {
  it('uses the first meaningful line as the fallback app name', () => {
    expect(buildAppNameFromPrompt('  为宠物店生成一个预约网站  \n需要包含后台管理')).toBe(
      '为宠物店生成一个预约网站',
    )
  })

  it('falls back to a default app name for empty prompts', () => {
    expect(buildAppNameFromPrompt('   \n  ')).toBe('AI 生成应用')
  })

  it('truncates very long prompt names', () => {
    expect(buildAppNameFromPrompt('这是一个特别特别特别长的应用名称描述，用来测试截断逻辑')).toBe(
      '这是一个特别特别特别长的应用...',
    )
  })
})

describe('resolveAppEditorMode', () => {
  it('gives admins full edit access', () => {
    expect(resolveAppEditorMode('admin', 1, 999)).toEqual({
      mode: 'admin',
      canEdit: true,
      useAdminApi: true,
    })
  })

  it('lets owners edit with the user api', () => {
    expect(resolveAppEditorMode('user', 12, 12)).toEqual({
      mode: 'self',
      canEdit: true,
      useAdminApi: false,
    })
  })

  it('treats matching string and number ids as the same owner', () => {
    expect(resolveAppEditorMode('user', '12' as never, 12 as never)).toEqual({
      mode: 'self',
      canEdit: true,
      useAdminApi: false,
    })
  })

  it('blocks non-owners from editing', () => {
    expect(resolveAppEditorMode('user', 12, 18)).toEqual({
      mode: 'forbidden',
      canEdit: false,
      useAdminApi: false,
    })
  })
})

describe('resolveAppDetailLoadMode', () => {
  it('keeps admin users on the self-service detail api by default', () => {
    expect(resolveAppDetailLoadMode('admin', undefined)).toEqual({
      useAdminApi: false,
      mode: 'self',
    })
  })

  it('uses the admin detail api only when admin mode is explicitly requested', () => {
    expect(resolveAppDetailLoadMode('admin', 'admin')).toEqual({
      useAdminApi: true,
      mode: 'admin',
    })
  })

  it('ignores admin mode requests for non-admin users', () => {
    expect(resolveAppDetailLoadMode('user', 'admin')).toEqual({
      useAdminApi: false,
      mode: 'self',
    })
  })
})

describe('formatAppRelativeTime', () => {
  const now = new Date('2026-05-08T12:00:00.000Z')

  it('shows recent times as just now', () => {
    expect(formatAppRelativeTime('2026-05-08T11:59:40.000Z', now)).toBe('刚刚')
  })

  it('shows hour-level relative times', () => {
    expect(formatAppRelativeTime('2026-05-08T09:10:00.000Z', now)).toBe('2 小时前')
  })

  it('shows day-level relative times', () => {
    expect(formatAppRelativeTime('2026-05-06T10:00:00.000Z', now)).toBe('2 天前')
  })

  it('falls back for missing times', () => {
    expect(formatAppRelativeTime(undefined, now)).toBe('-')
  })
})
