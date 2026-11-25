import { formatarDataHoraIso, formatarDataIso } from '../../../shared/utils/formatadores.js';

const CLIMA = {
  ENSOLARADO: { rotulo: 'Ensolarado', badge: 'Canteiro praticável', tom: 'green', icone: 'sun' },
  NUBLADO: { rotulo: 'Nublado', badge: 'Canteiro praticável', tom: 'amber', icone: 'cloud' },
  CHUVA_FRACA: { rotulo: 'Chuva fraca', badge: 'Atenção no canteiro', tom: 'amber', icone: 'cloudRain' },
  CHUVA_FORTE: { rotulo: 'Chuva forte', badge: 'Atenção no canteiro', tom: 'red', icone: 'cloudRain' },
};

const CATEGORIA = {
  INTEMPERIE: { rotulo: 'Clima / Paralisação', tom: 'red', icone: 'cloudRain' },
  LOGISTICA: { rotulo: 'Logística', tom: 'amber', icone: 'truck' },
  SEGURANCA: { rotulo: 'Segurança', tom: 'green', icone: 'shield' },
  OUTRO: { rotulo: 'Outro', tom: 'neutral', icone: 'clipboard' },
};

const texto = (v) => (v == null || v === '' ? '—' : String(v));

function capitalizar(v) {
  return texto(v)
    .toLowerCase()
    .split('_')
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
    .join(' ');
}

export function hojeIso() {
  const agora = new Date();
  const ano = agora.getFullYear();
  const mes = String(agora.getMonth() + 1).padStart(2, '0');
  const dia = String(agora.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

export function escolherDiario(resumos, hoje = hojeIso()) {
  if (!Array.isArray(resumos) || resumos.length === 0) return null;
  const deHoje = resumos.find((r) => String(r?.data) === hoje);
  if (deHoje) return deHoje;
  return [...resumos].sort(
    (a, b) => String(b?.data).localeCompare(String(a?.data)) || Number(b?.id ?? 0) - Number(a?.id ?? 0),
  )[0];
}

export function paraResumoDiario(dto) {
  if (!dto) return null;
  const clima = CLIMA[dto.clima];
  return {
    id: dto.id,
    dataIso: dto.data ? String(dto.data) : '',
    data: formatarDataIso(dto.data),
    clima: clima?.rotulo ?? capitalizar(dto.clima),
    tom: clima?.tom ?? 'neutral',
    totalOcorrencias: Number(dto.totalOcorrencias) || 0,
    descricao: texto(dto.descricao),
  };
}

export function paraClimaDia(dto) {
  const clima = CLIMA[dto?.clima];
  return {
    codigo: dto?.clima ?? null,
    condicao: clima?.rotulo ?? (dto?.clima ? capitalizar(dto.clima) : 'Sem diário registrado'),
    data: formatarDataIso(dto?.data ?? hojeIso()),
    badge: clima?.badge ?? (dto?.clima ? 'Registro do dia' : '—'),
    tom: clima?.tom ?? 'neutral',
    icone: clima?.icone ?? 'calendar',
  };
}

export function paraFoto(dto) {
  if (!dto) return null;
  const legenda = texto(dto.legenda);
  return {
    id: dto.id,
    ocorrenciaId: dto.ocorrenciaId,
    nomeArquivo: texto(dto.nomeArquivo),
    legenda: legenda === '—' ? texto(dto.nomeArquivo) : legenda,
    dataEnvio: formatarDataHoraIso(dto.dataEnvio),
  };
}

export function paraOcorrencia(dto) {
  if (!dto) return null;
  const categoria = CATEGORIA[dto.categoria];
  const fotos = Array.isArray(dto.fotos) ? dto.fotos.map(paraFoto).filter(Boolean) : [];
  return {
    id: dto.id,
    data: formatarDataHoraIso(dto.dataHora),
    categoria: texto(dto.categoria),
    tag: categoria?.rotulo ?? capitalizar(dto.categoria),
    tom: categoria?.tom ?? 'neutral',
    icone: categoria?.icone ?? 'clipboard',
    texto: texto(dto.descricao),
    autorUsuarioId: dto.autorUsuarioId,
    paralisacao: dto.paralisacao
      ? {
          inicio: formatarDataHoraIso(dto.paralisacao.inicio),
          fim: formatarDataHoraIso(dto.paralisacao.fim),
        }
      : null,
    fotos,
  };
}

export function paraDiario(dto) {
  if (!dto) return null;
  const ocorrencias = Array.isArray(dto.ocorrencias) ? dto.ocorrencias.map(paraOcorrencia).filter(Boolean) : [];
  return {
    id: dto.id,
    obraId: dto.obraId,
    data: formatarDataIso(dto.data),
    dataIso: dto.data ? String(dto.data) : '',
    descricao: texto(dto.descricao),
    equipePresenteIds: Array.isArray(dto.equipePresenteIds) ? dto.equipePresenteIds : [],
    ocorrencias,
    fotos: ocorrencias.flatMap((o) => o.fotos),
  };
}
