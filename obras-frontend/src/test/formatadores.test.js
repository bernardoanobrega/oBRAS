import { describe, expect, it } from 'vitest';
import { validarArquivo } from '../shared/utils/arquivo.js';

describe('validarArquivo', () => {
  it('aceita PDF pequeno', () => {
    expect(validarArquivo({ type: 'application/pdf', size: 1000 })).toBeNull();
  });
  it('rejeita formato inválido', () => {
    expect(validarArquivo({ type: 'text/plain', size: 10 })).toMatch(/Formato/);
  });
  it('rejeita arquivo acima de 10 MB', () => {
    expect(validarArquivo({ type: 'image/png', size: 11 * 1024 * 1024 })).toMatch(/10 MB/);
  });
});
