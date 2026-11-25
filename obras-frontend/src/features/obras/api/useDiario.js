import { useCallback, useEffect, useState } from 'react';
import { escolherDiario } from './adaptadoresDiario.js';
import { buscarDiario, listarDiarios } from './diarioApi.js';

const VAZIO = { carregando: true, resumos: [], diario: null, erro: null };

export function useDiario(obraId) {
  const [estado, setEstado] = useState(VAZIO);
  const [tentativa, setTentativa] = useState(0);
  const [selecionadoId, setSelecionadoId] = useState(null);

  const recarregar = useCallback(() => setTentativa((n) => n + 1), []);
  const selecionarDiario = useCallback((id) => setSelecionadoId(id), []);

  useEffect(() => {
    const id = Number(obraId);
    if (!obraId || !Number.isInteger(id) || id <= 0) {
      setEstado({ carregando: false, resumos: [], diario: null, erro: { mensagem: 'Obra inválida.' } });
      return undefined;
    }

    let vivo = true;
    setEstado(VAZIO);

    listarDiarios(id)
      .then((resumos) => {
        if (!vivo) return null;
        const escolhido =
          (selecionadoId != null && resumos.find((r) => r.id === selecionadoId)) || escolherDiario(resumos);
        setEstado({ carregando: true, resumos, diario: null, erro: null });
        if (!escolhido) {
          setEstado({ carregando: false, resumos, diario: null, erro: null });
          return null;
        }
        return buscarDiario(id, escolhido.id).then((diario) => {
          if (vivo) setEstado({ carregando: false, resumos, diario, erro: null });
        });
      })
      .catch((err) => {
        if (vivo) setEstado({ carregando: false, resumos: [], diario: null, erro: err });
      });

    return () => {
      vivo = false;
    };
  }, [obraId, tentativa, selecionadoId]);

  const idEfetivo =
    selecionadoId != null && estado.diario?.id === selecionadoId ? selecionadoId : (estado.diario?.id ?? null);

  return {
    ...estado,
    selecionadoId: idEfetivo,
    recarregar,
    selecionarDiario,
  };
}
