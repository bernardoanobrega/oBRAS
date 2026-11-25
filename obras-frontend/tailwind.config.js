/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Paleta provisória (verde/amarelo Petrobras) até a padronização oficial das cores.
        // Trocar a paleta = mudar só este bloco.
        petro: {
          verde: { DEFAULT: '#008542', claro: '#00A651', escuro: '#006B35' },
          amarelo: { DEFAULT: '#FDB913', claro: '#FFC940' },
        },
        // brand = cor de destaque das telas claras (login, menu, Usuários), agora em tons de verde
        brand: {
          50: '#e6f4ec', 100: '#c2e5d0', 200: '#8fd0a9', 500: '#00a651',
          600: '#008542', 700: '#006b35', 800: '#00532a',
        },
      },
    },
  },
  plugins: [],
};
