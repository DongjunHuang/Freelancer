import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { useAuth } from '@/stores/auth'
import router from '@/router'

const userWhiteList = [
  '/auth/signin',
  '/auth/signup',
  '/auth/verify',
  '/auth/refresh',
  '/auth/resendEmail',
]

let refreshPromise: Promise<string> | null = null

/**
 * Create http request with interceptors.
 */
export const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})

/**
 * Whether the request is in the white list, which means the request does not need to go through
 * Auth process.
 *
 * @param url the url.
 * @returns whether the request is in white list.
 */
function isAuthWhiteList(url?: string): boolean {
  if (!url) return false
  return userWhiteList.some((path) => url.startsWith(path))
}

/**
 * Clear the storage when auth is failed.
 */
function clearClientAuthState() {
  const auth = useAuth()
  auth.clear()
}

/**
 * If the user is not able to signin, redirect the page to let user signin.
 */
async function redirectToSignin() {
  const currentPath = router.currentRoute.value.fullPath
  if (!currentPath.startsWith('/signin')) {
    await router.replace({
      path: '/signin',
      query: currentPath ? { redirect: currentPath } : {},
    })
  }
}

/**
 * When the request is failed to auth, clear the storage and redirect user to sign in page.
 */
async function handleAuthFailure() {
  clearClientAuthState()
  await redirectToSignin()
}

/**
 * Fetch access token according to the valid refresh token.
 *
 * @returns the valid access token.
 */
async function refreshAccessToken(): Promise<string> {
  const res = await http.post('/auth/refresh')
  return res.data.accessToken
}

/**
 * Save the tokens into the storage.
 *
 * @returns the token.
 */
async function getNewToken(): Promise<string> {
  const token = await refreshAccessToken()

  const auth = useAuth()
  auth.setToken(token)
  return token
}

/**
 * The interceptor of the http request.
 */
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const url = config.url || ''
    const isNotAuth = isAuthWhiteList(url)
    if (!isNotAuth) {
      const auth = useAuth()
      if (auth.accessToken) {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${auth.accessToken}`
      }
    }

    return config
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  },
)

/**
 * The interceptor of the response.
 */
http.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
    const status = error.response?.status
    const url = original?.url || ''

    if (!original) {
      return Promise.reject(error)
    }

    if (status !== 401) {
      return Promise.reject(error)
    }

    if (isAuthWhiteList(url)) {
      await handleAuthFailure()
      return Promise.reject(error)
    }

    if (original._retry) {
      await handleAuthFailure()
      return Promise.reject(error)
    }

    original._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = getNewToken().finally(() => {
          refreshPromise = null
        })
      }

      const newToken = await refreshPromise

      original.headers = original.headers || {}
      original.headers.Authorization = `Bearer ${newToken}`

      return http(original)
    } catch (refreshError) {
      refreshPromise = null
      await handleAuthFailure()
      return Promise.reject(refreshError)
    }
  },
)
export default http
