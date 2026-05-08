export type AppCardProfile = {
  title: string
  creatorName: string
  avatarUrl: string
  initials: string
}

const DEFAULT_TITLE = '未命名应用'
const DEFAULT_CREATOR_NAME = '匿名用户'

const buildInitials = (name: string) => {
  const normalized = name.trim()
  const firstCharacter = Array.from(normalized)[0] ?? 'A'
  return /[a-z]/i.test(firstCharacter) ? firstCharacter.toUpperCase() : firstCharacter
}

export const hydrateOwnedAppCreator = (app: API.AppVO, loginUser?: API.LoginUserVO | null): API.AppVO => {
  if (!loginUser?.id) {
    return app
  }

  if (String(app.userId ?? '') !== String(loginUser.id)) {
    return app
  }

  return {
    ...app,
    userName: app.userName?.trim() || loginUser.userName || loginUser.userAccount || DEFAULT_CREATOR_NAME,
    userAvatar: app.userAvatar?.trim() || loginUser.userAvatar || '',
  }
}

export const resolveAppCardProfile = (app: API.AppVO): AppCardProfile => {
  const title = app.appName?.trim() || DEFAULT_TITLE
  const creatorName = app.userName?.trim() || DEFAULT_CREATOR_NAME
  const avatarUrl = app.userAvatar?.trim() || ''

  return {
    title,
    creatorName,
    avatarUrl,
    initials: buildInitials(creatorName),
  }
}
