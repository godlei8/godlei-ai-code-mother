import {
  ACCESS_ACTION,
  ACCESS_ROLE,
  ACCESS_ROLE_PRIORITY,
  type AccessAction,
  type AccessRole,
} from '@/access/accessConstants'

export interface AccessRouteEntry {
  key: string
  label: string
  description: string
  access: AccessRole
}

export interface AccessActionEntry {
  key: AccessAction
  label: string
  description: string
  access: AccessRole
}

export const ACCESS_ROUTE_ENTRIES: AccessRouteEntry[] = [
  {
    key: '/',
    label: '首页',
    description: '平台公开首页，未登录用户也可访问，后续可继续扩展业务展示内容。',
    access: ACCESS_ROLE.GUEST,
  },
  {
    key: '/user/profile',
    label: '个人中心',
    description: '登录用户可查看和编辑自己的资料。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: '/user/manage',
    label: '用户管理',
    description: '管理员可查看、筛选、创建、编辑和删除用户。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: '/access/manage',
    label: '权限管理',
    description: '管理员可查看全局角色与页面、动作权限映射。',
    access: ACCESS_ROLE.ADMIN,
  },
]

export const ACCESS_ACTION_ENTRIES: AccessActionEntry[] = [
  {
    key: ACCESS_ACTION.PROFILE_EDIT,
    label: '编辑个人资料',
    description: '编辑昵称、头像、简介等个人资料字段。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: ACCESS_ACTION.USER_VIEW,
    label: '查看用户列表',
    description: '查看后台用户列表和筛选结果。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.USER_CREATE,
    label: '新增用户',
    description: '创建新的后台用户账号。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.USER_EDIT,
    label: '编辑用户',
    description: '编辑用户昵称、简介、角色等信息。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.USER_DELETE,
    label: '删除用户',
    description: '删除后台用户记录。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.ACCESS_VIEW,
    label: '查看权限中心',
    description: '查看前端全局路由与动作权限映射。',
    access: ACCESS_ROLE.ADMIN,
  },
]

export const canRoleAccessRole = (currentRole: AccessRole, requiredRole?: AccessRole) => {
  if (!requiredRole) {
    return true
  }
  return ACCESS_ROLE_PRIORITY[currentRole] >= ACCESS_ROLE_PRIORITY[requiredRole]
}

export const canRoleAccessRoute = (currentRole: AccessRole, routeKey: string) => {
  const route = ACCESS_ROUTE_ENTRIES.find((item) => item.key === routeKey)
  if (!route) {
    return false
  }
  return canRoleAccessRole(currentRole, route.access)
}

export const canRoleAccessAction = (currentRole: AccessRole, action: AccessAction) => {
  const actionEntry = ACCESS_ACTION_ENTRIES.find((item) => item.key === action)
  if (!actionEntry) {
    return false
  }
  return canRoleAccessRole(currentRole, actionEntry.access)
}
