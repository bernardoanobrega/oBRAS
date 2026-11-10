const K_TOKEN = 'obras.token';
const K_USER = 'obras.usuario';
const K_EXP = 'obras.expiraEm';

// Sem refresh token no backend: a sessão expira junto com o JWT.
export const sessao = {
  salvar({ token, expiraEmSegundos, usuario }) {
    localStorage.setItem(K_TOKEN, token);
    localStorage.setItem(K_EXP, String(Date.now() + expiraEmSegundos * 1000));
    localStorage.setItem(K_USER, JSON.stringify(usuario));
  },
  token() {
    const exp = Number(localStorage.getItem(K_EXP));
    if (!exp || Date.now() > exp) {
      sessao.limpar();
      return null;
    }
    return localStorage.getItem(K_TOKEN);
  },
  usuario() {
    try {
      return JSON.parse(localStorage.getItem(K_USER));
    } catch {
      return null;
    }
  },
  limpar() {
    localStorage.removeItem(K_TOKEN);
    localStorage.removeItem(K_EXP);
    localStorage.removeItem(K_USER);
  },
};
