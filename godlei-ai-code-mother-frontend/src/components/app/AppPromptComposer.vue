<template>
  <div class="prompt-composer glass-card">
    <div class="composer-surface">
      <a-textarea
        :value="modelValue"
        :maxlength="2000"
        :auto-size="{ minRows: 4, maxRows: 8 }"
        :placeholder="placeholder"
        class="composer-textarea"
        @update:value="$emit('update:modelValue', $event)"
        @keydown="handleKeydown"
      />

      <div class="composer-footer" :class="{ 'is-compact': !showHelperCopy }">
        <div v-if="showHelperCopy" class="composer-copy">
          <strong v-if="helperTitle">{{ helperTitle }}</strong>
          <span v-if="helperText">{{ helperText }}</span>
        </div>

        <a-button
          type="primary"
          size="large"
          class="icon-submit-button"
          :loading="loading"
          :aria-label="submitText"
          :title="submitText"
          @click="$emit('submit')"
        >
          <span class="submit-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none">
              <path
                d="M20.1 4.8L4.6 10.7C3.7 11 3.7 12.3 4.6 12.6L10.9 14.8L13.1 21.1C13.4 22 14.7 22 15 21.1L20.9 5.6C21.2 4.8 20.9 4.5 20.1 4.8Z"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linejoin="round"
              />
              <path
                d="M10.9 14.8L20.4 5.3"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
            </svg>
          </span>
        </a-button>
      </div>
    </div>

    <div v-if="suggestions.length" class="suggestion-list">
      <button
        v-for="item in suggestions"
        :key="item"
        class="suggestion-card"
        type="button"
        @click="$emit('update:modelValue', item)"
      >
        {{ item }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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
    submitText: '立即创建',
    helperTitle: '',
    helperText: '',
    placeholder: '帮我创建个人博客网站',
    suggestions: () => [],
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
}>()

const showHelperCopy = computed(() => Boolean(props.helperTitle || props.helperText))

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
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: min(960px, 100%);
  padding: clamp(18px, 3vw, 24px);
  margin: 0 auto;
  background: rgb(255 255 255 / 58%);
  border-color: rgb(191 219 254 / 56%);
  box-shadow:
    0 24px 56px rgb(148 163 184 / 20%),
    inset 0 1px 0 rgb(255 255 255 / 72%);
}

.composer-surface {
  padding: 10px 10px 12px;
  background: linear-gradient(180deg, rgb(255 255 255 / 92%), rgb(241 245 249 / 96%));
  border: 1px solid rgb(191 219 254 / 44%);
  border-radius: 28px;
  backdrop-filter: blur(18px);
}

.composer-textarea {
  font-size: 16px;
}

:deep(.composer-textarea.ant-input) {
  padding: 16px 18px 10px;
  color: #0f172a;
  background: transparent;
  border: none;
  box-shadow: none;
}

:deep(.composer-textarea.ant-input::placeholder) {
  color: #94a3b8;
}

:deep(.composer-textarea.ant-input:focus) {
  box-shadow: none;
}

.composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
  padding: 0 8px 4px;
}

.composer-footer.is-compact {
  justify-content: flex-end;
}

.composer-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.composer-copy strong {
  color: #f8fafc;
  font-size: 15px;
}

.composer-copy span {
  color: rgb(226 232 240 / 72%);
  font-size: 13px;
}

.icon-submit-button {
  width: 76px;
  min-width: 76px;
  height: 40px;
  padding: 0;
  border-radius: 16px;
}

.submit-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
}

.submit-icon svg {
  width: 100%;
  height: 100%;
}

.suggestion-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.suggestion-card {
  min-height: 104px;
  padding: 16px 18px;
  color: #334155;
  font: inherit;
  line-height: 1.7;
  text-align: left;
  background:
    linear-gradient(140deg, rgb(255 255 255 / 92%), rgb(248 250 252 / 86%)),
    linear-gradient(135deg, rgb(191 219 254 / 36%), rgb(224 242 254 / 18%));
  border: 1px solid rgb(191 219 254 / 42%);
  border-radius: 22px;
  cursor: pointer;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease;
}

.suggestion-card:hover {
  border-color: rgb(96 165 250 / 42%);
  box-shadow: 0 18px 30px rgb(148 163 184 / 16%);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .composer-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .suggestion-list {
    grid-template-columns: 1fr;
  }

  .suggestion-card {
    min-height: auto;
  }
}
</style>
