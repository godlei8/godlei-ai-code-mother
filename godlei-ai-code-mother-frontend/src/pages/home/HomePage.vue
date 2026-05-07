<template>
  <div class="home-page page-stack">
    <section class="hero-section glass-card">
      <div class="hero-copy">
        <p class="hero-eyebrow">GodLei Low-Code AI Platform</p>
        <h1>用 AI 加速应用生成，让低代码平台真正服务业务交付</h1>
        <p class="hero-description">
          GodLei 面向低代码 AI 应用生成场景，串联需求理解、页面搭建、代码产出、权限治理与发布运维，
          帮助团队把业务创意更快沉淀成可上线、可复用、可持续迭代的平台应用。
        </p>

        <div class="hero-actions">
          <a-button
            v-if="!isLogin"
            type="primary"
            size="large"
            @click="router.push('/auth/login')"
          >
            立即登录
          </a-button>
          <a-button
            v-if="!isLogin"
            size="large"
            @click="router.push('/auth/register')"
          >
            创建账号
          </a-button>
          <a-button
            v-if="isLogin"
            type="primary"
            size="large"
            @click="router.push('/user/profile')"
          >
            进入个人中心
          </a-button>
          <a-button
            v-if="isLogin && canAccessRoute('/user/manage')"
            size="large"
            @click="router.push('/user/manage')"
          >
            进入用户管理
          </a-button>
        </div>
      </div>

      <div class="hero-panel">
        <article class="status-card">
          <span>当前访问身份</span>
          <strong>{{ visitorTitle }}</strong>
          <p>{{ visitorDescription }}</p>
        </article>

        <article class="status-card">
          <span>平台能力</span>
          <strong>生成、治理、发布一体化</strong>
          <p>从原型尝试到正式交付，保持统一的应用生成与后台管控体验。</p>
        </article>

        <article class="status-card">
          <span>适用场景</span>
          <strong>内部工具与业务系统快速搭建</strong>
          <p>适合中后台、运营后台、企业内管平台，以及 AI 驱动的业务应用试验场景。</p>
        </article>
      </div>
    </section>

    <section class="feature-section">
      <article
        v-for="item in featureCards"
        :key="item.title"
        class="feature-card glass-card"
      >
        <p class="feature-label">{{ item.label }}</p>
        <h3>{{ item.title }}</h3>
        <p>{{ item.description }}</p>
      </article>
    </section>

    <section class="journey-section glass-card">
      <div class="section-heading">
        <div>
          <p class="section-eyebrow">Platform Journey</p>
          <h2>从需求描述到平台交付，形成完整闭环</h2>
        </div>
        <p class="section-description">
          首页不仅是游客入口，也用于向团队说明 GodLei 平台如何连接 AI 生成、低代码搭建、权限治理与持续交付。
        </p>
      </div>

      <div class="journey-grid">
        <article
          v-for="item in journeySteps"
          :key="item.step"
          class="journey-card"
        >
          <span>{{ item.step }}</span>
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { ACCESS_ROLE, ACCESS_ROLE_LABEL } from '@/access/accessConstants'
import { useAccess } from '@/access/useAccess'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const access = useAccess()
const loginUserStore = useLoginUserStore()
const { isLogin, displayName, accessRole } = storeToRefs(loginUserStore)

const visitorTitle = computed(() => {
  if (!isLogin.value) {
    return '游客访问中'
  }

  return `${displayName.value} · ${ACCESS_ROLE_LABEL[accessRole.value]}`
})

const visitorDescription = computed(() => {
  if (!isLogin.value) {
    return '你当前可以浏览平台首页，并通过登录或注册进入完整的控制台能力。'
  }

  if (accessRole.value === ACCESS_ROLE.ADMIN) {
    return '你当前拥有管理员权限，可以继续进入用户管理与权限管理页面。'
  }

  return '你当前已登录，可进入个人中心查看资料，并使用受权限控制的平台功能。'
})

const featureCards = [
  {
    label: 'AI Generation',
    title: '把需求描述转成页面与代码骨架',
    description:
      '围绕业务目标描述需求，由 AI 协助生成界面结构、页面逻辑与项目骨架，缩短从想法到原型的时间。',
  },
  {
    label: 'Low-Code Collaboration',
    title: '让产品、运营与开发在同一平台协同',
    description:
      '结合低代码配置能力与可扩展代码实现，让不同角色围绕同一套平台产物协作，而不是在多套工具之间来回切换。',
  },
  {
    label: 'Governance',
    title: '把用户、权限与发布流程纳入统一治理',
    description:
      '在生成应用之外，同步建设登录体系、角色权限、后台管理与平台治理能力，让应用更容易进入真实业务环境。',
  },
]

const journeySteps = [
  {
    step: '01',
    title: '描述需求',
    description: '用业务语言说明想要的功能、页面和流程，让平台更快理解应用目标。',
  },
  {
    step: '02',
    title: '生成应用',
    description: '结合 AI 生成能力与低代码配置，快速形成页面、交互和项目基础结构。',
  },
  {
    step: '03',
    title: '接入治理',
    description: '补齐用户体系、权限控制、后台管理等平台能力，让应用具备可运营基础。',
  },
  {
    step: '04',
    title: '持续迭代',
    description: '围绕真实业务反馈不断更新页面、逻辑和平台策略，保持生成能力与系统治理同步演进。',
  },
]

const canAccessRoute = access.canAccessRoute
</script>

<style scoped>
.hero-section {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.85fr);
  gap: 26px;
  padding: clamp(28px, 4vw, 40px);
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-eyebrow,
.section-eyebrow,
.feature-label {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

h1 {
  max-width: 760px;
  margin: 0;
  color: #0f172a;
  font-size: clamp(38px, 5vw, 62px);
  line-height: 1.02;
  letter-spacing: -0.05em;
}

.hero-description,
.section-description,
.feature-card p,
.journey-card p,
.status-card p {
  color: #64748b;
  line-height: 1.8;
}

.hero-description {
  max-width: 760px;
  margin: 20px 0 0;
  font-size: 16px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 26px;
}

.hero-panel {
  display: grid;
  gap: 14px;
}

.status-card {
  padding: 20px 22px;
  background: rgb(255 255 255 / 78%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 20px;
}

.status-card span {
  display: inline-block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.status-card strong,
.feature-card h3,
.journey-card strong,
.section-heading h2 {
  color: #0f172a;
}

.status-card strong {
  display: block;
  font-size: 20px;
  line-height: 1.3;
}

.status-card p {
  margin: 10px 0 0;
  font-size: 14px;
}

.feature-section {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  padding: 24px;
}

.feature-card h3 {
  margin: 0;
  font-size: 22px;
  line-height: 1.3;
}

.feature-card p {
  margin: 14px 0 0;
}

.journey-section {
  padding: 28px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.section-heading h2 {
  max-width: 720px;
  margin: 0;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.1;
  letter-spacing: -0.04em;
}

.section-description {
  max-width: 360px;
  margin: 0;
  font-size: 15px;
}

.journey-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.journey-card {
  padding: 22px;
  background: rgb(255 255 255 / 74%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 20px;
}

.journey-card span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  height: 38px;
  margin-bottom: 14px;
  color: #1244c2;
  font-size: 13px;
  font-weight: 700;
  background: rgb(239 246 255 / 92%);
  border-radius: 999px;
}

.journey-card strong {
  display: block;
  font-size: 18px;
}

.journey-card p {
  margin: 10px 0 0;
}

@media (max-width: 1080px) {
  .hero-section,
  .feature-section,
  .journey-grid {
    grid-template-columns: 1fr;
  }

  .section-heading {
    flex-direction: column;
  }

  .section-description {
    max-width: none;
  }
}
</style>
