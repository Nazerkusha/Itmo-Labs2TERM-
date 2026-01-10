import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    port: 9080,
    host: true
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: './index.html'
      }
    }
  }
});
