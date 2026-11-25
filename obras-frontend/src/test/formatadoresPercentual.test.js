import { describe, expect, it } from 'vitest';
import { formatarPercentual, formatarPercentualComSinal } from '../shared/utils/formatadores.js';

describe('formatarPercentual', () => {
  it('usa uma casa decimal e vírgula', () => {
    expect(formatarPercentual(65.26)).toBe('65,3%');
  });

  it('mostra sinal de mais e de menos no desvio', () => {
    expect(formatarPercentualComSinal(4)).toBe('+4,0%');
    expect(formatarPercentualComSinal(-1.9)).toBe('-1,9%');
  });

  it('mostra traço quando não há valor', () => {
    expect(formatarPercentual(null)).toBe('—');
  });
});
