const CONTENT_DISPOSITION_UTF8_FILENAME_REGEX = /filename\*\s*=\s*UTF-8''([^;]+)/i
const CONTENT_DISPOSITION_FILENAME_REGEX = /filename\s*=\s*"([^"]+)"|filename\s*=\s*([^;]+)/i
const INVALID_FILENAME_CHAR_REGEX = /[\\/:*?"<>|]/g

const decodeFilenamePart = (value: string) => {
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

const normalizeFilename = (value: string) => {
  return value.replace(INVALID_FILENAME_CHAR_REGEX, '_').trim()
}

export const resolveDownloadFilename = (
  contentDisposition?: string,
  fallbackBaseName = 'download',
) => {
  const fallbackName = `${normalizeFilename(fallbackBaseName) || 'download'}.zip`
  if (!contentDisposition) {
    return fallbackName
  }

  const utf8Match = contentDisposition.match(CONTENT_DISPOSITION_UTF8_FILENAME_REGEX)
  const baseMatch = contentDisposition.match(CONTENT_DISPOSITION_FILENAME_REGEX)
  const rawFilename = utf8Match?.[1] ?? baseMatch?.[1] ?? baseMatch?.[2]
  if (!rawFilename) {
    return fallbackName
  }

  const normalized = normalizeFilename(decodeFilenamePart(rawFilename))
  if (!normalized) {
    return fallbackName
  }

  return normalized.toLowerCase().endsWith('.zip') ? normalized : `${normalized}.zip`
}

export const triggerBlobDownload = (blob: Blob, filename: string) => {
  const downloadUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = downloadUrl
  anchor.download = filename
  anchor.rel = 'noreferrer'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  window.setTimeout(() => URL.revokeObjectURL(downloadUrl), 0)
}
