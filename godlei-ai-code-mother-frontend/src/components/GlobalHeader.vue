<template>
  <div class="global-header">
    <div class="header-brand" @click="handleNavigate('/')">
      <div class="brand-badge">GA</div>
      <div class="brand-copy">
        <strong>Godlei AI</strong>
        <span>Frontend Console</span>
      </div>
    </div>

    <a-menu
      mode="horizontal"
      :selected-keys="selectedKeys"
      class="header-menu"
      @click="handleMenuClick"
    >
      <a-menu-item
        v-for="item in visibleItems"
        :key="item.key"
      >
        {{ item.label }}
      </a-menu-item>
    </a-menu>

    <div v-if="isGuest" class="header-actions">
      <a-button @click="handleNavigate('/auth/register')">
        注册
      </a-button>
      <a-button type="primary" @click="handleNavigate('/auth/login')">
        登录
      </a-button>
    </div>

    <div v-else class="header-actions">
      <a-tag class="role-tag" :color="roleColor">{{ roleLabel }}</a-tag>

      <a-dropdown placement="bottomRight">
        <a class="user-entry" @click.prevent>
          <a-avatar :src="loginUser?.userAvatar" :size="34">
            {{ avatarText }}
          </a-avatar>
          <div class="user-copy">
            <strong>{{ displayName }}</strong>
            <span>{{ loginUser?.userAccount || 'guest' }}</span>
          </div>
        </a>

        <template #overlay>
          <a-menu>
            <a-menu-item key="profile" @click="handleProfileClick">
              个人中心
            </a-menu-item>
            <a-menu-item key="logout" @click="handleLogout">
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import type { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { ACCESS_ROLE, ACCESS_ROLE_LABEL } from '@/access/accessConstants'
import { useAccess } from '@/access/useAccess'
import { appMenuRoutes } from '@/router/routes'
import { useLoginUserStore } from '@/stores/loginUser'

defineProps<{
  selectedKeys: string[]
}>()

const emit = defineEmits<{
  navigate: [path: string]
}>()

const router = useRouter()
const access = useAccess()
const loginUserStore = useLoginUserStore()
const { loginUser, displayName, accessRole } = storeToRefs(loginUserStore)

const isGuest = computed(() => accessRole.value === ACCESS_ROLE.GUEST)

const visibleItems = computed(() => {
  return appMenuRoutes.filter((item) => access.canAccessRole(item.access))
})

const avatarText = computed(() => {
  return displayName.value.slice(0, 1).toUpperCase()
})

const roleColor = computed(() => {
  return accessRole.value === ACCESS_ROLE.ADMIN ? 'blue' : 'green'
})

const roleLabel = computed(() => {
  return ACCESS_ROLE_LABEL[accessRole.value]
})

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  emit('navigate', String(key))
}

const handleNavigate = (path: string) => {
  emit('navigate', path)
}

const handleProfileClick = () => {
  void router.push('/user/profile')
}

const handleLogout = async () => {
  await loginUserStore.logout()
  void router.push('/auth/login')
}
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  min-height: 64px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-inline-start: 4px;
  padding-block: 2px;
  flex-shrink: 0;
  cursor: pointer;
}

.brand-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  color: #eff6ff;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
  background: linear-gradient(135deg, #155eef 0%, #0f766e 100%);
  border-radius: 12px;
  box-shadow: 0 12px 22px rgb(15 23 42 / 14%);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 36px;
  gap: 3px;
}

.brand-copy strong {
  color: #0f172a;
  font-size: 18px;
  line-height: 1.05;
}

.brand-copy span {
  color: #64748b;
  font-size: 12px;
  line-height: 1.15;
}

.header-menu {
  flex: 1;
  min-width: 0;
  background: transparent;
  border-bottom: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 2px 4px 6px;
  color: inherit;
  text-decoration: none;
}

.user-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-copy strong {
  color: #0f172a;
  font-size: 13px;
  line-height: 1;
}

.user-copy span {
  color: #64748b;
  font-size: 11px;
  line-height: 1;
}

:deep(.header-menu.ant-menu-horizontal) {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 64px;
  min-height: 64px;
}

:deep(.header-menu.ant-menu-horizontal::after) {
  display: none;
}

:deep(.header-menu .ant-menu-item) {
  height: 38px;
  line-height: 38px;
  margin-top: 0;
  margin-bottom: 0;
  padding-inline: 14px;
  border-radius: 11px;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
}

:deep(.header-menu .ant-menu-item::after) {
  display: none !important;
}

:deep(.header-menu .ant-menu-item-selected) {
  color: #1244c2;
  background: linear-gradient(135deg, rgb(239 246 255 / 96%), rgb(236 253 245 / 88%));
}

.role-tag {
  margin-inline-end: 0;
  padding-inline: 10px;
  border-radius: 999px;
  font-weight: 600;
}

@media (max-width: 980px) {
  .global-header {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .header-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }
}

@media (max-width: 640px) {
  .header-brand {
    padding-inline-start: 2px;
  }

  .brand-copy strong {
    font-size: 16px;
  }

  .user-copy {
    display: none;
  }
}
</style>
