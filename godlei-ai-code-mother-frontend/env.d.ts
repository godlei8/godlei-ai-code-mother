/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_APP_DEPLOY_BASE_URL?: string
  readonly VITE_APP_PREVIEW_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module 'json-bigint' {
  interface JsonBigIntOptions {
    storeAsString?: boolean
  }

  interface JsonBigIntInstance {
    parse(text: string): unknown
  }

  export default function JSONBigFactory(options?: JsonBigIntOptions): JsonBigIntInstance
}
