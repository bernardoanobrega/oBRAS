// Rótulos e tons de exibição. A classificação em si é calculada pelo backend (RF-34).
export const SITUACOES = {
  ELABORACAO: { rotulo: 'Em elaboração', tom: 'azul' },
  SAUDAVEL: { rotulo: 'Saudável', tom: 'verde' },
  ATENCAO: { rotulo: 'Atenção', tom: 'amarelo' },
  CRITICA: { rotulo: 'Crítica', tom: 'vermelho' },
  CANCELADO: { rotulo: 'Cancelado', tom: 'cinza' },
};

const SITUACAO_DESCONHECIDA = { rotulo: '—', tom: 'cinza' };

export const ORIGENS_CUSTO = {
  MATERIAL: 'Material',
  MAO_DE_OBRA: 'Mão de obra',
  EQUIPAMENTO: 'Equipamento',
  SERVICO_TERCEIRIZADO: 'Serviço terceirizado',
  OUTRO: 'Outro',
};

export const LIMITE_ALERTA_PADRAO = '90';

export const situacao = (codigo) => SITUACOES[codigo] ?? SITUACAO_DESCONHECIDA;

/**
 * LocalDate do backend (2026-11-25) -> 25/11/2026, sem passar por Date (que voltaria um dia no fuso).
 * Trocar por formatarDataIso de shared/utils/formatadores.js quando feat/painel-obra entrar em develop.
 */
export const formatarDataLocal = (iso) => {
  const [ano, mes, dia] = String(iso ?? '').split('-');
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : '—';
};

/** Campo numérico vazio vira null para o backend responder "Informe ..." em vez de erro de formato. */
export const paraDecimal = (texto) => {
  const valor = String(texto ?? '')
    .trim()
    .replace(',', '.');
  return valor === '' ? null : valor;
};
