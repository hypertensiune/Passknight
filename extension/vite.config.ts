import { fileURLToPath } from "url"

import { defineConfig, splitVendorChunkPlugin } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), splitVendorChunkPlugin()],
  build: {
    outDir: 'dist'
  },
  resolve: {
    alias: [
      { find: "@lib", replacement: fileURLToPath(new URL('./src/lib', import.meta.url)) },
      { find: "@hooks", replacement: fileURLToPath(new URL('./src/hooks', import.meta.url)) },
      { find: "@VaultComponents", replacement: fileURLToPath(new URL('./src/pages/Vault/Components', import.meta.url)) },
      { find: "@assets", replacement: fileURLToPath(new URL('./src/assets', import.meta.url)) },
    ]
  }
})
