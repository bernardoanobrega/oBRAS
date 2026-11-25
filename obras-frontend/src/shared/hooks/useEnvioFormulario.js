import { useCallback, useState } from 'react';
import { errosPorCampo, mensagemErro } from '../api/http.js';

/** Estado padrão de envio de formulário: erros por campo, erro geral e "enviando". */
export function useEnvioFormulario() {
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const limpar = useCallback(() => {
    setErros({});
    setErroGeral(null);
  }, []);

  /** Executa a ação; devolve true se deu certo. */
  const enviar = useCallback(
    async (acao) => {
      limpar();
      setEnviando(true);
      try {
        await acao();
        return true;
      } catch (e) {
        setErros(errosPorCampo(e));
        setErroGeral(mensagemErro(e));
        return false;
      } finally {
        setEnviando(false);
      }
    },
    [limpar],
  );

  return { erros, erroGeral, enviando, enviar, limpar };
}
