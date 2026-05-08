<template>
  <AuthCardShell
    eyebrow="Create Account"
    eyebrow-tone="success"
    title="注册账号"
    description="账号注册成功后会回到登录页，继续进入完整的前后端管理流程。"
    footer-text="已经有账号？"
    footer-link-text="去登录"
    footer-link-to="/auth/login"
  >
    <a-form layout="vertical" :model="formState" @finish="handleSubmit">
      <a-form-item
        label="用户账号"
        name="userAccount"
        :rules="[
          { required: true, message: '请输入用户账号' },
          { min: 4, message: '账号长度不能少于 4 位' },
        ]"
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
        :rules="[
          { required: true, message: '请输入登录密码' },
          { min: 8, message: '密码长度不能少于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          size="large"
          placeholder="请输入登录密码"
          autocomplete="new-password"
        />
      </a-form-item>

      <a-form-item
        label="确认密码"
        name="checkPassword"
        :rules="[
          { required: true, message: '请再次输入密码' },
          { validator: validateConfirmPassword },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          size="large"
          placeholder="请再次输入密码"
          autocomplete="new-password"
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
          完成注册
        </a-button>
      </a-form-item>
    </a-form>
  </AuthCardShell>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import AuthCardShell from '@/components/auth/AuthCardShell.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { actionLoading } = storeToRefs(loginUserStore)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateConfirmPassword = async () => {
  if (formState.userPassword !== formState.checkPassword) {
    return Promise.reject('两次输入的密码不一致')
  }

  return Promise.resolve()
}

const handleSubmit = async () => {
  const registerSuccess = await loginUserStore.register(formState)

  if (!registerSuccess) {
    return
  }

  void router.push({
    path: '/auth/login',
    query: {
      account: formState.userAccount,
    },
  })
}
</script>

<style scoped>
.submit-item {
  margin-bottom: 0;
}
</style>
