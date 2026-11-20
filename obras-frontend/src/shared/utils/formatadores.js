const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const data = new Intl.DateTimeFormat('pt-BR');
const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

export const formatarMoeda = (v) => (v == null ? '—' : moeda.format(v));
export const formatarData = (v) => (v ? data.format(new Date(v)) : '—');
export const formatarDataHora = (v) => (v ? dataHora.format(new Date(v)) : '—');

function partesIso(v) {
  if (v == null || v === '') return null;
  const [dataIso, horaIso = ''] = String(v).split('T');
  const [ano, mes, dia] = dataIso.split('-');
  if (!ano || !mes || !dia) return null;
  return { data: `${dia}/${mes}/${ano}`, horaIso };
}

export const formatarDataIso = (v) => partesIso(v)?.data ?? '—';

export function formatarDataHoraIso(v) {
  const partes = partesIso(v);
  if (!partes) return '—';
  const [hora, minuto] = partes.horaIso.split(':');
  return hora && minuto ? `${partes.data} · ${hora}:${minuto}` : partes.data;
}
