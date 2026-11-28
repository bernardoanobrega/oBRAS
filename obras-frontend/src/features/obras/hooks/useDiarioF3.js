import { useCallback, useMemo } from 'react';
import {
  criarDiario,
  criarOcorrencia,
  criarOcorrenciaRapida,
  editarDiario,
  editarOcorrencia,
  excluirDiario,
  excluirOcorrencia,
} from '../api/diarioApi.js';
import { useDiario } from '../api/useDiario.js';
import { normalizarDiarioDto } from '../adapters/diarioF3.js';
import { payloadAlterarDiario, payloadNovoDiario } from '../adapters/validarDiario.js';

export function useDiarioF3(obraId) {
  const dados = useDiario(obraId);
  const diario = useMemo(() => normalizarDiarioDto(dados.diario), [dados.diario]);

  const salvarDiario = useCallback(
    async (formulario) => {
      const salvo = diario?.id
        ? await editarDiario(obraId, diario.id, payloadAlterarDiario(formulario))
        : await criarDiario(obraId, payloadNovoDiario(formulario));
      dados.recarregar();
      return salvo;
    },
    [obraId, diario?.id, dados],
  );

  const removerDiario = useCallback(async () => {
    if (!diario?.id) return;
    await excluirDiario(obraId, diario.id);
    dados.recarregar();
  }, [obraId, diario?.id, dados]);

  const salvarOcorrencia = useCallback(
    async (payload, ocorrencia) => {
      if (!diario?.id) throw new Error('Selecione um diário para registrar a ocorrência.');
      const salva = ocorrencia?.id
        ? await editarOcorrencia(obraId, ocorrencia.id, payload)
        : await criarOcorrencia(obraId, diario.id, payload);
      dados.recarregar();
      return salva;
    },
    [obraId, diario?.id, dados],
  );

  const removerOcorrencia = useCallback(
    async (ocorrencia) => {
      if (!ocorrencia?.id) return;
      await excluirOcorrencia(obraId, ocorrencia.id);
      dados.recarregar();
    },
    [obraId, dados],
  );

  const salvarOcorrenciaRapida = useCallback(
    async (payload) => {
      const salva = await criarOcorrenciaRapida(obraId, payload);
      dados.recarregar();
      return salva;
    },
    [obraId, dados],
  );

  return {
    ...dados,
    diario,
    salvarDiario,
    removerDiario,
    salvarOcorrencia,
    removerOcorrencia,
    salvarOcorrenciaRapida,
  };
}
