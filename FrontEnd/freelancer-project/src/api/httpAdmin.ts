// adminHttp.ts
import axios, { AxiosInstance, AxiosError } from 'axios'
import router from '@/router'

const whiteList = ['/admin/auth/signin']

const adminHttp: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})

adminHttp.interceptors.request.use(
  (config) => {
    const url = config.url || ''
    const isNotAuth = whiteList.some((path) => url.startsWith(path))

    if (!isNotAuth) {
      const token = localStorage.getItem('admin_access_token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  },
)

let isRedirecting = false
adminHttp.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status

    if (status === 401 && !isRedirecting) {
      isRedirecting = true
      console.warn('Admin token expired or invalid, redirect to login')

      localStorage.removeItem('admin_access_token')

      router.replace({
        path: '/admin/login',
        query: {
          redirect: router.currentRoute.value.fullPath,
        },
      })
    }

    return Promise.reject(error)
  },
)
export default adminHttp
