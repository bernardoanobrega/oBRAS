import { http } from '../../../shared/api/http.js';

// Fornecedores (/api/fornecedores)
export const listarFornecedores = (apenasAtivos = false) =>
  http.get('/api/fornecedores', { params: { apenasAtivos } }).then((r) => r.data);

export const buscarFornecedor = (id) =>
  http.get(`/api/fornecedores/${id}`).then((r) => r.data);

export const cadastrarFornecedor = (dto) =>
  http.post('/api/fornecedores', dto).then((r) => r.data);

export const atualizarFornecedor = (id, dto) =>
  http.put(`/api/fornecedores/${id}`, dto).then((r) => r.data);

export const desativarFornecedor = (id) =>
  http.patch(`/api/fornecedores/${id}/desativar`).then((r) => r.data);

export const listarHistoricoFornecedor = (id) =>
  http.get(`/api/fornecedores/${id}/historico-compras`).then((r) => r.data);

// Pedidos de Compra da Obra (/api/obras/{obraId}/pedidos-compra)
export const listarPedidosCompra = (obraId, status) =>
  http
    .get(`/api/obras/${obraId}/pedidos-compra`, { params: status ? { status } : {} })
    .then((r) => r.data);

export const buscarPedidoCompra = (obraId, pedidoId) =>
  http.get(`/api/obras/${obraId}/pedidos-compra/${pedidoId}`).then((r) => r.data);

export const criarPedidoCompra = (obraId, dto) =>
  http.post(`/api/obras/${obraId}/pedidos-compra`, dto).then((r) => r.data);

export const adicionarItemPedido = (obraId, pedidoId, dto) =>
  http.post(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/itens`, dto).then((r) => r.data);

export const removerItemPedido = (obraId, pedidoId, itemId) =>
  http.delete(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/itens/${itemId}`).then((r) => r.data);

export const ajustarValorUnitarioItem = (obraId, pedidoId, itemId, valorUnitario) =>
  http
    .put(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/itens/${itemId}/valor-unitario`, {
      valorUnitario,
    })
    .then((r) => r.data);

export const registrarCotacao = (obraId, pedidoId, dto) =>
  http.post(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/cotacoes`, dto).then((r) => r.data);

export const selecionarCotacao = (obraId, pedidoId, cotacaoId) =>
  http
    .put(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/cotacoes/${cotacaoId}/selecionar`)
    .then((r) => r.data);

export const alterarStatusPedido = (obraId, pedidoId, status) =>
  http
    .patch(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/status`, { status })
    .then((r) => r.data);

export const cancelarPedidoCompra = (obraId, pedidoId) =>
  http.post(`/api/obras/${obraId}/pedidos-compra/${pedidoId}/cancelar`).then((r) => r.data);

// Consulta ao catálogo de materiais para adição de itens no pedido
export const listarMateriaisCatalogo = () =>
  http.get('/api/materiais').then((r) => r.data);