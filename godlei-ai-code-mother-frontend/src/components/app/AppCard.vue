<template>
  <article
    class="app-card"
    :class="{ 'is-clickable': clickable }"
    :role="clickable ? 'button' : undefined"
    :tabindex="clickable ? 0 : undefined"
    @click="handleSelect"
    @keydown.enter.prevent="handleSelect"
    @keydown.space.prevent="handleSelect"
  >
    <div class="card-visual">
      <img v-if="app.cover" :src="app.cover" :alt="profile.title" />
      <div v-else class="visual-fallback">
        <span>{{ codeGenLabel }}</span>
        <strong>{{ profile.title }}</strong>
      </div>
    </div>

    <div class="card-body">
      <div class="card-profile">
        <a-avatar v-if="profile.avatarUrl" :src="profile.avatarUrl" :size="46" />
        <a-avatar v-else class="profile-avatar-fallback" :size="46">
          {{ profile.initials }}
        </a-avatar>

        <div class="profile-copy">
          <h3>{{ profile.title }}</h3>
          <p>{{ profile.creatorName }}</p>
        </div>
      </div>

      <div class="card-meta">
        <span v-if="description">{{ description }}</span>
        <span>{{ codeGenLabel }}</span>
      </div>

      <div v-if="actions.length" class="card-actions">
        <a-button
          v-for="action in actions"
          :key="action.key"
          :type="action.variant === 'primary' ? 'primary' : 'default'"
          :danger="action.danger"
          :disabled="action.disabled"
          size="small"
          @click.stop="emit('action', action.key)"
        >
          {{ action.label }}
        </a-button>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AppActionItem } from '@/components/app/appAction'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { resolveAppCardProfile } from '@/utils/appCard'

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    description?: string
    actions?: AppActionItem[]
    clickable?: boolean
  }>(),
  {
    description: '',
    actions: () => [],
    clickable: false,
  },
)

const emit = defineEmits<{
  action: [key: string]
  select: []
}>()

const codeGenLabel = computed(() => formatCodeGenType(props.app.codeGenType))
const profile = computed(() => resolveAppCardProfile(props.app))

const handleSelect = () => {
  if (!props.clickable) {
    return
  }

  emit('select')
}
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

.app-card.is-clickable {
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.app-card.is-clickable:hover {
  border-color: rgb(37 99 235 / 22%);
  box-shadow: 0 18px 40px rgb(15 23 42 / 10%);
  transform: translateY(-2px);
}

.app-card.is-clickable:focus-visible {
  outline: 2px solid rgb(37 99 235 / 35%);
  outline-offset: 2px;
}

.card-visual {
  position: relative;
  aspect-ratio: 16 / 7.5;
  overflow: hidden;
  background:
    linear-gradient(140deg, rgb(219 234 254 / 92%), rgb(240 253 250 / 86%)),
    linear-gradient(180deg, #eff6ff, #f8fafc);
}

.card-visual img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  padding: 16px 16px 18px;
}

.card-profile {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.profile-avatar-fallback {
  color: #eff6ff;
  font-weight: 700;
  background: linear-gradient(135deg, #155eef 0%, #1244c2 100%);
  box-shadow: 0 10px 18px rgb(37 99 235 / 18%);
}

.profile-copy {
  min-width: 0;
}

.profile-copy h3 {
  margin: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.28;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-copy p {
  margin: 4px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
  margin-top: auto;
}

@media (max-width: 820px) {
  .card-profile {
    align-items: flex-start;
  }
}
</style>
