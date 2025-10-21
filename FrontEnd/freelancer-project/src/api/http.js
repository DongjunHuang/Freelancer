import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10000,
})

http.interceptors.response.use(
  res => res,
  err => {
    console.error('API Error:', err)
    return Promise.reject(err)
  }
)

export default http