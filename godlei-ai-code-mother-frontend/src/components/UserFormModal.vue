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
    <a-form ref="formRef" layout="vertical" :model="formState">
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

      <a-form-item v-if="showAccount" label="用户账号" name="userAccount" :rules="accountRules">
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

      <a-form-item :label="avatarFieldLabel" name="userAvatar">
        <div v-if="enableAvatarUpload" class="avatar-upload-field">
          <div class="avatar-upload-panel">
            <a-avatar :src="avatarPreviewUrl || undefined" :size="72" class="avatar-preview">
              {{ avatarFallbackText }}
            </a-avatar>

            <div class="avatar-upload-copy">
              <a-space wrap>
                <a-button :loading="avatarUploading" @click="triggerAvatarSelect">上传头像</a-button>
                <a-button v-if="avatarPreviewUrl" @click="clearAvatar">移除头像</a-button>
              </a-space>
              <p class="avatar-upload-tip">
                支持 JPG、PNG、WEBP、GIF，大小不超过 5MB。上传成功后还需要点击“{{ confirmText }}”。
              </p>
            </div>
          </div>

          <input
            ref="avatarInputRef"
            type="file"
            :accept="AVATAR_ACCEPT_ATTRIBUTE"
            class="avatar-file-input"
            @change="handleAvatarFileChange"
          />

          <a-input
            v-model:value="formState.userAvatar"
            placeholder="也可以直接粘贴头像 URL"
            autocomplete="off"
          />
        </div>

        <a-input
          v-else
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
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { uploadMyAvatar } from '@/api/userController'
import {
  AVATAR_ACCEPT_ATTRIBUTE,
  getTrimmedMediaUrl,
  validateAvatarUploadFile,
} from '@/utils/media'

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
const avatarInputRef = ref<HTMLInputElement>()
const avatarUploading = ref(false)
const formState = reactive<UserFormState>({
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const enableAvatarUpload = computed(() => {
  return props.mode === 'profile' && props.profileMode === 'persisted'
})

const avatarFieldLabel = computed(() => (enableAvatarUpload.value ? '头像' : '头像地址'))

const avatarPreviewUrl = computed(() => getTrimmedMediaUrl(formState.userAvatar))

const avatarFallbackText = computed(() => {
  const seed = formState.userName?.trim() || formState.userAccount?.trim() || 'U'
  return seed.slice(0, 1).toUpperCase()
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
      return
    }

    avatarUploading.value = false
    formRef.value?.clearValidate()
  },
  {
    immediate: true,
    deep: true,
  },
)

const triggerAvatarSelect = () => {
  if (avatarUploading.value) {
    return
  }

  avatarInputRef.value?.click()
}

const clearAvatar = () => {
  formState.userAvatar = ''
}

const handleAvatarFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]

  if (input) {
    input.value = ''
  }

  if (!file) {
    return
  }

  const validationMessage = validateAvatarUploadFile(file)
  if (validationMessage) {
    message.warning(validationMessage)
    return
  }

  avatarUploading.value = true
  try {
    const res = await uploadMyAvatar({}, file)
    if (res.data?.code !== 0 || !res.data.data) {
      message.error(res.data?.message || '头像上传失败')
      return
    }

    formState.userAvatar = getTrimmedMediaUrl(res.data.data)
    message.success('头像上传成功，保存资料后即可生效')
  } catch {
    message.error('头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

const handleOk = async () => {
  if (avatarUploading.value) {
    message.warning('头像还在上传中，请稍候')
    return
  }

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

.avatar-upload-field {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.avatar-upload-panel {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  background: linear-gradient(180deg, rgb(255 255 255 / 92%), rgb(248 250 252 / 88%));
  border: 1px solid rgb(148 163 184 / 16%);
  border-radius: 16px;
}

.avatar-preview {
  flex: 0 0 auto;
  box-shadow: 0 16px 32px rgb(59 130 246 / 14%);
}

.avatar-upload-copy {
  min-width: 0;
}

.avatar-upload-tip {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.avatar-file-input {
  display: none;
}

@media (max-width: 640px) {
  .avatar-upload-panel {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
