import type { RouteRecordRaw } from 'vue-router'
import { ACCESS_ROLE } from '@/access/accessConstants'
import BasicLayout from '@/layouts/BasicLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

export const AUTH_LOGIN_ROUTE_NAME = 'auth-login'
export const AUTH_REGISTER_ROUTE_NAME = 'auth-register'
export const FORBIDDEN_ROUTE_NAME = 'forbidden'
export const DEFAULT_HOME_ROUTE = '/'

export const routes: RouteRecordRaw[] = [
  {
    path: '/auth',
    component: AuthLayout,
    meta: {
      requiresAuth: false,
      hideInMenu: true,
    },
    children: [
      {
        path: 'login',
        name: AUTH_LOGIN_ROUTE_NAME,
        component: () => import('@/pages/auth/LoginPage.vue'),
        meta: {
          title: '登录',
          requiresAuth: false,
          hideInMenu: true,
        },
      },
      {
        path: 'register',
        name: AUTH_REGISTER_ROUTE_NAME,
        component: () => import('@/pages/auth/RegisterPage.vue'),
        meta: {
          title: '注册',
          requiresAuth: false,
          hideInMenu: true,
        },
      },
    ],
  },
  {
    path: '/',
    component: BasicLayout,
    meta: {
      requiresAuth: false,
      access: ACCESS_ROLE.GUEST,
    },
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/pages/home/HomePage.vue'),
        meta: {
          title: '首页',
          requiresAuth: false,
          access: ACCESS_ROLE.GUEST,
          menuLabel: '首页',
          menuOrder: 1,
        },
      },
      {
        path: 'dashboard',
        redirect: DEFAULT_HOME_ROUTE,
        meta: {
          hideInMenu: true,
        },
      },
      {
        path: 'user/profile',
        name: 'user-profile',
        component: () => import('@/pages/user/ProfilePage.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true,
          access: ACCESS_ROLE.USER,
          menuLabel: '个人中心',
          menuOrder: 2,
        },
      },
      {
        path: 'app/chat/:id',
        name: 'app-chat',
        component: () => import('@/pages/app/AppChatPage.vue'),
        meta: {
          title: '应用生成',
          requiresAuth: true,
          access: ACCESS_ROLE.USER,
          hideInMenu: true,
        },
      },
      {
        path: 'app/edit/:id',
        name: 'app-edit',
        component: () => import('@/pages/app/AppEditPage.vue'),
        meta: {
          title: '编辑应用',
          requiresAuth: true,
          access: ACCESS_ROLE.USER,
          hideInMenu: true,
        },
      },
      {
        path: 'app/manage',
        name: 'app-manage',
        component: () => import('@/pages/app/AppManagePage.vue'),
        meta: {
          title: '应用管理',
          requiresAuth: true,
          access: ACCESS_ROLE.ADMIN,
          menuLabel: '应用管理',
          menuOrder: 3,
        },
      },
      {
        path: 'user/manage',
        name: 'user-manage',
        component: () => import('@/pages/user/UserManagePage.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          access: ACCESS_ROLE.ADMIN,
          menuLabel: '用户管理',
          menuOrder: 4,
        },
      },
      {
        path: 'access/manage',
        name: 'access-manage',
        component: () => import('@/pages/access/AccessManagePage.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          access: ACCESS_ROLE.ADMIN,
          menuLabel: '权限管理',
          menuOrder: 5,
        },
      },
    ],
  },
  {
    path: '/403',
    name: FORBIDDEN_ROUTE_NAME,
    component: () => import('@/pages/system/ForbiddenPage.vue'),
    meta: {
      title: '无权限访问',
      requiresAuth: false,
      hideInMenu: true,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: DEFAULT_HOME_ROUTE,
  },
]

export const appMenuRoutes =
  routes
    .find((item) => item.path === '/')
    ?.children?.filter((item) => !item.meta?.hideInMenu)
    .sort((current, next) => (current.meta?.menuOrder ?? 0) - (next.meta?.menuOrder ?? 0))
    .map((item) => ({
      key: item.path?.startsWith('/') ? item.path : `/${item.path ?? ''}`,
      label: item.meta?.menuLabel ?? item.meta?.title ?? '',
      access: item.meta?.access ?? ACCESS_ROLE.USER,
    })) ?? []
