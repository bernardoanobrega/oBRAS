import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../shared/auth/useAuth.js';
import { mensagemErro } from '../../../shared/api/http.js';
import Button from '../../../shared/components/ui/Button.jsx';
import Input from '../../../shared/components/ui/Input.jsx';

export default function LoginPage() {
  const { usuario, entrar } = useAuth();
  const navegar = useNavigate();
  const local = useLocation();
  const [login, setLogin] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState(null);
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to={local.state?.de || '/'} replace />;

  async function enviar(e) {
    e.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await entrar(login.trim(), senha);
      navegar(local.state?.de || '/', { replace: true });
    } catch (err) {
      setErro(mensagemErro(err));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-full items-center justify-center p-4">
      <form onSubmit={enviar} className="w-full max-w-sm space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h1 className="text-center text-2xl font-bold text-brand-700">oBRAS</h1>
        <p className="text-center text-sm text-slate-500">Entre para acessar suas obras</p>
        <Input label="Login" name="login" autoComplete="username" value={login} onChange={(e) => setLogin(e.target.value)} required />
        <Input label="Senha" name="senha" type="password" autoComplete="current-password" value={senha} onChange={(e) => setSenha(e.target.value)} required />
        {erro && <p role="alert" className="rounded bg-red-50 p-2 text-sm text-red-700">{erro}</p>}
        <Button type="submit" className="w-full" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </Button>
      </form>
    </div>
  );
}
