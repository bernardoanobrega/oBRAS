import axios from 'axios';
import { sessao } from '../auth/sessao.js';

export const EVENTO_NAO_AUTORIZADO = 'obras:nao-autorizado';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  timeout: 30000,
});

http.interceptors.request.use((cfg) => {
  const token = sessao.token();
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

http.interceptors.response.use(
  (r) => r,
  (err) => {
    const ehLogin = err.config?.url?.includes('/api/auth/login');
    if (err.response?.status === 401 && !ehLogin) {
      window.dispatchEvent(new Event(EVENTO_NAO_AUTORIZADO));
    }
    return Promise.reject(err);
  },
);

/** Mensagem amigável a partir do ApiError do backend {status, mensagem, campos[], timestamp}. */
export function mensagemErro(err) {
  if (!err.response) return 'Sem conexão com o servidor. Verifique sua internet e tente novamente.';
  return err.response.data?.mensagem || 'Erro inesperado. Tente novamente.';
}

/** Erros de validação (HTTP 400) no formato { campo: mensagem }. */
export function errosPorCampo(err) {
  const campos = err.response?.data?.campos ?? [];
  return Object.fromEntries(campos.map((c) => [c.campo, c.mensagem]));
}
