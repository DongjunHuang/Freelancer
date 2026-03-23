import { defineStore } from 'pinia'
const ADMIN_ACCESS_TOKEN_KEY = 'admin_access_token'
type Admin = { username: string }

export const useAdminAuth = defineStore('adminAuth', {
  state: () => ({
    accessToken: localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY) || '',
    admin: null as Admin | null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },

  actions: {
    setAccessToken(token: string) {
      this.accessToken = token
      localStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, token)
    },
    setAdmin(admin: Admin | null) {
      this.admin = admin
    },
    clear() {
      this.$reset()
    },
  },

  persist: {
    key: 'admin',
  },
})
