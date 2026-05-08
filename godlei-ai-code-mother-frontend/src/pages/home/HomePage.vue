<template>
  <div class="home-page page-stack">
    <section class="hero-section">
      <div class="hero-copy">
        <p class="hero-eyebrow">一句话 · 生所想</p>
        <h1>与 AI 对话，创建属于你的网页应用</h1>
        <p class="hero-description">
          输入一个提示词，系统会自动创建应用、进入生成对话，并在完成后展示网页预览。你还可以继续对话、部署应用、管理自己的作品，或浏览精选案例获得灵感。
        </p>
      </div>

      <AppPromptComposer
        v-model="createPrompt"
        :loading="createLoading"
        :suggestions="promptSuggestions"
        submit-text="立即创建"
        helper-title="描述功能、页面和交互，越具体越容易生成理想结果"
        helper-text="支持多行输入；未登录时会先跳转到登录页，并为你保留当前草稿。"
        @submit="handleCreateApp"
      />
    </section>

    <section class="list-panel glass-card">
      <div class="panel-header">
        <div>
          <p class="section-eyebrow">My Apps</p>
          <h2>我的应用</h2>
          <span>{{ isLogin ? '继续创作、编辑或删除你自己的应用。' : '登录后即可查看并管理自己的应用。' }}</span>
        </div>

        <a-input-search
          v-if="isLogin"
          v-model:value="myQuery.appName"
          class="section-search"
          allow-clear
          placeholder="按应用名称搜索"
          enter-button="搜索"
          @search="handleMySearch"
        />
      </div>

      <AppEmptyState
        v-if="!isLogin"
        title="登录后开始你的第一个应用"
        description="你可以先浏览精选案例，或登录后直接使用上面的提示词输入框创建应用。"
        action-text="去登录"
        icon="GO"
        @action="router.push('/auth/login')"
      />

      <template v-else>
        <div v-if="myLoading" class="card-grid is-loading">
          <a-skeleton v-for="item in 3" :key="item" active class="grid-skeleton" />
        </div>

        <div v-else-if="myApps.length" class="card-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            :description="getMineDescription(app)"
            badge="我的作品"
            :actions="mineActions"
            @action="handleMineAction($event, app)"
          />
        </div>

        <AppEmptyState
          v-else
          title="还没有应用作品"
          description="从上方输入一句话开始，系统会先创建应用，再自动进入生成对话。"
          action-text="去创建"
          icon="NEW"
          @action="scrollToTop"
        />

        <div v-if="myTotal > (myQuery.pageSize ?? 6)" class="pagination-wrap">
          <a-pagination
            :current="myQuery.pageNum"
            :page-size="myQuery.pageSize"
            :total="myTotal"
            @change="handleMyPageChange"
          />
        </div>
      </template>
    </section>

    <section class="list-panel glass-card">
      <div class="panel-header">
        <div>
          <p class="section-eyebrow">Featured Apps</p>
          <h2>精选应用</h2>
          <span>浏览平台精选案例，直接复用提示词灵感，或打开本地预览看看生成效果。</span>
        </div>

        <a-input-search
          v-model:value="featuredQuery.appName"
          class="section-search"
          allow-clear
          placeholder="按应用名称搜索"
          enter-button="搜索"
          @search="handleFeaturedSearch"
        />
      </div>

      <div v-if="featuredLoading" class="card-grid is-loading">
        <a-skeleton v-for="item in 3" :key="item" active class="grid-skeleton" />
      </div>

      <div v-else-if="featuredApps.length" class="card-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :description="getFeaturedDescription(app)"
          badge="精选"
          :actions="featuredActions"
          @action="handleFeaturedAction($event, app)"
        />
      </div>

      <AppEmptyState
        v-else
        title="当前还没有精选应用"
        description="稍后再回来看看，或者先创建自己的应用并等待管理员推荐。"
        icon="TOP"
      />

      <div v-if="featuredTotal > (featuredQuery.pageSize ?? 6)" class="pagination-wrap">
        <a-pagination
          :current="featuredQuery.pageNum"
          :page-size="featuredQuery.pageSize"
          :total="featuredTotal"
          @change="handleFeaturedPageChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import AppCard, { type AppCardAction } from '@/components/app/AppCard.vue'
import AppEmptyState from '@/components/app/AppEmptyState.vue'
import AppPromptComposer from '@/components/app/AppPromptComposer.vue'
import { addApp, deleteMyApp, listFeaturedAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { getStaticPreviewUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import { buildAppNameFromPrompt, formatAppRelativeTime, savePendingAppPrompt } from '@/utils/appHelpers'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { isLogin } = storeToRefs(loginUserStore)

const HOME_PROMPT_DRAFT_KEY = 'godlei_home_prompt_draft'

const promptSuggestions = [
  '做一个电商首页，突出爆款推荐、活动专区和下单入口',
  '帮我生成一个企业官网，包含产品介绍、案例展示和联系表单',
  '创建一个个人博客，支持文章列表、详情页和关于我页面',
  '做一个后台管理首页，展示统计卡片、趋势图和快捷操作',
]

const mineActions: AppCardAction[] = [
  { key: 'chat', label: '继续创作', variant: 'primary' },
  { key: 'edit', label: '编辑' },
  { key: 'preview', label: '预览' },
  { key: 'delete', label: '删除', danger: true },
]

const featuredActions: AppCardAction[] = [
  { key: 'reuse', label: '复用提示词', variant: 'primary' },
  { key: 'preview', label: '打开预览' },
]

const createPrompt = ref('')
const createLoading = ref(false)
const myLoading = ref(false)
const featuredLoading = ref(false)
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myTotal = ref(0)
const featuredTotal = ref(0)

const myQuery = reactive<API.AppListPageRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'updateTime',
  sortOrder: 'desc',
  appName: '',
})

const featuredQuery = reactive<API.AppListPageRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'priority',
  sortOrder: 'desc',
  appName: '',
})

const persistPromptDraft = (value: string) => {
  if (typeof window === 'undefined') {
    return
  }
  if (value.trim()) {
    window.sessionStorage.setItem(HOME_PROMPT_DRAFT_KEY, value)
  } else {
    window.sessionStorage.removeItem(HOME_PROMPT_DRAFT_KEY)
  }
}

const readPromptDraft = () => {
  if (typeof window === 'undefined') {
    return ''
  }
  return window.sessionStorage.getItem(HOME_PROMPT_DRAFT_KEY) ?? ''
}

const clearPromptDraft = () => {
  if (typeof window === 'undefined') {
    return
  }
  window.sessionStorage.removeItem(HOME_PROMPT_DRAFT_KEY)
}

const getMineDescription = (app: API.AppVO) => {
  return `更新于 ${formatAppRelativeTime(app.updateTime || app.createTime)}`
}

const getFeaturedDescription = (app: API.AppVO) => {
  return app.initPrompt?.slice(0, 54) || `创建于 ${formatAppRelativeTime(app.createTime)}`
}

const openPreview = (app: API.AppVO) => {
  if (!app.id || !app.codeGenType) {
    message.warning('当前应用还没有可用的预览资源')
    return
  }
  window.open(getStaticPreviewUrl(app.codeGenType, app.id), '_blank', 'noopener,noreferrer')
}

const loadFeaturedApps = async () => {
  featuredLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage(featuredQuery)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '精选应用加载失败')
      return
    }
    featuredApps.value = res.data.data.records ?? []
    featuredTotal.value = res.data.data.totalRow ?? 0
  } catch {
    message.error('精选应用加载失败，请稍后重试')
  } finally {
    featuredLoading.value = false
  }
}

const loadMyApps = async () => {
  if (!isLogin.value) {
    myApps.value = []
    myTotal.value = 0
    return
  }

  myLoading.value = true
  try {
    const res = await listMyAppVoByPage(myQuery)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '我的应用加载失败')
      return
    }
    myApps.value = res.data.data.records ?? []
    myTotal.value = res.data.data.totalRow ?? 0
  } catch {
    message.error('我的应用加载失败，请稍后重试')
  } finally {
    myLoading.value = false
  }
}

const handleCreateApp = async () => {
  const prompt = createPrompt.value.trim()
  if (!prompt) {
    message.warning('先输入一句话描述你想创建的应用')
    return
  }

  if (!isLogin.value) {
    persistPromptDraft(prompt)
    message.info('请先登录，当前提示词已经为你保留')
    void router.push({
      path: '/auth/login',
      query: {
        redirect: '/',
      },
    })
    return
  }

  createLoading.value = true
  try {
    const res = await addApp({
      appName: buildAppNameFromPrompt(prompt),
      initPrompt: prompt,
    })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '创建应用失败')
      return
    }

    savePendingAppPrompt(res.data.data, prompt)
    clearPromptDraft()
    createPrompt.value = ''
    message.success('应用创建成功，正在进入生成对话')
    await router.push(`/app/chat/${res.data.data}`)
  } catch {
    message.error('创建应用失败，请稍后重试')
  } finally {
    createLoading.value = false
  }
}

const handleMineAction = (action: string, app: API.AppVO) => {
  if (!app.id) {
    return
  }

  if (action === 'chat') {
    void router.push(`/app/chat/${app.id}`)
    return
  }

  if (action === 'edit') {
    void router.push(`/app/edit/${app.id}`)
    return
  }

  if (action === 'preview') {
    openPreview(app)
    return
  }

  if (action === 'delete') {
    Modal.confirm({
      title: `确认删除应用“${app.appName || '未命名应用'}”吗？`,
      content: '删除后无法恢复，相关生成结果也将不可继续编辑。',
      okText: '确认删除',
      cancelText: '取消',
      okButtonProps: {
        danger: true,
      },
      onOk: async () => {
        try {
          const res = await deleteMyApp({
            id: app.id,
          })

          if (res.data?.code !== 0 || !res.data.data) {
            message.error(res.data?.message || '删除应用失败')
            return
          }

          message.success('应用已删除')
          if ((myApps.value.length ?? 0) === 1 && (myQuery.pageNum ?? 1) > 1) {
            myQuery.pageNum = (myQuery.pageNum ?? 1) - 1
          }
          await loadMyApps()
        } catch {
          message.error('删除应用失败，请稍后重试')
        }
      },
    })
  }
}

const handleFeaturedAction = (action: string, app: API.AppVO) => {
  if (action === 'reuse') {
    createPrompt.value = app.initPrompt || app.appName || ''
    persistPromptDraft(createPrompt.value)
    scrollToTop()
    return
  }

  if (action === 'preview') {
    openPreview(app)
  }
}

const handleMySearch = () => {
  myQuery.pageNum = 1
  void loadMyApps()
}

const handleFeaturedSearch = () => {
  featuredQuery.pageNum = 1
  void loadFeaturedApps()
}

const handleMyPageChange = (page: number) => {
  myQuery.pageNum = page
  void loadMyApps()
}

const handleFeaturedPageChange = (page: number) => {
  featuredQuery.pageNum = page
  void loadFeaturedApps()
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}

watch(createPrompt, (value) => {
  persistPromptDraft(value)
})

watch(
  isLogin,
  (value) => {
    if (value) {
      void loadMyApps()
    } else {
      myApps.value = []
      myTotal.value = 0
    }
  },
  {
    immediate: true,
  },
)

onMounted(() => {
  createPrompt.value = readPromptDraft()
  void loadFeaturedApps()
})
</script>

<style scoped>
.home-page {
  gap: 24px;
}

.hero-section {
  display: grid;
  gap: 22px;
}

.hero-copy {
  max-width: 920px;
  padding: 16px 4px 0;
}

.hero-eyebrow,
.section-eyebrow {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-copy h1,
.panel-header h2 {
  margin: 0;
  color: #0f172a;
  letter-spacing: -0.04em;
}

.hero-copy h1 {
  font-size: clamp(40px, 5vw, 68px);
  line-height: 1.03;
}

.hero-description {
  max-width: 860px;
  margin: 18px 0 0;
  color: #64748b;
  font-size: 16px;
  line-height: 1.9;
}

.list-panel {
  padding: 24px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.panel-header h2 {
  font-size: 30px;
  line-height: 1.15;
}

.panel-header span {
  display: inline-block;
  margin-top: 10px;
  color: #64748b;
  line-height: 1.75;
}

.section-search {
  width: min(320px, 100%);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.card-grid.is-loading {
  align-items: stretch;
}

.grid-skeleton {
  padding: 24px;
  background: rgb(255 255 255 / 74%);
  border-radius: 24px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .panel-header {
    flex-direction: column;
  }

  .section-search {
    width: 100%;
  }

  .card-grid {
    grid-template-columns: 1fr;
  }

  .list-panel {
    padding: 20px;
  }
}
</style>
