import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // wherever code says `global`, replace with `globalThis`
    global: "globalThis"
  },
})
