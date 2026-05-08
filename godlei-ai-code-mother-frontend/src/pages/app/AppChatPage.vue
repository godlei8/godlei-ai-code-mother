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
        />

        <AppChatMessageList :messages="messages" />

        <div v-if="deployedUrl" class="deploy-banner">
          <span>部署成功：</span>
          <a :href="deployedUrl" target="_blank" rel="noreferrer">{{ deployedUrl }}</a>
        </div>

        <AppChatInput
          v-model="draftMessage"
          :loading="sending"
          placeholder="请描述你想生成的网站，越详细效果越好。"
          @submit="handleSend"
        />
      </div>

      <div class="preview-column">
        <AppPreviewFrame
          :title="appDetail?.appName || '应用预览'"
          :src="previewSrc"
          :loading="sending && !previewReady"
          :empty-description="previewEmptyDescription"
        >
          <template #actions>
            <a-button @click="router.push(`/app/edit/${appId}`)">编辑信息</a-button>
            <a-button
              class="preview-deploy-button"
              type="primary"
              :loading="deploying"
              :disabled="!appDetail?.id"
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
import AppChatInput from '@/components/app/AppChatInput.vue'
import AppChatMessageList from '@/components/app/AppChatMessageList.vue'
import AppPreviewFrame from '@/components/app/AppPreviewFrame.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { deployApp, getAppVo } from '@/api/appController'
import { API_BASE_URL, getDeployUrl, getStaticPreviewUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import { consumePendingAppPrompt } from '@/utils/appHelpers'
import { consumeSseChunk, createSseAccumulator, flushSseAccumulator } from '@/utils/appStream'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { accessRole, loginUser } = storeToRefs(loginUserStore)

const pageReady = ref(false)
const sending = ref(false)
const deploying = ref(false)
const previewReady = ref(false)
const previewVersion = ref(Date.now())
const draftMessage = ref('')
const deployedUrlFromAction = ref('')
const appDetail = ref<API.AppVO | API.App | null>(null)
const messages = ref<ChatMessage[]>([])

const appId = computed(() => String(route.params.id ?? '').trim())

const headerDescription = computed(() => {
  if (accessRole.value === 'admin') {
    return '管理员身份下也会按应用拥有者视角进入对话与预览链路。'
  }
  return '左侧与 AI 持续对话生成页面，右侧会在生成完成后自动刷新网站预览。'
})

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
  return deployedUrl.value ? '重新部署应用' : '部署应用'
})

const deployButtonText = computed(() => {
  return deployedUrl.value ? '重新部署' : '部署应用'
})

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
    const res = await getAppVo({ id: appId.value })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '应用详情加载失败')
      return false
    }

    appDetail.value = res.data.data

    if (
      accessRole.value !== 'admin' &&
      loginUser.value?.id &&
      res.data.data.userId &&
      String(res.data.data.userId) !== String(loginUser.value.id)
    ) {
      await router.replace('/403')
      return false
    }

    previewReady.value = Boolean(res.data.data.id && res.data.data.codeGenType)
    previewVersion.value = Date.now()
    return true
  } catch {
    message.error('应用详情加载失败，请稍后重试')
    await router.replace('/')
    return false
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
  if (!content.trim() || sending.value) {
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

const handleDeploy = async () => {
  if (!appId.value) {
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({
      appId: appId.value,
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

onMounted(async () => {
  const success = await loadAppDetail()
  pageReady.value = success
  if (!success) {
    return
  }

  const pendingPrompt = consumePendingAppPrompt(appId.value)
  if (pendingPrompt) {
    await sendMessage(pendingPrompt)
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
  gap: 8px;
}

.preview-deploy-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 120px;
  height: 34px;
  padding: 0 14px;
  border-radius: 12px;
}

.preview-deploy-icon {
  width: 14px;
  height: 14px;
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
}
</style>
