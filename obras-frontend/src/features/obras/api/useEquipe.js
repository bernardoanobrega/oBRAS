import { useCallback, useEffect, useState } from 'react';
import { paraHeader } from './adaptadores.js';
import { paraAlocacoes, responsavelTecnico } from './adaptadoresEquipe.js';
import { listarAlocacoes } from './equipeApi.js';
import { buscarObra } from './obrasApi.js';

const CARREGANDO = { carregando: true, obra: null, alocacoes: [], responsavel: null, erro: null };

/** Obra + alocações ativas (L25). Devolve { obra, alocacoes, responsavel, carregando, erro, recarregar }. */
export function useEquipe(obraId) {
  const [estado, setEstado] = useState(CARREGANDO);
  const [tentativa, setTentativa] = useState(0);

  const recarregar = useCallback(() => setTentativa((n) => n + 1), []);

  useEffect(() => {
    const id = Number(obraId);
    if (!Number.isInteger(id) || id <= 0) {
      setEstado({ ...CARREGANDO, carregando: false, erro: { mensagem: 'Obra inválida.' } });
      return undefined;
    }

    let vivo = true;
    setEstado(CARREGANDO);
    Promise.all([buscarObra(id), listarAlocacoes(id)])
      .then(([dto, dtos]) => {
        if (!vivo) return;
        const alocacoes = paraAlocacoes(dtos);
        setEstado({
          carregando: false,
          obra: paraHeader(dto),
          alocacoes,
          responsavel: responsavelTecnico(dto, alocacoes),
          erro: null,
        });
      })
      .catch((erro) => vivo && setEstado({ ...CARREGANDO, carregando: false, erro }));

    return () => {
      vivo = false;
    };
  }, [obraId, tentativa]);

  return { ...estado, recarregar };
}
