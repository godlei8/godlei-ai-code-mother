import axios, {
  type AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { message } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'
import { parseApiJson } from '@/utils/jsonParser'

type ApiResponseShape = {
  code?: number
  message?: string
}

const myAxios = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
  transformResponse: [(data: unknown) => parseApiJson(data)],
})

myAxios.interceptors.request.use(
  function (config: InternalAxiosRequestConfig) {
    return config
  },
  function (error: AxiosError) {
    return Promise.reject(error)
  },
)

myAxios.interceptors.response.use(
  function (response: AxiosResponse<ApiResponseShape>) {
    const { data } = response

    if (data?.code === 40100) {
      const isAuthPage =
        window.location.pathname.includes('/auth/login') ||
        window.location.pathname.includes('/auth/register')
      const isBootstrapRequest = response.request.responseURL.includes('user/get/login')

      if (!isAuthPage && !isBootstrapRequest) {
        const redirect = `${window.location.pathname}${window.location.search}`
        message.warning('请先登录')
        window.location.href = `/auth/login?redirect=${encodeURIComponent(redirect)}`
      }
    }

    return response
  },
  function (error: AxiosError<ApiResponseShape>) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else if (error.message) {
      message.error(error.message)
    }

    return Promise.reject(error)
  },
)

export default myAxios
