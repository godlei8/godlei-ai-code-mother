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
      <div v-if="loading && !src" class="preview-generation-state">
        <div class="generation-aurora generation-aurora-left" />
        <div class="generation-aurora generation-aurora-right" />
        <div class="generation-orbit">
          <span class="generation-orbit-ring generation-orbit-ring-outer" />
          <span class="generation-orbit-ring generation-orbit-ring-inner" />
          <span class="generation-core">
            <span class="generation-core-dot" />
            <span class="generation-core-dot generation-core-dot-alt" />
          </span>
        </div>
        <div class="generation-copy">
          <p class="generation-eyebrow">AI GENERATING</p>
          <h4>正在构建你的应用预览</h4>
          <p>{{ loadingText }}</p>
        </div>
        <div class="generation-code-lines" aria-hidden="true">
          <span class="generation-code-line generation-code-line-long" />
          <span class="generation-code-line generation-code-line-medium" />
          <span class="generation-code-line generation-code-line-short" />
        </div>
      </div>

      <AppEmptyState v-else-if="!src" title="预览尚未就绪" :description="emptyDescription" icon="WEB" />

      <AppEmptyState
        v-else-if="hasError"
        title="预览加载失败"
        description="当前预览页没有成功加载，你可以稍后重试，或者在新窗口中打开排查。"
        icon="ERR"
      />

      <template v-else>
        <div v-if="loading || !hasLoaded" class="frame-loading">
          <div class="frame-loading-card">
            <div class="frame-loading-orbit">
              <span class="frame-loading-ring frame-loading-ring-outer" />
              <span class="frame-loading-ring frame-loading-ring-inner" />
              <span class="frame-loading-core" />
            </div>
            <div class="frame-loading-copy">
              <p class="frame-loading-eyebrow">SYNCING PREVIEW</p>
              <p>{{ loadingText }}</p>
            </div>
          </div>
        </div>

        <iframe
          ref="iframeRef"
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

const emit = defineEmits<{
  frameLoad: [iframe: HTMLIFrameElement | null]
}>()

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
const iframeRef = ref<HTMLIFrameElement | null>(null)

watch(
  () => props.src,
  () => {
    hasLoaded.value = false
    hasError.value = false
  },
)

const handleLoad = () => {
  hasLoaded.value = true
  emit('frameLoad', iframeRef.value)
}

const handleError = () => {
  hasError.value = true
}

const getIframeElement = () => iframeRef.value

defineExpose({
  getIframeElement,
})
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
  background:
    radial-gradient(circle at top left, rgb(191 219 254 / 24%), transparent 34%),
    linear-gradient(180deg, rgb(248 250 252 / 96%), rgb(241 245 249 / 94%));
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 22px;
}

.preview-generation-state {
  position: relative;
  display: grid;
  place-items: center;
  gap: 28px;
  min-height: 100%;
  padding: 44px 28px;
  isolation: isolate;
}

.generation-aurora {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  opacity: 0.82;
  z-index: -1;
}

.generation-aurora-left {
  top: 12%;
  left: 10%;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgb(96 165 250 / 28%), transparent 68%);
  animation: auroraFloat 8s ease-in-out infinite;
}

.generation-aurora-right {
  right: 8%;
  bottom: 12%;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgb(125 211 252 / 30%), transparent 68%);
  animation: auroraFloat 9s ease-in-out infinite reverse;
}

.generation-orbit {
  position: relative;
  display: grid;
  place-items: center;
  width: 180px;
  height: 180px;
}

.generation-orbit-ring {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  border: 1px solid rgb(96 165 250 / 22%);
}

.generation-orbit-ring-outer {
  box-shadow: 0 0 0 14px rgb(255 255 255 / 34%);
  animation: orbitSpin 8s linear infinite;
}

.generation-orbit-ring-inner {
  inset: 18px;
  border-color: rgb(59 130 246 / 24%);
  background: rgb(255 255 255 / 36%);
  backdrop-filter: blur(14px);
  animation: orbitSpinReverse 6s linear infinite;
}

.generation-core {
  position: relative;
  width: 86px;
  height: 86px;
  border-radius: 28px;
  background: linear-gradient(145deg, rgb(37 99 235), rgb(59 130 246));
  box-shadow:
    0 18px 44px rgb(37 99 235 / 28%),
    inset 0 1px 0 rgb(255 255 255 / 42%);
}

.generation-core::before {
  content: '';
  position: absolute;
  inset: 12px;
  border-radius: 22px;
  border: 1px solid rgb(255 255 255 / 24%);
}

.generation-core-dot {
  position: absolute;
  top: 24px;
  left: 24px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: rgb(255 255 255 / 92%);
  box-shadow: 24px 18px 0 rgb(191 219 254 / 86%);
  animation: corePulse 1.8s ease-in-out infinite;
}

.generation-core-dot-alt {
  top: 46px;
  left: 18px;
  width: 10px;
  height: 10px;
  background: rgb(224 231 255 / 92%);
  box-shadow: none;
  animation-delay: 0.4s;
}

.generation-copy {
  display: grid;
  gap: 10px;
  max-width: 440px;
  text-align: center;
}

.generation-copy h4 {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.15;
  color: #0f172a;
}

.generation-copy p {
  margin: 0;
  color: #475569;
  line-height: 1.7;
}

.generation-eyebrow {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.28em;
  color: #2563eb;
}

.generation-code-lines {
  display: grid;
  gap: 12px;
  width: min(420px, 100%);
}

.generation-code-line {
  display: block;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgb(148 163 184 / 16%), rgb(96 165 250 / 44%), rgb(148 163 184 / 16%));
  background-size: 200% 100%;
  animation: shimmer 2.2s linear infinite;
}

.generation-code-line-long {
  width: 100%;
}

.generation-code-line-medium {
  width: 76%;
  justify-self: center;
  animation-delay: 0.18s;
}

.generation-code-line-short {
  width: 52%;
  justify-self: center;
  animation-delay: 0.36s;
}

.frame-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgb(255 255 255 / 82%), rgb(248 250 252 / 88%));
  z-index: 1;
  backdrop-filter: blur(8px);
}

.frame-loading-card {
  display: grid;
  place-items: center;
  gap: 16px;
  padding: 24px 28px;
  border: 1px solid rgb(255 255 255 / 82%);
  border-radius: 24px;
  background: rgb(255 255 255 / 70%);
  box-shadow: 0 20px 40px rgb(148 163 184 / 12%);
}

.frame-loading-orbit {
  position: relative;
  width: 68px;
  height: 68px;
}

.frame-loading-ring {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  border: 2px solid transparent;
}

.frame-loading-ring-outer {
  border-top-color: rgb(59 130 246 / 88%);
  border-right-color: rgb(125 211 252 / 58%);
  animation: orbitSpin 1.4s linear infinite;
}

.frame-loading-ring-inner {
  inset: 10px;
  border-bottom-color: rgb(59 130 246 / 38%);
  border-left-color: rgb(191 219 254 / 88%);
  animation: orbitSpinReverse 1.1s linear infinite;
}

.frame-loading-core {
  position: absolute;
  inset: 24px;
  border-radius: 50%;
  background: linear-gradient(145deg, rgb(37 99 235), rgb(96 165 250));
  box-shadow: 0 0 18px rgb(59 130 246 / 28%);
}

.frame-loading-copy {
  display: grid;
  gap: 6px;
  text-align: center;
}

.frame-loading-copy p {
  margin: 0;
  color: #64748b;
}

.frame-loading-eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.22em;
  color: #2563eb;
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

@keyframes orbitSpin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@keyframes orbitSpinReverse {
  from {
    transform: rotate(360deg);
  }

  to {
    transform: rotate(0deg);
  }
}

@keyframes corePulse {
  0%,
  100% {
    transform: scale(0.92);
    opacity: 0.8;
  }

  50% {
    transform: scale(1.08);
    opacity: 1;
  }
}

@keyframes auroraFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(0, -16px, 0) scale(1.06);
  }
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -20% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .generation-aurora-left,
  .generation-aurora-right,
  .generation-orbit-ring-outer,
  .generation-orbit-ring-inner,
  .generation-core-dot,
  .generation-code-line,
  .frame-loading-ring-outer,
  .frame-loading-ring-inner {
    animation: none;
  }
}

@media (max-width: 1100px) {
  .frame-shell {
    min-height: 340px;
  }
}

@media (max-width: 720px) {
  .preview-generation-state {
    padding: 32px 18px;
    gap: 22px;
  }

  .generation-orbit {
    width: 148px;
    height: 148px;
  }

  .generation-copy h4 {
    font-size: 24px;
  }
}
</style>
