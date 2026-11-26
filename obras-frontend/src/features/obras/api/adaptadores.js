const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

const STATUS = {
  PLANEJADA: { rotulo: 'Planejada', tom: 'blue' },
  EM_ANDAMENTO: { rotulo: 'Em Andamento', tom: 'green' },
  PARALISADA: { rotulo: 'Paralisada', tom: 'amber' },
  CANCELADA: { rotulo: 'Cancelada', tom: 'red' },
  CONCLUIDA: { rotulo: 'Concluída', tom: 'blue' },
};

const TIPOLOGIA = {
  RESIDENCIAL: 'Residencial',
  COMERCIAL: 'Comercial',
  INDUSTRIAL: 'Industrial',
  INFRAESTRUTURA: 'Infraestrutura',
  REFORMA: 'Reforma',
  RESTAURO: 'Restauro',
};

const TONS_EQUIPE = ['amber', 'blue', 'neutral'];

const texto = (v) => (v == null || v === '' ? '—' : String(v));

function capitalizar(v) {
  return v
    .toLowerCase()
    .split('_')
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
    .join(' ');
}

function competencia(data) {
  if (!data) return '—';
  const [ano, mes] = String(data).split('-');
  const indice = Number(mes) - 1;
  if (!ano || !MESES[indice]) return '—';
  return `${MESES[indice].charAt(0).toUpperCase()}${MESES[indice].slice(1)}/${ano}`;
}

export function sigla(nome) {
  if (!nome) return '—';
  const partes = String(nome).trim().split(/\s+/).filter(Boolean);
  if (partes.length === 0) return '—';
  return (partes[0][0] + (partes[1]?.[0] ?? '')).toUpperCase();
}

export function tomEquipe(usuarioId) {
  const id = Number(usuarioId);
  if (!Number.isFinite(id)) return 'neutral';
  return TONS_EQUIPE[Math.abs(id) % TONS_EQUIPE.length];
}

export function paraHeader(dto) {
  if (!dto) return null;
  const status = STATUS[dto.status];
  const inicio = competencia(dto.inicioPrevisto);
  const fim = competencia(dto.fimPrevisto);

  return {
    nome: texto(dto.nome),
    status: status?.rotulo ?? capitalizar(texto(dto.status)),
    statusTom: status?.tom ?? 'neutral',
    art: texto(dto.art),
    tipologia: TIPOLOGIA[dto.tipologia] ?? capitalizar(texto(dto.tipologia)),
    localizacao: texto(dto.endereco),
    previsao: inicio === '—' || fim === '—' ? '—' : `${inicio} a ${fim}`,
  };
}

export function paraEquipe(membros) {
  if (!Array.isArray(membros)) return [];
  return membros.map((m) => ({
    usuarioId: m.usuarioId,
    sigla: sigla(m.nome),
    nome: texto(m.nome),
    cargo: Array.isArray(m.cargos) && m.cargos.length ? m.cargos.join(' / ') : '—',
    tom: tomEquipe(m.usuarioId),
    dataInicio: m.dataInicio ?? null,
  }));
}
