import { storeToRefs } from 'pinia'
import { computed } from 'vue'
import { canRoleAccessAction, canRoleAccessRole, canRoleAccessRoute } from '@/access/accessConfig'
import { ACCESS_ROLE, normalizeAccessRole, type AccessAction, type AccessRole } from '@/access/accessConstants'
import { useLoginUserStore } from '@/stores/loginUser'

export const useAccess = () => {
  const loginUserStore = useLoginUserStore()
  const { loginUser } = storeToRefs(loginUserStore)

  const accessRole = computed<AccessRole>(() => {
    return normalizeAccessRole(loginUser.value?.userRole)
  })

  const isLogin = computed(() => loginUserStore.isLogin)
  const isAdmin = computed(() => accessRole.value === ACCESS_ROLE.ADMIN)

  const canAccessRoute = (routeKey: string) => {
    return canRoleAccessRoute(accessRole.value, routeKey)
  }

  const canAccessAction = (action: AccessAction) => {
    return canRoleAccessAction(accessRole.value, action)
  }

  const canAccessRole = (role?: AccessRole) => {
    return canRoleAccessRole(accessRole.value, role)
  }

  return {
    accessRole,
    isLogin,
    isAdmin,
    canAccessRoute,
    canAccessAction,
    canAccessRole,
  }
}
