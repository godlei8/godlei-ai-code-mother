export const AVATAR_MAX_SIZE_BYTES = 5 * 1024 * 1024

const AVATAR_ACCEPTED_MIME_TYPES = new Set([
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif',
])

const AVATAR_ACCEPTED_EXTENSIONS = new Set(['jpg', 'jpeg', 'png', 'webp', 'gif'])

export const AVATAR_ACCEPT_ATTRIBUTE = '.jpg,.jpeg,.png,.webp,.gif'

export const getTrimmedMediaUrl = (value?: string | null) => {
  return value?.trim() || ''
}

export const getRenderableMediaUrl = (value?: string | null, hasLoadError = false) => {
  if (hasLoadError) {
    return ''
  }

  return getTrimmedMediaUrl(value)
}

export const validateAvatarUploadFile = (file: File) => {
  if (file.size > AVATAR_MAX_SIZE_BYTES) {
    return '头像大小不能超过 5MB'
  }

  const extension = file.name.split('.').pop()?.trim().toLowerCase() || ''
  if (AVATAR_ACCEPTED_MIME_TYPES.has(file.type) || AVATAR_ACCEPTED_EXTENSIONS.has(extension)) {
    return null
  }

  return '头像格式仅支持 JPG、PNG、WEBP 或 GIF'
}
