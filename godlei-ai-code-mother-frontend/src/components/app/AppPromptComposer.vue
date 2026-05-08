<template>
  <div class="prompt-composer glass-card">
    <a-textarea
      :value="modelValue"
      :maxlength="2000"
      :auto-size="{ minRows: 5, maxRows: 8 }"
      :placeholder="placeholder"
      class="composer-textarea"
      @update:value="$emit('update:modelValue', $event)"
      @keydown="handleKeydown"
    />

    <div class="composer-footer">
      <div class="composer-copy">
        <strong>{{ helperTitle }}</strong>
        <span>{{ helperText }}</span>
      </div>

      <a-button type="primary" size="large" :loading="loading" @click="$emit('submit')">
        {{ submitText }}
      </a-button>
    </div>

    <div v-if="suggestions.length" class="suggestion-list">
      <button
        v-for="item in suggestions"
        :key="item"
        class="suggestion-chip"
        type="button"
        @click="$emit('update:modelValue', item)"
      >
        {{ item }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue: string
    loading?: boolean
    submitText?: string
    helperTitle?: string
    helperText?: string
    placeholder?: string
    suggestions?: string[]
  }>(),
  {
    loading: false,
    submitText: '生成应用',
    helperTitle: '一句话描述你想做什么',
    helperText: '支持多行描述，按 Enter 快速发送，Shift + Enter 换行。',
    placeholder: '使用 NoCode 创建一个高效的小工具，帮我计算……',
    suggestions: () => [],
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
.prompt-composer {
  padding: clamp(22px, 4vw, 28px);
}

.composer-textarea {
  font-size: 16px;
}

:deep(.composer-textarea.ant-input) {
  padding: 18px 18px 14px;
  color: #0f172a;
  background: rgb(255 255 255 / 88%);
  border: 1px solid rgb(203 213 225 / 76%);
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 78%);
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 18px;
}

.composer-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.composer-copy strong {
  color: #0f172a;
  font-size: 16px;
}

.composer-copy span {
  color: #64748b;
  font-size: 13px;
}

.suggestion-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.suggestion-chip {
  padding: 10px 14px;
  color: #334155;
  font: inherit;
  background: rgb(255 255 255 / 78%);
  border: 1px solid rgb(148 163 184 / 18%);
  border-radius: 999px;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.suggestion-chip:hover {
  border-color: rgb(37 99 235 / 34%);
  box-shadow: 0 12px 24px rgb(15 23 42 / 6%);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .composer-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
