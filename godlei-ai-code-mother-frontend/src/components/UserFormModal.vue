<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="loading"
    :ok-text="confirmText"
    cancel-text="取消"
    destroy-on-close
    @cancel="handleCancel"
    @ok="handleOk"
  >
    <a-form
      ref="formRef"
      layout="vertical"
      :model="formState"
    >
      <a-alert
        v-if="mode === 'create'"
        type="info"
        show-icon
        class="modal-alert"
        message="新用户默认密码为 12345678"
      />

      <a-alert
        v-if="mode === 'profile' && profileMode === 'placeholder'"
        type="warning"
        show-icon
        class="modal-alert"
        message="当前为前端兼容模式，普通用户提交后会先保存为本地草稿。"
      />

      <a-form-item
        v-if="showAccount"
        label="用户账号"
        name="userAccount"
        :rules="accountRules"
      >
        <a-input
          v-model:value="formState.userAccount"
          :disabled="readonlyAccount"
          placeholder="请输入用户账号"
          autocomplete="off"
        />
      </a-form-item>

      <a-form-item
        label="用户昵称"
        name="userName"
        :rules="[{ required: true, message: '请输入用户昵称' }]"
      >
        <a-input
          v-model:value="formState.userName"
          placeholder="请输入用户昵称"
          autocomplete="off"
        />
      </a-form-item>

      <a-form-item label="头像地址" name="userAvatar">
        <a-input
          v-model:value="formState.userAvatar"
          placeholder="请输入头像 URL，可留空"
          autocomplete="off"
        />
      </a-form-item>

      <a-form-item label="个人简介" name="userProfile">
        <a-textarea
          v-model:value="formState.userProfile"
          :auto-size="{ minRows: 3, maxRows: 5 }"
          placeholder="请输入个人简介"
        />
      </a-form-item>

      <a-form-item
        v-if="showRole"
        label="用户角色"
        name="userRole"
        :rules="[{ required: true, message: '请选择用户角色' }]"
      >
        <a-select
          v-model:value="formState.userRole"
          :options="roleOptions"
          placeholder="请选择角色"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'

type UserFormState = Pick<
  API.UserAddRequest & API.UserUpdateRequest,
  'userName' | 'userAccount' | 'userAvatar' | 'userProfile' | 'userRole'
>

const props = withDefaults(
  defineProps<{
    open: boolean
    title: string
    loading?: boolean
    confirmText?: string
    mode?: 'create' | 'edit' | 'profile'
    showRole?: boolean
    showAccount?: boolean
    readonlyAccount?: boolean
    profileMode?: 'persisted' | 'placeholder'
    initialValues?: Partial<UserFormState>
  }>(),
  {
    loading: false,
    confirmText: '保存',
    mode: 'edit',
    showRole: true,
    showAccount: true,
    readonlyAccount: false,
    profileMode: 'persisted',
    initialValues: () => ({}),
  },
)

const emit = defineEmits<{
  submit: [payload: UserFormState]
  cancel: []
}>()

const formRef = ref<FormInstance>()
const formState = reactive<UserFormState>({
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' },
]

const accountRules = computed(() => {
  if (!props.showAccount) {
    return []
  }

  return [
    { required: true, message: '请输入用户账号' },
    { min: 4, message: '账号长度不能少于 4 位' },
  ]
})

const resetFormState = () => {
  formState.userAccount = props.initialValues.userAccount ?? ''
  formState.userName = props.initialValues.userName ?? ''
  formState.userAvatar = props.initialValues.userAvatar ?? ''
  formState.userProfile = props.initialValues.userProfile ?? ''
  formState.userRole = props.initialValues.userRole ?? 'user'
}

watch(
  () => [props.open, props.initialValues],
  ([open]) => {
    if (open) {
      resetFormState()
    } else {
      formRef.value?.clearValidate()
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

const handleCancel = () => {
  emit('cancel')
}

const handleOk = async () => {
  await formRef.value?.validate()

  emit('submit', {
    userAccount: formState.userAccount?.trim(),
    userName: formState.userName?.trim(),
    userAvatar: formState.userAvatar?.trim(),
    userProfile: formState.userProfile?.trim(),
    userRole: formState.userRole,
  })
}
</script>

<style scoped>
.modal-alert {
  margin-bottom: 16px;
}
</style>
