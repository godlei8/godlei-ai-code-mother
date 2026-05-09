export type AppCardProfile = {
  title: string
  creatorName: string
  avatarUrl: string
  initials: string
}

type AppCardSource = API.AppVO & {
  userName?: string
  userAvatar?: string
}

const DEFAULT_TITLE = '未命名应用'
const DEFAULT_CREATOR_NAME = '匿名用户'

const buildInitials = (name: string) => {
  const normalized = name.trim()
  const firstCharacter = Array.from(normalized)[0] ?? 'A'
  return /[a-z]/i.test(firstCharacter) ? firstCharacter.toUpperCase() : firstCharacter
}

export const hydrateOwnedAppCreator = (app: API.AppVO, loginUser?: API.LoginUserVO | null): API.AppVO => {
  const appWithCreator = app as AppCardSource

  if (!loginUser?.id) {
    return app
  }

  if (String(appWithCreator.userId ?? '') !== String(loginUser.id)) {
    return app
  }

  return {
    ...appWithCreator,
    userName:
      appWithCreator.userName?.trim() ||
      loginUser.userName ||
      loginUser.userAccount ||
      DEFAULT_CREATOR_NAME,
    userAvatar: appWithCreator.userAvatar?.trim() || loginUser.userAvatar || '',
  } as API.AppVO
}

export const resolveAppCardProfile = (app: API.AppVO): AppCardProfile => {
  const appWithCreator = app as AppCardSource
  const title = appWithCreator.appName?.trim() || DEFAULT_TITLE
  const creatorName = appWithCreator.userName?.trim() || DEFAULT_CREATOR_NAME
  const avatarUrl = appWithCreator.userAvatar?.trim() || ''

  return {
    title,
    creatorName,
    avatarUrl,
    initials: buildInitials(creatorName),
  }
}
