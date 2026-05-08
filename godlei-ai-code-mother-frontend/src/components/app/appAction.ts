export interface AppActionItem {
  key: string
  label: string
  danger?: boolean
  disabled?: boolean
  variant?: 'default' | 'primary'
}
