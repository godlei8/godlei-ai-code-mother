/// <reference types="vite/client" />

declare module 'json-bigint' {
  interface JsonBigIntOptions {
    storeAsString?: boolean
  }

  interface JsonBigIntInstance {
    parse(text: string): unknown
  }

  export default function JSONBigFactory(options?: JsonBigIntOptions): JsonBigIntInstance
}
