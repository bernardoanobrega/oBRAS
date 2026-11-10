import { useCallback, useEffect, useState } from 'react';
import { useAuth } from './useAuth.js';

/**
 * Permissões efetivas. Sem obraId => globais; com obraId => cargos da alocação + exceções individuais.
 * Esconder botões é só conforto: a segurança real é a do backend.
 */
export function usePermissao(obraId = null) {
  const { permissoesGlobais, permissoesDaObra } = useAuth();
  const [doObra, setDoObra] = useState(null);

  useEffect(() => {
    if (obraId == null) {
      setDoObra(null);
      return undefined;
    }
    let vivo = true;
    setDoObra(null);
    permissoesDaObra(obraId)
      .then((s) => vivo && setDoObra(s))
      .catch(() => vivo && setDoObra(new Set()));
    return () => {
      vivo = false;
    };
  }, [obraId, permissoesDaObra]);

  const conjunto = obraId == null ? permissoesGlobais : doObra;
  const pode = useCallback((modulo, acao) => !!conjunto && conjunto.has(`${modulo}:${acao}`), [conjunto]);

  return { pode, carregando: conjunto == null };
}
