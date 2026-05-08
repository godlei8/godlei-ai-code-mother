<template>
  <div class="page-section-header" :class="{ 'has-extra': hasExtra }">
    <div class="header-copy">
      <p v-if="eyebrow" class="header-eyebrow">{{ eyebrow }}</p>
      <component :is="titleTag" class="header-title">{{ title }}</component>
      <p v-if="description" class="header-description">{{ description }}</p>
    </div>

    <div v-if="hasExtra" class="header-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    eyebrow?: string
    title: string
    description?: string
    titleTag?: 'h1' | 'h2' | 'h3'
    showExtra?: boolean
  }>(),
  {
    eyebrow: '',
    description: '',
    titleTag: 'h2',
    showExtra: true,
  },
)

const slots = useSlots()

const hasExtra = computed(() => props.showExtra && Boolean(slots.extra))
const titleTag = computed(() => props.titleTag)
</script>

<style scoped>
.page-section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.header-copy {
  min-width: 0;
}

.header-eyebrow {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.header-title {
  margin: 0;
  color: #0f172a;
  line-height: 1.12;
  letter-spacing: -0.04em;
}

h1.header-title {
  font-size: clamp(28px, 4vw, 36px);
}

h2.header-title {
  font-size: clamp(24px, 3vw, 30px);
}

h3.header-title {
  font-size: clamp(20px, 2.2vw, 24px);
}

.header-description {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.75;
}

.header-extra {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 10px;
}

@media (max-width: 860px) {
  .page-section-header {
    flex-direction: column;
  }

  .header-extra {
    width: 100%;
  }
}
</style>
