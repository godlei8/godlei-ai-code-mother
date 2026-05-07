<template>
  <div class="forbidden-page glass-card">
    <p class="eyebrow">403 Forbidden</p>
    <h2>当前账号没有访问这个页面的权限</h2>
    <p class="description">
      你可以返回工作台，或切换为具备管理员权限的账号后再继续操作。
    </p>
    <a-space>
      <a-button type="primary" @click="router.push('/dashboard')">
        返回工作台
      </a-button>
      <a-button @click="router.push('/user/profile')">
        查看个人中心
      </a-button>
    </a-space>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.forbidden-page {
  padding: 34px;
}

.eyebrow {
  margin: 0 0 10px;
  color: #dc2626;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(28px, 4vw, 40px);
}

.description {
  max-width: 640px;
  margin: 16px 0 28px;
  color: #64748b;
  line-height: 1.8;
}
</style>
