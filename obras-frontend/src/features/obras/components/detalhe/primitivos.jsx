import { TOM } from './tokens.js';

export function Badge({ tom = 'neutral', dot = false, children }) {
  return (
    <span className={`inline-flex items-center gap-1.5 rounded border px-2 py-0.5 text-[11px] ${TOM[tom]}`}>
      {dot && <span className="size-1.5 rounded-full bg-current" />}
      {children}
    </span>
  );
}

export function Avatar({ tom = 'neutral', children, className = '' }) {
  const cor = {
    amber: 'bg-brand-fundo text-brand-claro',
    blue: 'bg-info-fundo text-info',
    green: 'bg-sucesso-fundo text-sucesso',
    red: 'bg-perigo-fundo text-perigo',
    neutral: 'bg-superficie-alta text-texto',
  }[tom];

  return (
    <span className={`grid size-8 shrink-0 place-items-center rounded-full text-[11px] font-semibold ${cor} ${className}`}>
      {children}
    </span>
  );
}

export function ProgressBar({ value, color = 'bg-brand-claro', className = '' }) {
  return (
    <div className={`h-1.5 w-full overflow-hidden rounded-full bg-superficie-alta ${className}`}>
      <div className={`h-full rounded-full ${color}`} style={{ width: `${value}%` }} />
    </div>
  );
}

export function IconButton({ icon: Icon, label, onClick }) {
  return (
    <button
      type="button"
      aria-label={label}
      onClick={onClick}
      className="grid size-8 place-items-center rounded-lg border border-borda text-texto-suave hover:bg-superficie-alta"
    >
      <Icon size={14} />
    </button>
  );
}
