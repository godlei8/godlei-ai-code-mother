<template>
  <div class="page-stack">
    <section class="profile-panel glass-card">
      <div class="profile-main">
        <a-avatar :src="loginUser?.userAvatar" :size="88">
          {{ displayName.slice(0, 1).toUpperCase() }}
        </a-avatar>

        <div class="profile-copy">
          <div class="profile-headline">
            <div>
              <h3>{{ displayName }}</h3>
              <p>{{ loginUser?.userAccount || 'guest' }}</p>
            </div>
            <a-tag :color="accessRole === 'admin' ? 'blue' : 'green'">
              {{ roleLabel }}
            </a-tag>
          </div>

          <p class="profile-description">
            {{ loginUser?.userProfile || '这个用户暂时还没有填写个人简介。' }}
          </p>

          <div class="profile-actions">
            <a-space wrap>
              <a-button type="primary" @click="openEditModal">编辑资料</a-button>
              <a-button @click="openPasswordModal">修改密码</a-button>
            </a-space>
          </div>
        </div>
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

    <section class="profile-security glass-card">
      <PageSectionHeader
        title="账号安全"
        description="你可以在这里修改当前登录密码。修改成功后，本次登录状态会保持不变。"
        title-tag="h3"
      />

      <div class="security-card">
        <div>
          <h4>登录密码</h4>
          <p>建议定期更换密码，并避免与其他平台重复使用。</p>
        </div>

        <a-button @click="openPasswordModal">更新密码</a-button>
      </div>
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
.profile-details,
.profile-security {
  padding: 26px;
}

.profile-main {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
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

.profile-headline h3 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.profile-headline p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.75;
}

.profile-description {
  margin: 18px 0 0;
  color: #334155;
  font-size: 15px;
  line-height: 1.8;
}

.profile-actions {
  margin-top: 20px;
}

.details-grid {
  margin-top: 18px;
}

.security-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
  padding: 20px 22px;
  background: rgb(255 255 255 / 76%);
  border: 1px solid rgb(148 163 184 / 15%);
  border-radius: 20px;
  box-shadow: var(--card-shadow-soft);
}

.security-card h4 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.security-card p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.75;
}

@media (max-width: 980px) {
  .profile-main {
    flex-direction: column;
  }

  .security-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
