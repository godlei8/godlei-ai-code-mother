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
            <a-button type="primary" @click="openEditModal">
              编辑资料
            </a-button>
          </div>
        </div>
      </div>

      <a-alert
        :type="accessRole === 'admin' ? 'success' : 'warning'"
        show-icon
        :message="profileModeMessage"
      />
    </section>

    <section class="profile-details glass-card">
      <div class="section-heading">
        <h3>资料详情</h3>
        <p>当前登录态信息来自全局 Pinia store，普通用户编辑会走兼容草稿分支。</p>
      </div>

      <div class="details-grid">
        <article class="detail-card">
          <span>用户昵称</span>
          <strong>{{ loginUser?.userName || '-' }}</strong>
        </article>
        <article class="detail-card">
          <span>账号标识</span>
          <strong>{{ loginUser?.userAccount || '-' }}</strong>
        </article>
        <article class="detail-card">
          <span>创建时间</span>
          <strong>{{ loginUser?.createTime || '-' }}</strong>
        </article>
        <article class="detail-card">
          <span>更新时间</span>
          <strong>{{ loginUser?.updateTime || '-' }}</strong>
        </article>
      </div>
    </section>

    <UserFormModal
      :open="editModalOpen"
      title="编辑个人资料"
      confirm-text="保存资料"
      mode="profile"
      :show-role="false"
      :show-account="false"
      :profile-mode="accessRole === 'admin' ? 'persisted' : 'placeholder'"
      :loading="actionLoading"
      :initial-values="profileInitialValues"
      @cancel="editModalOpen = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { ACCESS_ROLE_LABEL } from '@/access/accessConstants'
import UserFormModal from '@/components/UserFormModal.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()
const { loginUser, displayName, accessRole, actionLoading } = storeToRefs(loginUserStore)

const editModalOpen = ref(false)

const roleLabel = computed(() => ACCESS_ROLE_LABEL[accessRole.value])

const profileInitialValues = computed(() => ({
  userName: loginUser.value?.userName,
  userAvatar: loginUser.value?.userAvatar,
  userProfile: loginUser.value?.userProfile,
}))

const profileModeMessage = computed(() => {
  return accessRole.value === 'admin'
    ? '当前登录用户为管理员，编辑资料会直接调用现有后端更新接口。'
    : '当前登录用户为普通用户，编辑资料会先保存为前端兼容草稿，等待后端开放个人资料更新接口。'
})

const openEditModal = () => {
  editModalOpen.value = true
}

const handleSubmit = async (payload: { userName?: string; userAvatar?: string; userProfile?: string }) => {
  const success = await loginUserStore.saveProfile(payload)

  if (success) {
    editModalOpen.value = false
  }
}
</script>

<style scoped>
.profile-panel,
.profile-details {
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

.profile-headline h3,
.section-heading h3 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
}

.profile-headline p,
.section-heading p {
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
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.detail-card {
  padding: 18px;
  background: rgb(255 255 255 / 70%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 18px;
}

.detail-card span {
  display: block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.detail-card strong {
  color: #0f172a;
  line-height: 1.6;
}

@media (max-width: 980px) {
  .profile-main {
    flex-direction: column;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }
}
</style>
