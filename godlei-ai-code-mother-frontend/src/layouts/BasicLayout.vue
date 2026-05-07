<template>
  <a-layout class="basic-layout">
    <a-layout-header class="layout-header">
      <div class="header-shell glass-card">
        <GlobalHeader :selected-keys="selectedKeys" @navigate="handleNavigate" />
      </div>
    </a-layout-header>

    <a-layout-content class="layout-content">
      <div class="content-shell">
        <RouterView />
      </div>
    </a-layout-content>

    <a-layout-footer class="layout-footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'

const route = useRoute()
const router = useRouter()

const selectedKeys = computed(() => {
  if (route.path.startsWith('/user/manage')) {
    return ['/user/manage']
  }
  if (route.path.startsWith('/user/profile')) {
    return ['/user/profile']
  }
  if (route.path.startsWith('/access/manage')) {
    return ['/access/manage']
  }
  return ['/']
})

const handleNavigate = (path: string) => {
  if (path !== route.path) {
    void router.push(path)
  }
}
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: transparent;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  justify-content: center;
  height: auto;
  padding: 10px 14px 0;
  background: transparent;
}

.header-shell {
  width: min(1800px, 100%);
  padding: 0 16px;
  border-radius: 20px;
  box-shadow: 0 12px 30px rgb(15 23 42 / 8%);
}

.layout-content {
  padding: 18px 14px 48px;
}

.content-shell {
  width: min(1800px, 100%);
  margin: 0 auto;
}

.layout-footer {
  margin-top: auto;
  padding: 16px 14px 28px;
  background: transparent;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 18px 12px 36px;
  }

  .layout-header {
    padding: 10px 12px 0;
  }

  .header-shell {
    padding: 0 12px;
  }

  .layout-footer {
    padding: 10px 12px 24px;
  }
}
</style>
