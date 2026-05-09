<template>
  <div ref="containerRef" class="chat-message-list">
    <div v-if="hasMoreHistory || loadingMoreHistory" class="message-toolbar">
      <a-button size="small" :loading="loadingMoreHistory" @click="emit('loadMore')">
        加载更多
      </a-button>
    </div>

    <div v-if="messages.length === 0" class="message-empty">
      <p>这里会显示你与 AI 的对话过程。</p>
    </div>

    <article
      v-for="item in messages"
      :key="item.id"
      class="message-item"
      :class="item.role === 'user' ? 'is-user' : 'is-assistant'"
    >
      <div class="message-row">
        <img
          v-if="item.role === 'assistant'"
          class="assistant-avatar"
          :src="aiAvatar"
          alt="AI"
        />
        <div class="message-bubble">
          <AppMarkdownContent
            v-if="item.role === 'assistant'"
            :content="item.content || '正在生成中...'"
          />
          <p v-else>{{ item.content }}</p>
        </div>
        <a-avatar v-if="item.role === 'user'" :src="userAvatar" :size="34" class="user-avatar">
          {{ userAvatarText }}
        </a-avatar>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import AppMarkdownContent from './AppMarkdownContent.vue'
import aiAvatar from '@/assets/ai-chat-avatar.svg'

export interface AppChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const props = defineProps<{
  messages: AppChatMessage[]
  userAvatar?: string
  userName?: string
  hasMoreHistory?: boolean
  loadingMoreHistory?: boolean
}>()

const containerRef = ref<HTMLDivElement | null>(null)
const userAvatarText = computed(() => props.userName?.slice(0, 1).toUpperCase() || 'U')
const emit = defineEmits<{
  loadMore: []
}>()

const previousFirstMessageId = ref('')
const previousLastMessageId = ref('')
const previousLength = ref(0)
const previousScrollHeight = ref(0)
const previousScrollTop = ref(0)

const scrollToBottom = () => {
  const container = containerRef.value
  if (!container) {
    return
  }
  container.scrollTop = container.scrollHeight
}

watch(
  () => props.messages.map((item) => `${item.id}:${item.content}`).join('\u0001'),
  async () => {
    const container = containerRef.value
    previousScrollHeight.value = container?.scrollHeight ?? 0
    previousScrollTop.value = container?.scrollTop ?? 0

    await nextTick()

    const nextFirstMessageId = props.messages[0]?.id ?? ''
    const nextLastMessageId = props.messages[props.messages.length - 1]?.id ?? ''
    const isPrependingOlderMessages =
      previousLength.value > 0 &&
      props.messages.length > previousLength.value &&
      nextLastMessageId === previousLastMessageId.value &&
      nextFirstMessageId !== previousFirstMessageId.value

    if (isPrependingOlderMessages && containerRef.value) {
      const heightDiff = containerRef.value.scrollHeight - previousScrollHeight.value
      containerRef.value.scrollTop = previousScrollTop.value + heightDiff
    } else {
      scrollToBottom()
    }

    previousFirstMessageId.value = nextFirstMessageId
    previousLastMessageId.value = nextLastMessageId
    previousLength.value = props.messages.length
  },
  {
    immediate: true,
  },
)
</script>

<style scoped>
.chat-message-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
  min-height: 360px;
  max-height: 56vh;
  padding: 6px 4px 6px 2px;
  overflow-y: auto;
}

.message-toolbar {
  display: flex;
  justify-content: center;
  padding-bottom: 2px;
}

.message-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #64748b;
  text-align: center;
  border: 1px dashed rgb(148 163 184 / 24%);
  border-radius: 22px;
}

.message-item {
  display: flex;
}

.message-item.is-user {
  justify-content: flex-end;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  width: 100%;
}

.message-item.is-user .message-row {
  justify-content: flex-end;
}

.assistant-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  align-self: flex-start;
  border-radius: 50%;
  box-shadow: 0 10px 18px rgb(15 23 42 / 12%);
}

.user-avatar {
  flex: 0 0 34px;
  align-self: flex-start;
  color: #eff6ff;
  font-size: 13px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f5ef0 0%, #3b82f6 100%);
  box-shadow: 0 10px 18px rgb(21 94 239 / 18%);
}

.message-bubble {
  max-width: min(100%, 86%);
  padding: 14px 16px;
  border-radius: 20px;
  box-shadow: var(--card-shadow-soft);
}

.message-item.is-user .message-bubble {
  color: #eff6ff;
  background: linear-gradient(135deg, #155eef 0%, #1244c2 100%);
  border-top-right-radius: 8px;
}

.message-item.is-assistant .message-bubble {
  color: #0f172a;
  background: rgb(255 255 255 / 88%);
  border: 1px solid rgb(148 163 184 / 18%);
  border-top-left-radius: 8px;
  overflow: hidden;
}

.message-bubble p {
  margin: 0;
  line-height: 1.85;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 960px) {
  .chat-message-list {
    max-height: none;
  }
}
</style>
