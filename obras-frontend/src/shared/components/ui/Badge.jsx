const TONS = {
  verde: 'bg-sucesso-fundo text-sucesso',
  amarelo: 'bg-brand-fundo text-brand-claro',
  vermelho: 'bg-perigo-fundo text-perigo',
  cinza: 'bg-superficie-alta text-texto',
  azul: 'bg-info-fundo text-info',
};

export default function Badge({ tom = 'cinza', children }) {
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${TONS[tom]}`}>{children}</span>;
}
