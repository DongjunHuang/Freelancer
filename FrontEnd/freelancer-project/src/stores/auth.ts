import { defineStore } from 'pinia'

type User = { username: string; email: string }
export const useAuth = defineStore('auth', {
  state: () => ({
    accessToken: '' as string,
    user: null as User | null,
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken,
  },
  actions: {
    setToken(token: string) { this.accessToken = token },
    setUser(u: User | null) { this.user = u },
    clear() { this.$reset() },  
  },
  persist: {
    key: 'auth'
  },
})