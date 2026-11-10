import { http } from '../../../shared/api/http.js';

export async function loginRequest(login, senha) {
  const { data } = await http.post('/api/auth/login', { login, senha });
  return data; // { token, tipo, expiraEmSegundos, usuario }
}
