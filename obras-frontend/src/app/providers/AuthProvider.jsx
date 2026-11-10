import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { AuthContext } from '../../shared/auth/AuthContext.js';
import { sessao } from '../../shared/auth/sessao.js';
import { buscarPermissoes } from '../../shared/auth/permissoesApi.js';
import { EVENTO_NAO_AUTORIZADO } from '../../shared/api/http.js';
import { loginRequest } from '../../features/auth/api/authApi.js';

export default function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [permissoesGlobais, setPermissoesGlobais] = useState(null);
  const [iniciando, setIniciando] = useState(true);
  const cacheObras = useRef(new Map());

  const sair = useCallback(() => {
    sessao.limpar();
    cacheObras.current.clear();
    setPermissoesGlobais(null);
    setUsuario(null);
  }, []);

  const carregarGlobais = useCallback(async () => {
    try {
      setPermissoesGlobais(await buscarPermissoes(null));
    } catch {
      setPermissoesGlobais(new Set());
    }
  }, []);

  // restaura sessão ao abrir o app
  useEffect(() => {
    const u = sessao.token() ? sessao.usuario() : null;
    if (!u) {
      setIniciando(false);
      return;
    }
    setUsuario(u);
    carregarGlobais().finally(() => setIniciando(false));
  }, [carregarGlobais]);

  // 401 vindo do interceptor => sessão expirada
  useEffect(() => {
    window.addEventListener(EVENTO_NAO_AUTORIZADO, sair);
    return () => window.removeEventListener(EVENTO_NAO_AUTORIZADO, sair);
  }, [sair]);

  const entrar = useCallback(
    async (login, senha) => {
      const resposta = await loginRequest(login, senha);
      sessao.salvar(resposta);
      setUsuario(resposta.usuario);
      await carregarGlobais();
    },
    [carregarGlobais],
  );

  const permissoesDaObra = useCallback(async (obraId) => {
    const chave = String(obraId);
    if (!cacheObras.current.has(chave)) {
      cacheObras.current.set(chave, buscarPermissoes(obraId));
    }
    try {
      return await cacheObras.current.get(chave);
    } catch (e) {
      cacheObras.current.delete(chave);
      throw e;
    }
  }, []);

  const valor = useMemo(
    () => ({ usuario, permissoesGlobais, iniciando, entrar, sair, permissoesDaObra }),
    [usuario, permissoesGlobais, iniciando, entrar, sair, permissoesDaObra],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
