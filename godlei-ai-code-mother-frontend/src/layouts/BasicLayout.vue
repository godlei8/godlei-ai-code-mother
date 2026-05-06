<template>
  <a-layout class="basic-layout">
    <a-layout-header class="layout-header">
      <GlobalHeader
        title="Godlei AI 代码应用生成平台"
        logo-src="/logo.png"
        :menu-items="menuItems"
        :selected-keys="selectedKeys"
        @navigate="handleNavigate"
      />
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
import GlobalHeader, { type HeaderMenuItem } from '@/components/GlobalHeader.vue'

const route = useRoute()
const router = useRouter()

const menuItems = computed<HeaderMenuItem[]>(() => [
  { key: '/', label: '首页' },
])

const selectedKeys = computed(() => {
  const exactMatch = menuItems.value.find((item) => item.key === route.path)

  return exactMatch ? [exactMatch.key] : []
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
  background:
    radial-gradient(circle at top, rgb(191 219 254 / 40%), transparent 35%),
    linear-gradient(180deg, #f8fbff 0%, #eef4ff 100%);
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: auto;
  padding-inline: clamp(20px, 6vw, 72px);
  background: rgb(255 255 255 / 88%);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgb(148 163 184 / 18%);
}

.layout-content {
  display: flex;
  justify-content: center;
  padding: 32px 20px 48px;
}

.content-shell {
  width: min(1200px, 100%);
}

.layout-footer {
  margin-top: auto;
  padding: 18px 20px 24px;
  background: transparent;
}

@media (max-width: 768px) {
  .layout-header {
    padding-inline: 20px;
  }

  .layout-content {
    padding: 24px 16px 40px;
  }
}
</style>
