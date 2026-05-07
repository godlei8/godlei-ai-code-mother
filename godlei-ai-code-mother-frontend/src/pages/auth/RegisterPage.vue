<template>
  <div class="auth-card glass-card">
    <div class="auth-card-header">
      <p class="card-tip">Create Account</p>
      <h2>注册账号</h2>
      <p class="card-description">账号注册成功后会回到登录页，继续进入完整的前端后台管理流程。</p>
    </div>

    <a-form
      layout="vertical"
      :model="formState"
      @finish="handleSubmit"
    >
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

    <div class="auth-card-footer">
      <span>已经有账号？</span>
      <RouterLink to="/auth/login">去登录</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { storeToRefs } from 'pinia'
import { RouterLink, useRouter } from 'vue-router'
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
.auth-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  min-height: 100%;
  padding: clamp(30px, 5vw, 46px);
}

.auth-card-header {
  margin-bottom: 28px;
}

.card-tip {
  margin: 0 0 10px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(28px, 4vw, 38px);
}

.card-description {
  margin: 14px 0 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.75;
}

.submit-item {
  margin-bottom: 0;
}

.auth-card-footer {
  display: flex;
  gap: 8px;
  margin-top: 24px;
  color: #64748b;
}
</style>
