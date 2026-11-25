import { describe, expect, it } from 'vitest';
import { tituloDaRota } from '../app/nav.js';

describe('tituloDaRota', () => {
  it('dado a rota de um item do menu, então retorna o rótulo do item', () => {
    expect(tituloDaRota('/orcamento')).toBe('Orçamento');
  });

  it('dado uma página dentro de uma obra, então retorna Detalhe de Obra', () => {
    expect(tituloDaRota('/obras/1/cronograma')).toBe('Detalhe de Obra');
  });

  it('dado uma rota que só começa igual a um item, então não confunde os itens', () => {
    expect(tituloDaRota('/obrasx')).toBe('oBRAS');
  });
});
