<template>
  <div class="global-header">
    <div class="brand">
      <img :src="logoSrc" :alt="title" class="brand-logo" />
      <span class="brand-title">{{ title }}</span>
    </div>

    <a-menu
      mode="horizontal"
      :selected-keys="selectedKeys"
      class="header-menu"
      @click="handleMenuClick"
    >
      <a-menu-item v-for="item in menuItems" :key="item.key">
        {{ item.label }}
      </a-menu-item>
    </a-menu>

    <div class="user-entry">
      <a-button type="primary">登录</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { MenuProps } from 'ant-design-vue'

export interface HeaderMenuItem {
  key: string
  label: string
}

interface Props {
  title: string
  logoSrc: string
  menuItems: HeaderMenuItem[]
  selectedKeys: string[]
}

defineProps<Props>()

const emit = defineEmits<{
  navigate: [key: string]
}>()

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  emit('navigate', String(key))
}
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
  min-height: 72px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgb(15 23 42 / 18%);
  flex-shrink: 0;
}

.brand-title {
  color: #172554;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  min-width: 0;
  background: transparent;
  border-bottom: none;
}

.user-entry {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
}

:deep(.header-menu.ant-menu-horizontal) {
  line-height: 72px;
  justify-content: flex-start;
}

:deep(.header-menu.ant-menu-horizontal::after) {
  display: none;
}

@media (max-width: 960px) {
  .global-header {
    flex-wrap: wrap;
    gap: 16px;
    padding: 12px 0;
  }

  .header-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }

  :deep(.header-menu.ant-menu-horizontal) {
    justify-content: flex-start;
  }

  .user-entry {
    margin-left: auto;
  }
}

@media (max-width: 640px) {
  .brand-title {
    font-size: 18px;
  }

  .brand-logo {
    width: 36px;
    height: 36px;
  }
}
</style>
