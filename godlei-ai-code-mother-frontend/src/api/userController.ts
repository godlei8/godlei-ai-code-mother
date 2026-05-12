// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/add */
export async function addUser(body: API.UserAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/delete */
export async function deleteUser(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 GET /user/get */
export async function getUserById(
  // 鍙犲姞鐢熸垚鐨凱aram绫诲瀷 (闈瀊ody鍙傛暟swagger榛樿娌℃湁鐢熸垚瀵硅薄)
  params: API.getUserByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUser>('/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 GET /user/get/login */
export async function getLoginUser(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO>('/user/get/login', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 GET /user/get/vo */
export async function getUserVoById(
  // 鍙犲姞鐢熸垚鐨凱aram绫诲瀷 (闈瀊ody鍙傛暟swagger榛樿娌℃湁鐢熸垚瀵硅薄)
  params: API.getUserVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserVO>('/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/list/page/vo */
export async function listUserVoByPage(
  body: API.UserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageUserVO>('/user/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/login */
export async function userLogin(body: API.UserLoginRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/logout */
export async function userLogout(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/my/password */
export async function updateMyUserPassword(
  body: API.UserPasswordUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/user/my/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/my/update */
export async function updateMyUser(
  body: API.UserProfileUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/user/my/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 当前登录用户上传头像 POST /user/my/avatar */
export async function uploadMyAvatar(body: FormData, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/user/my/avatar', {
    method: 'POST',
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/register */
export async function userRegister(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 姝ゅ鍚庣娌℃湁鎻愪緵娉ㄩ噴 POST /user/update */
export async function updateUser(body: API.UserUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
