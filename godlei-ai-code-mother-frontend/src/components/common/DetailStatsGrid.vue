<template>
  <div class="detail-stats-grid" :style="{ '--detail-stat-min-width': `${minWidth}px` }">
    <article v-for="item in items" :key="item.label" class="detail-stat-card">
      <span>{{ item.label }}</span>
      <strong>{{ formatValue(item.value) }}</strong>
    </article>
  </div>
</template>

<script setup lang="ts">
export interface DetailStatItem {
  label: string
  value?: string | number | null
}

withDefaults(
  defineProps<{
    items: DetailStatItem[]
    minWidth?: number
  }>(),
  {
    minWidth: 180,
  },
)

const formatValue = (value?: string | number | null) => {
  if (value === null || value === undefined) {
    return '-'
  }

  const text = String(value).trim()
  return text || '-'
}
</script>

<style scoped>
.detail-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, var(--detail-stat-min-width)), 1fr));
  gap: 16px;
}

.detail-stat-card {
  padding: 18px;
  background: rgb(255 255 255 / 76%);
  border: 1px solid rgb(148 163 184 / 15%);
  border-radius: 20px;
  box-shadow: var(--card-shadow-soft);
}

.detail-stat-card span {
  display: block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.detail-stat-card strong {
  color: #0f172a;
  line-height: 1.6;
  word-break: break-word;
}
</style>
