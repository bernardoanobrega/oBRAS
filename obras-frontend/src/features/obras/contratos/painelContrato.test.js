import { describe, expect, it } from 'vitest';
import { adaptarPainel } from '../adapters/adaptarPainel.js';
import { apresentarKpi } from '../adapters/adaptarKpi.js';
import { painelComDados, painelComNulos } from '../fixtures/painel.fixture.js';

describe('contrato do painel F4', () => {
  it('contratoPainelBateComFrontend', () => {
    const painel = adaptarPainel(painelComDados);
    for (const campo of ['obra', 'clima', 'kpis', 'equipe', 'maoDeObra', 'ocorrencias', 'fotos']) {
      expect(painel).toHaveProperty(campo);
    }
    expect(Object.keys(painel.kpis).sort()).toEqual([
      'avancoPrevisto',
      'avancoRealizado',
      'executado',
      'fonte',
      'limiteAlerta',
      'orcamentoTotal',
      'spi',
    ]);
  });

  it('painelRenderizaDadosReais', () => {
    const painel = adaptarPainel(painelComDados);
    expect(painel.obra.nome).toBe('Obra de teste');
    expect(painel.equipe[0].usuarioId).toBe(10);
    expect(painel.ocorrencias[0].descricao).toBe('Verificação');
  });

  it('painelRenderizaNulos', () => {
    const painel = adaptarPainel(painelComNulos);
    expect(painel.clima.data).toBeNull();
    expect(painel.clima.condicao).toBeNull();
    expect(painel.kpis.spi).toBeNull();
    expect(painel.maoDeObra.presentesHoje).toBe(0);
    expect(painel.maoDeObra.vsOntem).toBeNull();
  });

  it('kpiNuloNaoViraZero', () => {
    expect(adaptarPainel(painelComNulos).kpis.orcamentoTotal).toBeNull();
  });

  it('fonteSimuladoEhVisivel', () => {
    expect(adaptarPainel(painelComDados).kpis.fonte).toBe('SIMULADO');
  });

  it('fotosVemDoPainel', () => {
    expect(adaptarPainel(painelComDados).fotos).toEqual(painelComDados.fotos);
  });

  it('kpiNuloMostraIndisponivel', () => {
    expect(apresentarKpi(null, 'SIMULADO').rotulo).toBe('Não disponível');
  });

  it('naoConverteNuloEmZero', () => {
    expect(apresentarKpi(null, 'SIMULADO').valor).toBe('—');
  });

  it('badgeSimuladoVisivel', () => {
    expect(apresentarKpi(42, 'SIMULADO').simulado).toBe(true);
  });

  it('tooltipExplicaFonte', () => {
    expect(apresentarKpi(42, 'SIMULADO').tooltip).toMatch(/simulado/i);
  });
});
