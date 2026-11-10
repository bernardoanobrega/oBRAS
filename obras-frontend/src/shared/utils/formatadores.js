const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const data = new Intl.DateTimeFormat('pt-BR');
const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const formatarMoeda = (v) => (v == null ? '—' : moeda.format(v));
export const formatarData = (v) => (v ? data.format(new Date(v)) : '—');
export const formatarDataHora = (v) => (v ? dataHora.format(new Date(v)) : '—');
