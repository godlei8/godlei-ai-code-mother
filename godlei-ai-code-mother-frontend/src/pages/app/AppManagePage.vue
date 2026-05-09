<template>
  <div class="page-stack">
    <section class="glass-card management-filter-panel">
      <a-form layout="inline" :model="queryForm" class="management-filter-form">
        <a-form-item label="应用编号">
          <a-input v-model:value="queryForm.id" placeholder="按编号筛选" />
        </a-form-item>

        <a-form-item label="应用名称">
          <a-input v-model:value="queryForm.appName" placeholder="按名称筛选" />
        </a-form-item>

        <a-form-item label="创建者">
          <a-input v-model:value="queryForm.userId" placeholder="按创建者编号筛选" />
        </a-form-item>

        <a-form-item label="生成模式">
          <a-select
            v-model:value="queryForm.codeGenType"
            allow-clear
            placeholder="全部模式"
            style="width: 180px"
            :options="codeGenTypeOptions"
          />
        </a-form-item>

        <a-form-item label="优先级">
          <a-input-number v-model:value="queryForm.priority" :min="0" placeholder="按优先级筛选" />
        </a-form-item>

        <a-form-item label="部署标识">
          <a-input v-model:value="queryForm.deployKey" placeholder="按 deployKey 筛选" />
        </a-form-item>

        <a-form-item label="初始提示词">
          <a-input v-model:value="queryForm.initPrompt" placeholder="按提示词筛选" />
        </a-form-item>

        <a-form-item label="封面地址">
          <a-input v-model:value="queryForm.cover" placeholder="按封面地址筛选" />
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
        title="应用管理列表"
        description="管理员可以按应用信息筛选，并执行详情查看、编辑、精选和删除操作。"
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
          <template v-if="column.key === 'codeGenType'">
            <a-tag color="blue">{{ formatCodeGenType(record.codeGenType) }}</a-tag>
          </template>

          <template v-else-if="column.key === 'priority'">
            <a-tag :color="record.priority === 99 ? 'gold' : 'default'">
              {{ record.priority ?? 0 }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'deployedTime'">
            {{ formatAppDateTime(record.deployedTime) }}
          </template>

          <template v-else-if="column.key === 'updateTime'">
            {{ formatAppDateTime(record.updateTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openDetailModal(record)">详情</a-button>
              <a-button
                type="link"
                @click="router.push({ path: `/app/edit/${record.id}`, query: { mode: 'admin' } })"
              >
                编辑
              </a-button>
              <a-button
                type="link"
                :disabled="record.priority === 99"
                @click="handleFeature(record)"
              >
                精选
              </a-button>
              <a-button danger type="link" @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import AppDetailModal from '@/components/app/AppDetailModal.vue'
import type { AppActionItem } from '@/components/app/appAction'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { deleteAppAdmin, listAppByPageAdmin, updateAppAdmin } from '@/api/appController'
import { getStaticPreviewUrl } from '@/config/env'
import { formatAppDateTime } from '@/utils/appHelpers'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'

type TableRecord = API.AppVO

const router = useRouter()

const queryForm = reactive<API.AppAdminQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  sortField: 'updateTime',
  sortOrder: 'desc',
  id: undefined,
  appName: '',
  cover: '',
  initPrompt: '',
  codeGenType: undefined,
  deployKey: '',
  priority: undefined,
  userId: undefined,
})

const tableLoading = ref(false)
const records = ref<TableRecord[]>([])
const total = ref(0)
const detailOpen = ref(false)
const detailApp = ref<TableRecord | null>(null)

const codeGenTypeOptions = CODE_GEN_TYPE_OPTIONS

const columns = [
  { title: '编号', dataIndex: 'id', key: 'id', width: 180 },
  { title: '应用名称', dataIndex: 'appName', key: 'appName', ellipsis: true },
  { title: '生成模式', dataIndex: 'codeGenType', key: 'codeGenType', width: 140 },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 110 },
  { title: '创建者', dataIndex: 'userId', key: 'userId', width: 180 },
  { title: '部署标识', dataIndex: 'deployKey', key: 'deployKey', ellipsis: true },
  { title: '部署时间', dataIndex: 'deployedTime', key: 'deployedTime', width: 240 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 240 },
  { title: '操作', key: 'action', width: 240 },
]

const pagination = computed<TablePaginationConfig>(() => ({
  current: queryForm.pageNum,
  pageSize: queryForm.pageSize,
  total: total.value,
  showSizeChanger: true,
  pageSizeOptions: ['20', '50', '100', '200'],
  showTotal: (value) => `共 ${value} 条`,
}))

const detailDescription = computed(() => detailApp.value?.initPrompt?.trim() ?? '')

const detailPreviewUrl = computed(() => {
  if (!detailApp.value?.id || !detailApp.value.codeGenType) {
    return ''
  }

  return getStaticPreviewUrl(detailApp.value.codeGenType, detailApp.value.id)
})

const detailActions = computed<AppActionItem[]>(() => [
  { key: 'edit', label: '编辑', variant: 'primary' },
  {
    key: 'feature',
    label: detailApp.value?.priority === 99 ? '已精选' : '设为精选',
    disabled: detailApp.value?.priority === 99,
  },
  { key: 'delete', label: '删除', danger: true },
])

const loadData = async () => {
  tableLoading.value = true
  try {
    const res = await listAppByPageAdmin(queryForm)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '应用列表加载失败')
      return
    }

    records.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } catch {
    message.error('应用列表加载失败，请稍后重试')
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
  queryForm.id = undefined
  queryForm.appName = ''
  queryForm.cover = ''
  queryForm.initPrompt = ''
  queryForm.codeGenType = undefined
  queryForm.deployKey = ''
  queryForm.priority = undefined
  queryForm.userId = undefined
  void loadData()
}

const handleTableChange = (page: TablePaginationConfig) => {
  queryForm.pageNum = page.current ?? 1
  queryForm.pageSize = page.pageSize ?? 20
  void loadData()
}

const openDetailModal = (record: TableRecord) => {
  detailApp.value = record
  detailOpen.value = true
}

const handleFeature = async (record: TableRecord) => {
  if (!record.id) {
    return
  }

  try {
    const res = await updateAppAdmin({
      id: record.id,
      priority: 99,
    })

    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '设置精选失败')
      return
    }

    detailOpen.value = false
    message.success('应用已设为精选')
    await loadData()
  } catch {
    message.error('设置精选失败，请稍后重试')
  }
}

const handleDelete = (record: TableRecord) => {
  Modal.confirm({
    title: `确认删除应用“${record.appName || '未命名应用'}”吗？`,
    content: '删除后无法恢复，请谨慎操作。',
    okText: '确认删除',
    cancelText: '取消',
    okButtonProps: {
      danger: true,
    },
    onOk: async () => {
      try {
        const res = await deleteAppAdmin({
          id: record.id,
        })

        if (res.data?.code !== 0 || !res.data.data) {
          message.error(res.data?.message || '删除应用失败')
          return
        }

        detailOpen.value = false
        message.success('应用已删除')
        if ((records.value.length ?? 0) === 1 && (queryForm.pageNum ?? 1) > 1) {
          queryForm.pageNum = (queryForm.pageNum ?? 1) - 1
        }
        await loadData()
      } catch {
        message.error('删除应用失败，请稍后重试')
      }
    },
  })
}

const handleDetailAction = (action: string) => {
  if (!detailApp.value) {
    return
  }

  if (action === 'edit') {
    detailOpen.value = false
    void router.push({ path: `/app/edit/${detailApp.value.id}`, query: { mode: 'admin' } })
    return
  }

  if (action === 'feature') {
    void handleFeature(detailApp.value)
    return
  }

  if (action === 'delete') {
    handleDelete(detailApp.value)
  }
}

onMounted(() => {
  void loadData()
})
</script>
