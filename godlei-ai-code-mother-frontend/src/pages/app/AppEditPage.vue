<template>
  <div class="app-edit-page page-stack">
    <section v-if="pageReady" class="edit-panel glass-card">
      <PageSectionHeader
        eyebrow="App Detail"
        :title="isAdminMode ? '管理员编辑应用' : '编辑我的应用'"
        :description="
          isAdminMode
            ? '管理员可以修改应用名称、封面和优先级。'
            : '普通用户当前仅支持修改应用名称。'
        "
        title-tag="h1"
        class="panel-head"
      >
        <template #extra>
          <a-space wrap>
            <a-button @click="router.push(`/app/chat/${appId}`)">返回对话页</a-button>
            <a-button v-if="previewUrl" @click="handleOpenPreview">打开预览</a-button>
          </a-space>
        </template>
      </PageSectionHeader>

      <DetailStatsGrid :items="summaryItems" />

      <a-form layout="vertical" :model="formState" class="edit-form">
        <a-form-item label="应用名称">
          <a-input v-model:value="formState.appName" :maxlength="60" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item v-if="isAdminMode" label="应用封面">
          <a-input v-model:value="formState.cover" placeholder="请输入封面图片地址" />

          <div class="cover-preview-panel">
            <div class="cover-preview-head">
              <span>封面回显</span>
              <small>保存前可实时确认当前封面</small>
            </div>

            <div v-if="coverPreviewUrl" class="cover-preview-card">
              <img
                :src="coverPreviewUrl"
                :alt="formState.appName.trim() || '应用封面'"
                class="cover-preview-image"
              />
            </div>

            <div v-else class="cover-preview-empty">
              输入封面图片地址后，这里会实时显示当前封面。
            </div>
          </div>
        </a-form-item>

        <a-form-item v-if="isAdminMode" label="优先级">
          <a-input-number v-model:value="formState.priority" :min="0" :max="999" style="width: 220px" />
        </a-form-item>

        <div class="form-actions">
          <a-button @click="router.push(`/app/chat/${appId}`)">取消</a-button>
          <a-button type="primary" :loading="submitLoading" @click="handleSubmit">保存修改</a-button>
        </div>
      </a-form>
    </section>

    <a-skeleton v-else active :paragraph="{ rows: 8 }" class="glass-card page-skeleton" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import DetailStatsGrid from '@/components/common/DetailStatsGrid.vue'
import type { DetailStatItem } from '@/components/common/DetailStatsGrid.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { getAppVo, updateAppAdmin, updateMyApp } from '@/api/appController'
import { getUserVoById } from '@/api/userController'
import { getStaticPreviewUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  formatAppDateTime,
  resolveAppCreatorDisplayName,
  resolveAppEditorMode,
} from '@/utils/appHelpers'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { getTrimmedMediaUrl } from '@/utils/media'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { accessRole, loginUser } = storeToRefs(loginUserStore)

const pageReady = ref(false)
const submitLoading = ref(false)
const appDetail = ref<API.AppVO | API.App | null>(null)
const creatorDisplayName = ref('-')
const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const appId = computed(() => String(route.params.id ?? '').trim())
const safeAppId = computed<API.LongId>(() => appId.value)

const editorMode = computed(() =>
  resolveAppEditorMode(accessRole.value, loginUser.value?.id, appDetail.value?.userId),
)

const isAdminMode = computed(() => editorMode.value.mode === 'admin')

const previewUrl = computed(() => {
  if (!appDetail.value?.id || !appDetail.value.codeGenType) {
    return ''
  }
  return getStaticPreviewUrl(appDetail.value.codeGenType, appDetail.value.id)
})

const coverPreviewUrl = computed(() => getTrimmedMediaUrl(formState.cover))

const codeGenLabel = computed(() => formatCodeGenType(appDetail.value?.codeGenType))
const formattedUpdateTime = computed(() =>
  formatAppDateTime(appDetail.value?.updateTime || appDetail.value?.createTime),
)

const summaryItems = computed<DetailStatItem[]>(() => [
  { label: '应用编号', value: appDetail.value?.id },
  { label: '生成模式', value: codeGenLabel.value },
  { label: '创建者', value: creatorDisplayName.value },
  { label: '最近更新时间', value: formattedUpdateTime.value },
])

const syncFormState = () => {
  formState.appName = appDetail.value?.appName || ''
  formState.cover = appDetail.value?.cover || ''
  formState.priority = appDetail.value?.priority ?? 0
}

const syncCreatorDisplayName = async (detail: API.AppVO | API.App) => {
  const creatorUserName = (detail as API.AppVO & { userName?: string }).userName

  creatorDisplayName.value = resolveAppCreatorDisplayName(
    detail.userId,
    creatorUserName,
    loginUser.value,
  )

  if (creatorDisplayName.value !== '-' || !detail.userId) {
    return
  }

  try {
    const res = await getUserVoById({ id: detail.userId })
    if (res.data?.code !== 0 || !res.data.data) {
      return
    }

    creatorDisplayName.value =
      res.data.data.userName?.trim() || res.data.data.userAccount?.trim() || creatorDisplayName.value
  } catch {
    // Ignore creator fetch failures and keep fallback text.
  }
}

const loadAppDetail = async () => {
  if (!/^\d+$/.test(appId.value)) {
    message.error('无效的应用编号')
    await router.replace('/')
    return false
  }

  try {
    const res = await getAppVo({ id: safeAppId.value })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '应用详情加载失败')
      return false
    }

    appDetail.value = res.data.data

    if (!resolveAppEditorMode(accessRole.value, loginUser.value?.id, res.data.data.userId).canEdit) {
      await router.replace('/403')
      return false
    }

    await syncCreatorDisplayName(res.data.data)
    syncFormState()
    return true
  } catch {
    message.error('应用详情加载失败，请稍后重试')
    await router.replace('/')
    return false
  }
}

const handleSubmit = async () => {
  if (!formState.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }

  submitLoading.value = true
  try {
    if (isAdminMode.value) {
      const res = await updateAppAdmin({
        id: safeAppId.value,
        appName: formState.appName.trim(),
        cover: formState.cover.trim(),
        priority: formState.priority,
      })

      if (res.data?.code !== 0 || !res.data.data) {
        message.error(res.data?.message || '保存失败')
        return
      }
    } else {
      const res = await updateMyApp({
        id: safeAppId.value,
        appName: formState.appName.trim(),
      })

      if (res.data?.code !== 0 || !res.data.data) {
        message.error(res.data?.message || '保存失败')
        return
      }
    }

    message.success('应用信息已更新')
    await loadAppDetail()
  } catch {
    message.error('保存失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const handleOpenPreview = () => {
  if (!previewUrl.value) {
    return
  }
  window.open(previewUrl.value, '_blank', 'noopener,noreferrer')
}

onMounted(async () => {
  pageReady.value = await loadAppDetail()
})
</script>

<style scoped>
.edit-panel,
.page-skeleton {
  padding: 26px;
}

.summary-grid {
  margin-top: 22px;
}

.edit-form {
  margin-top: 26px;
}

.cover-preview-panel {
  margin-top: 14px;
  padding: 16px;
  background: linear-gradient(180deg, rgb(255 255 255 / 92%), rgb(248 250 252 / 88%));
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 18px;
}

.cover-preview-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  color: #0f172a;
  font-weight: 600;
}

.cover-preview-head small {
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}

.cover-preview-card {
  margin-top: 14px;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid rgb(148 163 184 / 16%);
  box-shadow: 0 18px 36px rgb(148 163 184 / 12%);
}

.cover-preview-image {
  display: block;
  width: 100%;
  max-height: 240px;
  object-fit: cover;
}

.cover-preview-empty {
  display: grid;
  place-items: center;
  min-height: 140px;
  margin-top: 14px;
  padding: 18px;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
  background: linear-gradient(180deg, rgb(248 250 252), rgb(241 245 249));
  border: 1px dashed rgb(148 163 184 / 28%);
  border-radius: 18px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .cover-preview-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-actions {
    flex-direction: column-reverse;
  }
}
</style>
