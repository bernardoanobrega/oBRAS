import { describe, expect, it } from 'vitest';
import { paraDecimal, rotuloObra, situacao } from './orcamento.js';

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

describe('rotuloObra', () => {
  it('dado a obra na lista do usuário, então mostra o nome', () => {
    expect(rotuloObra(2, [{ id: 2, nome: 'Residencial Teste' }])).toBe('Residencial Teste');
  });

  it('dado a obra fora da lista, então mostra o número', () => {
    expect(rotuloObra(7, [])).toBe('Obra #7');
  });
});
