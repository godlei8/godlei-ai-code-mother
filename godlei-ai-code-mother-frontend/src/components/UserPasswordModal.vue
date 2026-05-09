<template>
  <a-modal
    :open="open"
    title="修改密码"
    :confirm-loading="loading"
    ok-text="保存新密码"
    cancel-text="取消"
    destroy-on-close
    @cancel="handleCancel"
    @ok="handleOk"
  >
    <a-form ref="formRef" layout="vertical" :model="formState">
      <a-alert
        type="info"
        show-icon
        class="modal-alert"
        message="修改成功后将保持当前登录态，后续登录请使用新密码。"
      />

      <a-form-item
        label="旧密码"
        name="oldPassword"
        :rules="[{ required: true, message: '请输入旧密码' }]"
      >
        <a-input-password
          v-model:value="formState.oldPassword"
          placeholder="请输入当前登录密码"
          autocomplete="current-password"
        />
      </a-form-item>

      <a-form-item
        label="新密码"
        name="newPassword"
        :rules="[
          { required: true, message: '请输入新密码' },
          { min: 8, message: '密码长度不能少于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.newPassword"
          placeholder="请输入不少于 8 位的新密码"
          autocomplete="new-password"
        />
      </a-form-item>

      <a-form-item
        label="确认新密码"
        name="checkPassword"
        :rules="[
          { required: true, message: '请再次输入新密码' },
          { validator: validateConfirmPassword },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          placeholder="请再次输入新密码"
          autocomplete="new-password"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)

const emit = defineEmits<{
  submit: [payload: API.UserPasswordUpdateRequest]
  cancel: []
}>()

const formRef = ref<FormInstance>()
const formState = reactive<API.UserPasswordUpdateRequest>({
  oldPassword: '',
  newPassword: '',
  checkPassword: '',
})

const resetFormState = () => {
  formState.oldPassword = ''
  formState.newPassword = ''
  formState.checkPassword = ''
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      resetFormState()
    } else {
      formRef.value?.clearValidate()
    }
  },
  {
    immediate: true,
  },
)

const validateConfirmPassword = async () => {
  if (formState.newPassword !== formState.checkPassword) {
    return Promise.reject('两次输入的新密码不一致')
  }

  return Promise.resolve()
}

const handleCancel = () => {
  emit('cancel')
}

const handleOk = async () => {
  await formRef.value?.validate()

  emit('submit', {
    oldPassword: formState.oldPassword?.trim(),
    newPassword: formState.newPassword?.trim(),
    checkPassword: formState.checkPassword?.trim(),
  })
}
</script>

<style scoped>
.modal-alert {
  margin-bottom: 16px;
}
</style>
