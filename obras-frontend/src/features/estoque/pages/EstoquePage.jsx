import { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  AlertCircle,
  AlertTriangle,
  ArrowLeftRight,
  ClipboardList,
  Download,
  History,
  MinusCircle,
  Plus,
  Settings,
  Wallet,
  Warehouse,
} from 'lucide-react';
import Button from '../../../shared/components/ui/Button.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Can from '../../../shared/auth/Can.jsx';
import { mensagemErro } from '../../../shared/api/http.js';
import { formatarDataHora } from '../../../shared/utils/formatadores.js';
import {
  configurarSaldo,
  consultarSaldoConsolidado,
  consultarSaldos,
  listarMateriais,
  listarMovimentacoes,
  listarPedidosEmTransito,
  registrarEntradaEstoque,
  registrarSaidaEstoque,
  registrarTransferenciaEstoque,
  solicitarReposicao,
} from '../api/estoqueApi.js';

export default function EstoquePage() {
  const { obraId } = useParams();
  const [canteiroFiltro, setCanteiroFiltro] = useState('');
  const [aba, setAba] = useState('saldos'); // 'saldos' | 'consolidado' | 'movimentacoes'
  const [saldos, setSaldos] = useState(null);
  const [consolidado, setConsolidado] = useState([]);
  const [movimentacoes, setMovimentacoes] = useState([]);
  const [materiais, setMateriais] = useState([]);
  const [pedidosTransito, setPedidosTransito] = useState([]);
  const [erro, setErro] = useState(null);
  const [avisoSucesso, setAvisoSucesso] = useState(null);

  // Modais de operação: 'entrada' | 'saida' | 'transferencia' | 'configurar' | 'reposicao' | null
  const [modalAtivo, setModalAtivo] = useState(null);
  const [itemSelecionado, setItemSelecionado] = useState(null);
  const [formOp, setFormOp] = useState({
    pedidoId: '',
    materialId: '',
    canteiroId: '1',
    canteiroDestinoId: '2',
    quantidade: '',
    quantidadeMinima: '',
  });
  const [erroModal, setErroModal] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const carregarTudo = useCallback(async () => {
    setErro(null);
    try {
      const idCanteiro = canteiroFiltro ? Number(canteiroFiltro) : undefined;
      const [listaSaldos, listaCons, listaMovs, listaMat] = await Promise.all([
        consultarSaldos(obraId, idCanteiro),
        consultarSaldoConsolidado(obraId),
        listarMovimentacoes(obraId, { canteiroId: idCanteiro }),
        listarMateriais().catch(() => []),
      ]);
      setSaldos(listaSaldos);
      setConsolidado(listaCons);
      setMovimentacoes(listaMovs);
      setMateriais(listaMat);
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, [obraId, canteiroFiltro]);

  useEffect(() => {
    carregarTudo();
  }, [carregarTudo]);

  // Canteiros conhecidos a partir dos saldos e movimentações
  const canteirosDisponiveis = useMemo(() => {
    const ids = new Set([1, 2]);
    (saldos || []).forEach((s) => ids.add(s.canteiroId));
    (movimentacoes || []).forEach((m) => {
      if (m.canteiroOrigemId) ids.add(m.canteiroOrigemId);
      if (m.canteiroDestinoId) ids.add(m.canteiroDestinoId);
    });
    return Array.from(ids).sort((a, b) => a - b);
  }, [saldos, movimentacoes]);

  // Indicadores dos 3 KPI Cards do topo (conforme 1440w light (1).svg)
  const kpis = useMemo(() => {
    const lista = saldos || [];
    const totalMonitorados = lista.length;
    const volumeTotalAcumulado = lista.reduce(
      (acc, s) => acc + Number(s.quantidadeAtual || 0),
      0,
    );
    const criticosOuAlerta = lista.filter(
      (s) => s.situacao === 'ESGOTADO' || s.situacao === 'ALERTA_MINIMO',
    ).length;
    return { totalMonitorados, volumeTotalAcumulado, criticosOuAlerta };
  }, [saldos]);

  // Ordena saldos destacando primeiro ESGOTADO (vermelho) e ALERTA_MINIMO (amarelo), como no SVG
  const saldosOrdenados = useMemo(() => {
    if (!saldos) return [];
    const peso = { ESGOTADO: 0, ALERTA_MINIMO: 1, REGULAR: 2 };
    return [...saldos].sort(
      (a, b) => (peso[a.situacao] ?? 3) - (peso[b.situacao] ?? 3),
    );
  }, [saldos]);

  async function abrirModal(tipo, saldo = null) {
    setErroModal(null);
    setItemSelecionado(saldo);
    const canteiroPadrao = saldo?.canteiroId || canteiroFiltro || '1';
    const qtdSugerida =
      saldo && Number(saldo.quantidadeMinima) > Number(saldo.quantidadeAtual)
        ? (Number(saldo.quantidadeMinima) - Number(saldo.quantidadeAtual) + 10).toFixed(3)
        : '10';

    setFormOp({
      pedidoId: '',
      materialId: saldo?.materialId || materiais[0]?.id || '',
      canteiroId: String(canteiroPadrao),
      canteiroDestinoId: String(Number(canteiroPadrao) === 1 ? 2 : 1),
      quantidade: tipo === 'reposicao' ? qtdSugerida : '',
      quantidadeMinima: saldo ? String(saldo.quantidadeMinima) : '10',
    });
    setModalAtivo(tipo);

    if (tipo === 'entrada') {
      try {
        const emTransito = await listarPedidosEmTransito(obraId);
        setPedidosTransito(emTransito);
        if (emTransito.length > 0) {
          setFormOp((f) => ({ ...f, pedidoId: String(emTransito[0].id) }));
        }
      } catch {
        setPedidosTransito([]);
      }
    }
  }

  async function executarOperacao(e) {
    e.preventDefault();
    setErroModal(null);
    setAvisoSucesso(null);
    setEnviando(true);
    try {
      if (modalAtivo === 'entrada') {
        await registrarEntradaEstoque(obraId, {
          pedidoId: Number(formOp.pedidoId),
          canteiroId: Number(formOp.canteiroId),
        });
        setAvisoSucesso(`Entrada do pedido #${formOp.pedidoId} registrada no Canteiro #${formOp.canteiroId}.`);
      } else if (modalAtivo === 'saida') {
        const res = await registrarSaidaEstoque(obraId, {
          materialId: Number(formOp.materialId),
          canteiroId: Number(formOp.canteiroId),
          quantidade: Number(formOp.quantidade),
        });
        setAvisoSucesso(
          res.emAlerta
            ? `Saída registrada! Atenção: o material entrou em alerta de estoque mínimo (${res.saldo.situacao}).`
            : 'Saída para consumo registrada com sucesso.',
        );
      } else if (modalAtivo === 'transferencia') {
        const res = await registrarTransferenciaEstoque(obraId, {
          materialId: Number(formOp.materialId),
          canteiroOrigemId: Number(formOp.canteiroId),
          canteiroDestinoId: Number(formOp.canteiroDestinoId),
          quantidade: Number(formOp.quantidade),
        });
        setAvisoSucesso(
          res.emAlertaOrigem
            ? 'Transferência concluída! O canteiro de origem atingiu o nível de alerta mínimo.'
            : 'Transferência entre canteiros concluída.',
        );
      } else if (modalAtivo === 'configurar') {
        await configurarSaldo(obraId, {
          materialId: Number(formOp.materialId),
          canteiroId: Number(formOp.canteiroId),
          quantidadeMinima: Number(formOp.quantidadeMinima),
        });
        setAvisoSucesso('Parâmetro de estoque mínimo atualizado.');
      } else if (modalAtivo === 'reposicao') {
        const pedido = await solicitarReposicao(obraId, {
          materialId: Number(formOp.materialId),
          canteiroId: Number(formOp.canteiroId),
          quantidade: Number(formOp.quantidade),
        });
        setAvisoSucesso(
          `Pedido de Compra #${pedido.id} (${pedido.prioridade}) gerado automaticamente em Compras!`,
        );
      }
      setModalAtivo(null);
      carregarTudo();
    } catch (err) {
      setErroModal(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="space-y-5 rounded-2xl bg-[#111318] p-5 text-slate-100 shadow-xl">
      {/* Barra de Ferramentas Superior (idêntica a 1440w light (1).svg) */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          {/* Seletor de Almoxarifado / Canteiro */}
          <div className="relative flex items-center rounded-lg border border-slate-800 bg-[#181b22] px-3 py-2">
            <Warehouse size={16} className="mr-2 text-amber-500" />
            <select
              aria-label="Selecionar canteiro"
              value={canteiroFiltro}
              onChange={(e) => setCanteiroFiltro(e.target.value)}
              className="w-52 bg-transparent text-sm text-slate-200 outline-none"
            >
              <option value="" className="bg-[#181b22]">
                Todos os Canteiros (Obra #{obraId})
              </option>
              {canteirosDisponiveis.map((cId) => (
                <option key={cId} value={cId} className="bg-[#181b22]">
                  Canteiro / Almoxarifado #{cId}
                </option>
              ))}
            </select>
          </div>

          {/* Botão Secundário de Entrada de Pedido (ícone de bandeja de entrada do SVG) */}
          <Can modulo="ESTOQUE" acao="CRIAR" obraId={obraId}>
            <button
              type="button"
              onClick={() => abrirModal('entrada')}
              className="flex items-center gap-2 rounded-lg border border-slate-800 bg-[#181b22] px-4 py-2 text-sm font-medium text-slate-300 transition hover:border-slate-700 hover:bg-slate-800"
              title="Registrar Entrada de Pedido em Trânsito (UC13)"
            >
              <Download size={16} className="text-slate-300" />
              <span>Receber Pedido</span>
            </button>
          </Can>

          {/* Botão Primário Laranja (+) */}
          <Can modulo="ESTOQUE" acao="EDITAR" obraId={obraId}>
            <button
              type="button"
              onClick={() => abrirModal('configurar')}
              className="flex items-center gap-2 rounded-lg bg-amber-500 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-amber-400"
            >
              <Plus size={16} />
              <span>Configurar Saldo</span>
            </button>
          </Can>
        </div>

        {/* Ações operacionais complementares (Saída, Transferência e Visões) */}
        <div className="flex flex-wrap items-center gap-2">
          <Can modulo="ESTOQUE" acao="CRIAR" obraId={obraId}>
            <button
              type="button"
              onClick={() => abrirModal('saida')}
              className="flex items-center gap-1.5 rounded-lg border border-slate-800 bg-[#181b22] px-3 py-2 text-xs font-medium text-slate-300 hover:bg-slate-800"
            >
              <MinusCircle size={14} className="text-rose-400" /> Saída Consumo
            </button>
            <button
              type="button"
              onClick={() => abrirModal('transferencia')}
              className="flex items-center gap-1.5 rounded-lg border border-slate-800 bg-[#181b22] px-3 py-2 text-xs font-medium text-slate-300 hover:bg-slate-800"
            >
              <ArrowLeftRight size={14} className="text-sky-400" /> Transferir
            </button>
          </Can>
          <div className="flex rounded-lg border border-slate-800 bg-[#181b22] p-0.5 text-xs">
            <button
              type="button"
              onClick={() => setAba('saldos')}
              className={`rounded-md px-2.5 py-1.5 font-medium ${
                aba === 'saldos' ? 'bg-amber-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Monitoramento
            </button>
            <button
              type="button"
              onClick={() => setAba('consolidado')}
              className={`rounded-md px-2.5 py-1.5 font-medium ${
                aba === 'consolidado' ? 'bg-amber-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Consolidado
            </button>
            <button
              type="button"
              onClick={() => setAba('movimentacoes')}
              className={`rounded-md px-2.5 py-1.5 font-medium ${
                aba === 'movimentacoes' ? 'bg-amber-500 text-slate-950' : 'text-slate-400'
              }`}
            >
              Histórico
            </button>
          </div>
        </div>
      </div>

      {avisoSucesso && (
        <div className="flex items-center justify-between rounded-xl border border-emerald-500/30 bg-emerald-950/40 px-4 py-2.5 text-sm text-emerald-300">
          <span>{avisoSucesso}</span>
          <button
            type="button"
            onClick={() => setAvisoSucesso(null)}
            className="text-xs underline"
          >
            Fechar
          </button>
        </div>
      )}

      {/* 3 KPI Cards Superiores (1440w light (1).svg) */}
      <div className="grid grid-cols-1 gap-4 rounded-xl border border-slate-800/90 bg-[#161920] p-4 md:grid-cols-3">
        <div className="flex items-center justify-between rounded-xl border border-slate-800 bg-[#1b1f28] p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-amber-500/15 text-amber-400">
              <ClipboardList size={20} />
            </div>
            <div>
              <p className="text-xs text-slate-400">Itens Monitorados</p>
              <p className="text-xl font-bold text-slate-100">{kpis.totalMonitorados}</p>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between rounded-xl border border-slate-800 bg-[#1b1f28] p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-sky-500/15 text-sky-400">
              <Wallet size={20} />
            </div>
            <div>
              <p className="text-xs text-slate-400">Volume Físico Total</p>
              <p className="text-xl font-bold text-slate-100">
                {kpis.volumeTotalAcumulado.toLocaleString('pt-BR', {
                  maximumFractionDigits: 2,
                })}
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between rounded-xl border border-slate-800 bg-[#1b1f28] p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-rose-500/15 text-rose-400">
              <AlertTriangle size={20} />
            </div>
            <div>
              <p className="text-xs text-slate-400">Itens em Alerta / Esgotados</p>
              <p className="text-xl font-bold text-rose-400">{kpis.criticosOuAlerta}</p>
            </div>
          </div>
        </div>
      </div>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregarTudo} />}
      {!erro && !saldos && <Spinner className="mt-8" />}

      {/* Painel Principal: Lista de Saldos com Barras de Nível e Ações Rápidas (1440w light (1).svg) */}
      {aba === 'saldos' && saldos && (
        <div className="rounded-xl border border-slate-800 bg-[#161920] p-5">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
              Níveis de Saldo por Canteiro (UC13 / RF-17 / RF-35)
            </h2>
            <span className="text-xs text-slate-500">
              {saldosOrdenados.length} registro(s)
            </span>
          </div>

          {saldosOrdenados.length === 0 ? (
            <p className="py-8 text-center text-sm text-slate-400">
              Nenhum saldo configurado para este filtro. Clique em &ldquo;Configurar Saldo&rdquo; para iniciar.
            </p>
          ) : (
            <div className="divide-y divide-slate-800/60">
              {saldosOrdenados.map((s) => {
                const atual = Number(s.quantidadeAtual || 0);
                const minima = Number(s.quantidadeMinima || 0);
                const referencia = minima > 0 ? minima * 1.5 : Math.max(atual, 1);
                const percentual = Math.min(100, Math.max(6, Math.round((atual / referencia) * 100)));

                const ehCritico = s.situacao === 'ESGOTADO';
                const ehAtencao = s.situacao === 'ALERTA_MINIMO';

                const corBarra = ehCritico
                  ? 'bg-rose-500'
                  : ehAtencao
                  ? 'bg-amber-400'
                  : 'bg-emerald-500';

                return (
                  <div
                    key={s.id}
                    className="flex flex-wrap items-center justify-between gap-4 py-4"
                  >
                    {/* Identificação e Barra de Nível */}
                    <div className="flex min-w-[260px] flex-1 items-center gap-4">
                      <span className="inline-flex min-w-[78px] justify-center rounded-md bg-slate-800 px-2.5 py-1 text-xs font-medium text-slate-300">
                        Cant. #{s.canteiroId}
                      </span>
                      <div className="min-w-[200px] flex-1">
                        <div className="mb-1.5 flex items-baseline justify-between gap-2">
                          <span className="font-medium text-slate-100">
                            {s.materialNome}{' '}
                            <span className="text-xs text-slate-400">({s.unidadeMedida})</span>
                          </span>
                          <span className="text-xs text-slate-400">
                            Saldo: <strong className="text-slate-200">{atual}</strong> / Mín:{' '}
                            {minima}
                            {s.mediaConsumoSemanal != null && (
                              <span className="ml-2 text-slate-500">
                                • Consumo sem.: {s.mediaConsumoSemanal}
                              </span>
                            )}
                          </span>
                        </div>
                        <div className="h-2 w-full max-w-xs overflow-hidden rounded-full bg-slate-800">
                          <div
                            className={`h-full rounded-full transition-all ${corBarra}`}
                            style={{ width: `${percentual}%` }}
                          />
                        </div>
                      </div>
                    </div>

                    {/* Badges de Estado e Botão de Ação Rápida (1440w light (1).svg) */}
                    <div className="flex items-center gap-3">
                      {ehCritico && (
                        <span className="inline-flex min-w-[140px] items-center gap-1.5 rounded-md border border-rose-500/40 bg-rose-950/50 px-3 py-1.5 text-xs font-medium text-rose-300">
                          <AlertTriangle size={14} className="text-rose-400" />
                          Esgotado (Crítico)
                        </span>
                      )}
                      {ehAtencao && (
                        <span className="inline-flex min-w-[140px] items-center gap-1.5 rounded-md border border-amber-500/40 bg-amber-950/50 px-3 py-1.5 text-xs font-medium text-amber-300">
                          <AlertCircle size={14} className="text-amber-400" />
                          Alerta Mínimo
                        </span>
                      )}
                      {!ehCritico && !ehAtencao && (
                        <span className="inline-flex min-w-[140px] items-center gap-1.5 rounded-md border border-emerald-500/30 bg-emerald-950/30 px-3 py-1.5 text-xs font-medium text-emerald-300">
                          Regular
                        </span>
                      )}

                      <Can modulo="ESTOQUE" acao="CRIAR" obraId={obraId}>
                        <button
                          type="button"
                          onClick={() => abrirModal('reposicao', s)}
                          className={`flex min-w-[135px] items-center justify-center gap-1.5 rounded-lg px-3.5 py-2 text-xs font-semibold transition ${
                            ehCritico
                              ? 'bg-amber-500 text-slate-950 hover:bg-amber-400'
                              : 'border border-slate-700 bg-[#1b1f28] text-slate-200 hover:bg-slate-800'
                          }`}
                          title="Gerar PedidoCompra de reposição (RF-35)"
                        >
                          <Plus size={14} />
                          <span>{ehCritico ? 'Repor Urgente' : 'Solicitar Compra'}</span>
                        </button>
                      </Can>

                      <Can modulo="ESTOQUE" acao="EDITAR" obraId={obraId}>
                        <button
                          type="button"
                          onClick={() => abrirModal('configurar', s)}
                          className="rounded-lg border border-slate-800 p-2 text-slate-400 hover:bg-slate-800 hover:text-slate-200"
                          title="Ajustar quantidade mínima"
                        >
                          <Settings size={15} />
                        </button>
                      </Can>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Visão Consolidada da Obra (calcularSaldoObra) */}
      {aba === 'consolidado' && (
        <div className="rounded-xl border border-slate-800 bg-[#161920] p-5">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wider text-slate-400">
            Saldo Consolidado da Obra #{obraId} (Soma de Todos os Canteiros)
          </h2>
          {consolidado.length === 0 ? (
            <p className="py-6 text-center text-sm text-slate-400">
              Sem saldo consolidado registrado.
            </p>
          ) : (
            <table className="w-full text-sm">
              <thead className="border-b border-slate-800 text-left text-slate-400">
                <tr>
                  <th className="py-2">Material</th>
                  <th className="py-2">Unidade</th>
                  <th className="py-2 text-right">Quantidade Total na Obra</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {consolidado.map((c) => (
                  <tr key={c.materialId}>
                    <td className="py-3 font-medium text-slate-100">{c.materialNome}</td>
                    <td className="py-3 text-slate-400">{c.unidadeMedida}</td>
                    <td className="py-3 text-right font-semibold text-amber-400">
                      {c.quantidadeTotal}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Histórico de Movimentações (UC14) */}
      {aba === 'movimentacoes' && (
        <div className="rounded-xl border border-slate-800 bg-[#161920] p-5">
          <div className="mb-3 flex items-center gap-2">
            <History size={16} className="text-amber-400" />
            <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
              Histórico de Movimentações (Últimos 30 dias)
            </h2>
          </div>
          {movimentacoes.length === 0 ? (
            <p className="py-6 text-center text-sm text-slate-400">
              Nenhuma movimentação registrada no período.
            </p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="border-b border-slate-800 text-left text-slate-400">
                  <tr>
                    <th className="py-2">Data/Hora</th>
                    <th className="py-2">Tipo</th>
                    <th className="py-2">Material</th>
                    <th className="py-2">Qtd.</th>
                    <th className="py-2">Origem / Destino</th>
                    <th className="py-2">Pedido</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {movimentacoes.map((m) => (
                    <tr key={m.id}>
                      <td className="py-2.5 text-slate-400">
                        {formatarDataHora(m.dataMovimentacao)}
                      </td>
                      <td className="py-2.5 font-medium text-slate-200">{m.tipo}</td>
                      <td className="py-2.5 text-slate-100">{m.materialNome}</td>
                      <td className="py-2.5 font-semibold text-amber-400">{m.quantidade}</td>
                      <td className="py-2.5 text-slate-400">
                        {m.canteiroOrigemId ? `Cant. #${m.canteiroOrigemId}` : 'Fornecedor'}{' '}
                        → {m.canteiroDestinoId ? `Cant. #${m.canteiroDestinoId}` : 'Consumo'}
                      </td>
                      <td className="py-2.5 text-slate-400">
                        {m.pedidoCompraId ? `#${m.pedidoCompraId}` : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Modal unificado de Operações de Estoque */}
      <Modal
        aberto={!!modalAtivo}
        onFechar={() => setModalAtivo(null)}
        titulo={
          modalAtivo === 'entrada'
            ? 'Registrar Entrada por Pedido de Compra (EM_TRANSITO)'
            : modalAtivo === 'saida'
            ? 'Registrar Saída para Consumo em Obra'
            : modalAtivo === 'transferencia'
            ? 'Transferência entre Canteiros da Obra'
            : modalAtivo === 'configurar'
            ? 'Configurar Saldo e Estoque Mínimo'
            : 'Gerar Solicitação de Reposição (Estoque → Compras)'
        }
      >
        <form onSubmit={executarOperacao} className="space-y-3 text-slate-800">
          {modalAtivo === 'entrada' ? (
            <>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">
                  Pedido de Compra (Status: EM_TRANSITO)
                </label>
                {pedidosTransito.length > 0 ? (
                  <select
                    value={formOp.pedidoId}
                    onChange={(e) => setFormOp((f) => ({ ...f, pedidoId: e.target.value }))}
                    className="min-h-[38px] w-full rounded-lg border border-slate-300 px-3 text-sm"
                    required
                  >
                    {pedidosTransito.map((p) => (
                      <option key={p.id} value={p.id}>
                        Pedido #{p.id} — {p.fornecedorNome || 'Fornecedor'} (R$ {p.valorTotal})
                      </option>
                    ))}
                  </select>
                ) : (
                  <Input
                    name="pedidoId"
                    type="number"
                    placeholder="ID do Pedido em trânsito"
                    value={formOp.pedidoId}
                    onChange={(e) => setFormOp((f) => ({ ...f, pedidoId: e.target.value }))}
                    required
                  />
                )}
              </div>
              <Input
                label="ID do Canteiro de Destino"
                name="canteiroId"
                type="number"
                min="1"
                value={formOp.canteiroId}
                onChange={(e) => setFormOp((f) => ({ ...f, canteiroId: e.target.value }))}
                required
              />
            </>
          ) : (
            <>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">
                  Material
                </label>
                {materiais.length > 0 ? (
                  <select
                    value={formOp.materialId}
                    onChange={(e) =>
                      setFormOp((f) => ({ ...f, materialId: e.target.value }))
                    }
                    disabled={!!itemSelecionado}
                    className="min-h-[38px] w-full rounded-lg border border-slate-300 px-3 text-sm disabled:bg-slate-100"
                    required
                  >
                    <option value="">Selecione...</option>
                    {materiais.map((m) => (
                      <option key={m.id} value={m.id}>
                        #{m.id} — {m.nome} ({m.unidadeMedida})
                      </option>
                    ))}
                  </select>
                ) : (
                  <Input
                    name="materialId"
                    type="number"
                    value={formOp.materialId}
                    onChange={(e) =>
                      setFormOp((f) => ({ ...f, materialId: e.target.value }))
                    }
                    required
                  />
                )}
              </div>

              <Input
                label={
                  modalAtivo === 'transferencia'
                    ? 'ID do Canteiro de Origem'
                    : 'ID do Canteiro'
                }
                name="canteiroId"
                type="number"
                min="1"
                value={formOp.canteiroId}
                onChange={(e) => setFormOp((f) => ({ ...f, canteiroId: e.target.value }))}
                required
              />

              {modalAtivo === 'transferencia' && (
                <Input
                  label="ID do Canteiro de Destino"
                  name="canteiroDestinoId"
                  type="number"
                  min="1"
                  value={formOp.canteiroDestinoId}
                  onChange={(e) =>
                    setFormOp((f) => ({ ...f, canteiroDestinoId: e.target.value }))
                  }
                  required
                />
              )}

              {modalAtivo === 'configurar' ? (
                <Input
                  label="Quantidade Mínima de Alerta"
                  name="quantidadeMinima"
                  type="number"
                  step="0.001"
                  min="0"
                  value={formOp.quantidadeMinima}
                  onChange={(e) =>
                    setFormOp((f) => ({ ...f, quantidadeMinima: e.target.value }))
                  }
                  required
                />
              ) : (
                <Input
                  label="Quantidade"
                  name="quantidade"
                  type="number"
                  step="0.001"
                  min="0.001"
                  value={formOp.quantidade}
                  onChange={(e) =>
                    setFormOp((f) => ({ ...f, quantidade: e.target.value }))
                  }
                  required
                />
              )}
            </>
          )}

          {erroModal && (
            <p role="alert" className="rounded bg-red-50 p-2 text-sm text-red-700">
              {erroModal}
            </p>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <Button variante="secundario" onClick={() => setModalAtivo(null)}>
              Cancelar
            </Button>
            <Button type="submit" disabled={enviando}>
              {enviando ? 'Processando...' : 'Confirmar'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}