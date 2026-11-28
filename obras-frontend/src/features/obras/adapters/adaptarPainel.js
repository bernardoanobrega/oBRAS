const CAMPOS_KPI = [
  'avancoRealizado',
  'avancoPrevisto',
  'spi',
  'orcamentoTotal',
  'executado',
  'limiteAlerta',
];

function numeroOuNull(valor) {
  if (valor === null || valor === undefined || valor === '') return null;
  const numero = Number(valor);
  return Number.isFinite(numero) ? numero : null;
}

function adaptarKpis(kpis) {
  const adaptado = {};
  for (const campo of CAMPOS_KPI) adaptado[campo] = numeroOuNull(kpis?.[campo]);
  for (const [chave, valor] of Object.entries(kpis ?? {})) {
    if (chave === 'fonte') adaptado.fonte = valor ?? null;
    else if (!CAMPOS_KPI.includes(chave)) adaptado[chave] = numeroOuNull(valor);
  }
  if (!('fonte' in adaptado)) adaptado.fonte = null;
  return adaptado;
}

function adaptarMaoDeObra(dto) {
  if (!dto || typeof dto !== 'object') return null;
  return {
    alocados: numeroOuNull(dto.alocados),
    presentesHoje: numeroOuNull(dto.presentesHoje),
    vsOntem: numeroOuNull(dto.vsOntem),
    fonte: dto.fonte ?? null,
  };
}

export function adaptarPainel(dto) {
  if (!dto || typeof dto !== 'object') throw new TypeError('Painel inválido');

  const clima = dto.clima ?? {};
  const equipe = Array.isArray(dto.equipe) ? dto.equipe : [];
  const ocorrencias = Array.isArray(dto.ocorrencias) ? dto.ocorrencias : [];
  const fotos = Array.isArray(dto.fotos) ? dto.fotos : [];

  return {
    obra: dto.obra ?? null,
    clima: { data: clima.data ?? null, condicao: clima.condicao ?? null },
    kpis: adaptarKpis(dto.kpis),
    equipe,
    maoDeObra: adaptarMaoDeObra(dto.maoDeObra),
    ocorrencias,
    fotos: fotos.filter((foto) => foto?.ocorrenciaId != null),
  };
}
