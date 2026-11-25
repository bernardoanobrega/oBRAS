import { http } from '../../../shared/api/http.js';

// Catálogo global de materiais (/api/materiais)
export const listarMateriais = (busca) =>
  http.get('/api/materiais', { params: busca ? { busca } : {} }).then((r) => r.data);

export const buscarMaterial = (id) =>
  http.get(`/api/materiais/${id}`).then((r) => r.data);

export const criarMaterial = (dto) =>
  http.post('/api/materiais', dto).then((r) => r.data);

export const atualizarMaterial = (id, dto) =>
  http.put(`/api/materiais/${id}`, dto).then((r) => r.data);

// Operações de estoque por obra (/api/obras/{obraId}/estoque)
export const consultarSaldos = (obraId, canteiroId) =>
  http
    .get(`/api/obras/${obraId}/estoque/saldos`, {
      params: canteiroId ? { canteiroId } : {},
    })
    .then((r) => r.data);

export const consultarSaldoConsolidado = (obraId) =>
  http.get(`/api/obras/${obraId}/estoque/saldos/consolidado`).then((r) => r.data);

export const configurarSaldo = (obraId, dto) =>
  http.put(`/api/obras/${obraId}/estoque/saldos`, dto).then((r) => r.data);

export const listarMovimentacoes = (obraId, filtros = {}) => {
  const params = {};
  if (filtros.canteiroId) params.canteiroId = filtros.canteiroId;
  if (filtros.tipo) params.tipo = filtros.tipo;
  if (filtros.inicio) params.inicio = filtros.inicio;
  if (filtros.fim) params.fim = filtros.fim;
  return http.get(`/api/obras/${obraId}/estoque/movimentacoes`, { params }).then((r) => r.data);
};

export const registrarEntradaEstoque = (obraId, dto) =>
  http.post(`/api/obras/${obraId}/estoque/entradas`, dto).then((r) => r.data);

export const registrarSaidaEstoque = (obraId, dto) =>
  http.post(`/api/obras/${obraId}/estoque/saidas`, dto).then((r) => r.data);

export const registrarTransferenciaEstoque = (obraId, dto) =>
  http.post(`/api/obras/${obraId}/estoque/transferencias`, dto).then((r) => r.data);

export const solicitarReposicao = (obraId, dto) =>
  http.post(`/api/obras/${obraId}/estoque/reposicoes`, dto).then((r) => r.data);

// Consulta de pedidos EM_TRANSITO da obra para popular o modal de Entrada por Compra
export const listarPedidosEmTransito = (obraId) =>
  http
    .get(`/api/obras/${obraId}/pedidos-compra`, { params: { status: 'EM_TRANSITO' } })
    .then((r) => r.data);