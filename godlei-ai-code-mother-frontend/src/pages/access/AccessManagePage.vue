<template>
  <div class="page-stack">
    <section class="glass-card role-overview">
      <PageSectionHeader
        title="角色总览"
        description="当前前端权限系统以 guest / user / admin 为核心角色，并统一驱动路由与动作控制。"
        title-tag="h3"
      />

      <div class="role-grid">
        <article v-for="item in roleCards" :key="item.key" class="role-card">
          <span>{{ item.key }}</span>
          <strong>{{ item.label }}</strong>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>

    <section class="glass-card management-table-panel">
      <PageSectionHeader
        class="management-section-header"
        title="页面访问矩阵"
        description="页面权限读取与路由守卫共用同一份 access 配置，避免说明页和真实行为不一致。"
        title-tag="h3"
      />

      <div class="management-table-shell">
        <a-table
          :columns="matrixColumns"
          :data-source="routeRows"
          :pagination="false"
          :scroll="{ x: 860 }"
          row-key="key"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'guest' || column.key === 'user' || column.key === 'admin'">
              <a-tag :color="record[column.key] ? 'green' : 'default'">
                {{ record[column.key] ? '允许' : '禁止' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </div>
    </section>

    <section class="glass-card management-table-panel">
      <PageSectionHeader
        class="management-section-header"
        title="动作权限矩阵"
        description="按钮级权限和页面可见性已经解耦，未来后端返回更细粒度权限码时只需替换适配层。"
        title-tag="h3"
      />

      <div class="management-table-shell">
        <a-table
          :columns="matrixColumns"
          :data-source="actionRows"
          :pagination="false"
          :scroll="{ x: 860 }"
          row-key="key"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'guest' || column.key === 'user' || column.key === 'admin'">
              <a-tag :color="record[column.key] ? 'green' : 'default'">
                {{ record[column.key] ? '允许' : '禁止' }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PageSectionHeader from '@/components/common/PageSectionHeader.vue'
import { ACCESS_ACTION_ENTRIES, ACCESS_ROUTE_ENTRIES, canRoleAccessRole } from '@/access/accessConfig'
import { ACCESS_ROLE, ACCESS_ROLE_LABEL } from '@/access/accessConstants'

const roleCards = [
  {
    key: ACCESS_ROLE.GUEST,
    label: ACCESS_ROLE_LABEL[ACCESS_ROLE.GUEST],
    description: '未登录状态，只能访问登录与注册页。',
  },
  {
    key: ACCESS_ROLE.USER,
    label: ACCESS_ROLE_LABEL[ACCESS_ROLE.USER],
    description: '登录后的普通用户，可访问工作台与个人中心。',
  },
  {
    key: ACCESS_ROLE.ADMIN,
    label: ACCESS_ROLE_LABEL[ACCESS_ROLE.ADMIN],
    description: '管理员拥有用户管理与权限管理等完整后台入口。',
  },
]

const matrixColumns = [
  { title: '权限项', dataIndex: 'label', key: 'label', width: 220 },
  { title: '说明', dataIndex: 'description', key: 'description', width: 360 },
  { title: '游客', dataIndex: 'guest', key: 'guest', width: 100 },
  { title: '普通用户', dataIndex: 'user', key: 'user', width: 120 },
  { title: '管理员', dataIndex: 'admin', key: 'admin', width: 100 },
]

const routeRows = computed(() => {
  return ACCESS_ROUTE_ENTRIES.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.description,
    guest: canRoleAccessRole(ACCESS_ROLE.GUEST, item.access),
    user: canRoleAccessRole(ACCESS_ROLE.USER, item.access),
    admin: canRoleAccessRole(ACCESS_ROLE.ADMIN, item.access),
  }))
})

const actionRows = computed(() => {
  return ACCESS_ACTION_ENTRIES.map((item) => ({
    key: item.key,
    label: item.label,
    description: item.description,
    guest: canRoleAccessRole(ACCESS_ROLE.GUEST, item.access),
    user: canRoleAccessRole(ACCESS_ROLE.USER, item.access),
    admin: canRoleAccessRole(ACCESS_ROLE.ADMIN, item.access),
  }))
})
</script>

<style scoped>
.role-overview {
  padding: 24px;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.role-card {
  padding: 22px;
  background: rgb(255 255 255 / 72%);
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 20px;
}

.role-card span {
  display: inline-block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.role-card strong {
  display: block;
  color: #0f172a;
  font-size: 20px;
}

.role-card p {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.75;
}

@media (max-width: 980px) {
  .role-grid {
    grid-template-columns: 1fr;
  }
}
</style>
