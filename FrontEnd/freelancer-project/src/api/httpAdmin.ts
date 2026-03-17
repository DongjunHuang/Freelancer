// adminHttp.ts
import axios, {AxiosInstance, AxiosError } from 'axios'

const whiteList = [
  '/admin/auth/signin',       
]

const adminHttp: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 10000,
  withCredentials: true,
})


adminHttp.interceptors.request.use(
  (config) => {
    const url = config.url || ''
    const isNotAuth = whiteList.some(path => url.startsWith(path))

    if (!isNotAuth) {
      const token = localStorage.getItem("admin_access_token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  async (error: AxiosError) => {
    console.error('Request setup failed:', error)
    return Promise.reject(error)
  }
);

export default adminHttp;