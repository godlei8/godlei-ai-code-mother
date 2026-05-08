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
    description: '平台公开首页，游客也可以浏览精选应用和产品能力说明。',
    access: ACCESS_ROLE.GUEST,
  },
  {
    key: '/user/profile',
    label: '个人中心',
    description: '登录用户可以查看和编辑自己的账户资料。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: '/user/manage',
    label: '用户管理',
    description: '管理员可以筛选、创建、编辑和删除用户。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: '/app/manage',
    label: '应用管理',
    description: '管理员可以查看、筛选、编辑、删除应用并设置精选。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: '/access/manage',
    label: '权限管理',
    description: '管理员可以查看前端路由和动作级权限映射。',
    access: ACCESS_ROLE.ADMIN,
  },
]

export const ACCESS_ACTION_ENTRIES: AccessActionEntry[] = [
  {
    key: ACCESS_ACTION.PROFILE_EDIT,
    label: '编辑个人资料',
    description: '编辑昵称、头像和个人简介。',
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
    description: '编辑用户昵称、简介和角色。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.USER_DELETE,
    label: '删除用户',
    description: '删除后台用户记录。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.APP_CREATE,
    label: '创建应用',
    description: '登录用户可以输入提示词创建新的 AI 应用。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: ACCESS_ACTION.APP_EDIT,
    label: '编辑应用',
    description: '应用所有者可编辑名称，管理员可编辑完整应用信息。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: ACCESS_ACTION.APP_DELETE,
    label: '删除应用',
    description: '应用所有者或管理员可以删除应用。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: ACCESS_ACTION.APP_DEPLOY,
    label: '部署应用',
    description: '登录用户可以部署应用并获取访问地址。',
    access: ACCESS_ROLE.USER,
  },
  {
    key: ACCESS_ACTION.APP_MANAGE_VIEW,
    label: '查看应用管理',
    description: '管理员可以进入应用管理页面管理全站应用。',
    access: ACCESS_ROLE.ADMIN,
  },
  {
    key: ACCESS_ACTION.ACCESS_VIEW,
    label: '查看权限中心',
    description: '查看前端路由和动作级权限映射。',
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
