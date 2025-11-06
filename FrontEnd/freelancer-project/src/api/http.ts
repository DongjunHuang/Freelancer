import axios, { AxiosInstance, AxiosError } from 'axios'

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})

http.interceptors.request.use((config) => {
    const url = config.url || ''
    const isNotAuth = url.startsWith('/auth') || url.startsWith('/api/tests')
    if (!isNotAuth) {
      const token = localStorage.getItem('access_token')
      if (token) {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  }
)

// The function to refresh access token
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
    if (original.url?.includes('/auth/refresh')) 
      return Promise.reject(error)

    original._retry = true

    try {
      // 如果已有刷新在进行，等它；否则创建一个
      if (!refreshPromise) {
        refreshPromise = getNewToken().finally(() => { refreshPromise = null })
      }
      const newToken = await refreshPromise

      original.headers = original.headers || {}
      original.headers.Authorization = `Bearer ${newToken}`
      console.log(newToken)
      return http(original)
    } catch (e) {
      // 刷新失败——可在这里统一跳登录
      // router.push('/signin')
      return Promise.reject(e)
    }
  }
)
export default http
