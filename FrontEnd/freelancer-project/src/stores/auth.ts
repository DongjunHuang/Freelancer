import { ref, computed } from 'vue'

const tokenRef = ref<string | null>(localStorage.getItem('access_token') || null)

export function useAuth() {
  // 计算属性：当前是否登录
  const isLoggedIn = computed(() => !!tokenRef.value)

  // ✅ 设置 token（登录时用这个）
  function setToken(t: string | null) {
    tokenRef.value = t
    if (t) {
      localStorage.setItem('access_token', t)
    } else {
      localStorage.removeItem('access_token')
    }
  }

  // 可选：读取当前 token
  function getToken() {
    return tokenRef.value
  }

  return { isLoggedIn, setToken, getToken }
}