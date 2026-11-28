import { describe, expect, it } from 'vitest';
import { TOOLTIP_SIMULADO, apresentarKpi, calcularKpi } from './adaptarKpi.js';

describe('apresentação de KPI', () => {
  it('kpiNuloMostraIndisponivel', () => {
    expect(apresentarKpi(null, 'DIARIO').rotulo).toBe('Não disponível');
  });

  it('naoConverteNuloEmZero', () => {
    expect(apresentarKpi(null).valor).toBe('—');
  });

  it('badgeSimuladoVisivel', () => {
    expect(apresentarKpi(25, 'SIMULADO').simulado).toBe(true);
  });

  it('tooltipExplicaFonte', () => {
    expect(apresentarKpi(25, 'SIMULADO').tooltip).toMatch(/simulado/i);
    expect(TOOLTIP_SIMULADO).toMatch(/não representa medição real/i);
  });

  it('calculoNullSafe', () => {
    expect(calcularKpi((a, b) => a + b, null, 2)).toBeNull();
    expect(calcularKpi((a, b) => a + b, 1, 2)).toBe(3);
  });
});
