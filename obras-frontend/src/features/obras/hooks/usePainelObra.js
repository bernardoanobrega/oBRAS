import { useCallback, useEffect, useState } from 'react';
import { buscarPainelObra } from '../api/painelApi.js';
import { adaptarPainel } from '../adapters/adaptarPainel.js';

export function usePainelObra(obraId) {
  const [painel, setPainel] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState(null);

  const recarregar = useCallback(async () => {
    const id = Number(obraId);
    if (!obraId || !Number.isInteger(id) || id <= 0) {
      setErro({ mensagem: 'Obra inválida.' });
      setPainel(null);
      setCarregando(false);
      return;
    }

    setCarregando(true);
    setErro(null);
    try {
      const dto = await buscarPainelObra(id);
      setPainel(adaptarPainel(dto));
    } catch (falha) {
      setErro(falha);
    } finally {
      setCarregando(false);
    }
  }, [obraId]);

  useEffect(() => {
    recarregar();
  }, [recarregar]);

  return { painel, carregando, erro, recarregar };
}
