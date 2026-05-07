import 'vue-router'
import type { AccessRole } from '@/access/accessConstants'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    access?: AccessRole
    hideInMenu?: boolean
    menuLabel?: string
    menuOrder?: number
  }
}
