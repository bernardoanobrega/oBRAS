import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Button from '../../../shared/components/ui/Button.jsx';
import { errosPorCampo, mensagemErro } from '../../../shared/api/http.js';
import { atualizarUsuario, criarUsuario } from '../api/usuariosApi.js';

/** usuario nulo => cadastro; usuario preenchido => edição (só o nome, login não muda). */
export default function UsuarioFormModal({ aberto, usuario, onFechar, onSalvo }) {
  const edicao = !!usuario;
  const [form, setForm] = useState({ nome: '', login: '', senha: '' });
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (aberto) {
      setForm({ nome: usuario?.nome ?? '', login: usuario?.login ?? '', senha: '' });
      setErros({});
      setErroGeral(null);
    }
  }, [aberto, usuario]);

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  async function enviar(e) {
    e.preventDefault();
    setErros({});
    setErroGeral(null);
    setEnviando(true);
    try {
      if (edicao) await atualizarUsuario(usuario.id, { nome: form.nome });
      else await criarUsuario(form);
      onSalvo();
    } catch (err) {
      setErros(errosPorCampo(err));
      setErroGeral(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo={edicao ? 'Editar usuário' : 'Novo usuário'}>
      <form onSubmit={enviar} className="space-y-3">
        <Input label="Nome" name="nome" value={form.nome} onChange={mudar('nome')} erro={erros.nome} required />
        <Input label="Login" name="login" value={form.login} onChange={mudar('login')} erro={erros.login} disabled={edicao} required />
        {!edicao && (
          <Input label="Senha (mín. 8 caracteres)" name="senha" type="password" autoComplete="new-password"
                 value={form.senha} onChange={mudar('senha')} erro={erros.senha} required />
        )}
        {erroGeral && <p role="alert" className="rounded bg-red-50 p-2 text-sm text-red-700">{erroGeral}</p>}
        <div className="flex justify-end gap-2 pt-2">
          <Button variante="secundario" onClick={onFechar}>Cancelar</Button>
          <Button type="submit" disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar'}</Button>
        </div>
      </form>
    </Modal>
  );
}
