// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => ({
  resolve: {
    alias: {'@': path.resolve(__dirname, './src')},
  },
  base: '/', 
  plugins: [vue()],
}))