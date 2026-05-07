import { createRouter, createWebHistory } from 'vue-router'
import { canRoleAccessRole } from '@/access/accessConfig'
import { normalizeAccessRole } from '@/access/accessConstants'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  AUTH_LOGIN_ROUTE_NAME,
  AUTH_REGISTER_ROUTE_NAME,
  DEFAULT_HOME_ROUTE,
  FORBIDDEN_ROUTE_NAME,
  routes,
} from '@/router/routes'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore()

  if (!loginUserStore.hasBootstrap) {
    await loginUserStore.fetchLoginUser()
  }

  const isLoggedIn = loginUserStore.isLogin

  if (to.meta.requiresAuth && !isLoggedIn) {
    return {
      name: AUTH_LOGIN_ROUTE_NAME,
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (
    isLoggedIn &&
    (to.name === AUTH_LOGIN_ROUTE_NAME || to.name === AUTH_REGISTER_ROUTE_NAME)
  ) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : DEFAULT_HOME_ROUTE

    return redirect
  }

  if (to.name !== FORBIDDEN_ROUTE_NAME && to.meta.access) {
    const currentRole = normalizeAccessRole(loginUserStore.loginUser?.userRole)
    const hasAccess = canRoleAccessRole(currentRole, to.meta.access)

    if (!hasAccess) {
      return {
        name: FORBIDDEN_ROUTE_NAME,
        replace: true,
      }
    }
  }

  return true
})

router.afterEach((to) => {
  const pageTitle = to.meta.title ? `${to.meta.title} - Godlei AI 代码应用生成平台` : 'Godlei AI 代码应用生成平台'
  document.title = pageTitle
})

export default router
