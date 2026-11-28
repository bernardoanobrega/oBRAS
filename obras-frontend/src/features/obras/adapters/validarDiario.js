import { CLIMAS } from './diarioF3.js';

export function validarDiario(formulario) {
  const erros = {};
  const data = String(formulario?.data ?? '').trim();
  const clima = String(formulario?.clima ?? '').trim();

  if (!data) erros.data = 'Informe a data do diário.';
  if (!clima || !Object.hasOwn(CLIMAS, clima)) erros.clima = 'Informe as condições climáticas.';

  return { valido: Object.keys(erros).length === 0, erros };
}

function erroDeValidacao(erros) {
  const erro = new Error('Dados inválidos');
  erro.errosPorCampo = erros;
  return erro;
}

function equipeIds(formulario) {
  const ids = Array.isArray(formulario?.equipePresenteIds) ? formulario.equipePresenteIds : [];
  return ids.filter((id) => id !== '' && id != null).map(Number).filter(Number.isInteger);
}

export function payloadNovoDiario(formulario) {
  const { valido, erros } = validarDiario(formulario);
  if (!valido) throw erroDeValidacao(erros);
  return {
    data: String(formulario.data).trim(),
    clima: String(formulario.clima).trim(),
    descricao: String(formulario.descricao ?? '').trim(),
    equipePresenteIds: equipeIds(formulario),
  };
}

export function payloadAlterarDiario(formulario) {
  const { valido, erros } = validarDiario(formulario);
  if (!valido) throw erroDeValidacao(erros);
  return {
    clima: String(formulario.clima).trim(),
    descricao: String(formulario.descricao ?? '').trim(),
    equipePresenteIds: equipeIds(formulario),
  };
}
