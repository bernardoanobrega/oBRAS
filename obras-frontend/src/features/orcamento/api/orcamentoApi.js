import { http } from '../../../shared/api/http.js';

const urlOrcamento = (obraId) => `/api/obras/${obraId}/orcamento`;

export const listarOrcamentos = (signal) =>
  http.get('/api/orcamentos', { signal }).then((r) => r.data);

export const buscarOrcamento = (obraId, signal) =>
  http.get(urlOrcamento(obraId), { signal }).then((r) => r.data);

export const elaborarOrcamento = (obraId, dto) =>
  http.post(urlOrcamento(obraId), dto).then((r) => r.data);

export const cancelarOrcamento = (obraId) =>
  http.patch(`${urlOrcamento(obraId)}/cancelar`).then((r) => r.data);

export const adicionarItem = (obraId, dto) =>
  http.post(`${urlOrcamento(obraId)}/itens`, dto).then((r) => r.data);

export const atualizarItem = (obraId, itemId, dto) =>
  http.put(`${urlOrcamento(obraId)}/itens/${itemId}`, dto).then((r) => r.data);

export const removerItem = (obraId, itemId) =>
  http.delete(`${urlOrcamento(obraId)}/itens/${itemId}`);

const urlCustos = (obraId, itemId) => `${urlOrcamento(obraId)}/itens/${itemId}/custos`;

export const listarCustos = (obraId, itemId, signal) =>
  http.get(urlCustos(obraId, itemId), { signal }).then((r) => r.data);

export const registrarCusto = (obraId, itemId, dto) =>
  http.post(urlCustos(obraId, itemId), dto).then((r) => r.data);

export const editarCusto = (obraId, itemId, custoId, dto) =>
  http.put(`${urlCustos(obraId, itemId)}/${custoId}`, dto).then((r) => r.data);

export const removerCusto = (obraId, itemId, custoId) =>
  http.delete(`${urlCustos(obraId, itemId)}/${custoId}`);

export const registrarDecisao = (obraId, dto) =>
  http.post(`${urlOrcamento(obraId)}/decisoes`, dto).then((r) => r.data);

export const editarDecisao = (obraId, decisaoId, dto) =>
  http.put(`${urlOrcamento(obraId)}/decisoes/${decisaoId}`, dto).then((r) => r.data);
