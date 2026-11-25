import { describe, expect, it } from 'vitest';
import { formatarDataLocal, paraDecimal, situacao } from './orcamento.js';

describe('formatarDataLocal', () => {
  it('não volta um dia em datas ISO sem hora', () => {
    expect(formatarDataLocal('2026-11-25')).toBe('25/11/2026');
  });

  it('mostra traço sem data', () => {
    expect(formatarDataLocal(null)).toBe('—');
  });
});

describe('situacao', () => {
  it('devolve rótulo e tom da saúde financeira', () => {
    expect(situacao('CRITICA')).toEqual({ rotulo: 'Crítica', tom: 'vermelho' });
  });

  it('usa traço para código desconhecido', () => {
    expect(situacao('INEXISTENTE').rotulo).toBe('—');
  });
});

describe('paraDecimal', () => {
  it('converte campo vazio em null', () => {
    expect(paraDecimal('  ')).toBeNull();
  });

  it('aceita vírgula como separador decimal', () => {
    expect(paraDecimal('1250,5')).toBe('1250.5');
  });
});
