<template>
  <div ref="containerRef" class="chat-message-list">
    <div v-if="messages.length === 0" class="message-empty">
      <p>这里会显示你与 AI 的对话过程。</p>
    </div>

    <article
      v-for="item in messages"
      :key="item.id"
      class="message-item"
      :class="item.role === 'user' ? 'is-user' : 'is-assistant'"
    >
      <div class="message-bubble">
        <p>{{ item.content || (item.role === 'assistant' ? '正在生成…' : '') }}</p>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

export interface AppChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
}

const props = defineProps<{
  messages: AppChatMessage[]
}>()

const containerRef = ref<HTMLDivElement | null>(null)

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
    await nextTick()
    scrollToBottom()
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
