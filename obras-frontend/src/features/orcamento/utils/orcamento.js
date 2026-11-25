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

/** Nome da obra; usa o número se a obra não estiver na lista do usuário. */
export const rotuloObra = (obraId, obras = []) =>
  obras.find((obra) => obra.id === obraId)?.nome ?? `Obra #${obraId}`;

/** Campo numérico vazio vira null para o backend responder "Informe ..." em vez de erro de formato. */
export const paraDecimal = (texto) => {
  const valor = String(texto ?? '')
    .trim()
    .replace(',', '.');
  return valor === '' ? null : valor;
};
