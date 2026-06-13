import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    allowedHosts: [
      'api.timerbook.com.br',
      'timerbook.com.br',
      '44.202.167.42'
    ]
  }
})