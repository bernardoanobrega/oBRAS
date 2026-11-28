import { errosPorCampo, mensagemErro } from '../../../shared/api/http.js';

export function estadoDoErro(err) {
  return { erros: errosPorCampo(err), erroGeral: mensagemErro(err) };
}
