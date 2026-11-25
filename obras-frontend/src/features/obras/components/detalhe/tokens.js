// Combinações de classes do módulo Obras. As cores vêm do tema do Figma (tailwind.config.js).
// Âmbar em TOM continua sendo cor de SIGNIFICADO (alerta).

export const CARD = 'rounded-xl border border-borda bg-superficie';

export const TOM = {
  amber: 'bg-brand-fundo text-brand border-brand/40',
  blue: 'bg-info-fundo text-info border-info/40',
  green: 'bg-sucesso-fundo text-sucesso border-sucesso/40',
  red: 'bg-perigo-fundo text-perigo border-perigo/40',
  neutral: 'bg-superficie-alta/60 text-texto-suave border-borda',
};

export const ICON_BOX = 'grid place-items-center rounded-lg bg-brand-fundo text-brand';

export const LINK = 'text-xs font-semibold text-brand hover:text-brand-claro';

export const FOCO = 'focus:ring-2 focus:ring-brand-claro/60';

export const BOTAO_PRIMARIO =
  'flex items-center gap-2 rounded-lg bg-brand px-3 py-1.5 text-xs font-semibold text-fundo hover:bg-brand-claro';

export const BOTAO_SECUNDARIO =
  'flex items-center gap-2 rounded-lg border border-borda bg-superficie px-3 py-1.5 text-xs text-texto hover:bg-superficie-alta';
