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
    amber: 'bg-amber-500/20 text-amber-400',
    blue: 'bg-blue-500/20 text-blue-300',
    green: 'bg-green-500/20 text-green-400',
    red: 'bg-red-500/20 text-red-400',
    neutral: 'bg-zinc-800 text-zinc-200',
  }[tom];

  return (
    <span className={`grid size-8 shrink-0 place-items-center rounded-full text-[11px] font-semibold ${cor} ${className}`}>
      {children}
    </span>
  );
}

export function ProgressBar({ value, color = 'bg-petro-verde-claro', className = '' }) {
  return (
    <div className={`h-1.5 w-full overflow-hidden rounded-full bg-zinc-800 ${className}`}>
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
      className="grid size-8 place-items-center rounded-lg border border-zinc-800 text-zinc-300 hover:bg-zinc-800"
    >
      <Icon size={14} />
    </button>
  );
}
