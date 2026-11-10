import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [react()],
    server: {
      port: 5173,
      // em dev o front chama /api e o Vite encaminha ao backend (sem CORS)
      proxy: { '/api': env.VITE_PROXY_TARGET || 'http://localhost:8080' },
    },
    test: { environment: 'node' },
  };
});
