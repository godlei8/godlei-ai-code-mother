// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chat/history/admin/list/page */
export async function listByPageAdmin(
  body: API.ChatHistoryAdminQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistoryAdminVO>('/chat/history/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chat/history/list/latest */
export async function listLatest(
  body: API.ChatHistoryLatestRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseChatHistoryCursorVO>('/chat/history/list/latest', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chat/history/list/older */
export async function listOlder(
  body: API.ChatHistoryOlderRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseChatHistoryCursorVO>('/chat/history/list/older', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
