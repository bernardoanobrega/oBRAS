import { useCallback, useEffect, useState } from 'react';
import { History, Mail, MapPin, MoreVertical, Phone, Plus, User } from 'lucide-react';
import Button from '../../../shared/components/ui/Button.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Badge from '../../../shared/components/ui/Badge.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import Can from '../../../shared/auth/Can.jsx';
import { errosPorCampo, mensagemErro } from '../../../shared/api/http.js';
import { formatarData, formatarMoeda } from '../../../shared/utils/formatadores.js';
import {
  atualizarFornecedor,
  cadastrarFornecedor,
  desativarFornecedor,
  listarFornecedores,
  listarHistoricoFornecedor,
} from '../api/comprasApi.js';

export default function FornecedoresPage() {
  const [fornecedores, setFornecedores] = useState(null);
  const [apenasAtivos, setApenasAtivos] = useState(false);
  const [erro, setErro] = useState(null);
  const [modalForm, setModalForm] = useState({ aberto: false, fornecedor: null });
  const [form, setForm] = useState({
    nomeRazaoSocial: '',
    cpfCnpj: '',
    email: '',
    telefone: '',
  });
  const [errosCampo, setErrosCampo] = useState({});
  const [erroForm, setErroForm] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const [modalHistorico, setModalHistorico] = useState({
    aberto: false,
    fornecedor: null,
    pedidos: null,
  });

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      setFornecedores(await listarFornecedores(apenasAtivos));
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, [apenasAtivos]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function abrirCadastro(fornecedor = null) {
    setModalForm({ aberto: true, fornecedor });
    setForm({
      nomeRazaoSocial: fornecedor?.nomeRazaoSocial ?? '',
      cpfCnpj: fornecedor?.cpfCnpj ?? '',
      email: fornecedor?.email ?? '',
      telefone: fornecedor?.telefone ?? '',
    });
    setErrosCampo({});
    setErroForm(null);
  }

  async function salvar(e) {
    e.preventDefault();
    setErrosCampo({});
    setErroForm(null);
    setEnviando(true);
    try {
      if (modalForm.fornecedor) {
        await atualizarFornecedor(modalForm.fornecedor.id, form);
      } else {
        await cadastrarFornecedor(form);
      }
      setModalForm({ aberto: false, fornecedor: null });
      carregar();
    } catch (err) {
      setErrosCampo(errosPorCampo(err));
      setErroForm(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  async function desativar(f) {
    if (!window.confirm(`Desativar o fornecedor ${f.nomeRazaoSocial}?`)) return;
    try {
      await desativarFornecedor(f.id);
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  async function verHistorico(f) {
    setModalHistorico({ aberto: true, fornecedor: f, pedidos: null });
    try {
      const pedidos = await listarHistoricoFornecedor(f.id);
      setModalHistorico({ aberto: true, fornecedor: f, pedidos });
    } catch {
      setModalHistorico({ aberto: true, fornecedor: f, pedidos: [] });
    }
  }

  return (
    <div className="space-y-5 rounded-2xl bg-fundo p-5 text-texto shadow-xl">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Can modulo="COMPRAS_FORNECEDORES" acao="CRIAR">
            <button
              type="button"
              onClick={() => abrirCadastro(null)}
              className="flex items-center gap-2 rounded-lg bg-brand px-4 py-2 text-sm font-semibold text-fundo transition hover:bg-brand-claro"
            >
              <Plus size={16} /> Novo Fornecedor
            </button>
          </Can>
          <label className="flex items-center gap-2 text-xs text-texto-suave">
            <input
              type="checkbox"
              checked={apenasAtivos}
              onChange={(e) => setApenasAtivos(e.target.checked)}
              className="h-4 w-4 accent-brand"
            />
            Exibir apenas ativos
          </label>
        </div>
        <span className="text-xs text-texto-fraco">
          Catálogo Central de Fornecedores (UC12 / RF-07 / RF-08)
        </span>
      </div>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregar} />}
      {!erro && !fornecedores && <Spinner className="mt-8" />}
      {fornecedores && fornecedores.length === 0 && (
        <EmptyState mensagem="Nenhum fornecedor cadastrado." />
      )}

      {/* Grid de 4 Cards de Fornecedores (idêntico à seção inferior de 1440w light.svg) */}
      {fornecedores && fornecedores.length > 0 && (
        <div className="grid grid-cols-1 gap-4 rounded-xl border border-borda bg-superficie p-4 sm:grid-cols-2 lg:grid-cols-4">
          {fornecedores.map((f) => (
            <div
              key={f.id}
              className="flex flex-col justify-between rounded-xl border border-borda bg-superficie-alta p-4"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h3 className="font-semibold text-texto">{f.nomeRazaoSocial}</h3>
                    <p className="mt-1 flex items-center gap-1 text-xs text-sucesso">
                      <MapPin size={13} /> doc: {f.cpfCnpj}
                    </p>
                  </div>
                  <Can modulo="COMPRAS_FORNECEDORES" acao="EDITAR">
                    <button
                      type="button"
                      onClick={() => abrirCadastro(f)}
                      className="rounded p-1 text-texto-fraco hover:bg-superficie-alta hover:text-texto"
                      title="Editar fornecedor"
                    >
                      <MoreVertical size={16} />
                    </button>
                  </Can>
                </div>

                <div className="my-3 border-t border-borda pt-3 text-xs text-texto-suave space-y-1.5">
                  <p className="flex items-center gap-2">
                    <User size={13} className="text-texto-fraco" />
                    <span className="truncate">{f.email || 'Sem e-mail'}</span>
                  </p>
                  <p className="flex items-center gap-2">
                    <Phone size={13} className="text-texto-fraco" />
                    <span>{f.telefone || 'Sem telefone'}</span>
                  </p>
                </div>

                {/* Tags inferiores do Card (Situação + ID + Catálogo) */}
                <div className="mt-3 flex flex-wrap gap-1.5">
                  <span
                    className={`rounded px-2 py-0.5 text-[11px] font-medium ${
                      f.situacao === 'ATIVO'
                        ? 'bg-sucesso-fundo text-sucesso border border-sucesso/30'
                        : 'bg-superficie-alta text-texto-fraco'
                    }`}
                  >
                    {f.situacao}
                  </span>
                  <span className="rounded bg-superficie-alta px-2 py-0.5 text-[11px] text-texto-fraco">
                    ID #{f.id}
                  </span>
                  <span className="rounded bg-superficie-alta px-2 py-0.5 text-[11px] text-texto-fraco">
                    Insumos
                  </span>
                </div>
              </div>

              {/* Botões de Ação Inferiores do Card (1440w light.svg) */}
              <div className="mt-4 flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => verHistorico(f)}
                  className="flex flex-1 items-center justify-center gap-1 rounded-lg bg-menu-ativo py-2 text-xs font-medium text-info hover:bg-menu-ativo"
                >
                  <History size={13} /> Histórico
                </button>
                {f.situacao === 'ATIVO' ? (
                  <Can modulo="COMPRAS_FORNECEDORES" acao="EXCLUIR">
                    <button
                      type="button"
                      onClick={() => desativar(f)}
                      className="flex flex-1 items-center justify-center gap-1 rounded-lg bg-brand py-2 text-xs font-semibold text-fundo hover:bg-brand-claro"
                    >
                      Desativar
                    </button>
                  </Can>
                ) : (
                  <button
                    type="button"
                    onClick={() => abrirCadastro(f)}
                    className="flex flex-1 items-center justify-center gap-1 rounded-lg bg-brand py-2 text-xs font-semibold text-fundo hover:bg-brand-claro"
                  >
                    <Plus size={13} /> Editar
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal de Cadastro/Edição */}
      <Modal
        aberto={modalForm.aberto}
        onFechar={() => setModalForm({ aberto: false, fornecedor: null })}
        titulo={modalForm.fornecedor ? 'Editar Fornecedor' : 'Novo Fornecedor'}
      >
        <form onSubmit={salvar} className="space-y-3 text-texto">
          <Input
            label="Nome / Razão Social"
            name="nomeRazaoSocial"
            value={form.nomeRazaoSocial}
            onChange={(e) => setForm((f) => ({ ...f, nomeRazaoSocial: e.target.value }))}
            erro={errosCampo.nomeRazaoSocial}
            required
          />
          <Input
            label="CPF ou CNPJ"
            name="cpfCnpj"
            value={form.cpfCnpj}
            onChange={(e) => setForm((f) => ({ ...f, cpfCnpj: e.target.value }))}
            erro={errosCampo.cpfCnpj}
            required
          />
          <Input
            label="E-mail"
            name="email"
            type="email"
            value={form.email}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            erro={errosCampo.email}
          />
          <Input
            label="Telefone"
            name="telefone"
            value={form.telefone}
            onChange={(e) => setForm((f) => ({ ...f, telefone: e.target.value }))}
            erro={errosCampo.telefone}
          />
          {erroForm && (
            <p role="alert" className="rounded bg-perigo-fundo p-2 text-sm text-perigo">
              {erroForm}
            </p>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button
              variante="secundario"
              onClick={() => setModalForm({ aberto: false, fornecedor: null })}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={enviando}>
              {enviando ? 'Salvando...' : 'Salvar'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal de Histórico de Compras do Fornecedor (RF-08) */}
      <Modal
        aberto={modalHistorico.aberto}
        onFechar={() =>
          setModalHistorico({ aberto: false, fornecedor: null, pedidos: null })
        }
        titulo={`Histórico de Compras — ${modalHistorico.fornecedor?.nomeRazaoSocial ?? ''}`}
        largura="max-w-2xl"
      >
        <div className="text-texto">
          {!modalHistorico.pedidos && <Spinner />}
          {modalHistorico.pedidos && modalHistorico.pedidos.length === 0 && (
            <p className="py-4 text-sm text-texto-suave">
              Nenhum pedido de compra vinculado a este fornecedor.
            </p>
          )}
          {modalHistorico.pedidos && modalHistorico.pedidos.length > 0 && (
            <table className="w-full text-sm">
              <thead className="border-b text-left text-texto-suave">
                <tr>
                  <th className="py-2">Pedido</th>
                  <th className="py-2">Solicitação</th>
                  <th className="py-2">Status</th>
                  <th className="py-2 text-right">Valor Total</th>
                </tr>
              </thead>
              <tbody>
                {modalHistorico.pedidos.map((p) => (
                  <tr key={p.id} className="border-b last:border-0">
                    <td className="py-2 font-medium">#{p.id}</td>
                    <td className="py-2">{formatarData(p.dataSolicitacao)}</td>
                    <td className="py-2">
                      <Badge tom={p.status === 'ENTREGUE' ? 'verde' : 'azul'}>
                        {p.status}
                      </Badge>
                    </td>
                    <td className="py-2 text-right font-medium">
                      {formatarMoeda(p.valorTotal)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </Modal>
    </div>
  );
}