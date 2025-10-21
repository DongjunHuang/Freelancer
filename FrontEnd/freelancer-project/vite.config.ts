// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => ({
  base: '/app/', // ← 开发和部署都以 /app/ 为基座
  plugins: [vue()],
}))