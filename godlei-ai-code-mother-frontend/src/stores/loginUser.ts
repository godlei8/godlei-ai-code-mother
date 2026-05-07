import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ACCESS_ROLE, normalizeAccessRole } from '@/access/accessConstants'
import {
  getLoginUser,
  updateUser,
  userLogin,
  userLogout,
  userRegister,
} from '@/api/userController'

const PROFILE_DRAFT_STORAGE_KEY = 'godlei_profile_draft'

const getProfileDraftKey = (userId?: number) => {
  return userId ? `${PROFILE_DRAFT_STORAGE_KEY}_${userId}` : ''
}

const readProfileDraft = (userId?: number): Partial<API.LoginUserVO> => {
  const storageKey = getProfileDraftKey(userId)

  if (!storageKey) {
    return {}
  }

  try {
    const cacheValue = window.localStorage.getItem(storageKey)
    return cacheValue ? (JSON.parse(cacheValue) as Partial<API.LoginUserVO>) : {}
  } catch {
    return {}
  }
}

const writeProfileDraft = (userId: number | undefined, payload: Partial<API.LoginUserVO>) => {
  const storageKey = getProfileDraftKey(userId)

  if (!storageKey) {
    return
  }

  window.localStorage.setItem(storageKey, JSON.stringify(payload))
}

const clearProfileDraft = (userId?: number) => {
  const storageKey = getProfileDraftKey(userId)

  if (!storageKey) {
    return
  }

  window.localStorage.removeItem(storageKey)
}

const mergeLoginUserWithDraft = (loginUser?: API.LoginUserVO | null): API.LoginUserVO | null => {
  if (!loginUser?.id) {
    return loginUser ?? null
  }

  if (normalizeAccessRole(loginUser.userRole) !== ACCESS_ROLE.USER) {
    return loginUser
  }

  return {
    ...loginUser,
    ...readProfileDraft(loginUser.id),
  }
}

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO | null>(null)
  const hasBootstrap = ref(false)
  const bootstrapLoading = ref(false)
  const actionLoading = ref(false)

  const isLogin = computed(() => Boolean(loginUser.value?.id))
  const accessRole = computed(() => normalizeAccessRole(loginUser.value?.userRole))
  const displayName = computed(() => {
    return loginUser.value?.userName || loginUser.value?.userAccount || '未登录'
  })

  const setLoginUser = (newLoginUser: API.LoginUserVO | null, useDraft = true) => {
    loginUser.value = useDraft ? mergeLoginUserWithDraft(newLoginUser) : newLoginUser
  }

  const clearLoginUser = () => {
    loginUser.value = null
    hasBootstrap.value = true
  }

  const fetchLoginUser = async (force = false) => {
    if (bootstrapLoading.value) {
      return loginUser.value
    }

    if (hasBootstrap.value && !force) {
      return loginUser.value
    }

    bootstrapLoading.value = true

    try {
      const res = await getLoginUser()
      const nextLoginUser = res.data?.code === 0 ? (res.data.data ?? null) : null
      setLoginUser(nextLoginUser)
      return loginUser.value
    } catch {
      loginUser.value = null
      return null
    } finally {
      hasBootstrap.value = true
      bootstrapLoading.value = false
    }
  }

  const login = async (payload: API.UserLoginRequest) => {
    actionLoading.value = true

    try {
      const res = await userLogin(payload)
      if (res.data?.code !== 0 || !res.data.data) {
        message.error(res.data?.message || '登录失败，请检查账号和密码')
        return false
      }

      clearProfileDraft(res.data.data.id)
      setLoginUser(res.data.data)
      hasBootstrap.value = true
      message.success(`欢迎回来，${res.data.data.userName || res.data.data.userAccount || '用户'}`)
      return true
    } catch {
      message.error('登录失败，请稍后重试')
      return false
    } finally {
      actionLoading.value = false
    }
  }

  const register = async (payload: API.UserRegisterRequest) => {
    actionLoading.value = true

    try {
      const res = await userRegister(payload)
      if (res.data?.code !== 0) {
        message.error(res.data?.message || '注册失败，请稍后重试')
        return false
      }

      message.success('注册成功，请登录后继续')
      return true
    } catch {
      message.error('注册失败，请稍后重试')
      return false
    } finally {
      actionLoading.value = false
    }
  }

  const logout = async () => {
    actionLoading.value = true

    try {
      await userLogout()
      message.success('已退出登录')
    } catch {
      message.warning('退出请求未完成，已为你清理本地登录态')
    } finally {
      clearLoginUser()
      actionLoading.value = false
    }
  }

  const saveProfile = async (payload: Pick<API.UserUpdateRequest, 'userName' | 'userAvatar' | 'userProfile'>) => {
    if (!loginUser.value?.id) {
      message.warning('请先登录后再操作')
      return false
    }

    actionLoading.value = true

    try {
      if (accessRole.value === ACCESS_ROLE.ADMIN) {
        const res = await updateUser({
          id: loginUser.value.id,
          ...payload,
        })

        if (res.data?.code !== 0 || !res.data.data) {
          message.error(res.data?.message || '资料更新失败')
          return false
        }

        clearProfileDraft(loginUser.value.id)
        setLoginUser(
          {
            ...loginUser.value,
            ...payload,
          },
          false,
        )
        message.success('资料已更新')
        return true
      }

      writeProfileDraft(loginUser.value.id, payload)
      setLoginUser(
        {
          ...loginUser.value,
          ...payload,
        },
        false,
      )
      message.success('资料已在前端兼容模式下保存，待后端接口开放后可直接切换')
      return true
    } catch {
      message.error('资料更新失败，请稍后重试')
      return false
    } finally {
      actionLoading.value = false
    }
  }

  return {
    loginUser,
    hasBootstrap,
    bootstrapLoading,
    actionLoading,
    isLogin,
    accessRole,
    displayName,
    fetchLoginUser,
    setLoginUser,
    clearLoginUser,
    login,
    register,
    logout,
    saveProfile,
  }
})
