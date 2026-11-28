import { http } from '../../../shared/api/http.js';

export const listarObras = () => http.get('/api/obras').then((r) => r.data);
export const buscarObra = (obraId) => http.get(`/api/obras/${obraId}`).then((r) => r.data);
export const listarEquipe = (obraId) => http.get(`/api/obras/${obraId}/equipe`).then((r) => r.data);

/** POST /api/obras (L12) → 201 com ObraDto. Exige DETALHES_OBRA:CRIAR global (ADR-11). */
export const criarObra = (dto) => http.post('/api/obras', dto).then((r) => r.data);

/** PUT /api/obras/{id} (L12) → 200 com ObraDto. Não envia status: transição é o L13. */
export const editarObra = (obraId, dto) => http.put(`/api/obras/${obraId}`, dto).then((r) => r.data);

const base = (obraId) => `/api/obras/${obraId}`;

export const definirResponsavelObra = (obraId, usuarioId) =>
  http.put(`${base(obraId)}/responsavel`, { usuarioId }).then((r) => r.data);

export const iniciarObra = (obraId) => http.post(`${base(obraId)}/iniciar`).then((r) => r.data);
export const paralisarObra = (obraId) => http.post(`${base(obraId)}/paralisar`).then((r) => r.data);
export const retomarObra = (obraId) => http.post(`${base(obraId)}/retomar`).then((r) => r.data);
export const concluirObra = (obraId) => http.post(`${base(obraId)}/concluir`).then((r) => r.data);
export const cancelarObra = (obraId) => http.post(`${base(obraId)}/cancelar`).then((r) => r.data);

export const ACOES_OBRA = {
  iniciar: iniciarObra,
  paralisar: paralisarObra,
  retomar: retomarObra,
  concluir: concluirObra,
  cancelar: cancelarObra,
};
