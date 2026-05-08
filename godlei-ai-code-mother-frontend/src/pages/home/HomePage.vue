<template>
  <div class="home-page page-stack">
    <section class="hero-section">
      <div class="hero-copy">
        <h1>{{ HOME_HERO_TITLE }}</h1>
        <p class="hero-subtitle">{{ HOME_HERO_SUBTITLE }}</p>
      </div>

      <AppPromptComposer
        v-model="createPrompt"
        :loading="createLoading"
        :suggestions="HOME_PROMPT_SUGGESTIONS"
        :placeholder="HOME_PROMPT_PLACEHOLDER"
        submit-text="立即创建"
        @submit="handleCreateApp"
      />
    </section>

    <section class="list-panel glass-card">
      <PageSectionHeader
        class="panel-header"
        eyebrow="My Apps"
        title="我的应用"
        :description="
          isLogin
            ? '继续创作、编辑或删除你自己的应用。'
            : '登录后即可查看并管理自己的应用。'
        "
        title-tag="h2"
        :show-extra="isLogin"
      >
        <template #extra>
          <a-input-search
            v-if="isLogin"
            v-model:value="myQuery.appName"
            class="section-search"
            allow-clear
            placeholder="按应用名称搜索"
            enter-button="搜索"
            @search="handleMySearch"
          />
        </template>
      </PageSectionHeader>

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
            :actions="mineActions"
            clickable
            @select="openDetailModal('mine', app)"
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
      <PageSectionHeader
        class="panel-header"
        eyebrow="Featured Apps"
        title="精选应用"
        description="浏览平台精选案例，直接复用提示词灵感，或打开本地预览看看生成效果。"
        title-tag="h2"
      >
        <template #extra>
          <a-input-search
            v-model:value="featuredQuery.appName"
            class="section-search"
            allow-clear
            placeholder="按应用名称搜索"
            enter-button="搜索"
            @search="handleFeaturedSearch"
          />
        </template>
      </PageSectionHeader>

      <div v-if="featuredLoading" class="card-grid is-loading">
        <a-skeleton v-for="item in 3" :key="item" active class="grid-skeleton" />
      </div>

      <div v-else-if="featuredApps.length" class="card-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          :description="getFeaturedDescription(app)"
          :actions="featuredActions"
          clickable
          @select="openDetailModal('featured', app)"
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

    <AppDetailModal
      :open="detailOpen"
      :app="detailApp"
      :description="detailDescription"
      :preview-url="detailPreviewUrl"
      :actions="detailActions"
      @close="detailOpen = false"
      @action="handleDetailAction"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { addApp, deleteMyApp, listFeaturedAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import AppCard from '@/components/app/AppCard.vue'
import AppDetailModal from '@/components/app/AppDetailModal.vue'
import type { AppActionItem } from '@/components/app/appAction'
import AppEmptyState from '@/components/app/AppEmptyState.vue'
import AppPromptComposer from '@/components/app/AppPromptComposer.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { getStaticPreviewUrl } from '@/config/env'
import {
  HOME_HERO_SUBTITLE,
  HOME_HERO_TITLE,
  HOME_PROMPT_PLACEHOLDER,
  HOME_PROMPT_SUGGESTIONS,
} from '@/pages/home/homeContent'
import { useLoginUserStore } from '@/stores/loginUser'
import { hydrateOwnedAppCreator } from '@/utils/appCard'
import { buildAppNameFromPrompt, formatAppRelativeTime, savePendingAppPrompt } from '@/utils/appHelpers'

type DetailSource = 'mine' | 'featured'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { isLogin } = storeToRefs(loginUserStore)

const HOME_PROMPT_DRAFT_KEY = 'godlei_home_prompt_draft'

const mineActions: AppActionItem[] = [
  { key: 'chat', label: '继续创作', variant: 'primary' },
  { key: 'edit', label: '编辑' },
  { key: 'preview', label: '预览' },
  { key: 'delete', label: '删除', danger: true },
]

const featuredActions: AppActionItem[] = [
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
const detailOpen = ref(false)
const detailApp = ref<API.AppVO | null>(null)
const detailSource = ref<DetailSource>('mine')

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

const detailDescription = computed(() => {
  if (!detailApp.value) {
    return ''
  }

  return detailSource.value === 'mine'
    ? getMineDescription(detailApp.value)
    : getFeaturedDescription(detailApp.value)
})

const detailPreviewUrl = computed(() => {
  if (!detailApp.value?.id || !detailApp.value.codeGenType) {
    return ''
  }

  return getStaticPreviewUrl(detailApp.value.codeGenType, detailApp.value.id)
})

const detailActions = computed(() => {
  return detailSource.value === 'mine' ? mineActions : featuredActions
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
  return `最近更新于 ${formatAppRelativeTime(app.updateTime || app.createTime)}`
}

const getFeaturedDescription = (app: API.AppVO) => {
  return app.initPrompt?.slice(0, 54) || `创建于 ${formatAppRelativeTime(app.createTime)}`
}

const openDetailModal = (source: DetailSource, app: API.AppVO) => {
  detailSource.value = source
  detailApp.value = app
  detailOpen.value = true
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
    myApps.value = (res.data.data.records ?? []).map((item) =>
      hydrateOwnedAppCreator(item, loginUserStore.loginUser),
    )
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

          detailOpen.value = false
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

const handleDetailAction = (action: string) => {
  if (!detailApp.value) {
    return
  }

  const app = detailApp.value
  detailOpen.value = false

  if (detailSource.value === 'mine') {
    handleMineAction(action, app)
    return
  }

  handleFeaturedAction(action, app)
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
  gap: 26px;
}

.hero-section {
  position: relative;
  display: grid;
  gap: 22px;
  padding: clamp(18px, 3.2vw, 34px) clamp(20px, 4vw, 40px) clamp(22px, 3vw, 32px);
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 18%, rgb(147 197 253 / 0.82), transparent 22%),
    radial-gradient(circle at 82% 24%, rgb(96 165 250 / 0.46), transparent 20%),
    radial-gradient(circle at 72% 72%, rgb(103 232 249 / 0.24), transparent 22%),
    linear-gradient(135deg, #f8fbff 0%, #eef5ff 38%, #edf6ff 64%, #f9fcff 100%);
  border-radius: 36px;
  box-shadow: 0 26px 64px rgb(148 163 184 / 18%);
}

.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.55), rgb(255 255 255 / 0.04)),
    linear-gradient(90deg, rgb(148 163 184 / 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgb(148 163 184 / 0.08) 1px, transparent 1px);
  background-size: auto, 48px 48px, 48px 48px;
  mask-image: linear-gradient(180deg, rgb(0 0 0 / 0.9), rgb(0 0 0 / 0.2));
  pointer-events: none;
}

.hero-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  text-align: center;
}

.hero-copy h1 {
  margin: 0;
  color: transparent;
  font-size: clamp(28px, 5.2vw, 60px);
  font-weight: 700;
  letter-spacing: -0.06em;
  line-height: 0.98;
  background: linear-gradient(135deg, #0f172a 0%, #2563eb 38%, #38bdf8 72%, #0f172a 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.hero-subtitle {
  margin: 0;
  color: #475569;
  font-size: clamp(16px, 1.7vw, 20px);
  line-height: 1.7;
}

.list-panel {
  padding: 24px;
  background: rgb(255 255 255 / 82%);
}

.panel-header {
  margin-bottom: 20px;
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
  .hero-section {
    padding: 16px 16px 20px;
    border-radius: 28px;
  }

  .section-search {
    width: 100%;
  }

  .card-grid {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .card-grid :deep(.app-card) {
    width: min(100%, 520px);
  }

  .list-panel {
    padding: 20px;
  }
}
</style>
