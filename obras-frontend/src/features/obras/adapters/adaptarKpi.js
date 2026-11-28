export const TOOLTIP_SIMULADO = 'Valor estimado/simulado; não representa medição real.';
const TOOLTIP_INDISPONIVEL = 'Não há valor disponível para este indicador.';
const TOOLTIP_REAL = 'Valor informado pelo painel.';

export function apresentarKpi(valor, fonte) {
  const indisponivel = valor === null || valor === undefined;
  const simulado = fonte === 'SIMULADO';
  return {
    valor: indisponivel ? '—' : valor,
    disponivel: !indisponivel,
    rotulo: indisponivel ? 'Não disponível' : null,
    fonte: fonte ?? null,
    simulado,
    tooltip: simulado ? TOOLTIP_SIMULADO : indisponivel ? TOOLTIP_INDISPONIVEL : TOOLTIP_REAL,
  };
}

export function calcularKpi(fn, ...valores) {
  if (valores.some((valor) => valor === null || valor === undefined)) return null;
  return fn(...valores);
}

export function tooltipFonte(fonte) {
  return fonte === 'SIMULADO' ? TOOLTIP_SIMULADO : TOOLTIP_REAL;
}
