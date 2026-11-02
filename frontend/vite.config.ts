import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// No Docker o backend é alcançado pelo nome do serviço (VITE_PROXY_TARGET=http://app:8080).
// Localmente o padrão é o backend na porta 8080 do host.
const target = process.env.VITE_PROXY_TARGET ?? 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    watch: { usePolling: true },
    // O backend não configura CORS; o proxy evita o problema em desenvolvimento.
    proxy: {
      '/api': {
        target,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
