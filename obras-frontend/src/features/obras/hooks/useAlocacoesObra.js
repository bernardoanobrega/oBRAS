import { useCallback, useEffect, useState } from 'react';
import { listarAlocacoes } from '../api/equipeApi.js';
import { mensagemErro } from '../../../shared/api/http.js';

const VAZIO = { carregando: false, alocacoes: [], erro: null };

export function useAlocacoesObra(obraId, aberto = false) {
  const [estado, setEstado] = useState(VAZIO);
  const [tentativa, setTentativa] = useState(0);
  const recarregar = useCallback(() => setTentativa((n) => n + 1), []);

  useEffect(() => {
    if (!aberto || !obraId) {
      setEstado(VAZIO);
      return undefined;
    }
    let vivo = true;
    setEstado({ carregando: true, alocacoes: [], erro: null });
    listarAlocacoes(obraId)
      .then((dtos) => {
        if (!vivo) return;
        const alocacoes = (Array.isArray(dtos) ? dtos : []).map((dto) => ({
          id: dto?.id,
          nome: dto?.nome ?? dto?.nomeUsuario ?? '—',
        }));
        setEstado({ carregando: false, alocacoes, erro: null });
      })
      .catch((err) => {
        if (vivo) setEstado({ carregando: false, alocacoes: [], erro: mensagemErro(err) });
      });
    return () => {
      vivo = false;
    };
  }, [obraId, aberto, tentativa]);

  return { ...estado, recarregar };
}
