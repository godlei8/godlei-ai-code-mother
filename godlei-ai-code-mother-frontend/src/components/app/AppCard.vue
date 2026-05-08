<template>
  <article class="app-card">
    <div class="card-visual">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName || '应用封面'" />
      <div v-else class="visual-fallback">
        <span>{{ codeGenLabel }}</span>
        <strong>{{ app.appName || '未命名应用' }}</strong>
      </div>
    </div>

    <div class="card-body">
      <div class="card-head">
        <div>
          <h3>{{ app.appName || '未命名应用' }}</h3>
          <p>{{ description }}</p>
        </div>
        <a-tag v-if="badge" color="blue">{{ badge }}</a-tag>
      </div>

      <div class="card-meta">
        <span>{{ codeGenLabel }}</span>
        <span v-if="showPriority && app.priority !== undefined">优先级 {{ app.priority }}</span>
        <span v-if="showUserId && app.userId">创建者 {{ app.userId }}</span>
      </div>

      <div v-if="actions.length" class="card-actions">
        <a-button
          v-for="action in actions"
          :key="action.key"
          :type="action.variant === 'primary' ? 'primary' : 'default'"
          :danger="action.danger"
          size="small"
          @click="$emit('action', action.key)"
        >
          {{ action.label }}
        </a-button>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatCodeGenType } from '@/utils/codeGenTypes'

export interface AppCardAction {
  key: string
  label: string
  danger?: boolean
  variant?: 'default' | 'primary'
}

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    description?: string
    badge?: string
    actions?: AppCardAction[]
    showPriority?: boolean
    showUserId?: boolean
  }>(),
  {
    description: '',
    badge: '',
    actions: () => [],
    showPriority: false,
    showUserId: false,
  },
)

defineEmits<{
  action: [key: string]
}>()

const codeGenLabel = computed(() => formatCodeGenType(props.app.codeGenType))
</script>

<style scoped>
.app-card {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  overflow: hidden;
  background: rgb(255 255 255 / 74%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 24px;
  box-shadow: var(--card-shadow-soft);
}

.card-visual {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background:
    linear-gradient(140deg, rgb(219 234 254 / 92%), rgb(240 253 250 / 86%)),
    linear-gradient(180deg, #eff6ff, #f8fafc);
}

.card-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.visual-fallback {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 10px;
  width: 100%;
  height: 100%;
  padding: 20px;
  background:
    radial-gradient(circle at top right, rgb(37 99 235 / 22%), transparent 32%),
    linear-gradient(180deg, rgb(255 255 255 / 0%), rgb(15 23 42 / 14%)),
    linear-gradient(135deg, #dbeafe 0%, #ecfeff 100%);
}

.visual-fallback span {
  color: #155eef;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.visual-fallback strong {
  color: #0f172a;
  font-size: 22px;
  line-height: 1.2;
}

.card-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
  padding: 18px 18px 20px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.card-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  line-height: 1.25;
}

.card-head p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

.card-meta span {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  color: #475569;
  font-size: 12px;
  background: rgb(248 250 252 / 90%);
  border-radius: 999px;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
