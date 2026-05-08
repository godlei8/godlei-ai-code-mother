import { describe, expect, it } from 'vitest'
import { resolveAppRuntimeEnv } from '../env'

describe('resolveAppRuntimeEnv', () => {
  it('keeps deploy and preview base urls independent from the api url', () => {
    const runtimeEnv = resolveAppRuntimeEnv({
      VITE_API_BASE_URL: 'https://api.example.com/api',
      VITE_APP_DEPLOY_BASE_URL: 'https://deploy.example.com',
      VITE_APP_PREVIEW_BASE_URL: 'https://preview.example.com/static',
    })

    expect(runtimeEnv.apiBaseUrl).toBe('https://api.example.com/api')
    expect(runtimeEnv.deployBaseUrl).toBe('https://deploy.example.com')
    expect(runtimeEnv.previewBaseUrl).toBe('https://preview.example.com/static')
  })

  it('falls back to the local defaults when the custom env vars are missing', () => {
    const runtimeEnv = resolveAppRuntimeEnv({})

    expect(runtimeEnv.apiBaseUrl).toBe('http://localhost:8123/api')
    expect(runtimeEnv.deployBaseUrl).toBe('http://localhost')
    expect(runtimeEnv.previewBaseUrl).toBe('http://localhost:8123/api/static')
  })
})
