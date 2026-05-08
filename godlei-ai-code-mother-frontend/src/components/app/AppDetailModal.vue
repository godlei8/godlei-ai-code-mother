<template>
  <a-modal
    :open="open"
    :title="null"
    :footer="null"
    width="880px"
    destroy-on-close
    @cancel="$emit('close')"
  >
    <div v-if="app" class="app-detail-modal">
      <div class="detail-visual">
        <img v-if="app.cover" :src="app.cover" :alt="profile.title" />
        <div v-else class="detail-visual-fallback">
          <span>{{ codeGenLabel }}</span>
          <strong>{{ profile.title }}</strong>
        </div>
      </div>

      <PageSectionHeader
        eyebrow="App Detail"
        :title="profile.title"
        :description="detailDescription"
        title-tag="h2"
        :show-extra="Boolean(previewUrl)"
      >
        <template #extra>
          <a v-if="previewUrl" :href="previewUrl" target="_blank" rel="noreferrer">打开预览</a>
        </template>
      </PageSectionHeader>

      <DetailStatsGrid :items="detailItems" />

      <section v-if="promptText" class="detail-section">
        <h3>初始提示词</h3>
        <p>{{ promptText }}</p>
      </section>

      <div v-if="actions.length" class="detail-actions">
        <a-button
          v-for="action in actions"
          :key="action.key"
          :type="action.variant === 'primary' ? 'primary' : 'default'"
          :danger="action.danger"
          :disabled="action.disabled"
          @click="$emit('action', action.key)"
        >
          {{ action.label }}
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AppActionItem } from '@/components/app/appAction'
import DetailStatsGrid from '@/components/common/DetailStatsGrid.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { resolveAppCardProfile } from '@/utils/appCard'
import { formatAppDateTime } from '@/utils/appHelpers'
import { formatCodeGenType } from '@/utils/codeGenTypes'

const props = withDefaults(
  defineProps<{
    open: boolean
    app?: API.AppVO | API.App | null
    description?: string
    previewUrl?: string
    actions?: AppActionItem[]
  }>(),
  {
    app: null,
    description: '',
    previewUrl: '',
    actions: () => [],
  },
)

defineEmits<{
  close: []
  action: [key: string]
}>()

const profile = computed(() =>
  resolveAppCardProfile((props.app ?? {}) as API.AppVO),
)

const codeGenLabel = computed(() => formatCodeGenType(props.app?.codeGenType))
const promptText = computed(() => props.app?.initPrompt?.trim() ?? '')
const detailDescription = computed(() => {
  return (
    props.description?.trim() ||
    promptText.value ||
    '查看应用的封面、生成模式、创建者与最新更新时间。'
  )
})

const detailItems = computed(() => [
  {
    label: '应用编号',
    value: props.app?.id,
  },
  {
    label: '生成模式',
    value: codeGenLabel.value,
  },
  {
    label: '创建者',
    value: profile.value.creatorName,
  },
  {
    label: '最近更新时间',
    value: formatAppDateTime(props.app?.updateTime || props.app?.createTime),
  },
  {
    label: '预览状态',
    value: props.previewUrl ? '可预览' : '待生成',
  },
])
</script>

<style scoped>
.app-detail-modal {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-visual {
  position: relative;
  aspect-ratio: 16 / 7;
  overflow: hidden;
  background:
    linear-gradient(140deg, rgb(219 234 254 / 92%), rgb(240 253 250 / 86%)),
    linear-gradient(180deg, #eff6ff, #f8fafc);
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 24px;
}

.detail-visual img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-visual-fallback {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
  width: 100%;
  height: 100%;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgb(37 99 235 / 22%), transparent 32%),
    linear-gradient(180deg, rgb(255 255 255 / 0%), rgb(15 23 42 / 14%)),
    linear-gradient(135deg, #dbeafe 0%, #ecfeff 100%);
}

.detail-visual-fallback span {
  color: #155eef;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-visual-fallback strong {
  color: #0f172a;
  font-size: clamp(24px, 3vw, 32px);
  line-height: 1.2;
}

.detail-section {
  padding: 18px 20px;
  background: rgb(248 250 252 / 88%);
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 20px;
}

.detail-section h3 {
  margin: 0 0 12px;
  color: #0f172a;
  font-size: 18px;
}

.detail-section p {
  margin: 0;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}
</style>
