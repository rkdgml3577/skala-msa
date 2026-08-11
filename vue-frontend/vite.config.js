import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: 'localhost',
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // 브라우저에는 같은 출처(/api) 요청이지만, Vite가 Origin 헤더를
        // 게이트웨이(8080)로 그대로 전달하면 PATCH가 게이트웨이 CORS
        // 정책에 걸릴 수 있다. 개발 프록시 요청에서는 Origin을 제거한다.
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            proxyReq.removeHeader('origin')
          })
        }
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/userinfo': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
