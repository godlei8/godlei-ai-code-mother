<template>
  <div class="page-stack">
    <section class="profile-panel glass-card">
      <div class="profile-main">
        <div class="profile-avatar-wrap">
          <a-avatar :src="loginUser?.userAvatar" :size="104" class="profile-avatar">
            {{ displayName.slice(0, 1).toUpperCase() }}
          </a-avatar>
        </div>

        <div class="profile-copy">
          <div class="profile-headline">
            <div class="profile-identity">
              <h3>{{ displayName }}</h3>
              <p>{{ loginUser?.userAccount || 'guest' }}</p>
            </div>
            <a-tag :color="accessRole === 'admin' ? 'blue' : 'green'" class="profile-role-tag">
              {{ roleLabel }}
            </a-tag>
          </div>

          <p class="profile-description">
            {{ loginUser?.userProfile || '这个用户暂时还没有填写个人简介。' }}
          </p>
        </div>
      </div>

      <div class="profile-overview">
        <div v-for="item in overviewItems" :key="item.label" class="overview-item">
          <span class="overview-label">{{ item.label }}</span>
          <strong class="overview-value">{{ item.value }}</strong>
        </div>
      </div>

      <div class="profile-actions">
        <a-space wrap size="middle">
          <a-button type="primary" @click="openEditModal">编辑资料</a-button>
          <a-button @click="openPasswordModal">更新密码</a-button>
        </a-space>
      </div>

      <a-alert type="success" show-icon :message="profileModeMessage" />
    </section>

    <section class="profile-details glass-card">
      <PageSectionHeader
        title="资料详情"
        description="这里展示的是当前登录用户的实时资料，保存后会从后端重新同步最新信息。"
        title-tag="h3"
      />

      <DetailStatsGrid :items="detailItems" class="details-grid" />
    </section>

    <UserFormModal
      :open="editModalOpen"
      title="编辑个人资料"
      confirm-text="保存资料"
      mode="profile"
      :show-role="false"
      :show-account="false"
      profile-mode="persisted"
      :loading="actionLoading"
      :initial-values="profileInitialValues"
      @cancel="editModalOpen = false"
      @submit="handleSubmit"
    />

    <UserPasswordModal
      :open="passwordModalOpen"
      :loading="actionLoading"
      @cancel="passwordModalOpen = false"
      @submit="handlePasswordSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ACCESS_ROLE_LABEL } from '@/access/accessConstants'
import UserFormModal from '@/components/UserFormModal.vue'
import UserPasswordModal from '@/components/UserPasswordModal.vue'
import DetailStatsGrid from '@/components/common/DetailStatsGrid.vue'
import type { DetailStatItem } from '@/components/common/DetailStatsGrid.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatAppDateTime } from '@/utils/appHelpers'

const loginUserStore = useLoginUserStore()
const { loginUser, displayName, accessRole, actionLoading } = storeToRefs(loginUserStore)

const editModalOpen = ref(false)
const passwordModalOpen = ref(false)

const roleLabel = computed(() => ACCESS_ROLE_LABEL[accessRole.value])

const profileInitialValues = computed(() => ({
  userName: loginUser.value?.userName,
  userAvatar: loginUser.value?.userAvatar,
  userProfile: loginUser.value?.userProfile,
}))

const detailItems = computed<DetailStatItem[]>(() => [
  { label: '用户昵称', value: loginUser.value?.userName },
  { label: '账号标识', value: loginUser.value?.userAccount },
  { label: '创建时间', value: formatAppDateTime(loginUser.value?.createTime) },
  { label: '更新时间', value: formatAppDateTime(loginUser.value?.updateTime) },
])

const overviewItems = computed(() => [
  { label: '当前身份', value: roleLabel.value },
  { label: '账号', value: loginUser.value?.userAccount || 'guest' },
  { label: '资料状态', value: '实时同步' },
  { label: '安全操作', value: '可更新密码' },
])

const profileModeMessage = computed(() => {
  return '个人资料现在会直接保存到后端，并在保存后同步刷新当前登录信息。'
})

const openEditModal = () => {
  editModalOpen.value = true
}

const openPasswordModal = () => {
  passwordModalOpen.value = true
}

const handleSubmit = async (payload: { userName?: string; userAvatar?: string; userProfile?: string }) => {
  const success = await loginUserStore.saveProfile(payload)

  if (success) {
    editModalOpen.value = false
  }
}

const handlePasswordSubmit = async (payload: API.UserPasswordUpdateRequest) => {
  const success = await loginUserStore.changePassword(payload)

  if (success) {
    passwordModalOpen.value = false
  }
}
</script>

<style scoped>
.profile-panel,
.profile-details {
  padding: 26px;
}

.profile-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-main {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.profile-avatar-wrap {
  flex: 0 0 auto;
}

.profile-avatar {
  box-shadow: 0 18px 40px rgb(148 163 184 / 20%);
}

.profile-copy {
  flex: 1;
  min-width: 0;
}

.profile-headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.profile-identity h3 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
  line-height: 1.08;
}

.profile-identity p {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 18px;
  line-height: 1.5;
}

.profile-role-tag {
  margin-top: 4px;
}

.profile-description {
  margin: 20px 0 0;
  color: #334155;
  font-size: 15px;
  line-height: 1.85;
}

.profile-overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 24px;
}

.overview-item {
  padding: 14px 16px;
  background: linear-gradient(180deg, rgb(255 255 255 / 88%), rgb(248 250 252 / 84%));
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 18px;
  box-shadow: 0 14px 32px rgb(148 163 184 / 10%);
}

.overview-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.04em;
}

.overview-value {
  display: block;
  margin-top: 8px;
  color: #0f172a;
  font-size: 16px;
  line-height: 1.4;
}

.profile-actions {
  margin-top: 24px;
}

.details-grid {
  margin-top: 18px;
}

@media (max-width: 1180px) {
  .profile-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .profile-main {
    flex-direction: column;
  }

  .profile-headline {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 720px) {
  .profile-panel,
  .profile-details {
    padding: 20px;
  }

  .profile-identity h3 {
    font-size: 26px;
  }

  .profile-identity p {
    font-size: 16px;
  }

  .profile-overview {
    grid-template-columns: 1fr;
  }
}
</style>
