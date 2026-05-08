<template>
  <div class="app-edit-page page-stack">
    <section v-if="pageReady" class="edit-panel glass-card">
      <div class="panel-head">
        <div>
          <p class="panel-label">App Detail</p>
          <h1>{{ isAdminMode ? '管理员编辑应用' : '编辑我的应用' }}</h1>
          <span>{{ isAdminMode ? '管理员可以修改名称、封面和优先级。' : '普通用户当前仅支持修改应用名称。' }}</span>
        </div>

        <a-space wrap>
          <a-button @click="router.push(`/app/chat/${appId}`)">返回对话页</a-button>
          <a-button v-if="previewUrl" @click="handleOpenPreview">打开预览</a-button>
        </a-space>
      </div>

      <div class="summary-grid">
        <article class="summary-card">
          <span>应用编号</span>
          <strong>{{ appDetail?.id || '-' }}</strong>
        </article>
        <article class="summary-card">
          <span>生成模式</span>
          <strong>{{ codeGenLabel }}</strong>
        </article>
        <article class="summary-card">
          <span>创建者</span>
          <strong>{{ appDetail?.userId || '-' }}</strong>
        </article>
        <article class="summary-card">
          <span>最近更新</span>
          <strong>{{ appDetail?.updateTime || '-' }}</strong>
        </article>
      </div>

      <a-form layout="vertical" :model="formState" class="edit-form">
        <a-form-item label="应用名称">
          <a-input v-model:value="formState.appName" :maxlength="60" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item v-if="isAdminMode" label="应用封面">
          <a-input v-model:value="formState.cover" placeholder="请输入封面图片地址" />
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
import { getAppByIdAdmin, getAppVo, updateAppAdmin, updateMyApp } from '@/api/appController'
import { getStaticPreviewUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import { resolveAppDetailLoadMode, resolveAppEditorMode } from '@/utils/appHelpers'
import { formatCodeGenType } from '@/utils/codeGenTypes'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { accessRole, loginUser } = storeToRefs(loginUserStore)

const pageReady = ref(false)
const submitLoading = ref(false)
const appDetail = ref<API.AppVO | API.App | null>(null)
const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const appId = computed(() => String(route.params.id ?? '').trim())
const detailLoadMode = computed(() =>
  resolveAppDetailLoadMode(accessRole.value, String(route.query.mode || '')),
)

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

const codeGenLabel = computed(() => formatCodeGenType(appDetail.value?.codeGenType))

const syncFormState = () => {
  formState.appName = appDetail.value?.appName || ''
  formState.cover = appDetail.value?.cover || ''
  formState.priority = appDetail.value?.priority ?? 0
}

const loadAppDetail = async () => {
  if (!/^\d+$/.test(appId.value)) {
    message.error('无效的应用编号')
    await router.replace('/')
    return false
  }

  try {
    const res = detailLoadMode.value.useAdminApi
      ? await getAppByIdAdmin({ id: appId.value })
      : await getAppVo({ id: appId.value })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '应用详情加载失败')
      return false
    }

    appDetail.value = res.data.data

    if (!resolveAppEditorMode(accessRole.value, loginUser.value?.id, res.data.data.userId).canEdit) {
      await router.replace('/403')
      return false
    }

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
        id: appId.value,
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
        id: appId.value,
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

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.panel-label {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.panel-head h1 {
  margin: 0;
  color: #0f172a;
  font-size: 32px;
  line-height: 1.1;
}

.panel-head span {
  display: inline-block;
  margin-top: 10px;
  color: #64748b;
  line-height: 1.75;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
}

.summary-card {
  padding: 18px;
  background: rgb(255 255 255 / 78%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 20px;
}

.summary-card span {
  display: block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.summary-card strong {
  color: #0f172a;
  line-height: 1.5;
}

.edit-form {
  margin-top: 26px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 980px) {
  .panel-head {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column-reverse;
  }
}
</style>
