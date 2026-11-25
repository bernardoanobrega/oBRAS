import { useCallback, useEffect, useState } from 'react';
import { Package, Plus, Search } from 'lucide-react';
import Button from '../../../shared/components/ui/Button.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import Can from '../../../shared/auth/Can.jsx';
import { errosPorCampo, mensagemErro } from '../../../shared/api/http.js';
import { atualizarMaterial, criarMaterial, listarMateriais } from '../api/estoqueApi.js';

export default function MateriaisPage() {
  const [materiais, setMateriais] = useState(null);
  const [busca, setBusca] = useState('');
  const [erro, setErro] = useState(null);
  const [modal, setModal] = useState({ aberto: false, material: null });
  const [form, setForm] = useState({ nome: '', descricao: '', unidadeMedida: '' });
  const [errosCampo, setErrosCampo] = useState({});
  const [erroModal, setErroModal] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const carregar = useCallback(async (termo = '') => {
    setErro(null);
    try {
      setMateriais(await listarMateriais(termo));
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function abrirModal(material = null) {
    setModal({ aberto: true, material });
    setForm({
      nome: material?.nome ?? '',
      descricao: material?.descricao ?? '',
      unidadeMedida: material?.unidadeMedida ?? '',
    });
    setErrosCampo({});
    setErroModal(null);
  }

  async function salvar(e) {
    e.preventDefault();
    setErrosCampo({});
    setErroModal(null);
    setEnviando(true);
    try {
      if (modal.material) {
        await atualizarMaterial(modal.material.id, form);
      } else {
        await criarMaterial(form);
      }
      setModal({ aberto: false, material: null });
      carregar(busca);
    } catch (err) {
      setErrosCampo(errosPorCampo(err));
      setErroModal(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Package className="text-brand-600" size={22} />
          <h1 className="text-xl font-semibold text-slate-800">Catálogo de Materiais</h1>
        </div>
        <Can modulo="ESTOQUE" acao="CRIAR">
          <Button onClick={() => abrirModal(null)}>
            <Plus size={16} /> Novo Material
          </Button>
        </Can>
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          carregar(busca);
        }}
        className="flex gap-2"
      >
        <div className="relative flex-1">
          <Search size={16} className=" absolute left-3 top-3 text-slate-400" />
          <input
            type="text"
            placeholder="Buscar material por nome..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            className="min-h-[38px] w-full rounded-lg border border-slate-300 pl-9 pr-3 text-sm outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>
        <Button type="submit" variante="secundario">
          Filtrar
        </Button>
      </form>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={() => carregar(busca)} />}
      {!erro && !materiais && <Spinner className="mt-10" />}
      {materiais && materiais.length === 0 && (
        <EmptyState mensagem="Nenhum material encontrado no catálogo." />
      )}

      {materiais && materiais.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-slate-600">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">Insumo / Material</th>
                <th className="px-4 py-3">Unidade</th>
                <th className="px-4 py-3">Descrição</th>
                <th className="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {materiais.map((m) => (
                <tr key={m.id} className="border-t border-slate-100 hover:bg-slate-50">
                  <td className="px-4 py-3 text-slate-500">#{m.id}</td>
                  <td className="px-4 py-3 font-medium text-slate-800">{m.nome}</td>
                  <td className="px-4 py-3">
                    <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-700">
                      {m.unidadeMedida}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{m.descricao || '—'}</td>
                  <td className="px-4 py-3 text-right">
                    <Can modulo="ESTOQUE" acao="EDITAR">
                      <Button variante="secundario" onClick={() => abrirModal(m)}>
                        Editar
                      </Button>
                    </Can>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        aberto={modal.aberto}
        onFechar={() => setModal({ aberto: false, material: null })}
        titulo={modal.material ? 'Editar Material' : 'Novo Material'}
      >
        <form onSubmit={salvar} className="space-y-3">
          <Input
            label="Nome do material"
            name="nome"
            value={form.nome}
            onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))}
            erro={errosCampo.nome}
            required
          />
          <Input
            label="Unidade de medida (ex: saco, m3, kg, un)"
            name="unidadeMedida"
            value={form.unidadeMedida}
            onChange={(e) => setForm((f) => ({ ...f, unidadeMedida: e.target.value }))}
            erro={errosCampo.unidadeMedida}
            required
          />
          <Input
            label="Descrição técnica (opcional)"
            name="descricao"
            value={form.descricao}
            onChange={(e) => setForm((f) => ({ ...f, descricao: e.target.value }))}
            erro={errosCampo.descricao}
          />
          {erroModal && (
            <p role="alert" className="rounded bg-red-50 p-2 text-sm text-red-700">
              {erroModal}
            </p>
          )}
          <div className="flex justify-end gap-2 pt-2">
            <Button
              variante="secundario"
              onClick={() => setModal({ aberto: false, material: null })}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={enviando}>
              {enviando ? 'Salvando...' : 'Salvar'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}