import { describe, expect, it } from 'vitest';
import { adaptarPainel } from './adaptarPainel.js';

describe('adaptarPainel', () => {
  it('preserva nulos do clima e dos KPIs', () => {
    const painel = adaptarPainel({ clima: { data: null, condicao: null }, kpis: { spi: null, fonte: 'SIMULADO' } });
    expect(painel.clima.data).toBeNull();
    expect(painel.clima.condicao).toBeNull();
    expect(painel.kpis.spi).toBeNull();
  });

  it('kpiNuloNaoViraZero', () => {
    expect(adaptarPainel({ kpis: { orcamentoTotal: null } }).kpis.orcamentoTotal).toBeNull();
  });

  it('semDiarioMostraAlocados', () => {
    const painel = adaptarPainel({ equipe: [{ usuarioId: 1 }], clima: null, kpis: { fonte: 'SIMULADO' } });
    expect(painel.equipe).toHaveLength(1);
    expect(painel.clima.condicao).toBeNull();
  });

  it('fotosVemDoPainel', () => {
    const painel = adaptarPainel({ fotos: [{ id: 1, ocorrenciaId: 5 }, { id: 2 }] });
    expect(painel.fotos).toEqual([{ id: 1, ocorrenciaId: 5 }]);
  });

  it('fonteNaoEhConvertidaEmNumero', () => {
    expect(adaptarPainel({ kpis: { fonte: 'SIMULADO' } }).kpis.fonte).toBe('SIMULADO');
  });
});
