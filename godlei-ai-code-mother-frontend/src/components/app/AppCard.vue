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
    <div class="card-stage">
      <div class="card-visual-frame">
        <div class="card-visual">
          <img
            v-if="coverUrl"
            :src="coverUrl"
            :alt="profile.title"
            @error="coverLoadFailed = true"
          />
          <div v-else class="visual-fallback">
            <span>{{ codeGenLabel }}</span>
            <strong>{{ profile.title }}</strong>
          </div>
        </div>

        <div v-if="primaryActions.length" class="visual-actions">
          <div class="visual-actions-inner">
            <a-button
              v-for="action in primaryActions"
              :key="action.key"
              class="visual-action-button"
              :type="action.variant === 'primary' ? 'primary' : 'default'"
              :danger="action.danger"
              :disabled="action.disabled"
              @click.stop="emit('action', action.key)"
            >
              {{ action.label }}
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <div class="card-content">
      <h3 class="card-title">{{ profile.title }}</h3>

      <div class="card-profile">
        <div class="profile-main">
          <a-avatar v-if="profile.avatarUrl" :src="profile.avatarUrl" :size="40" />
          <a-avatar v-else class="profile-avatar-fallback" :size="40">
            {{ profile.initials }}
          </a-avatar>

          <div class="profile-copy">
            <h3>{{ profile.title }}</h3>
            <p>{{ profile.creatorName }}</p>
          </div>
        </div>

        <div v-if="utilityActions.length" class="profile-side-actions">
          <a-button
            v-for="action in utilityActions"
            :key="action.key"
            class="profile-side-action-button"
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

      <div class="card-meta">
        <span v-if="description">{{ description }}</span>
        <span>{{ codeGenLabel }}</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { AppActionItem } from '@/components/app/appAction'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { resolveAppCardProfile } from '@/utils/appCard'
import { getRenderableMediaUrl } from '@/utils/media'

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

const overlayActionKeys = new Set(['chat', 'edit', 'reuse'])
const utilityActionKeys = new Set(['preview', 'delete'])
const coverLoadFailed = ref(false)

const codeGenLabel = computed(() => formatCodeGenType(props.app.codeGenType))
const profile = computed(() => resolveAppCardProfile(props.app))
const coverUrl = computed(() => getRenderableMediaUrl(props.app.cover, coverLoadFailed.value))
const primaryActions = computed(() =>
  props.actions.filter((action) => overlayActionKeys.has(action.key)).slice(0, 2),
)
const utilityActions = computed(() => props.actions.filter((action) => utilityActionKeys.has(action.key)))

watch(
  () => props.app.cover,
  () => {
    coverLoadFailed.value = false
  },
)

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
  gap: 10px;
  min-height: 100%;
  padding: 12px 12px 14px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgb(244 248 255 / 96%), rgb(255 255 255 / 98%)),
    linear-gradient(135deg, rgb(191 219 254 / 25%), rgb(255 255 255 / 0%));
  border: 1px solid rgb(147 197 253 / 32%);
  border-radius: 28px;
  box-shadow:
    0 18px 42px rgb(148 163 184 / 14%),
    inset 0 1px 0 rgb(255 255 255 / 65%);
}

.app-card.is-clickable {
  cursor: pointer;
  transition:
    transform 0.24s ease,
    box-shadow 0.24s ease,
    border-color 0.24s ease;
}

.app-card.is-clickable:hover {
  border-color: rgb(59 130 246 / 34%);
  box-shadow:
    0 24px 56px rgb(37 99 235 / 16%),
    inset 0 1px 0 rgb(255 255 255 / 72%);
  transform: translateY(-4px);
}

.app-card.is-clickable:focus-visible {
  outline: 2px solid rgb(37 99 235 / 35%);
  outline-offset: 2px;
}

.card-stage {
  padding: 1px;
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, rgb(96 165 250 / 28%), transparent 34%),
    linear-gradient(180deg, rgb(255 255 255 / 88%), rgb(226 232 240 / 56%));
}

.card-visual-frame {
  position: relative;
  padding: 12px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 92%), rgb(241 245 249 / 86%)),
    linear-gradient(135deg, rgb(191 219 254 / 25%), rgb(255 255 255 / 0%));
  border-radius: 20px;
  box-shadow:
    inset 0 1px 0 rgb(255 255 255 / 88%),
    0 18px 36px rgb(148 163 184 / 22%);
}

.card-visual {
  position: relative;
  aspect-ratio: 16 / 9.6;
  overflow: hidden;
  background:
    radial-gradient(circle at 82% 20%, rgb(96 165 250 / 26%), transparent 20%),
    linear-gradient(140deg, rgb(219 234 254 / 96%), rgb(240 249 255 / 92%)),
    linear-gradient(180deg, #eff6ff, #f8fafc);
  border-radius: 16px;
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
  gap: 8px;
  width: 100%;
  height: 100%;
  padding: 18px;
  background:
    radial-gradient(circle at top right, rgb(37 99 235 / 24%), transparent 32%),
    linear-gradient(180deg, rgb(255 255 255 / 0%), rgb(15 23 42 / 18%)),
    linear-gradient(135deg, #dbeafe 0%, #ecfeff 100%);
}

.visual-fallback span {
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.visual-fallback strong {
  color: #0f172a;
  font-size: clamp(18px, 1.25vw, 24px);
  line-height: 1.22;
  text-shadow: 0 2px 10px rgb(255 255 255 / 42%);
}

.visual-actions {
  position: absolute;
  inset: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateY(8px);
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
  z-index: 1;
}

.visual-actions::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgb(15 23 42 / 8%), rgb(15 23 42 / 18%));
  backdrop-filter: blur(3px);
  border-radius: 16px;
}

.visual-actions-inner {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  width: min(252px, 100%);
}

.app-card:hover .visual-actions,
.app-card:focus-visible .visual-actions,
.app-card:focus-within .visual-actions {
  opacity: 1;
  transform: translateY(0);
}

.visual-action-button {
  height: 36px;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgb(15 23 42 / 12%);
  position: relative;
}

.card-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  padding: 2px 4px 0;
}

.card-title {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-profile {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  min-width: 0;
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 10px;
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
  display: none;
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
  margin: 0;
  overflow: hidden;
  color: #475569;
  font-size: 12px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-side-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 6px;
}

.profile-side-action-button {
  min-width: 0;
  height: 28px;
  padding: 0 10px;
  font-size: 12px;
  border-radius: 10px;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-meta span {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  color: #475569;
  font-size: 11px;
  background: rgb(248 250 252 / 95%);
  border: 1px solid rgb(226 232 240 / 92%);
  border-radius: 999px;
}

@media (max-width: 1200px) {
  .card-title {
    font-size: 17px;
  }

  .visual-fallback strong {
    font-size: 21px;
  }
}

@media (max-width: 820px) {
  .app-card {
    padding: 12px 12px 14px;
  }

  .card-visual-frame {
    padding: 12px;
  }

  .visual-actions-inner {
    width: min(228px, 100%);
  }

  .card-profile {
    align-items: center;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .card-title {
    font-size: 16px;
  }

  .profile-side-actions {
    width: auto;
    justify-content: flex-start;
    margin-left: 2px;
  }
}

@media (max-width: 640px) {
  .visual-actions-inner {
    grid-template-columns: 1fr;
  }

  .visual-action-button {
    width: 100%;
  }
}
</style>
