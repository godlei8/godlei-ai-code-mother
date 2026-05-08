<template>
  <div class="auth-card glass-card">
    <div class="auth-card-header">
      <p class="card-tip" :class="`is-${eyebrowTone}`">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p class="card-description">{{ description }}</p>
    </div>

    <div class="auth-card-body">
      <slot />
    </div>

    <div v-if="footerText && footerLinkText && footerLinkTo" class="auth-card-footer">
      <span>{{ footerText }}</span>
      <RouterLink :to="footerLinkTo">{{ footerLinkText }}</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'
import { RouterLink } from 'vue-router'

withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    description: string
    eyebrowTone?: 'primary' | 'success'
    footerText?: string
    footerLinkText?: string
    footerLinkTo?: RouteLocationRaw
  }>(),
  {
    eyebrowTone: 'primary',
    footerText: '',
    footerLinkText: '',
    footerLinkTo: '',
  },
)
</script>

<style scoped>
.auth-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 100%;
  min-height: 100%;
  padding: clamp(30px, 5vw, 46px);
}

.auth-card-header {
  margin-bottom: 28px;
}

.card-tip {
  margin: 0 0 10px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.card-tip.is-primary {
  color: #2563eb;
}

.card-tip.is-success {
  color: #0f766e;
}

h2 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(28px, 4vw, 38px);
}

.card-description {
  margin: 14px 0 0;
  color: #64748b;
  font-size: 15px;
  line-height: 1.75;
}

.auth-card-footer {
  display: flex;
  gap: 8px;
  margin-top: 24px;
  color: #64748b;
}
</style>
