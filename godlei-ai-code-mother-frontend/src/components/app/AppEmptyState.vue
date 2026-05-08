<template>
  <div class="app-empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <a-button v-if="actionText" type="primary" @click="$emit('action')">
      {{ actionText }}
    </a-button>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description: string
    actionText?: string
    icon?: string
  }>(),
  {
    actionText: undefined,
    icon: 'AI',
  },
)

defineEmits<{
  action: []
}>()
</script>

<style scoped>
.app-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 240px;
  padding: 32px 20px;
  text-align: center;
  background: linear-gradient(180deg, rgb(255 255 255 / 84%), rgb(248 250 252 / 92%));
  border: 1px dashed rgb(148 163 184 / 28%);
  border-radius: 24px;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  color: #eff6ff;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.08em;
  background: linear-gradient(135deg, #155eef 0%, #0f766e 100%);
  border-radius: 18px;
  box-shadow: 0 14px 28px rgb(15 23 42 / 12%);
}

.app-empty-state h3 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.app-empty-state p {
  max-width: 420px;
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}
</style>
