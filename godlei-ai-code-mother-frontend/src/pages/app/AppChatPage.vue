<template>
  <div class="app-chat-page page-stack">
    <section v-if="pageReady" class="chat-layout">
      <div class="chat-column glass-card">
        <PageSectionHeader
          eyebrow="App Workspace"
          :title="appDetail?.appName || '未命名应用'"
          :description="headerDescription"
          title-tag="h1"
          class="chat-header"
        >
          <template #extra>
            <div class="codegen-badge">
              <span class="codegen-badge-label">生成类型</span>
              <strong>{{ codeGenTypeLabel }}</strong>
            </div>
          </template>
        </PageSectionHeader>

        <AppChatMessageList
          :messages="messages"
          :user-avatar="loginUser?.userAvatar"
          :user-name="loginUser?.userName || loginUser?.userAccount || '用户'"
          :has-more-history="hasMoreHistory"
          :loading-more-history="loadingMoreHistory"
          @load-more="handleLoadMoreHistory"
        />

        <div v-if="deployedUrl" class="deploy-banner">
          <span>部署成功：</span>
          <a :href="deployedUrl" target="_blank" rel="noreferrer">{{ deployedUrl }}</a>
        </div>

        <AppChatInput
          v-model="draftMessage"
          :loading="sending"
          :disabled="isReadOnlyView"
          placeholder="请描述你想生成的网站，越详细效果越好。"
          @submit="handleSend"
        />
      </div>

      <div class="preview-column">
        <AppPreviewFrame
          :title="appDetail?.appName || '应用预览'"
          :src="previewSrc"
          :loading="sending"
          :empty-description="previewEmptyDescription"
        >
          <template #actions>
            <a-button class="preview-action-button" @click="router.push(`/app/edit/${appId}`)">
              编辑信息
            </a-button>
            <a-button
              class="preview-action-button"
              :loading="downloadingCode"
              :disabled="!appDetail?.id || isReadOnlyView"
              @click="handleDownloadCode"
            >
              下载代码
            </a-button>
            <a-button
              class="preview-action-button preview-deploy-button"
              type="primary"
              :loading="deploying"
              :disabled="!appDetail?.id || isReadOnlyView"
              :title="deployButtonTitle"
              :aria-label="deployButtonText"
              @click="handleDeploy"
            >
              <svg
                class="preview-deploy-icon"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
              >
                <path
                  d="M10 3.2L15.6 6.4V10.2C15.6 13.6 13.22 16.72 10 17.6C6.78 16.72 4.4 13.6 4.4 10.2V6.4L10 3.2Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M10 6.4V11.2"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                />
                <path
                  d="M8.2 9.4L10 11.2L11.8 9.4"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <span>{{ deployButtonText }}</span>
            </a-button>
          </template>
        </AppPreviewFrame>
      </div>
    </section>

    <a-skeleton v-else active :paragraph="{ rows: 10 }" class="glass-card page-skeleton" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { listLatest, listOlder } from '@/api/chatHistoryController'
import AppChatInput from '@/components/app/AppChatInput.vue'
import AppChatMessageList from '@/components/app/AppChatMessageList.vue'
import AppPreviewFrame from '@/components/app/AppPreviewFrame.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { deployApp, downloadAppCode, getAppVo } from '@/api/appController'
import { API_BASE_URL, getDeployUrl, getStaticPreviewUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import { consumeSseChunk, createSseAccumulator, flushSseAccumulator } from '@/utils/appStream'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { resolveDownloadFilename, triggerBlobDownload } from '@/utils/download'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
  createdTime?: string
  messageType?: string
}

const CHAT_HISTORY_PAGE_SIZE = 10

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { accessRole, loginUser } = storeToRefs(loginUserStore)

const pageReady = ref(false)
const sending = ref(false)
const deploying = ref(false)
const downloadingCode = ref(false)
const previewReady = ref(false)
const previewVersion = ref(Date.now())
const draftMessage = ref('')
const hasMoreHistory = ref(false)
const loadingMoreHistory = ref(false)
const nextBeforeCreateTime = ref('')
const nextBeforeId = ref<API.LongId>()
const deployedUrlFromAction = ref('')
const appDetail = ref<API.AppVO | API.App | null>(null)
const messages = ref<ChatMessage[]>([])

const appId = computed(() => String(route.params.id ?? '').trim())
const safeAppId = computed<API.LongId>(() => appId.value)
const safeAppIdParam = computed(() => safeAppId.value as unknown as number)
const safeBeforeIdParam = computed(() =>
  nextBeforeId.value ? (nextBeforeId.value as unknown as number) : undefined,
)
const isAdminViewer = computed(() => accessRole.value === 'admin')
const isOwnApp = computed(() => {
  if (!loginUser.value?.id || !appDetail.value?.userId) {
    return false
  }
  return String(loginUser.value.id) === String(appDetail.value.userId)
})
const isReadOnlyView = computed(() => isAdminViewer.value && !isOwnApp.value)

const headerDescription = computed(() => {
  if (isReadOnlyView.value) {
    return '当前是管理员只读查看模式，可以查看历史对话和网站结果，但不能继续生成或部署该应用。'
  }
  if (isAdminViewer.value) {
    return '当前以管理员身份查看自己的应用，对话与预览链路保持正常可操作。'
  }
  return '左侧与 AI 持续对话生成页面，右侧会在生成完成后自动刷新网站预览。'
})

const codeGenTypeLabel = computed(() => formatCodeGenType(appDetail.value?.codeGenType))

const previewSrc = computed(() => {
  if (!previewReady.value || !appDetail.value?.id || !appDetail.value.codeGenType) {
    return ''
  }

  const baseSrc = getStaticPreviewUrl(appDetail.value.codeGenType, appDetail.value.id)
  return `${baseSrc}${baseSrc.includes('?') ? '&' : '?'}t=${previewVersion.value}`
})

const previewEmptyDescription = computed(() => {
  if (sending.value) {
    return 'AI 正在生成网站代码，流式返回结束后会自动刷新右侧预览。'
  }
  return '先在左侧发送消息，待生成完成后这里会展示网页效果。'
})

const deployedUrl = computed(() => {
  if (deployedUrlFromAction.value) {
    return deployedUrlFromAction.value
  }

  const deployKey = appDetail.value?.deployKey
  return deployKey ? getDeployUrl(deployKey) : ''
})

const deployButtonTitle = computed(() => {
  if (isReadOnlyView.value) {
    return '管理员只读查看模式下不能部署非本人应用'
  }
  return deployedUrl.value ? '重新部署应用' : '部署应用'
})

const deployButtonText = computed(() => {
  return deployedUrl.value ? '重新部署' : '部署应用'
})

const mapHistoryMessageRole = (messageType?: string): ChatMessage['role'] => {
  return messageType === 'user' ? 'user' : 'assistant'
}

const normalizeHistoryMessageContent = (record: API.ChatHistoryVO) => {
  if (record.messageType === 'ai_error') {
    return `AI 生成失败：${record.message || '未知错误'}`
  }

  return record.message || ''
}

const toChatMessage = (record: API.ChatHistoryVO): ChatMessage => ({
  id: `history-${record.id ?? `${record.createTime ?? 'unknown'}-${record.messageType ?? 'message'}`}`,
  role: mapHistoryMessageRole(record.messageType),
  content: normalizeHistoryMessageContent(record),
  createdTime: record.createTime,
  messageType: record.messageType,
})

const mergeChatMessages = (nextMessages: ChatMessage[], currentMessages: ChatMessage[]) => {
  const merged: ChatMessage[] = []
  const seen = new Set<string>()

  for (const item of [...nextMessages, ...currentMessages]) {
    if (seen.has(item.id)) {
      continue
    }
    seen.add(item.id)
    merged.push(item)
  }

  return merged
}

const updateHistoryCursor = (cursor?: API.ChatHistoryCursorVO) => {
  hasMoreHistory.value = Boolean(cursor?.hasMore)
  nextBeforeCreateTime.value = cursor?.nextBeforeCreateTime ?? ''
  nextBeforeId.value = cursor?.nextBeforeId
}

const applyInitialHistory = (cursor?: API.ChatHistoryCursorVO) => {
  const records = (cursor?.records ?? []).map(toChatMessage)
  messages.value = records
  updateHistoryCursor(cursor)
  previewReady.value = records.length >= 2
  if (previewReady.value) {
    previewVersion.value = Date.now()
  }
  return records.length
}

const prependOlderHistory = (cursor?: API.ChatHistoryCursorVO) => {
  const records = (cursor?.records ?? []).map(toChatMessage)
  messages.value = mergeChatMessages(records, messages.value)
  updateHistoryCursor(cursor)
  return records.length
}

const createChatMessage = (role: ChatMessage['role'], content = ''): ChatMessage => ({
  id: `${role}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  role,
  content,
})

const loadAppDetail = async () => {
  if (!/^\d+$/.test(appId.value)) {
    message.error('无效的应用编号')
    await router.replace('/')
    return false
  }

  try {
    const res = await getAppVo({ id: safeAppIdParam.value })


    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '应用详情加载失败')
      return false
    }

    appDetail.value = res.data.data

    if (
      !isAdminViewer.value &&
      loginUser.value?.id &&
      res.data.data.userId &&
      String(res.data.data.userId) !== String(loginUser.value.id)
    ) {
      await router.replace('/403')
      return false
    }

    return true
  } catch {
    message.error('应用详情加载失败，请稍后重试')
    await router.replace('/')
    return false
  }
}

const loadLatestHistory = async () => {
  if (!/^\d+$/.test(appId.value)) {
    return null
  }

  try {
    const res = await listLatest({
      appId: safeAppIdParam.value,
      pageSize: CHAT_HISTORY_PAGE_SIZE,
    })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '对话历史加载失败')
      return null
    }

    return applyInitialHistory(res.data.data)
  } catch {
    message.error('对话历史加载失败，请稍后重试')
    return null
  }
}

const loadOlderHistory = async () => {
  if (
    loadingMoreHistory.value ||
    !hasMoreHistory.value ||
    !nextBeforeCreateTime.value ||
    !nextBeforeId.value ||
    !/^\d+$/.test(appId.value)
  ) {
    return
  }

  loadingMoreHistory.value = true
  try {
    const res = await listOlder({
      appId: safeAppIdParam.value,
      pageSize: CHAT_HISTORY_PAGE_SIZE,
      beforeCreateTime: nextBeforeCreateTime.value,
      beforeId: safeBeforeIdParam.value,
    })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '更早对话加载失败')
      return
    }

    prependOlderHistory(res.data.data)
  } catch {
    message.error('更早对话加载失败，请稍后重试')
  } finally {
    loadingMoreHistory.value = false
  }
}

const appendAssistantText = (messageId: string, chunks: string[]) => {
  if (!chunks.length) {
    return
  }

  const target = messages.value.find((item) => item.id === messageId)
  if (!target) {
    return
  }

  target.content += chunks.join('')
}

const sendMessage = async (content: string) => {
  if (!content.trim() || sending.value || isReadOnlyView.value) {
    if (isReadOnlyView.value) {
      message.warning('管理员只读查看模式下不能继续生成该应用')
    }
    return
  }

  const userMessage = createChatMessage('user', content.trim())
  const assistantMessage = createChatMessage('assistant', '')
  messages.value.push(userMessage, assistantMessage)
  draftMessage.value = ''
  sending.value = true

  try {
    const url = new URL(`${API_BASE_URL}/app/chat/gen/code`)
    url.searchParams.set('appId', appId.value)
    url.searchParams.set('message', content.trim())

    const response = await fetch(url.toString(), {
      method: 'GET',
      credentials: 'include',
    })

    if (!response.ok || !response.body) {
      throw new Error('生成服务暂时不可用')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    const accumulator = createSseAccumulator()

    while (true) {
      const { value, done } = await reader.read()
      if (done) {
        break
      }

      const chunk = decoder.decode(value, { stream: true })
      appendAssistantText(assistantMessage.id, consumeSseChunk(accumulator, chunk))
    }

    appendAssistantText(assistantMessage.id, flushSseAccumulator(accumulator))

    if (!assistantMessage.content) {
      assistantMessage.content = '生成完成，你可以继续补充修改需求。'
    }

    previewReady.value = true
    previewVersion.value = Date.now()
    await loadAppDetail()
  } catch (error) {
    const target = messages.value.find((item) => item.id === assistantMessage.id)
    if (target && !target.content) {
      target.content = error instanceof Error ? error.message : '生成失败，请稍后重试。'
    }
    message.error('生成过程中出现异常，请稍后再试')
  } finally {
    sending.value = false
  }
}

const handleSend = async () => {
  await sendMessage(draftMessage.value)
}

const handleLoadMoreHistory = async () => {
  await loadOlderHistory()
}

const handleDeploy = async () => {
  if (!appId.value || isReadOnlyView.value) {
    if (isReadOnlyView.value) {
      message.warning('管理员只读查看模式下不能部署非本人应用')
    }
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({
      appId: safeAppIdParam.value,
    })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '部署失败')
      return
    }

    deployedUrlFromAction.value = String(res.data.data)
    message.success('部署成功，已生成访问地址')
    await loadAppDetail()
  } catch {
    message.error('部署失败，请稍后重试')
  } finally {
    deploying.value = false
  }
}

const handleDownloadCode = async () => {
  if (!appDetail.value?.id) {
    message.warning('应用信息尚未加载完成')
    return
  }

  if (isReadOnlyView.value) {
    message.warning('管理员只读查看模式下不能下载非本人应用代码')
    return
  }

  downloadingCode.value = true
  try {
    const response = await downloadAppCode(
      {
        appId: String(appDetail.value.id) as unknown as number,
      },
      {
        responseType: 'blob',
      },
    )

    const zipBlob =
      response.data instanceof Blob
        ? response.data
        : new Blob([response.data], { type: 'application/zip' })
    const fallbackBaseName = (appDetail.value.appName || `app-${appDetail.value.id}`).trim()
    const filename = resolveDownloadFilename(
      response.headers?.['content-disposition'],
      fallbackBaseName,
    )

    triggerBlobDownload(zipBlob, filename)
    message.success('代码包开始下载')
  } catch {
    message.error('下载代码失败，请稍后重试')
  } finally {
    downloadingCode.value = false
  }
}

onMounted(async () => {
  const success = await loadAppDetail()
  if (!success) {
    return
  }

  const initialHistoryCount = await loadLatestHistory()
  pageReady.value = true

  if (
    initialHistoryCount === 0 &&
    isOwnApp.value &&
    appDetail.value?.initPrompt?.trim()
  ) {
    await sendMessage(appDetail.value.initPrompt.trim())
  }
})
</script>

<style scoped>
.app-chat-page {
  gap: 14px;
}

.chat-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 3fr);
  gap: 14px;
}

.chat-column {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: calc(100vh - 154px);
  padding: 18px;
}

.codegen-badge {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  min-width: 132px;
  margin-top: 14px;
  padding: 10px 14px;
  background: linear-gradient(180deg, rgb(255 255 255 / 94%), rgb(239 246 255 / 92%));
  border: 1px solid rgb(96 165 250 / 22%);
  border-radius: 16px;
  box-shadow: 0 14px 28px rgb(59 130 246 / 10%);
}

.codegen-badge-label {
  color: #64748b;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.codegen-badge strong {
  color: #1d4ed8;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
}

.deploy-banner {
  padding: 12px 14px;
  color: #0f766e;
  background: rgb(236 253 245 / 88%);
  border: 1px solid rgb(16 185 129 / 16%);
  border-radius: 16px;
}

.preview-column {
  min-width: 0;
}

.preview-column :deep(.frame-actions) {
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.preview-column :deep(.frame-actions > a) {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 2px;
  font-size: 13px;
  font-weight: 500;
  color: #2563eb;
  white-space: nowrap;
}

.preview-action-button {
  height: 30px;
  padding: 0 12px;
  border-radius: 10px;
  font-size: 13px;
  white-space: nowrap;
}

.preview-deploy-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  min-width: 96px;
  box-shadow: 0 10px 24px rgb(59 130 246 / 20%);
}

.preview-deploy-icon {
  width: 12px;
  height: 12px;
}

.preview-column :deep(.preview-frame) {
  position: sticky;
  top: 84px;
  min-height: calc(100vh - 154px);
}

.page-skeleton {
  padding: 24px;
}

@media (max-width: 1180px) {
  .chat-layout {
    grid-template-columns: 1fr;
  }

  .preview-column :deep(.preview-frame) {
    position: static;
    min-height: auto;
  }
}

@media (max-width: 720px) {
  .chat-column {
    min-height: auto;
    padding: 16px;
  }

  .preview-column :deep(.frame-actions) {
    justify-content: flex-start;
  }

  .preview-action-button {
    height: 28px;
    padding: 0 10px;
    font-size: 12px;
  }

  .preview-column :deep(.frame-actions > a) {
    min-height: 28px;
    padding: 0;
    font-size: 12px;
  }
}
</style>
