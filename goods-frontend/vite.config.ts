import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 简化构建配置，避免模块初始化顺序问题
  build: {
    chunkSizeWarningLimit: 1000,
    minify: 'esbuild' // 使用esbuild替代terser，构建更快
  },
  preview: {
    host: '0.0.0.0',
    port: process.env.PORT ? parseInt(process.env.PORT) : 4173,
    allowedHosts: ['goods-system-frontend.onrender.com']
  },
  // 开发环境优化
  server: {
    hmr: {
      overlay: false
    }
  }
})
