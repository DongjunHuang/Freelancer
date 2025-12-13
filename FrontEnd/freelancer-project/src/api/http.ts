import axios, { AxiosInstance, AxiosError } from 'axios'
import { useAuth } from '@/stores/auth'

const whiteList = [
  '/auth/signin',       
  '/auth/signup',       
  '/auth/verify',       
  '/auth/refresh',       
  '/auth/resendEmail',       
]

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})

http.interceptors.request.use(
  (config) => {
    const url = config.url || ''
    const isNotAuth = whiteList.some(path => url.startsWith(path))

    if (!isNotAuth) {
      const auth = useAuth()
      if (auth.accessToken) {
        config.headers.Authorization = `Bearer ${auth.accessToken}`
      }
    }

    return config
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  }
)

async function refreshAccessToken(): Promise<string> {
  const res = await http.post('/auth/refresh', {}, { withCredentials: true })
  return res.data.accessToken
}
let refreshPromise: Promise<string> | null = null

async function getNewToken(): Promise<string> {
  const t = await refreshAccessToken()
  localStorage.setItem('access_token', t)
  return t
}

// Intercept the response and handle the refreshing access token logic.
http.interceptors.response.use(
  res => res,
  async (error) => {
    const original = error.config as any
    const status = error.response?.status

    // Only accept 401 error code
    if (status !== 401 || original._retry) 
      return Promise.reject(error)

    // Reject response from refresh
    if (original.url?.includes('/auth')) 
      return Promise.reject(error)

    original._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = getNewToken().finally(() => { refreshPromise = null })
      }
      const newToken = await refreshPromise

      original.headers = original.headers || {}
      original.headers.Authorization = `Bearer ${newToken}`
      return http(original)
    } catch (e) {
      return Promise.reject(e)
    }
  }
)
export default http
