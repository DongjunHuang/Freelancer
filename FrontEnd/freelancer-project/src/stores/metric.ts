import { defineStore } from 'pinia'
import axios from 'axios'

export const useMetricStore = defineStore('metric', {
  state: () => ({
    data: null,
    loading: false,
    error: null,
  }),
  actions: {
    async fetchLatest() {
      this.loading = true
      this.error = null
      try {
        const base = import.meta.env.VITE_API_BASE || 'http://localhost:8080'
        
        const res = await axios.get(`${base}/api/metrics/latest`)
        this.data = res.data
      } catch (e) {
        this.error = e?.response?.data || e.message
      } finally {
        this.loading = false
      }
    },
  },
})