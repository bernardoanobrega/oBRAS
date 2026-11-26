import { http } from '../../../shared/api/http.js';

/*
 * Equipe da obra (L25). Contrato do L22 (alocações), ainda não implementado no backend.
 * Cada rota marcada com PENDENTE BACKEND é o que o front espera; se o backend mudar, ajustar aqui.
 */

/**
 * PENDENTE BACKEND (L22). GET /api/obras/{obraId}/alocacoes → 200, só alocações ativas da obra.
 * Formato esperado: [{ id, usuarioId, nome, cargos: [{ id, nome }], dataInicio: 'AAAA-MM-DD' }]
 */
export const listarAlocacoes = (obraId) => http.get(`/api/obras/${obraId}/alocacoes`).then((r) => r.data);

/** PENDENTE BACKEND (L22). POST /api/obras/{obraId}/alocacoes { usuarioId, cargoIds } → 201. Duplicado → 409/422. */
export const alocar = (obraId, dto) => http.post(`/api/obras/${obraId}/alocacoes`, dto).then((r) => r.data);

/** PENDENTE BACKEND (L22). PUT /api/obras/{obraId}/alocacoes/{id}/cargos { cargoIds } → 200. */
export const alterarCargos = (obraId, alocacaoId, dto) =>
  http.put(`/api/obras/${obraId}/alocacoes/${alocacaoId}/cargos`, dto).then((r) => r.data);

/** PENDENTE BACKEND (L22). DELETE /api/obras/{obraId}/alocacoes/{id} → 204. Encerra (data fim), não apaga. */
export const encerrarAlocacao = (obraId, alocacaoId) =>
  http.delete(`/api/obras/${obraId}/alocacoes/${alocacaoId}`).then((r) => r.data);

/** PENDENTE BACKEND (não está em nenhum lote). GET /api/cargos → [{ id, nome }]. */
export const listarCargos = () => http.get('/api/cargos').then((r) => r.data);

/**
 * PENDENTE BACKEND (não está em nenhum lote). GET /api/obras/{obraId}/alocacoes/candidatos → [{ id, nome }].
 * Usuários ativos que ainda não estão alocados na obra. Deve exigir só DETALHES_OBRA:CRIAR da obra,
 * e não USUARIOS:VISUALIZAR global (que é o que /api/usuarios exige).
 */
export const listarCandidatos = (obraId) =>
  http.get(`/api/obras/${obraId}/alocacoes/candidatos`).then((r) => r.data);
