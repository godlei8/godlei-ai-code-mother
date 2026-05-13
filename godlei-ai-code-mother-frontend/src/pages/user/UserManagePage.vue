<template>
  <div class="page-stack">
    <section class="glass-card management-filter-panel">
      <a-form layout="inline" :model="queryForm" class="management-filter-form">
        <a-form-item label="用户账号">
          <a-input v-model:value="queryForm.userAccount" placeholder="按账号筛选" />
        </a-form-item>

        <a-form-item label="用户昵称">
          <a-input v-model:value="queryForm.userName" placeholder="按昵称筛选" />
        </a-form-item>

        <a-form-item label="用户角色">
          <a-select
            v-model:value="queryForm.userRole"
            allow-clear
            placeholder="全部角色"
            style="width: 160px"
            :options="roleOptions"
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
        title="用户列表"
        description="管理员可以筛选、新增、编辑和删除用户。"
        title-tag="h3"
      >
        <template #extra>
          <a-button
            v-if="access.canAccessAction(ACCESS_ACTION.USER_CREATE)"
            type="primary"
            @click="openCreateModal"
          >
            新增用户
          </a-button>
        </template>
      </PageSectionHeader>

      <div class="management-table-shell">
        <a-table
          row-key="id"
          :columns="columns"
          :data-source="records"
          :pagination="pagination"
          :loading="tableLoading"
          :scroll="{ x: 1120 }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userAvatar'">
              <a-avatar :src="record.userAvatar">
                {{ (record.userName || record.userAccount || 'U').slice(0, 1).toUpperCase() }}
              </a-avatar>
            </template>

            <template v-else-if="column.key === 'userRole'">
              <a-tag :color="record.userRole === 'admin' ? 'blue' : 'green'">
                {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'createTime'">
              {{ formatAppDateTime(record.createTime) }}
            </template>

            <template v-else-if="column.key === 'action'">
              <a-space class="management-link-actions" size="small">
                <a-button type="link" @click="openEditModal(record)">编辑</a-button>
                <a-button danger type="link" @click="handleDelete(record)">删除</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </section>

    <UserFormModal
      :open="modalOpen"
      :title="modalMode === 'create' ? '新增用户' : '编辑用户'"
      :confirm-text="modalMode === 'create' ? '创建用户' : '保存修改'"
      :loading="submitLoading"
      :mode="modalMode"
      :show-account="modalMode === 'create'"
      :show-role="true"
      :initial-values="modalInitialValues"
      @cancel="modalOpen = false"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ACCESS_ACTION } from '@/access/accessConstants'
import { useAccess } from '@/access/useAccess'
import UserFormModal from '@/components/UserFormModal.vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { addUser, deleteUser, listUserVoByPage, updateUser } from '@/api/userController'
import { formatAppDateTime } from '@/utils/appHelpers'

type TableRecord = API.UserVO
type ModalMode = 'create' | 'edit'

const access = useAccess()

const queryForm = reactive<API.UserQueryRequest>({
  userAccount: '',
  userName: '',
  userRole: undefined,
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const tableLoading = ref(false)
const submitLoading = ref(false)
const records = ref<TableRecord[]>([])
const total = ref(0)
const modalOpen = ref(false)
const modalMode = ref<ModalMode>('create')
const currentEditUser = ref<TableRecord | null>(null)

const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' },
]

const columns = [
  { title: '头像', dataIndex: 'userAvatar', key: 'userAvatar', width: 88 },
  { title: '用户账号', dataIndex: 'userAccount', key: 'userAccount', width: 190 },
  { title: '用户昵称', dataIndex: 'userName', key: 'userName', width: 160 },
  { title: '个人简介', dataIndex: 'userProfile', key: 'userProfile', width: 280, ellipsis: true },
  { title: '用户角色', dataIndex: 'userRole', key: 'userRole', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 210 },
  { title: '操作', key: 'action', width: 140, align: 'center' },
]

const pagination = computed<TablePaginationConfig>(() => ({
  current: queryForm.pageNum,
  pageSize: queryForm.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value) => `共 ${value} 条`,
}))

const modalInitialValues = computed(() => {
  return modalMode.value === 'create'
    ? {
        userRole: 'user',
      }
    : {
        userName: currentEditUser.value?.userName,
        userAvatar: currentEditUser.value?.userAvatar,
        userProfile: currentEditUser.value?.userProfile,
        userRole: currentEditUser.value?.userRole,
      }
})

const loadData = async () => {
  tableLoading.value = true

  try {
    const res = await listUserVoByPage(queryForm)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '用户列表加载失败')
      return
    }

    records.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } catch {
    message.error('用户列表加载失败，请稍后重试')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  void loadData()
}

const handleReset = () => {
  queryForm.userAccount = ''
  queryForm.userName = ''
  queryForm.userRole = undefined
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  void loadData()
}

const handleTableChange = (page: TablePaginationConfig) => {
  queryForm.pageNum = page.current ?? 1
  queryForm.pageSize = page.pageSize ?? 10
  void loadData()
}

const openCreateModal = () => {
  modalMode.value = 'create'
  currentEditUser.value = null
  modalOpen.value = true
}

const openEditModal = (record: TableRecord) => {
  modalMode.value = 'edit'
  currentEditUser.value = record
  modalOpen.value = true
}

const handleSubmit = async (payload: {
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
}) => {
  submitLoading.value = true

  try {
    if (modalMode.value === 'create') {
      const res = await addUser({
        userAccount: payload.userAccount,
        userName: payload.userName,
        userAvatar: payload.userAvatar,
        userProfile: payload.userProfile,
        userRole: payload.userRole,
      })

      if (res.data?.code !== 0 || !res.data.data) {
        message.error(res.data?.message || '新增用户失败')
        return
      }

      message.success('用户创建成功')
    } else if (currentEditUser.value?.id) {
      const res = await updateUser({
        id: currentEditUser.value.id,
        userName: payload.userName,
        userAvatar: payload.userAvatar,
        userProfile: payload.userProfile,
        userRole: payload.userRole,
      })

      if (res.data?.code !== 0 || !res.data.data) {
        message.error(res.data?.message || '更新用户失败')
        return
      }

      message.success('用户信息已更新')
    }

    modalOpen.value = false
    void loadData()
  } catch {
    message.error(modalMode.value === 'create' ? '新增用户失败' : '更新用户失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (record: TableRecord) => {
  Modal.confirm({
    title: `确认删除用户 ${record.userName || record.userAccount || ''} 吗？`,
    content: '删除后无法恢复，请谨慎操作。',
    okText: '确认删除',
    cancelText: '取消',
    okButtonProps: {
      danger: true,
    },
    onOk: async () => {
      try {
        const res = await deleteUser({
          id: record.id,
        })

        if (res.data?.code !== 0 || !res.data.data) {
          message.error(res.data?.message || '删除用户失败')
          return
        }

        message.success('用户已删除')
        if ((records.value.length ?? 0) === 1 && (queryForm.pageNum ?? 1) > 1) {
          queryForm.pageNum = (queryForm.pageNum ?? 1) - 1
        }
        void loadData()
      } catch {
        message.error('删除用户失败，请稍后重试')
      }
    },
  })
}

onMounted(() => {
  void loadData()
})
</script>
