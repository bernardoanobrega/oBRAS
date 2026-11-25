import { describe, expect, it } from 'vitest';
import { exibirNumero, interpretarEntradaNumerica } from '../shared/utils/entradaNumerica.js';

describe('interpretarEntradaNumerica', () => {
  it('agrupa milhares com ponto enquanto digita', () => {
    expect(interpretarEntradaNumerica('1000000')).toEqual({
      exibicao: '1.000.000',
      valor: '1000000',
    });
  });

  it('usa vírgula para centavos e normaliza com ponto para a API', () => {
    expect(interpretarEntradaNumerica('1.000.000,5')).toEqual({
      exibicao: '1.000.000,5',
      valor: '1000000.5',
    });
  });

  it('mantém a vírgula recém-digitada', () => {
    expect(interpretarEntradaNumerica('1.250,').exibicao).toBe('1.250,');
  });

  it('limita as casas decimais', () => {
    expect(interpretarEntradaNumerica('10,999').valor).toBe('10.99');
    expect(interpretarEntradaNumerica('2,12345', { casasDecimais: 4 }).valor).toBe('2.1234');
  });

  it('ignora letras e zeros à esquerda', () => {
    expect(interpretarEntradaNumerica('R$ 0012a3').exibicao).toBe('123');
  });

  it('aceita negativo só quando permitido', () => {
    expect(interpretarEntradaNumerica('-1500', { permitirNegativo: true }).exibicao).toBe('-1.500');
    expect(interpretarEntradaNumerica('-1500').valor).toBe('1500');
  });

  it('campo vazio vira valor vazio', () => {
    expect(interpretarEntradaNumerica('')).toEqual({ exibicao: '', valor: '' });
  });
});

describe('exibirNumero', () => {
  it('formata o número vindo da API', () => {
    expect(exibirNumero(4500000)).toBe('4.500.000');
    expect(exibirNumero('1250.5')).toBe('1.250,5');
    expect(exibirNumero(-150000)).toBe('-150.000');
  });

  it('mostra vazio sem valor', () => {
    expect(exibirNumero(null)).toBe('');
  });
});
