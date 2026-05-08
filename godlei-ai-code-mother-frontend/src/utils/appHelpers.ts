import type { AccessRole } from '@/access/accessConstants'

const DEFAULT_APP_NAME = 'AI 生成应用'
const APP_NAME_MAX_LENGTH = 16
const APP_PENDING_PROMPT_KEY_PREFIX = 'godlei_app_pending_prompt_'

export interface AppEditorModeResult {
  mode: 'admin' | 'self' | 'forbidden'
  canEdit: boolean
  useAdminApi: boolean
}

export interface AppDetailLoadModeResult {
  mode: 'admin' | 'self'
  useAdminApi: boolean
}

const normalizeIdValue = (id?: string | number) => {
  if (id === null || id === undefined) {
    return ''
  }
  return String(id)
}

const padDateSegment = (value: number) => String(value).padStart(2, '0')

const getPendingPromptStorageKey = (appId: string | number) => {
  return `${APP_PENDING_PROMPT_KEY_PREFIX}${appId}`
}

export const buildAppNameFromPrompt = (prompt: string, maxLength = APP_NAME_MAX_LENGTH) => {
  const firstMeaningfulLine = prompt
    .split('\n')
    .map((line) => line.trim())
    .find(Boolean)

  if (!firstMeaningfulLine) {
    return DEFAULT_APP_NAME
  }

  if (firstMeaningfulLine.length <= maxLength) {
    return firstMeaningfulLine
  }

  return `${firstMeaningfulLine.slice(0, Math.max(1, maxLength - 2))}...`
}

export const resolveAppEditorMode = (
  currentRole: AccessRole,
  currentUserId?: string | number,
  appUserId?: string | number,
): AppEditorModeResult => {
  if (currentRole === 'admin') {
    return {
      mode: 'admin',
      canEdit: true,
      useAdminApi: true,
    }
  }

  if (
    currentRole === 'user' &&
    normalizeIdValue(currentUserId) &&
    normalizeIdValue(currentUserId) === normalizeIdValue(appUserId)
  ) {
    return {
      mode: 'self',
      canEdit: true,
      useAdminApi: false,
    }
  }

  return {
    mode: 'forbidden',
    canEdit: false,
    useAdminApi: false,
  }
}

export const resolveAppDetailLoadMode = (
  currentRole: AccessRole,
  requestedMode?: string,
): AppDetailLoadModeResult => {
  if (currentRole === 'admin' && requestedMode === 'admin') {
    return {
      mode: 'admin',
      useAdminApi: true,
    }
  }

  return {
    mode: 'self',
    useAdminApi: false,
  }
}

export const formatAppRelativeTime = (input?: string, now = new Date()) => {
  if (!input) {
    return '-'
  }

  const targetTime = new Date(input)
  if (Number.isNaN(targetTime.getTime())) {
    return '-'
  }

  const diff = Math.max(0, now.getTime() - targetTime.getTime())
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  }

  if (diff < hour) {
    return `${Math.floor(diff / minute)} 分钟前`
  }

  if (diff < day) {
    return `${Math.floor(diff / hour)} 小时前`
  }

  if (diff < 7 * day) {
    return `${Math.floor(diff / day)} 天前`
  }

  return input.slice(0, 10)
}

export const formatAppDateTime = (input?: string) => {
  if (!input) {
    return '-'
  }

  const targetTime = new Date(input)
  if (Number.isNaN(targetTime.getTime())) {
    return '-'
  }

  const year = targetTime.getFullYear()
  const month = padDateSegment(targetTime.getMonth() + 1)
  const date = padDateSegment(targetTime.getDate())
  const hours = padDateSegment(targetTime.getHours())
  const minutes = padDateSegment(targetTime.getMinutes())
  const seconds = padDateSegment(targetTime.getSeconds())

  return `${year}-${month}-${date} ${hours}:${minutes}:${seconds}`
}

export const resolveAppCreatorDisplayName = (
  appUserId?: string | number,
  appUserName?: string,
  currentLoginUser?: API.LoginUserVO | null,
) => {
  const creatorName = appUserName?.trim()
  if (creatorName) {
    return creatorName
  }

  if (
    currentLoginUser?.id !== undefined &&
    normalizeIdValue(currentLoginUser.id) === normalizeIdValue(appUserId)
  ) {
    return currentLoginUser.userName?.trim() || currentLoginUser.userAccount?.trim() || '-'
  }

  return '-'
}

export const savePendingAppPrompt = (appId: string | number, prompt: string) => {
  if (typeof window === 'undefined') {
    return
  }

  window.sessionStorage.setItem(getPendingPromptStorageKey(appId), prompt)
}

export const consumePendingAppPrompt = (appId: string | number) => {
  if (typeof window === 'undefined') {
    return ''
  }

  const storageKey = getPendingPromptStorageKey(appId)
  const prompt = window.sessionStorage.getItem(storageKey) ?? ''

  if (prompt) {
    window.sessionStorage.removeItem(storageKey)
  }

  return prompt
}
