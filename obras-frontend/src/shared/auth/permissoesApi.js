import { http } from '../api/http.js';

/** Devolve Set de "MODULO:ACAO". obraId nulo => permissões globais (individuais). */
export async function buscarPermissoes(obraId) {
  const { data } = await http.get('/api/me/permissoes', { params: obraId ? { obraId } : {} });
  return new Set(data.map((p) => `${p.modulo}:${p.acao}`));
}
