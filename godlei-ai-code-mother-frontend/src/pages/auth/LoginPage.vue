<template>
  <AuthCardShell
    eyebrow="Welcome Back"
    title="登录系统"
    description="登录后即可进入首页、个人中心，以及管理员专属的用户与权限管理页面。"
    footer-text="还没有账号？"
    footer-link-text="去注册"
    footer-link-to="/auth/register"
  >
    <a-form layout="vertical" :model="formState" @finish="handleSubmit">
      <a-form-item
        label="用户账号"
        name="userAccount"
        :rules="[{ required: true, message: '请输入用户账号' }]"
      >
        <a-input
          v-model:value="formState.userAccount"
          size="large"
          placeholder="请输入用户账号"
          autocomplete="username"
        />
      </a-form-item>

      <a-form-item
        label="登录密码"
        name="userPassword"
        :rules="[{ required: true, message: '请输入登录密码' }]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          size="large"
          placeholder="请输入登录密码"
          autocomplete="current-password"
        />
      </a-form-item>

      <a-form-item class="submit-item">
        <a-button
          block
          type="primary"
          size="large"
          html-type="submit"
          :loading="actionLoading"
        >
          立即登录
        </a-button>
      </a-form-item>
    </a-form>
  </AuthCardShell>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import AuthCardShell from '@/components/auth/AuthCardShell.vue'
import { DEFAULT_HOME_ROUTE } from '@/router/routes'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { actionLoading } = storeToRefs(loginUserStore)

const formState = reactive<API.UserLoginRequest>({
  userAccount: typeof route.query.account === 'string' ? route.query.account : '',
  userPassword: '',
})

const handleSubmit = async () => {
  const loginSuccess = await loginUserStore.login(formState)

  if (!loginSuccess) {
    return
  }

  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : DEFAULT_HOME_ROUTE
  void router.push(redirect)
}
</script>

<style scoped>
.submit-item {
  margin-bottom: 0;
}
</style>
