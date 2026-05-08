<template>
  <div class="chat-input glass-card">
    <a-textarea
      :value="modelValue"
      :auto-size="{ minRows: 4, maxRows: 7 }"
      :maxlength="2000"
      :placeholder="placeholder"
      @update:value="$emit('update:modelValue', $event)"
      @keydown="handleKeydown"
    />

    <div class="chat-input-footer">
      <span>Enter 发送，Shift + Enter 换行</span>
      <a-button type="primary" :loading="loading" @click="$emit('submit')">
        发送消息
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: string
    loading?: boolean
    placeholder?: string
  }>(),
  {
    loading: false,
    placeholder: '继续告诉 AI 你想生成或修改什么…',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
}>()

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!props.loading) {
      emit('submit')
    }
  }
}
</script>

<style scoped>
.chat-input {
  padding: 18px;
}

.chat-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
}

.chat-input-footer span {
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 640px) {
  .chat-input-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
