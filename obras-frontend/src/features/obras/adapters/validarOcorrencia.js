import { CLIMAS } from './diarioF3.js';

export const CATEGORIAS = {
  INTEMPERIE: 'Clima / Paralisação',
  LOGISTICA: 'Logística',
  SEGURANCA: 'Segurança',
  OUTRO: 'Outro',
};

export function normalizarDataHora(valor) {
  const texto = String(valor ?? '').trim();
  if (!texto) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(texto)) return `${texto}T00:00`;
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(texto)) return `${texto.slice(0, 10)}T${texto.slice(11, 16)}`;
  return null;
}

function paraComparacao(valor) {
  const texto = String(valor ?? '').trim();
  return texto.length === 16 ? `${texto}:00` : texto;
}

export function validarOcorrencia(formulario) {
  const erros = {};
  if (!String(formulario?.categoria ?? '').trim()) {
    erros.categoria = 'Selecione uma categoria.';
  }
  if (!String(formulario?.descricao ?? '').trim()) {
    erros.descricao = 'Informe a descrição da ocorrência.';
  }

  const paralisacao = formulario?.paralisacao;
  if (paralisacao && typeof paralisacao === 'object') {
    const inicio = String(paralisacao.inicio ?? '').trim();
    const fim = String(paralisacao.fim ?? '').trim();
    if (fim && !inicio) {
      erros['paralisacao.inicio'] = 'Informe o início da paralisação.';
    }
    if (inicio && fim) {
      const inicioDate = new Date(paraComparacao(inicio));
      const fimDate = new Date(paraComparacao(fim));
      if (Number.isNaN(inicioDate.getTime()) || Number.isNaN(fimDate.getTime()) || fimDate < inicioDate) {
        erros['paralisacao.fim'] = 'O fim da paralisação não pode ser anterior ao início.';
      }
    }
  }

  return { valido: Object.keys(erros).length === 0, erros };
}

function erroDeValidacao(erros) {
  const erro = new Error('Dados inválidos');
  erro.errosPorCampo = erros;
  return erro;
}

export function payloadOcorrencia(formulario) {
  const { valido, erros } = validarOcorrencia(formulario);
  if (!valido) throw erroDeValidacao(erros);
  const paralisacao = formulario.paralisacao;
  return {
    categoria: String(formulario.categoria).trim(),
    descricao: String(formulario.descricao).trim(),
    dataHora: normalizarDataHora(formulario.dataHora),
    paralisacao: paralisacao && typeof paralisacao === 'object'
      ? {
          inicio: normalizarDataHora(paralisacao.inicio),
          fim: normalizarDataHora(paralisacao.fim),
        }
      : null,
  };
}

export function validarOcorrenciaRapida(formulario, { requerClima = false } = {}) {
  const erros = {};
  if (!String(formulario?.data ?? '').trim()) erros.data = 'Informe a data da ocorrência.';
  if (!String(formulario?.descricao ?? '').trim()) erros.descricao = 'Informe a descrição da ocorrência.';
  if (requerClima && !Object.hasOwn(CLIMAS, String(formulario?.clima ?? ''))) {
    erros.clima = 'Informe as condições climáticas.';
  }
  return { valido: Object.keys(erros).length === 0, erros };
}

export function payloadOcorrenciaRapida(formulario, { requerClima = false } = {}) {
  const { valido, erros } = validarOcorrenciaRapida(formulario, { requerClima });
  if (!valido) throw erroDeValidacao(erros);
  const dataHora = normalizarDataHora(formulario.dataHora);
  return {
    data: String(formulario.data).trim(),
    ...(requerClima ? { clima: String(formulario.clima).trim() } : {}),
    categoria: 'OUTRO',
    descricao: String(formulario.descricao).trim(),
    ...(dataHora ? { dataHora } : {}),
  };
}
