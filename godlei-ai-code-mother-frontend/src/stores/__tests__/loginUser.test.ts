import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
}))

vi.mock('@/api/userController', () => ({
  getLoginUser: vi.fn(),
  updateMyUser: vi.fn(),
  updateMyUserPassword: vi.fn(),
  updateUser: vi.fn(),
  userLogin: vi.fn(),
  userLogout: vi.fn(),
  userRegister: vi.fn(),
}))

import { message } from 'ant-design-vue'
import { getLoginUser, updateMyUser, updateMyUserPassword } from '@/api/userController'
import { useLoginUserStore } from '../loginUser'

describe('useLoginUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('persists profile changes through the current-user api and refreshes login user', async () => {
    const store = useLoginUserStore()
    store.setLoginUser(
      {
        id: 1,
        userRole: 'user',
        userName: '旧昵称',
        userAvatar: 'https://example.com/old.png',
        userProfile: '旧简介',
      } as API.LoginUserVO,
      false,
    )

    vi.mocked(updateMyUser).mockResolvedValue({
      data: { code: 0, data: true },
    } as never)
    vi.mocked(getLoginUser).mockResolvedValue({
      data: {
        code: 0,
        data: {
          id: 1,
          userRole: 'user',
          userName: '新昵称',
          userAvatar: 'https://example.com/new.png',
          userProfile: '新简介',
        },
      },
    } as never)

    const success = await store.saveProfile({
      userName: '新昵称',
      userAvatar: 'https://example.com/new.png',
      userProfile: '新简介',
    })

    expect(success).toBe(true)
    expect(updateMyUser).toHaveBeenCalledWith({
      userName: '新昵称',
      userAvatar: 'https://example.com/new.png',
      userProfile: '新简介',
    })
    expect(getLoginUser).toHaveBeenCalled()
    expect(store.loginUser?.userName).toBe('新昵称')
    expect(message.success).toHaveBeenCalled()
  })

  it('changes password through the current-user password api', async () => {
    const store = useLoginUserStore()
    store.setLoginUser(
      {
        id: 1,
        userRole: 'user',
      } as API.LoginUserVO,
      false,
    )

    vi.mocked(updateMyUserPassword).mockResolvedValue({
      data: { code: 0, data: true },
    } as never)

    const success = await store.changePassword({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      checkPassword: 'newPassword123',
    })

    expect(success).toBe(true)
    expect(updateMyUserPassword).toHaveBeenCalledWith({
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      checkPassword: 'newPassword123',
    })
    expect(message.success).toHaveBeenCalled()
  })
})
