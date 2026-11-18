import { describe, expect, it } from 'vitest';
import { validarArquivo } from '../shared/utils/arquivo.js';
import { formatarDataHoraIso, formatarDataIso } from '../shared/utils/formatadores.js';

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

describe('formatadores ISO do backend', () => {
  it('formata LocalDate sem deslocar fuso', () => {
    expect(formatarDataIso('2026-11-18')).toBe('18/11/2026');
  });

  it('formata LocalDateTime sem deslocar fuso', () => {
    expect(formatarDataHoraIso('2026-11-18T10:30:00')).toBe('18/11/2026 · 10:30');
  });

  it('usa traco para valor ausente ou inválido', () => {
    expect(formatarDataIso(null)).toBe('—');
    expect(formatarDataHoraIso('')).toBe('—');
    expect(formatarDataIso('18/11/2026')).toBe('—');
  });
});
