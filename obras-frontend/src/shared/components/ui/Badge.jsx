const TONS = {
  verde: 'bg-green-100 text-green-800',
  amarelo: 'bg-yellow-100 text-yellow-800',
  vermelho: 'bg-red-100 text-red-800',
  cinza: 'bg-slate-100 text-slate-700',
  azul: 'bg-blue-100 text-blue-800',
};

export default function Badge({ tom = 'cinza', children }) {
  return <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${TONS[tom]}`}>{children}</span>;
}
