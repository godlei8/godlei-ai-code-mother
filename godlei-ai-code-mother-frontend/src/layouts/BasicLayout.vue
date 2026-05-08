<template>
  <a-layout class="basic-layout">
    <a-layout-header class="layout-header">
      <div
        class="header-shell glass-card"
        :class="{ 'is-floating': isHeaderFloating }"
      >
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
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'

const route = useRoute()
const router = useRouter()
const isHeaderFloating = ref(false)

const selectedKeys = computed(() => {
  if (route.path.startsWith('/app/manage')) {
    return ['/app/manage']
  }
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

const syncHeaderFloating = () => {
  isHeaderFloating.value = window.scrollY > 18
}

onMounted(() => {
  syncHeaderFloating()
  window.addEventListener('scroll', syncHeaderFloating, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', syncHeaderFloating)
})
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: transparent;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  justify-content: center;
  isolation: isolate;
  height: auto;
  padding: 8px 14px 10px;
  background: transparent;
}

.layout-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgb(248 251 255 / 94%) 0%, rgb(248 251 255 / 86%) 62%, rgb(248 251 255 / 0%) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  pointer-events: none;
  z-index: -1;
}

.header-shell {
  position: relative;
  z-index: 1;
  width: min(1800px, 100%);
  padding: 0 16px;
  background: rgb(255 255 255 / 74%);
  border-color: rgb(148 163 184 / 13%);
  border-radius: 20px;
  box-shadow: var(--card-shadow-floating);
  transition:
    transform 0.22s ease,
    background-color 0.22s ease,
    border-color 0.22s ease,
    box-shadow 0.22s ease;
}

.header-shell.is-floating {
  background: rgb(255 255 255 / 62%);
  border-color: rgb(148 163 184 / 11%);
  box-shadow:
    0 10px 24px rgb(15 23 42 / 5%),
    0 1px 0 rgb(255 255 255 / 56%) inset;
  transform: translateY(-1px);
}

.layout-content {
  padding: 8px 14px 48px;
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
    padding: 8px 12px 36px;
  }

  .layout-header {
    padding: 8px 12px 8px;
  }

  .header-shell {
    padding: 0 12px;
  }

  .layout-footer {
    padding: 10px 12px 24px;
  }
}
</style>
