import { describe, expect, it } from 'vitest';
import {
  adaptarClimaPainel,
  adaptarEquipePainel,
  adaptarFotosPainel,
  adaptarOcorrenciasPainel,
} from './adaptarSecoesPainel.js';

describe('seções do painel', () => {
  it('painelListaEquipeComCargos', () => {
    const equipe = adaptarEquipePainel([{ usuarioId: 1, nome: 'Ana', cargos: ['Engenheira'] }]);
    expect(equipe[0].cargos).toEqual(['Engenheira']);
    expect(equipe[0].cargo).toBe('Engenheira');
  });

  it('painelListaOcorrenciasRecentes', () => {
    const ocorrencias = adaptarOcorrenciasPainel([
      { categoria: 'OUTRO', descricao: 'Antiga', dataHora: '2026-11-01T10:00:00' },
      { categoria: 'OUTRO', descricao: 'Recente', dataHora: '2026-11-27T10:00:00' },
    ]);
    expect(ocorrencias[0].texto).toBe('Recente');
  });

  it('muralSoMostraFotosDeOcorrencias', () => {
    expect(adaptarFotosPainel({ fotos: [{ id: 1, ocorrenciaId: 2 }, { id: 2, ocorrenciaId: null }] }))
      .toEqual([{ id: 1, ocorrenciaId: 2 }]);
  });

  it('fotoSemOcorrenciaNaoRenderiza', () => {
    expect(adaptarFotosPainel({ fotos: [{ id: 2 }] })).toEqual([]);
  });

  it('semDiarioMostraAviso', () => {
    const clima = adaptarClimaPainel({ data: null, condicao: null });
    expect(clima.semDiario).toBe(true);
    expect(clima.data).toBeNull();
    expect(clima.condicao).toBeNull();
  });
});
