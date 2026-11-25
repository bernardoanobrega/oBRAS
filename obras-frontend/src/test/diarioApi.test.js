import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../shared/api/http.js', () => ({
  http: { get: vi.fn() },
}));

import { http } from '../shared/api/http.js';
import { buscarDiario, buscarFotoBlob, listarDiarios } from '../features/obras/api/diarioApi.js';

beforeEach(() => {
  vi.clearAllMocks();
});

describe('diarioApi', () => {
  it('lista diários da obra', async () => {
    http.get.mockResolvedValue({ data: [{ id: 3 }] });
    await expect(listarDiarios(2)).resolves.toEqual([{ id: 3 }]);
    expect(http.get).toHaveBeenCalledWith('/api/obras/2/diarios');
  });

  it('busca diário específico', async () => {
    http.get.mockResolvedValue({ data: { id: 3 } });
    await expect(buscarDiario(2, 3)).resolves.toEqual({ id: 3 });
    expect(http.get).toHaveBeenCalledWith('/api/obras/2/diarios/3');
  });

  it('busca foto como blob', async () => {
    const blob = new Blob(['foto']);
    http.get.mockResolvedValue({ data: blob });
    await expect(buscarFotoBlob(2, 6)).resolves.toBe(blob);
    expect(http.get).toHaveBeenCalledWith('/api/obras/2/fotos/6', { responseType: 'blob' });
  });
});
