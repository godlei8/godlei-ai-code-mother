<template>
  <div class="page-stack">
    <section class="glass-card management-filter-panel">
      <a-form layout="inline" :model="queryForm" class="management-filter-form">
        <a-form-item label="应用编号">
          <a-input v-model:value="queryForm.appId" placeholder="按应用编号筛选" />
        </a-form-item>

        <a-form-item label="创建者">
          <a-input v-model:value="queryForm.userId" placeholder="按用户编号筛选" />
        </a-form-item>

        <a-form-item label="消息类型">
          <a-select
            v-model:value="queryForm.messageType"
            allow-clear
            placeholder="全部类型"
            style="width: 180px"
            :options="messageTypeOptions"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="glass-card management-table-panel">
      <PageSectionHeader
        class="management-section-header"
        title="对话管理列表"
        description="管理员可以按应用、创建者和消息类型筛选全站对话历史，并快速查看消息详情。"
        title-tag="h3"
      />

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="records"
        :pagination="pagination"
        :loading="tableLoading"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag :color="resolveMessageTypeColor(record.messageType)">
              {{ resolveMessageTypeLabel(record.messageType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'message'">
            <span class="message-cell">{{ record.message || '-' }}</span>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatAppDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openDetailModal(record)">详情</a-button>
              <a-button type="link" :disabled="!record.appId" @click="openAppChat(record)">
                查看应用
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal
      v-model:open="detailOpen"
      title="对话详情"
      width="760px"
      :footer="null"
      destroy-on-close
    >
      <div v-if="currentDetail" class="detail-layout">
        <DetailStatsGrid :items="detailItems" :min-width="160" />

        <section class="detail-message-section">
          <PageSectionHeader
            title="消息内容"
            :description="`当前记录来自 ${currentDetail.appName || `应用 #${currentDetail.appId ?? '-'}`}`"
            title-tag="h3"
          />

          <div class="detail-message-card">
            <p class="detail-message-body">{{ currentDetail.message || '-' }}</p>
          </div>
        </section>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import DetailStatsGrid from '@/components/common/DetailStatsGrid.vue'
import type { DetailStatItem } from '@/components/common/DetailStatsGrid.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { listByPageAdmin } from '@/api/chatHistoryController'
import { formatAppDateTime } from '@/utils/appHelpers'

type TableRecord = API.ChatHistoryAdminVO

const router = useRouter()

const queryForm = reactive<API.ChatHistoryAdminQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  sortField: 'createTime',
  sortOrder: 'desc',
  appId: undefined,
  userId: undefined,
  messageType: undefined,
})

const tableLoading = ref(false)
const records = ref<TableRecord[]>([])
const total = ref(0)
const detailOpen = ref(false)
const currentDetail = ref<TableRecord | null>(null)

const messageTypeOptions = [
  { label: '用户消息', value: 'user' },
  { label: 'AI 回复', value: 'ai' },
  { label: 'AI 错误', value: 'ai_error' },
]

const columns = [
  { title: '记录编号', dataIndex: 'id', key: 'id', width: 180 },
  { title: '所属应用', dataIndex: 'appName', key: 'appName', ellipsis: true, width: 220 },
  { title: '应用编号', dataIndex: 'appId', key: 'appId', width: 160 },
  { title: '创建者', dataIndex: 'userId', key: 'userId', width: 160 },
  { title: '消息类型', dataIndex: 'messageType', key: 'messageType', width: 120 },
  { title: '消息内容', dataIndex: 'message', key: 'message', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 240 },
  { title: '操作', key: 'action', width: 180 },
]

const pagination = computed<TablePaginationConfig>(() => ({
  current: queryForm.pageNum,
  pageSize: queryForm.pageSize,
  total: total.value,
  showSizeChanger: true,
  pageSizeOptions: ['20', '50', '100'],
  showTotal: (value) => `共 ${value} 条`,
}))

const detailItems = computed<DetailStatItem[]>(() => [
  { label: '记录编号', value: currentDetail.value?.id },
  { label: '所属应用', value: currentDetail.value?.appName || `#${currentDetail.value?.appId ?? '-'}` },
  { label: '应用编号', value: currentDetail.value?.appId },
  { label: '创建者', value: currentDetail.value?.userId },
  { label: '消息类型', value: resolveMessageTypeLabel(currentDetail.value?.messageType) },
  { label: '创建时间', value: formatAppDateTime(currentDetail.value?.createTime) },
])

const resolveMessageTypeLabel = (value?: string) => {
  if (value === 'user') {
    return '用户消息'
  }
  if (value === 'ai') {
    return 'AI 回复'
  }
  if (value === 'ai_error') {
    return 'AI 错误'
  }
  return value || '-'
}

const resolveMessageTypeColor = (value?: string) => {
  if (value === 'user') {
    return 'blue'
  }
  if (value === 'ai') {
    return 'green'
  }
  if (value === 'ai_error') {
    return 'red'
  }
  return 'default'
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const res = await listByPageAdmin(queryForm)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '对话列表加载失败')
      return
    }

    records.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } catch {
    message.error('对话列表加载失败，请稍后重试')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  void loadData()
}

const handleReset = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = 20
  queryForm.appId = undefined
  queryForm.userId = undefined
  queryForm.messageType = undefined
  void loadData()
}

const handleTableChange = (page: TablePaginationConfig) => {
  queryForm.pageNum = page.current ?? 1
  queryForm.pageSize = page.pageSize ?? 20
  void loadData()
}

const openDetailModal = (record: TableRecord) => {
  currentDetail.value = record
  detailOpen.value = true
}

const openAppChat = (record: TableRecord) => {
  if (!record.appId) {
    return
  }

  void router.push(`/app/chat/${record.appId}`)
}

onMounted(() => {
  void loadData()
})
</script>

<style scoped>
.message-cell {
  display: inline-block;
  width: 100%;
  color: #334155;
}

.detail-layout {
  display: grid;
  gap: 18px;
}

.detail-message-section {
  display: grid;
  gap: 16px;
}

.detail-message-card {
  padding: 18px 20px;
  background: rgb(248 250 252 / 88%);
  border: 1px solid rgb(148 163 184 / 14%);
  border-radius: 20px;
}

.detail-message-body {
  margin: 0;
  color: #0f172a;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
