// Tokens visuais do módulo Obras (tema escuro + paleta provisória verde/amarelo, ver tailwind.config.js).
// Âmbar em TOM continua sendo cor de SIGNIFICADO (alerta), não de marca.
// Isolados aqui para facilitar troca de tema depois (doc §10.1). Não mexer no tema claro do AppShell.

export const CARD = 'rounded-xl border border-[#27272A] bg-[#121215]';

export const TOM = {
  amber: 'bg-amber-500/10 text-amber-500 border-amber-500/40',
  blue: 'bg-blue-500/10 text-blue-400 border-blue-500/40',
  green: 'bg-green-500/10 text-green-500 border-green-500/40',
  red: 'bg-red-500/10 text-red-500 border-red-500/40',
  neutral: 'bg-zinc-800/60 text-zinc-400 border-zinc-700',
};

export const ICON_BOX = 'grid place-items-center rounded-lg bg-petro-amarelo/10 text-petro-amarelo';

export const LINK = 'text-xs font-semibold text-petro-amarelo hover:text-petro-amarelo-claro';

export const FOCO = 'focus:ring-2 focus:ring-petro-verde-claro/60';

export const BOTAO_PRIMARIO =
  'flex items-center gap-2 rounded-lg bg-petro-verde px-3 py-1.5 text-xs font-semibold text-white hover:bg-petro-verde-claro';

export const BOTAO_SECUNDARIO =
  'flex items-center gap-2 rounded-lg border border-zinc-800 bg-[#121215] px-3 py-1.5 text-xs text-zinc-200 hover:bg-zinc-800';
