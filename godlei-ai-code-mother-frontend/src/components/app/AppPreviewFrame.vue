<template>
  <div class="preview-frame glass-card">
    <PageSectionHeader eyebrow="Live Preview" :title="title" title-tag="h3" class="frame-header">
      <template #extra>
        <div class="frame-actions">
          <slot name="actions" />
          <a v-if="src" :href="src" target="_blank" rel="noreferrer">新窗口打开</a>
        </div>
      </template>
    </PageSectionHeader>

    <div class="frame-shell">
      <AppEmptyState v-if="!src" title="预览尚未就绪" :description="emptyDescription" icon="WEB" />

      <AppEmptyState
        v-else-if="hasError"
        title="预览加载失败"
        description="当前预览页没有成功加载，你可以稍后重试，或者在新窗口中打开排查。"
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
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'

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
    loadingText: '正在加载最新生成结果...',
    emptyDescription: '当应用生成完成后，右侧会自动展示可访问的网站效果。',
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
  gap: 14px;
  height: 100%;
  padding: 18px;
}

.frame-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.frame-shell {
  position: relative;
  flex: 1;
  min-height: 460px;
  overflow: hidden;
  background: rgb(248 250 252 / 82%);
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 22px;
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
    min-height: 340px;
  }
}
</style>
