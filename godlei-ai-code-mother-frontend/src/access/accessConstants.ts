export const ACCESS_ROLE = {
  GUEST: 'guest',
  USER: 'user',
  ADMIN: 'admin',
} as const

export type AccessRole = (typeof ACCESS_ROLE)[keyof typeof ACCESS_ROLE]

export const ACCESS_ROLE_LABEL: Record<AccessRole, string> = {
  guest: '游客',
  user: '普通用户',
  admin: '管理员',
}

export const ACCESS_ACTION = {
  PROFILE_EDIT: 'profile.edit',
  USER_VIEW: 'user.view',
  USER_CREATE: 'user.create',
  USER_EDIT: 'user.edit',
  USER_DELETE: 'user.delete',
  APP_CREATE: 'app.create',
  APP_EDIT: 'app.edit',
  APP_DELETE: 'app.delete',
  APP_DEPLOY: 'app.deploy',
  APP_MANAGE_VIEW: 'app.manage.view',
  CHAT_HISTORY_VIEW: 'chat.history.view',
  ACCESS_VIEW: 'access.view',
} as const

export type AccessAction = (typeof ACCESS_ACTION)[keyof typeof ACCESS_ACTION]

export const ACCESS_ACTION_LABEL: Record<AccessAction, string> = {
  'profile.edit': '编辑个人资料',
  'user.view': '查看用户列表',
  'user.create': '新增用户',
  'user.edit': '编辑用户',
  'user.delete': '删除用户',
  'app.create': '创建应用',
  'app.edit': '编辑应用',
  'app.delete': '删除应用',
  'app.deploy': '部署应用',
  'app.manage.view': '查看应用管理',
  'chat.history.view': '查看对话管理',
  'access.view': '查看权限中心',
}

export const ACCESS_ROLE_PRIORITY: Record<AccessRole, number> = {
  guest: 0,
  user: 1,
  admin: 2,
}

export const normalizeAccessRole = (role?: string): AccessRole => {
  if (role === ACCESS_ROLE.ADMIN) {
    return ACCESS_ROLE.ADMIN
  }
  if (role === ACCESS_ROLE.USER) {
    return ACCESS_ROLE.USER
  }
  return ACCESS_ROLE.GUEST
}
