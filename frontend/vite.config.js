import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      // Delimita la cobertura a la lógica unitaria (Contextos, Hooks, Utils) y excluye las vistas/layouts visuales
      include: [
        'src/context/**/*.{js,jsx}',
        'src/hooks/useLocalStorage.js',
        'src/services/api.js',
        'src/components/RutaProtegida.jsx',
        'src/utils/formatos.js',
        'src/pages/NotFound.jsx'
      ],
    },
  },
})