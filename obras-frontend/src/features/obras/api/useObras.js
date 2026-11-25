import { useCallback, useEffect, useState } from 'react';
import { listarObras } from './obrasApi.js';

const CARREGANDO = { carregando: true, obras: null, erro: null };

/**
 * Lista de obras visíveis ao usuário (o backend já filtra por DETALHES_OBRA:VISUALIZAR).
 * Devolve os ObraDto crus: a página adapta para exibir e o formulário de edição usa os campos originais.
 */
export function useObras() {
  const [estado, setEstado] = useState(CARREGANDO);
  const [tentativa, setTentativa] = useState(0);

  const recarregar = useCallback(() => setTentativa((n) => n + 1), []);

  useEffect(() => {
    let vivo = true;
    setEstado(CARREGANDO);
    listarObras()
      .then((obras) => vivo && setEstado({ carregando: false, obras, erro: null }))
      .catch((erro) => vivo && setEstado({ carregando: false, obras: null, erro }));
    return () => {
      vivo = false;
    };
  }, [tentativa]);

  return { ...estado, recarregar };
}
