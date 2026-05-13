<template>
  <div class="chat-input glass-card">
    <a-textarea
      :value="modelValue"
      :auto-size="{ minRows: 3, maxRows: 6 }"
      :maxlength="2000"
      :placeholder="placeholder"
      :disabled="disabled"
      @update:value="$emit('update:modelValue', $event)"
      @keydown="handleKeydown"
    />

    <div class="chat-input-footer">
      <span>Enter 发送，Shift + Enter 换行</span>
      <a-button type="primary" :loading="loading" :disabled="disabled" @click="$emit('submit')">
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
    disabled?: boolean
  }>(),
  {
    loading: false,
    disabled: false,
    placeholder: '请描述你想生成的网站，越详细效果越好哦。',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
}>()

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    if (!props.loading && !props.disabled) {
      emit('submit')
    }
  }
}
</script>

<style scoped>
.chat-input {
  padding: 14px;
  background: rgb(255 255 255 / 78%);
}

:deep(.ant-input) {
  padding: 14px 16px;
  border: none;
  background: rgb(248 250 252 / 96%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 85%);
}

:deep(.ant-input:focus) {
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 85%),
    0 0 0 3px rgb(96 165 250 / 16%);
}

.chat-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.chat-input-footer span {
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 640px) {
  .chat-input-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
