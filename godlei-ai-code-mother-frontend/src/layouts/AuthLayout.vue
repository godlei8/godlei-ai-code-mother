<template>
  <div class="auth-layout">
    <div class="auth-shell">
      <section class="auth-intro glass-card">
        <p class="eyebrow">GodLei Low-Code AI Platform</p>
        <h1>把想法交给 AI，把应用落到业务现场</h1>
        <p class="description">
          GodLei 聚焦低代码 AI 应用生成，从需求理解、页面搭建到代码产出与平台治理，
          帮助团队更快把业务创意沉淀成可上线、可持续迭代的应用系统。
        </p>

        <div class="intro-grid">
          <div class="intro-card">
            <strong>AI 驱动生成</strong>
            <span>通过自然语言描述需求，快速生成页面、逻辑与项目骨架，让原型到产品更顺滑。</span>
          </div>
          <div class="intro-card">
            <strong>低代码高效率</strong>
            <span>结合可视化配置与代码能力，让产品、运营与开发在同一平台高效协同落地。</span>
          </div>
          <div class="intro-card">
            <strong>发布与治理一体化</strong>
            <span>统一管理用户、权限与平台能力，支撑应用生成后的交付、运营与持续迭代。</span>
          </div>
        </div>
      </section>

      <section class="auth-content">
        <RouterView />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<style scoped>
.auth-layout {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
}

.auth-shell {
  display: grid;
  grid-template-columns: minmax(320px, 1.15fr) minmax(320px, 0.95fr);
  gap: 22px;
  width: min(1180px, 100%);
}

.auth-intro,
.auth-content {
  min-height: 720px;
}

.auth-intro {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: clamp(30px, 5vw, 48px);
}

.eyebrow {
  margin: 0 0 14px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

h1 {
  max-width: 560px;
  margin: 0;
  color: #0f172a;
  font-size: clamp(34px, 5vw, 56px);
  line-height: 1.04;
  letter-spacing: -0.04em;
}

.description {
  max-width: 580px;
  margin: 18px 0 0;
  color: #475569;
  font-size: 17px;
  line-height: 1.8;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.intro-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  background: rgb(255 255 255 / 72%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 20px;
}

.intro-card strong {
  color: #0f172a;
  font-size: 15px;
}

.intro-card span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.auth-content {
  display: flex;
  align-items: stretch;
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-intro,
  .auth-content {
    min-height: auto;
  }

  .intro-grid {
    grid-template-columns: 1fr;
  }
}
</style>
