// Pílula com borda fina na cor da situação, como os selos do protótipo do Figma.
const TONS = {
  verde: 'border-sucesso/40 bg-sucesso-fundo text-sucesso',
  amarelo: 'border-brand/40 bg-brand-fundo text-brand-claro',
  vermelho: 'border-perigo/40 bg-perigo-fundo text-perigo',
  cinza: 'border-borda bg-superficie-alta text-texto',
  azul: 'border-info/40 bg-info-fundo text-info',
};

export default function Badge({ tom = 'cinza', children }) {
  return (
    <span className={`rounded-full border px-2 py-0.5 text-xs font-medium ${TONS[tom]}`}>
      {children}
    </span>
  );
}
