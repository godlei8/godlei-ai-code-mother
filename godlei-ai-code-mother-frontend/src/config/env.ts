import { CodeGenTypeEnum } from '@/utils/codeGenTypes'

type AppRuntimeEnvInput = Partial<
  Record<'VITE_API_BASE_URL' | 'VITE_APP_DEPLOY_BASE_URL' | 'VITE_APP_PREVIEW_BASE_URL', string>
>

type AppRuntimeEnv = {
  apiBaseUrl: string
  deployBaseUrl: string
  previewBaseUrl: string
}

const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '')

export const resolveAppRuntimeEnv = (env: AppRuntimeEnvInput): AppRuntimeEnv => {
  const apiBaseUrl = trimTrailingSlash(env.VITE_API_BASE_URL || '/api')
  const deployBaseUrl = trimTrailingSlash(env.VITE_APP_DEPLOY_BASE_URL || 'http://localhost')
  const previewBaseUrl = trimTrailingSlash(
    env.VITE_APP_PREVIEW_BASE_URL || '/api/static',
  )

  return {
    apiBaseUrl,
    deployBaseUrl,
    previewBaseUrl,
  }
}

const runtimeEnv = resolveAppRuntimeEnv(import.meta.env)

export const API_BASE_URL = runtimeEnv.apiBaseUrl

export const DEPLOY_DOMAIN = runtimeEnv.deployBaseUrl

export const STATIC_PREVIEW_BASE_URL = runtimeEnv.previewBaseUrl

export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

export const getStaticPreviewUrl = (codeGenType: string, appId: string | number) => {
  const baseUrl = `${STATIC_PREVIEW_BASE_URL}/${codeGenType}_${appId}/`
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}
