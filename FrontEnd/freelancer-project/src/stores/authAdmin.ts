import { defineStore } from 'pinia'

// The administration login.
const ADMIN_ACCESS_TOKEN_KEY = "admin_access_token";
type Admin = { username: string; }

export const useAdminAuth = defineStore("adminAuth", {
  state: () => ({
    accessToken: localStorage.getItem(ADMIN_ACCESS_TOKEN_KEY) || "",
    admin: null as Admin | null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },

  actions: {
    setAccessToken(token: string) {
      this.accessToken = token;
      localStorage.setItem(ADMIN_ACCESS_TOKEN_KEY, token);
    },
    setAdmin(u: Admin | null) { this.admin = u },
    logout() {
      this.accessToken = "";
      localStorage.removeItem(ADMIN_ACCESS_TOKEN_KEY);
    },
  },
  persist: {
    key: 'admin'
  },
});