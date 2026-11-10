import { useCallback, useEffect, useState } from 'react';
import Button from '../../../shared/components/ui/Button.jsx';
import Badge from '../../../shared/components/ui/Badge.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import Can from '../../../shared/auth/Can.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import { mensagemErro } from '../../../shared/api/http.js';
import { formatarDataHora } from '../../../shared/utils/formatadores.js';
import { desativarUsuario, listarUsuarios } from '../api/usuariosApi.js';
import UsuarioFormModal from '../components/UsuarioFormModal.jsx';
import PermissoesModal from '../components/PermissoesModal.jsx';

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState(null);
  const [erro, setErro] = useState(null);
  const [form, setForm] = useState({ aberto: false, usuario: null });
  const [permissoes, setPermissoes] = useState(null);

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      setUsuarios(await listarUsuarios());
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, []);

  useEffect(() => { carregar(); }, [carregar]);

  async function desativar(u) {
    if (!window.confirm(`Desativar ${u.nome}? O usuário não poderá mais entrar, mas o histórico é mantido.`)) return;
    try {
      await desativarUsuario(u.id);
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-semibold">Usuários</h1>
        <Can modulo="USUARIOS" acao="CRIAR">
          <Button onClick={() => setForm({ aberto: true, usuario: null })}>Novo usuário</Button>
        </Can>
      </div>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregar} />}
      {!erro && !usuarios && <Spinner className="mt-10" />}
      {usuarios && usuarios.length === 0 && <EmptyState mensagem="Nenhum usuário cadastrado." />}

      {usuarios && usuarios.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-slate-600">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">Login</th>
                <th className="px-4 py-3">Situação</th>
                <th className="px-4 py-3">Cadastro</th>
                <th className="px-4 py-3 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id} className="border-t">
                  <td className="px-4 py-3 font-medium">{u.nome}</td>
                  <td className="px-4 py-3">{u.login}</td>
                  <td className="px-4 py-3"><Badge tom={u.situacao === 'ATIVO' ? 'verde' : 'cinza'}>{u.situacao}</Badge></td>
                  <td className="px-4 py-3">{formatarDataHora(u.dataCadastro)}</td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <Can modulo="USUARIOS" acao="EDITAR">
                        <Button variante="secundario" onClick={() => setForm({ aberto: true, usuario: u })}>Editar</Button>
                        <Button variante="secundario" onClick={() => setPermissoes(u)}>Permissões</Button>
                      </Can>
                      <Can modulo="USUARIOS" acao="EXCLUIR">
                        {u.situacao === 'ATIVO' && <Button variante="perigo" onClick={() => desativar(u)}>Desativar</Button>}
                      </Can>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <UsuarioFormModal
        aberto={form.aberto}
        usuario={form.usuario}
        onFechar={() => setForm({ aberto: false, usuario: null })}
        onSalvo={() => { setForm({ aberto: false, usuario: null }); carregar(); }}
      />
      <PermissoesModal aberto={!!permissoes} usuario={permissoes} onFechar={() => setPermissoes(null)} />
    </div>
  );
}
