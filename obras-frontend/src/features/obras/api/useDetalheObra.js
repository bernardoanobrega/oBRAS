import { useCallback, useEffect, useState } from 'react';
import { paraEquipe, paraHeader } from './adaptadores.js';
import { buscarObra, listarEquipe } from './obrasApi.js';

const VAZIO = { carregando: true, obra: null, equipe: [], erro: null };

export function useDetalheObra(obraId) {
  const [estado, setEstado] = useState(VAZIO);
  const [tentativa, setTentativa] = useState(0);

  const recarregar = useCallback(() => setTentativa((n) => n + 1), []);

  useEffect(() => {
    const id = Number(obraId);
    if (!obraId || !Number.isInteger(id) || id <= 0) {
      setEstado({ carregando: false, obra: null, equipe: [], erro: { mensagem: 'Obra inválida.' } });
      return undefined;
    }

    let vivo = true;
    setEstado(VAZIO);
    Promise.all([buscarObra(id), listarEquipe(id)])
      .then(([dto, membros]) => {
        if (vivo) setEstado({ carregando: false, obra: paraHeader(dto), equipe: paraEquipe(membros), erro: null });
      })
      .catch((err) => {
        if (vivo) setEstado({ carregando: false, obra: null, equipe: [], erro: err });
      });

    return () => {
      vivo = false;
    };
  }, [obraId, tentativa]);

  return { ...estado, recarregar };
}
