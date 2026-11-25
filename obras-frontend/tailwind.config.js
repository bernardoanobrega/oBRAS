/** @type {import('tailwindcss').Config} */
// Paleta do protótipo do Figma (tema escuro). As telas usam só estes nomes, nunca hexadecimal solto.
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        fundo: '#111111',
        superficie: { DEFAULT: '#161616', alta: '#202224' },
        borda: { DEFAULT: '#27272a', forte: '#cbcbcb' },
        texto: { DEFAULT: '#f1f5f9', suave: '#9f9fa9', fraco: '#8b8b94' },
        neutro: '#808080',
        menu: { DEFAULT: '#0f172a', ativo: '#1e293b', texto: '#9b9fa8', destaque: '#314158' },
        brand: { DEFAULT: '#fe9a00', claro: '#ffb900', escuro: '#e58c02', fundo: '#392a13' },
        sucesso: { DEFAULT: '#00d492', marcador: '#22c55e', fundo: '#132f26' },
        perigo: { DEFAULT: '#ff6467', forte: '#fb2c36', fundo: '#391a1b' },
        info: { DEFAULT: '#51a2ff', fundo: '#152033' },
      },
    },
  },
  plugins: [],
};
