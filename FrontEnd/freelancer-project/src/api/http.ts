import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

let refreshing = false
let pendingQueue: Array<(token: string) => void> = []

async function refreshAccessToken(): Promise<string> {
  const res = await axios.post('/auth/refresh', {}, { withCredentials: true })
  return res.data.accessToken
}

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})

http.interceptors.request.use((config) => {
    const url = config.url || ''
    const isNotAuth = url.startsWith('/auth') || url.startsWith('/api/metrics')
    if (!isNotAuth) {
      const token = localStorage.getItem('access_token')
      if (token) {
        config.headers = config.headers || {}
        config.headers.Authorization = 'Bearer ${token}'
      }
    }
    return config
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  res => res,
  async (error: AxiosError) => {
    const original = error.config as AxiosRequestConfig & { _retry?: boolean }
    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error)
    }

    original._retry = true

    
    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error)
    }

    // === 进入刷新逻辑 ===
    if (!refreshing) {
      refreshing = true
      try {
        const newToken = await refreshAccessToken()
        localStorage.setItem('access_token', newToken)
        refreshing = false

        // 唤醒队列中的请求
        pendingQueue.forEach(cb => cb(newToken))
        pendingQueue = []

        // 🔁 重发原请求
        original.headers = original.headers || {}
        original.headers.Authorization = `Bearer ${newToken}`
        return http(original)
      } catch (err) {
        refreshing = false
        pendingQueue = []
        return Promise.reject(err)
      }
    }
    

    // Waiting if queue is not empty
    return new Promise((resolve) => {
      pendingQueue.push((newToken: string) => {
          original.headers = original.headers || {}
          original.headers.Authorization = 'Bearer ${newToken}'
          resolve(http(original))
        })
      })
    }
)

export default http
