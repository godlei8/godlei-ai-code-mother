<template>
  <div class="preview-frame glass-card">
    <div class="frame-header">
      <div>
        <p class="frame-label">Live Preview</p>
        <h3>{{ title }}</h3>
      </div>
      <a v-if="src" :href="src" target="_blank" rel="noreferrer">新窗口打开</a>
    </div>

    <div class="frame-shell">
      <AppEmptyState
        v-if="!src"
        title="预览尚未就绪"
        :description="emptyDescription"
        icon="WEB"
      />

      <AppEmptyState
        v-else-if="hasError"
        title="预览加载失败"
        description="当前预览页没有成功加载，你可以稍后重试，或在新窗口中打开检查。"
        icon="ERR"
      />

      <template v-else>
        <div v-if="loading || !hasLoaded" class="frame-loading">
          <a-spin size="large" />
          <p>{{ loadingText }}</p>
        </div>

        <iframe
          :key="src"
          :src="src"
          class="preview-iframe"
          :class="{ 'is-hidden': !hasLoaded }"
          title="应用预览"
          @load="handleLoad"
          @error="handleError"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import AppEmptyState from '@/components/app/AppEmptyState.vue'

const props = withDefaults(
  defineProps<{
    title: string
    src?: string
    loading?: boolean
    loadingText?: string
    emptyDescription?: string
  }>(),
  {
    src: '',
    loading: false,
    loadingText: '正在加载最新生成结果…',
    emptyDescription: '当应用生成完成后，右侧会自动展示可访问的网页效果。',
  },
)

const hasLoaded = ref(false)
const hasError = ref(false)

watch(
  () => props.src,
  () => {
    hasLoaded.value = false
    hasError.value = false
  },
)

const handleLoad = () => {
  hasLoaded.value = true
}

const handleError = () => {
  hasError.value = true
}
</script>

<style scoped>
.preview-frame {
  display: flex;
  flex-direction: column;
  gap: 18px;
  height: 100%;
  padding: 24px;
}

.frame-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.frame-label {
  margin: 0 0 8px;
  color: #155eef;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.frame-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.frame-shell {
  position: relative;
  flex: 1;
  min-height: 520px;
  overflow: hidden;
  background: rgb(248 250 252 / 70%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 24px;
}

.frame-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: linear-gradient(180deg, rgb(255 255 255 / 88%), rgb(248 250 252 / 94%));
  z-index: 1;
}

.frame-loading p {
  margin: 0;
  color: #64748b;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: #fff;
}

.preview-iframe.is-hidden {
  opacity: 0;
}

@media (max-width: 1100px) {
  .frame-shell {
    min-height: 360px;
  }
}
</style>
