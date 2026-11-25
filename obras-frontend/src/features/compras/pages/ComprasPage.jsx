import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardList,
  Clock,
  FileText,
  Flame,
  MapPin,
  MoreVertical,
  PackageCheck,
  Phone,
  Plus,
  Scale,
  ShoppingCart,
  Trash2,
  Truck,
  User,
  Users,
  Wallet,
  Warehouse,
} from 'lucide-react';
import Button from '../../../shared/components/ui/Button.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Badge from '../../../shared/components/ui/Badge.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Can from '../../../shared/auth/Can.jsx';
import { mensagemErro } from '../../../shared/api/http.js';
import { formatarData, formatarDataHora, formatarMoeda } from '../../../shared/utils/formatadores.js';
import {
  adicionarItemPedido,
  alterarStatusPedido,
  buscarPedidoCompra,
  cadastrarFornecedor,
  cancelarPedidoCompra,
  criarPedidoCompra,
  listarFornecedores,
  listarMateriaisCatalogo,
  listarPedidosCompra,
  registrarCotacao,
  removerItemPedido,
  selecionarCotacao,
} from '../api/comprasApi.js';

const COLUNAS_KANBAN = [
  {
    status: 'SOLICITADO',
    titulo: 'Solicitado',
    dotColor: 'bg-neutro',
    icon: FileText,
    proximo: 'EM_COTACAO',
    rotuloProximo: 'Iniciar Cotação',
  },
  {
    status: 'EM_COTACAO',
    titulo: 'Em Cotação',
    dotColor: 'bg-info',
    icon: Scale,
    proximo: 'COMPRADO',
    rotuloProximo: 'Aprovar Compra',
  },
  {
    status: 'COMPRADO',
    titulo: 'Comprado',
    dotColor: 'bg-brand-claro',
    icon: ShoppingCart,
    proximo: 'EM_TRANSITO',
    rotuloProximo: 'Marcar em Trânsito',
  },
  {
    status: 'EM_TRANSITO',
    titulo: 'Em Trânsito',
    dotColor: 'bg-brand',
    icon: Truck,
    proximo: null, // Entrega é registrada via Entrada no Estoque
    rotuloProximo: null,
  },
  {
    status: 'ENTREGUE',
    titulo: 'Entregue',
    dotColor: 'bg-sucesso',
    icon: PackageCheck,
    proximo: null,
    rotuloProximo: null,
  },
];

export default function ComprasPage() {
  const { obraId } = useParams();
  const [pedidos, setPedidos] = useState(null);
  const [fornecedores, setFornecedores] = useState([]);
  const [materiais, setMateriais] = useState([]);
  const [exibirCancelados, setExibirCancelados] = useState(false);
  const [erro, setErro] = useState(null);

  // Modal Novo Pedido
  const [modalNovoPedido, setModalNovoPedido] = useState(false);
  const [formNovoPedido, setFormNovoPedido] = useState({
    prioridade: 'MEDIA',
    dataPrevista: '',
  });

  // Modal Novo Fornecedor Rápido
  const [modalFornecedor, setModalFornecedor] = useState(false);
  const [formFornecedor, setFormFornecedor] = useState({
    nomeRazaoSocial: '',
    cpfCnpj: '',
    email: '',
    telefone: '',
  });

  // Modal de Detalhes / Gestão do Pedido (Itens, Cotações, Transições de Status)
  const [pedidoDetalhe, setPedidoDetalhe] = useState(null);
  const [carregandoDetalhe, setCarregandoDetalhe] = useState(false);
  const [erroDetalhe, setErroDetalhe] = useState(null);
  const [formItem, setFormItem] = useState({
    materialId: '',
    quantidade: '1',
    valorUnitario: '0',
    itemOrcadoId: '',
  });
  const [formCotacao, setFormCotacao] = useState({
    fornecedorId: '',
    valorTotalProposto: '',
    prazoEntregaDias: '5',
  });

  const carregarTudo = useCallback(async () => {
    setErro(null);
    try {
      const [listaPedidos, listaForn, listaMat] = await Promise.all([
        listarPedidosCompra(obraId),
        listarFornecedores(false).catch(() => []),
        listarMateriaisCatalogo().catch(() => []),
      ]);
      setPedidos(listaPedidos);
      setFornecedores(listaForn);
      setMateriais(listaMat);
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, [obraId]);

  useEffect(() => {
    carregarTudo();
  }, [carregarTudo]);

  // 4 KPI Cards do Topo (conforme 1440w light.svg)
  const kpis = useMemo(() => {
    const lista = pedidos || [];
    const ativos = lista.filter((p) => p.status !== 'CANCELADO');
    const emAberto = ativos.filter((p) => p.status !== 'ENTREGUE').length;
    const valorComprometido = ativos.reduce(
      (acc, p) => acc + Number(p.valorTotal || 0),
      0,
    );
    const emTransito = ativos.filter((p) => p.status === 'EM_TRANSITO').length;
    const urgentesOuAtrasados = ativos.filter(
      (p) => p.atrasado || (p.prioridade === 'ALTA' && p.status !== 'ENTREGUE'),
    ).length;
    return { emAberto, valorComprometido, emTransito, urgentesOuAtrasados };
  }, [pedidos]);

  async function abrirDetalhesPedido(pedidoId) {
    setErroDetalhe(null);
    setCarregandoDetalhe(true);
    try {
      const dto = await buscarPedidoCompra(obraId, pedidoId);
      setPedidoDetalhe(dto);
      if (materiais.length > 0 && !formItem.materialId) {
        setFormItem((f) => ({ ...f, materialId: String(materiais[0].id) }));
      }
      const ativos = fornecedores.filter((f) => f.situacao === 'ATIVO');
      if (ativos.length > 0 && !formCotacao.fornecedorId) {
        setFormCotacao((f) => ({ ...f, fornecedorId: String(ativos[0].id) }));
      }
    } catch (e) {
      setErro(mensagemErro(e));
    } finally {
      setCarregandoDetalhe(false);
    }
  }

  async function handleCriarPedido(e) {
    e.preventDefault();
    try {
      const novo = await criarPedidoCompra(obraId, {
        prioridade: formNovoPedido.prioridade,
        dataPrevista: formNovoPedido.dataPrevista || null,
      });
      setModalNovoPedido(false);
      await carregarTudo();
      abrirDetalhesPedido(novo.id);
    } catch (err) {
      setErro(mensagemErro(err));
    }
  }

  async function handleAdicionarItem(e) {
    e.preventDefault();
    setErroDetalhe(null);
    try {
      const res = await adicionarItemPedido(obraId, pedidoDetalhe.id, {
        materialId: Number(formItem.materialId),
        quantidade: Number(formItem.quantidade),
        valorUnitario: Number(formItem.valorUnitario),
        itemOrcadoId: formItem.itemOrcadoId ? Number(formItem.itemOrcadoId) : null,
      });
      setPedidoDetalhe(res.pedido);
      if (res.excedeOrcamento) {
        setErroDetalhe('Atenção: este item excede o saldo previsto no item orçado vinculado!');
      }
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleRemoverItem(itemId) {
    setErroDetalhe(null);
    try {
      const atualizado = await removerItemPedido(obraId, pedidoDetalhe.id, itemId);
      setPedidoDetalhe(atualizado);
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleRegistrarCotacao(e) {
    e.preventDefault();
    setErroDetalhe(null);
    try {
      const atualizado = await registrarCotacao(obraId, pedidoDetalhe.id, {
        fornecedorId: Number(formCotacao.fornecedorId),
        valorTotalProposto: Number(formCotacao.valorTotalProposto),
        prazoEntregaDias: Number(formCotacao.prazoEntregaDias),
      });
      setPedidoDetalhe(atualizado);
      setFormCotacao((f) => ({ ...f, valorTotalProposto: '' }));
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleSelecionarCotacao(cotacaoId) {
    setErroDetalhe(null);
    try {
      const atualizado = await selecionarCotacao(obraId, pedidoDetalhe.id, cotacaoId);
      setPedidoDetalhe(atualizado);
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleAvancarStatus(novoStatus) {
    setErroDetalhe(null);
    try {
      const atualizado = await alterarStatusPedido(obraId, pedidoDetalhe.id, novoStatus);
      setPedidoDetalhe(atualizado);
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleCancelarPedido() {
    if (!window.confirm(`Cancelar o pedido #${pedidoDetalhe.id}?`)) return;
    setErroDetalhe(null);
    try {
      const atualizado = await cancelarPedidoCompra(obraId, pedidoDetalhe.id);
      setPedidoDetalhe(atualizado);
      carregarTudo();
    } catch (err) {
      setErroDetalhe(mensagemErro(err));
    }
  }

  async function handleCriarFornecedor(e) {
    e.preventDefault();
    try {
      await cadastrarFornecedor(formFornecedor);
      setModalFornecedor(false);
      setFormFornecedor({ nomeRazaoSocial: '', cpfCnpj: '', email: '', telefone: '' });
      carregarTudo();
    } catch (err) {
      setErro(mensagemErro(err));
    }
  }

  return (
    <div className="space-y-6 rounded-2xl bg-fundo p-5 text-texto shadow-xl">
      {/* Barra de Ferramentas Superior (idêntica a 1440w light.svg) */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center rounded-lg border border-borda bg-superficie px-3 py-2 text-sm text-texto">
            <Warehouse size={16} className="mr-2 text-brand" />
            <span>Central de Suprimentos — Obra #{obraId}</span>
          </div>

          <button
            type="button"
            onClick={() => setExibirCancelados((v) => !v)}
            className="flex items-center gap-2 rounded-lg border border-borda bg-superficie px-3.5 py-2 text-xs font-medium text-texto-suave hover:bg-superficie-alta"
          >
            <span>{exibirCancelados ? 'Ocultar Cancelados' : 'Ver Cancelados'}</span>
          </button>

          <Can modulo="COMPRAS_FORNECEDORES" acao="CRIAR" obraId={obraId}>
            <button
              type="button"
              onClick={() => setModalNovoPedido(true)}
              className="flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-fundo transition hover:bg-brand-claro"
            >
              <Plus size={16} />
              <span>Novo Pedido</span>
            </button>
          </Can>
        </div>
      </div>

      {/* 4 KPI Cards Superiores (idênticos a 1440w light.svg) */}
      <div className="grid grid-cols-1 gap-4 rounded-xl border border-borda bg-superficie p-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="flex items-center gap-3 rounded-xl border border-borda bg-superficie-alta p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand-fundo text-brand-claro">
            <ClipboardList size={20} />
          </div>
          <div>
            <p className="text-xs text-texto-fraco">Pedidos em Aberto</p>
            <p className="text-xl font-bold text-texto">{kpis.emAberto}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 rounded-xl border border-borda bg-superficie-alta p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-info-fundo text-info">
            <Wallet size={20} />
          </div>
          <div>
            <p className="text-xs text-texto-fraco">Valor Comprometido</p>
            <p className="text-xl font-bold text-texto">
              {formatarMoeda(kpis.valorComprometido)}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3 rounded-xl border border-borda bg-superficie-alta p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-sucesso-fundo text-sucesso">
            <Truck size={20} />
          </div>
          <div>
            <p className="text-xs text-texto-fraco">Em Trânsito</p>
            <p className="text-xl font-bold text-sucesso">{kpis.emTransito}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 rounded-xl border border-borda bg-superficie-alta p-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-perigo-fundo text-perigo">
            <AlertTriangle size={20} />
          </div>
          <div>
            <p className="text-xs text-texto-fraco">Urgentes / Atrasados</p>
            <p className="text-xl font-bold text-perigo">{kpis.urgentesOuAtrasados}</p>
          </div>
        </div>
      </div>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregarTudo} />}
      {!erro && !pedidos && <Spinner className="mt-8" />}

      {/* Quadro Kanban de 5 Colunas (pedidocompra-states.puml / 1440w light.svg) */}
      {pedidos && (
        <div className="rounded-xl border border-borda bg-superficie p-4">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xs font-semibold uppercase tracking-wider text-texto-fraco">
              Fluxo de Pedidos de Compra (Clique em um card para gerenciar itens e cotações)
            </h2>
            <span className="rounded-md bg-superficie-alta px-3 py-1 text-xs text-texto-fraco">
              {pedidos.length} pedido(s)
            </span>
          </div>

          <div className="grid grid-cols-1 gap-3 lg:grid-cols-5">
            {COLUNAS_KANBAN.map((col) => {
              const IconeCol = col.icon;
              const cards = pedidos.filter((p) => p.status === col.status);
              return (
                <div
                  key={col.status}
                  className="flex min-h-[380px] flex-col rounded-xl border border-borda bg-superficie p-3"
                >
                  {/* Cabeçalho da Coluna */}
                  <div className="mb-3 flex items-center justify-between px-1">
                    <div className="flex items-center gap-2">
                      <span className={`h-2 w-2 rounded-full ${col.dotColor}`} />
                      <IconeCol size={15} className="text-texto-suave" />
                      <span className="text-xs font-semibold text-texto">
                        {col.titulo}
                      </span>
                    </div>
                    <span className="rounded bg-superficie-alta px-2 py-0.5 text-[11px] font-semibold text-texto-suave">
                      {cards.length}
                    </span>
                  </div>

                  {/* Cards da Coluna */}
                  <div className="flex flex-1 flex-col gap-3">
                    {cards.map((p) => {
                      const urgente = p.prioridade === 'ALTA' || p.atrasado;
                      return (
                        <button
                          key={p.id}
                          type="button"
                          onClick={() => abrirDetalhesPedido(p.id)}
                          className={`group relative flex flex-col justify-between rounded-xl border bg-superficie-alta p-3.5 text-left transition hover:bg-superficie-alta ${
                            urgente
                              ? 'border-l-4 border-borda border-l-perigo'
                              : 'border-borda'
                          }`}
                        >
                          {/* Topo do Card: Código + Badge de chama (urgente) ou Prioridade */}
                          <div className="flex items-center justify-between gap-2">
                            <span className="rounded bg-superficie-alta px-2 py-0.5 text-xs font-semibold text-texto">
                              #{p.id}
                            </span>
                            {urgente ? (
                              <span className="inline-flex items-center gap-1 rounded border border-perigo/40 bg-perigo-fundo px-2 py-0.5 text-[11px] font-medium text-perigo">
                                <Flame size={12} className="text-perigo" />
                                {p.atrasado ? 'Atrasado' : 'Urgente'}
                              </span>
                            ) : (
                              <span className="rounded bg-superficie-alta/80 px-2 py-0.5 text-[11px] text-texto-fraco">
                                {p.prioridade}
                              </span>
                            )}
                          </div>

                          {/* Corpo do Card */}
                          <div className="my-3 border-b border-borda pb-3">
                            <p className="text-sm font-semibold text-texto">
                              {formatarMoeda(p.valorTotal)}
                            </p>
                            <p className="mt-0.5 truncate text-xs text-texto-fraco">
                              {p.fornecedorNome || 'Aguardando definição de fornecedor'}
                            </p>
                          </div>

                          {/* Rodapé do Card (ícones conforme 1440w light.svg) */}
                          <div className="flex items-center justify-between text-xs text-texto-fraco">
                            {col.status === 'EM_COTACAO' ? (
                              <span className="flex items-center gap-1 text-info">
                                <Users size={13} /> Cotando fornecedores
                              </span>
                            ) : col.status === 'EM_TRANSITO' ? (
                              <span className="flex items-center gap-1 text-brand-claro">
                                <Truck size={13} /> Em rota de entrega
                              </span>
                            ) : col.status === 'ENTREGUE' ? (
                              <span className="inline-flex items-center gap-1 rounded border border-sucesso/30 bg-sucesso-fundo px-2 py-0.5 text-[11px] text-sucesso">
                                <CheckCircle2 size={12} /> Recebido no Estoque
                              </span>
                            ) : (
                              <span className="flex items-center gap-1">
                                <Clock size={12} />{' '}
                                {p.dataPrevista
                                  ? `Prev: ${formatarData(p.dataPrevista)}`
                                  : formatarData(p.dataSolicitacao)}
                              </span>
                            )}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>

          {exibirCancelados && (
            <div className="mt-4 border-t border-borda pt-3">
              <h3 className="mb-2 text-xs font-semibold uppercase text-texto-fraco">
                Pedidos Cancelados
              </h3>
              <div className="flex flex-wrap gap-2">
                {pedidos
                  .filter((p) => p.status === 'CANCELADO')
                  .map((p) => (
                    <button
                      key={p.id}
                      type="button"
                      onClick={() => abrirDetalhesPedido(p.id)}
                      className="rounded-lg border border-borda bg-superficie-alta px-3 py-1.5 text-xs text-texto-fraco hover:text-texto"
                    >
                      Pedido #{p.id} — {formatarMoeda(p.valorTotal)} (CANCELADO)
                    </button>
                  ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Seção Inferior: Catálogo Rápido de Fornecedores (idêntica a 1440w light.svg) */}
      <div className="rounded-xl border border-borda bg-superficie p-4">
        <div className="mb-4 flex items-center justify-between">
          <Can modulo="COMPRAS_FORNECEDORES" acao="CRIAR" obraId={obraId}>
            <button
              type="button"
              onClick={() => setModalFornecedor(true)}
              className="flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-fundo hover:bg-brand-claro"
            >
              <Plus size={16} /> Novo Fornecedor
            </button>
          </Can>
          <span className="text-xs text-texto-fraco">
            Fornecedores Cadastrados ({fornecedores.length})
          </span>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {fornecedores.slice(0, 4).map((f) => (
            <div
              key={f.id}
              className="flex flex-col justify-between rounded-xl border border-borda bg-superficie-alta p-4"
            >
              <div>
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="font-semibold text-texto">{f.nomeRazaoSocial}</h3>
                    <p className="mt-1 flex items-center gap-1 text-xs text-sucesso">
                      <MapPin size={12} /> {f.cpfCnpj}
                    </p>
                  </div>
                  <MoreVertical size={15} className="text-texto-suave" />
                </div>

                <div className="my-3 border-t border-borda pt-3 text-xs text-texto-suave space-y-1">
                  <p className="flex items-center gap-2">
                    <User size={13} className="text-texto-fraco" />
                    <span className="truncate">{f.email || 'Contato comercial'}</span>
                  </p>
                  <p className="flex items-center gap-2">
                    <Phone size={13} className="text-texto-fraco" />
                    <span>{f.telefone || '—'}</span>
                  </p>
                </div>

                <div className="flex gap-1.5">
                  <span className="rounded bg-superficie-alta px-2 py-0.5 text-[11px] text-texto-suave">
                    {f.situacao}
                  </span>
                  <span className="rounded bg-superficie-alta px-2 py-0.5 text-[11px] text-texto-fraco">
                    Fornecedor #{f.id}
                  </span>
                </div>
              </div>

              <div className="mt-4 flex items-center gap-2">
                <span className="flex-1 rounded-lg bg-menu-ativo py-1.5 text-center text-xs text-info">
                  Homologado
                </span>
                <button
                  type="button"
                  onClick={() => {
                    setFormCotacao((c) => ({ ...c, fornecedorId: String(f.id) }));
                    const emCotacao = (pedidos || []).find((p) => p.status === 'EM_COTACAO');
                    if (emCotacao) abrirDetalhesPedido(emCotacao.id);
                  }}
                  className="flex flex-1 items-center justify-center gap-1 rounded-lg bg-brand py-1.5 text-xs font-semibold text-fundo hover:bg-brand-claro"
                >
                  <Plus size={13} /> Cotar
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Modal: Criar Novo Pedido */}
      <Modal
        aberto={modalNovoPedido}
        onFechar={() => setModalNovoPedido(false)}
        titulo="Novo Pedido de Compra"
      >
        <form onSubmit={handleCriarPedido} className="space-y-3 text-texto">
          <div>
            <label className="mb-1 block text-sm font-medium text-texto">
              Prioridade do Pedido
            </label>
            <select
              value={formNovoPedido.prioridade}
              onChange={(e) =>
                setFormNovoPedido((f) => ({ ...f, prioridade: e.target.value }))
              }
              className="min-h-[38px] w-full rounded-lg border border-borda-forte px-3 text-sm"
            >
              <option value="BAIXA">BAIXA</option>
              <option value="MEDIA">MÉDIA</option>
              <option value="ALTA">ALTA (Urgente)</option>
            </select>
          </div>
          <Input
            label="Data Prevista de Entrega (opcional)"
            type="date"
            name="dataPrevista"
            value={formNovoPedido.dataPrevista}
            onChange={(e) =>
              setFormNovoPedido((f) => ({ ...f, dataPrevista: e.target.value }))
            }
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button variante="secundario" onClick={() => setModalNovoPedido(false)}>
              Cancelar
            </Button>
            <Button type="submit">Criar Pedido</Button>
          </div>
        </form>
      </Modal>

      {/* Modal: Novo Fornecedor Rápido */}
      <Modal
        aberto={modalFornecedor}
        onFechar={() => setModalFornecedor(false)}
        titulo="Cadastrar Fornecedor"
      >
        <form onSubmit={handleCriarFornecedor} className="space-y-3 text-texto">
          <Input
            label="Nome / Razão Social"
            value={formFornecedor.nomeRazaoSocial}
            onChange={(e) =>
              setFormFornecedor((f) => ({ ...f, nomeRazaoSocial: e.target.value }))
            }
            required
          />
          <Input
            label="CPF ou CNPJ"
            value={formFornecedor.cpfCnpj}
            onChange={(e) =>
              setFormFornecedor((f) => ({ ...f, cpfCnpj: e.target.value }))
            }
            required
          />
          <Input
            label="E-mail"
            type="email"
            value={formFornecedor.email}
            onChange={(e) =>
              setFormFornecedor((f) => ({ ...f, email: e.target.value }))
            }
          />
          <Input
            label="Telefone"
            value={formFornecedor.telefone}
            onChange={(e) =>
              setFormFornecedor((f) => ({ ...f, telefone: e.target.value }))
            }
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button variante="secundario" onClick={() => setModalFornecedor(false)}>
              Cancelar
            </Button>
            <Button type="submit">Salvar Fornecedor</Button>
          </div>
        </form>
      </Modal>

      {/* Modal Completo de Detalhes do Pedido (Itens, Cotações, Seleção e Transição de Estado) */}
      <Modal
        aberto={!!pedidoDetalhe || carregandoDetalhe}
        onFechar={() => setPedidoDetalhe(null)}
        titulo={
          pedidoDetalhe
            ? `Pedido de Compra #${pedidoDetalhe.id} — ${pedidoDetalhe.status}`
            : 'Carregando pedido...'
        }
        largura="max-w-3xl"
      >
        {carregandoDetalhe && <Spinner />}
        {pedidoDetalhe && (
          <div className="space-y-5 text-texto">
            {erroDetalhe && (
              <p role="alert" className="rounded bg-perigo-fundo p-2.5 text-sm text-perigo">
                {erroDetalhe}
              </p>
            )}

            {/* Resumo e Ações de Transição de Estado */}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-superficie-alta p-3.5">
              <div className="space-y-0.5 text-sm">
                <p>
                  <strong>Status:</strong> <Badge tom="azul">{pedidoDetalhe.status}</Badge>{' '}
                  <strong className="ml-2">Prioridade:</strong>{' '}
                  <Badge tom={pedidoDetalhe.prioridade === 'ALTA' ? 'vermelho' : 'cinza'}>
                    {pedidoDetalhe.prioridade}
                  </Badge>
                </p>
                <p className="text-xs text-texto-suave">
                  Solicitado em {formatarData(pedidoDetalhe.dataSolicitacao)} • Fornecedor:{' '}
                  <strong>{pedidoDetalhe.fornecedorNome || 'Não selecionado'}</strong> • Total:{' '}
                  <strong>{formatarMoeda(pedidoDetalhe.valorTotal)}</strong>
                </p>
              </div>

              <div className="flex flex-wrap gap-2">
                <Can modulo="COMPRAS_FORNECEDORES" acao="EDITAR" obraId={obraId}>
                  {pedidoDetalhe.status === 'SOLICITADO' && (
                    <Button onClick={() => handleAvancarStatus('EM_COTACAO')}>
                      Enviar para Cotação
                    </Button>
                  )}
                  {pedidoDetalhe.status === 'EM_COTACAO' && (
                    <Button onClick={() => handleAvancarStatus('COMPRADO')}>
                      Confirmar Compra
                    </Button>
                  )}
                  {pedidoDetalhe.status === 'COMPRADO' && (
                    <Button onClick={() => handleAvancarStatus('EM_TRANSITO')}>
                      Despachar (Em Trânsito)
                    </Button>
                  )}
                  {(pedidoDetalhe.status === 'SOLICITADO' ||
                    pedidoDetalhe.status === 'EM_COTACAO') && (
                    <Button variante="perigo" onClick={handleCancelarPedido}>
                      Cancelar Pedido
                    </Button>
                  )}
                </Can>
              </div>
            </div>

            {/* 1. Itens do Pedido */}
            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-texto">
                Itens do Pedido ({pedidoDetalhe.itens.length})
              </h3>
              {pedidoDetalhe.itens.length === 0 ? (
                <p className="text-xs text-texto-suave">
                  Nenhum item adicionado. Adicione ao menos 1 item para iniciar a cotação.
                </p>
              ) : (
                <table className="w-full text-sm">
                  <thead className="border-b text-left text-xs text-texto-suave">
                    <tr>
                      <th className="py-1.5">Material</th>
                      <th className="py-1.5">Qtd.</th>
                      <th className="py-1.5">Vlr. Unitário</th>
                      <th className="py-1.5">Subtotal</th>
                      <th className="py-1.5 text-right">Ação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pedidoDetalhe.itens.map((i) => (
                      <tr key={i.id} className="border-b last:border-0">
                        <td className="py-2 font-medium">
                          {i.materialNome} ({i.unidadeMedida})
                        </td>
                        <td className="py-2">{i.quantidade}</td>
                        <td className="py-2">{formatarMoeda(i.valorUnitario)}</td>
                        <td className="py-2 font-medium">{formatarMoeda(i.subtotal)}</td>
                        <td className="py-2 text-right">
                          {pedidoDetalhe.status === 'SOLICITADO' && (
                            <button
                              type="button"
                              onClick={() => handleRemoverItem(i.id)}
                              className="text-perigo hover:text-perigo"
                              title="Remover item"
                            >
                              <Trash2 size={15} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {pedidoDetalhe.status === 'SOLICITADO' && (
                <Can modulo="COMPRAS_FORNECEDORES" acao="EDITAR" obraId={obraId}>
                  <form
                    onSubmit={handleAdicionarItem}
                    className="grid grid-cols-1 gap-2 rounded-lg border border-borda p-3 sm:grid-cols-4"
                  >
                    <select
                      value={formItem.materialId}
                      onChange={(e) =>
                        setFormItem((f) => ({ ...f, materialId: e.target.value }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    >
                      <option value="">Material...</option>
                      {materiais.map((m) => (
                        <option key={m.id} value={m.id}>
                          {m.nome} ({m.unidadeMedida})
                        </option>
                      ))}
                    </select>
                    <input
                      type="number"
                      step="0.001"
                      min="0.001"
                      placeholder="Quantidade"
                      value={formItem.quantidade}
                      onChange={(e) =>
                        setFormItem((f) => ({ ...f, quantidade: e.target.value }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    />
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="Valor Unit. (R$)"
                      value={formItem.valorUnitario}
                      onChange={(e) =>
                        setFormItem((f) => ({ ...f, valorUnitario: e.target.value }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    />
                    <Button type="submit">Adicionar Item</Button>
                  </form>
                </Can>
              )}
            </div>

            {/* 2. Cotações de Fornecedores */}
            <div className="space-y-2 border-t pt-3">
              <h3 className="text-sm font-semibold text-texto">
                Cotações Registradas ({pedidoDetalhe.cotacoes.length})
              </h3>
              {pedidoDetalhe.cotacoes.length > 0 && (
                <table className="w-full text-sm">
                  <thead className="border-b text-left text-xs text-texto-suave">
                    <tr>
                      <th className="py-1.5">Fornecedor</th>
                      <th className="py-1.5">Valor Proposto</th>
                      <th className="py-1.5">Prazo (dias)</th>
                      <th className="py-1.5">Status</th>
                      <th className="py-1.5 text-right">Aprovação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pedidoDetalhe.cotacoes.map((c) => (
                      <tr key={c.id} className="border-b last:border-0">
                        <td className="py-2 font-medium">{c.fornecedorNome}</td>
                        <td className="py-2">{formatarMoeda(c.valorTotalProposto)}</td>
                        <td className="py-2">{c.prazoEntregaDias} dias</td>
                        <td className="py-2">
                          {c.selecionada ? (
                            <Badge tom="verde">Vencedora</Badge>
                          ) : (
                            <Badge tom="cinza">Proposta</Badge>
                          )}
                        </td>
                        <td className="py-2 text-right">
                          {pedidoDetalhe.status === 'EM_COTACAO' && !c.selecionada && (
                            <Can
                              modulo="COMPRAS_FORNECEDORES"
                              acao="APROVAR"
                              obraId={obraId}
                            >
                              <Button
                                variante="secundario"
                                onClick={() => handleSelecionarCotacao(c.id)}
                              >
                                Selecionar
                              </Button>
                            </Can>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {pedidoDetalhe.status === 'EM_COTACAO' && (
                <Can modulo="COMPRAS_FORNECEDORES" acao="EDITAR" obraId={obraId}>
                  <form
                    onSubmit={handleRegistrarCotacao}
                    className="grid grid-cols-1 gap-2 rounded-lg border border-borda p-3 sm:grid-cols-4"
                  >
                    <select
                      value={formCotacao.fornecedorId}
                      onChange={(e) =>
                        setFormCotacao((f) => ({ ...f, fornecedorId: e.target.value }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    >
                      <option value="">Fornecedor ativo...</option>
                      {fornecedores
                        .filter((f) => f.situacao === 'ATIVO')
                        .map((f) => (
                          <option key={f.id} value={f.id}>
                            {f.nomeRazaoSocial}
                          </option>
                        ))}
                    </select>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      placeholder="Valor Proposto (R$)"
                      value={formCotacao.valorTotalProposto}
                      onChange={(e) =>
                        setFormCotacao((f) => ({
                          ...f,
                          valorTotalProposto: e.target.value,
                        }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    />
                    <input
                      type="number"
                      min="0"
                      placeholder="Prazo (dias)"
                      value={formCotacao.prazoEntregaDias}
                      onChange={(e) =>
                        setFormCotacao((f) => ({
                          ...f,
                          prazoEntregaDias: e.target.value,
                        }))
                      }
                      className="rounded-lg border border-borda-forte px-2 py-1.5 text-sm"
                      required
                    />
                    <Button type="submit">Registrar Cotação</Button>
                  </form>
                </Can>
              )}
            </div>

            {/* 3. Histórico de Status do Pedido */}
            <div className="border-t pt-3">
              <h3 className="mb-1.5 text-xs font-semibold uppercase text-texto-suave">
                Histórico de Transições
              </h3>
              <div className="flex flex-wrap gap-2 text-xs text-texto-suave">
                {pedidoDetalhe.historico.map((h, idx) => (
                  <span key={idx} className="rounded bg-superficie-alta px-2 py-1">
                    <strong>{h.status}</strong> ({formatarDataHora(h.dataHora)})
                  </span>
                ))}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}