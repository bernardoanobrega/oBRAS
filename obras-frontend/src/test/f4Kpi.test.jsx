import { describe, expect, it } from 'vitest';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { KpiGrid } from '../features/obras/components/detalhe/KpiCard.jsx';
import MuralFotos from '../features/obras/components/detalhe/MuralFotos.jsx';
import { adaptarFotosPainel } from '../features/obras/adapters/adaptarSecoesPainel.js';
import { painelComDados, painelComNulos } from '../features/obras/fixtures/painel.fixture.js';

describe('KPIs e fotos no SSR F4', () => {
  it('fonteSimuladoEhVisivel', () => {
    const markup = renderToStaticMarkup(React.createElement(KpiGrid, { kpis: painelComDados.kpis }));
    expect(markup).toContain('SIMULADO');
  });

  it('tooltipExplicaFonte', () => {
    const markup = renderToStaticMarkup(React.createElement(KpiGrid, { kpis: painelComDados.kpis }));
    expect(markup).toMatch(/não representa medição real/i);
  });

  it('kpisNaoQuebramLayoutMobile', () => {
    const markup = renderToStaticMarkup(React.createElement(KpiGrid, { kpis: painelComDados.kpis }));
    expect(markup).toContain('grid-cols-1');
    expect(markup).not.toMatch(/min-w-\[/);
    expect(markup).not.toMatch(/w-\[\d+px\]/);
  });

  it('kpiNuloMostraIndisponivel', () => {
    const markup = renderToStaticMarkup(React.createElement(KpiGrid, { kpis: painelComNulos.kpis }));
    expect(markup).toContain('Não disponível');
  });

  it('naoConverteNuloEmZero', () => {
    const markup = renderToStaticMarkup(React.createElement(KpiGrid, { kpis: painelComNulos.kpis }));
    expect(markup).toContain('Não disponível');
    expect(markup).not.toContain('R$ 0,00');
  });

  it('fotosVemDoPainel', () => {
    const markup = renderToStaticMarkup(
      React.createElement(MuralFotos, { obraId: 5, fotos: adaptarFotosPainel(painelComDados) }),
    );
    expect(markup).toContain('Foto de teste');
  });
});
