import { http } from '../../../shared/api/http.js';

export const listarObras = () => http.get('/api/obras').then((r) => r.data);
export const buscarObra = (obraId) => http.get(`/api/obras/${obraId}`).then((r) => r.data);
export const listarEquipe = (obraId) => http.get(`/api/obras/${obraId}/equipe`).then((r) => r.data);

/** POST /api/obras (L12) → 201 com ObraDto. Exige DETALHES_OBRA:CRIAR global (ADR-11). */
export const criarObra = (dto) => http.post('/api/obras', dto).then((r) => r.data);

/** PUT /api/obras/{id} (L12) → 200 com ObraDto. Não envia status: transição é o L13. */
export const editarObra = (obraId, dto) => http.put(`/api/obras/${obraId}`, dto).then((r) => r.data);
