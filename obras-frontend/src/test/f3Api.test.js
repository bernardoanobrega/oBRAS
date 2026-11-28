import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../shared/api/http.js', () => ({
  http: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}));

import { http } from '../shared/api/http.js';
import {
  criarDiario,
  criarOcorrencia,
  criarOcorrenciaRapida,
  editarDiario,
  editarOcorrencia,
  enviarFoto,
  excluirDiario,
  excluirFoto,
  excluirOcorrencia,
} from '../features/obras/api/diarioApi.js';

beforeEach(() => {
  vi.clearAllMocks();
});

describe('diarioApi F3', () => {
  it('cria e edita diário com payload real', async () => {
    http.post.mockResolvedValue({ data: { id: 1 } });
    http.put.mockResolvedValue({ data: { id: 1, clima: 'NUBLADO' } });
    await expect(criarDiario(2, { data: '2026-11-27', clima: 'NUBLADO' })).resolves.toEqual({ id: 1 });
    expect(http.post).toHaveBeenCalledWith('/api/obras/2/diarios', { data: '2026-11-27', clima: 'NUBLADO' });
    await expect(editarDiario(2, 1, { clima: 'NUBLADO' })).resolves.toEqual({ id: 1, clima: 'NUBLADO' });
    expect(http.put).toHaveBeenCalledWith('/api/obras/2/diarios/1', { clima: 'NUBLADO' });
  });

  it('exclui diário, ocorrência e foto', async () => {
    http.delete.mockResolvedValue({ data: undefined });
    await excluirDiario(2, 3);
    await excluirOcorrencia(2, 4);
    await excluirFoto(2, 6);
    expect(http.delete.mock.calls.map((chamada) => chamada[0])).toEqual([
      '/api/obras/2/diarios/3',
      '/api/obras/2/ocorrencias/4',
      '/api/obras/2/fotos/6',
    ]);
  });

  it('envia todas as horas no PUT da ocorrência', async () => {
    const payload = {
      categoria: 'LOGISTICA',
      descricao: 'Atraso',
      dataHora: '2026-11-18T10:30',
      paralisacao: { inicio: '2026-11-18T10:30', fim: null },
    };
    http.put.mockResolvedValue({ data: { id: 4 } });
    await expect(editarOcorrencia(2, 4, payload)).resolves.toEqual({ id: 4 });
    expect(http.put).toHaveBeenCalledWith('/api/obras/2/ocorrencias/4', payload);
  });

  it('cria ocorrência e ocorrência rápida nas rotas do controller', async () => {
    http.post.mockResolvedValue({ data: { id: 5 } });
    await criarOcorrencia(2, 3, { categoria: 'OUTRO', descricao: 'X' });
    expect(http.post).toHaveBeenCalledWith('/api/obras/2/diarios/3/ocorrencias', {
      categoria: 'OUTRO',
      descricao: 'X',
    });
    await criarOcorrenciaRapida(2, { data: '2026-11-27', categoria: 'OUTRO', descricao: 'X' });
    expect(http.post).toHaveBeenCalledWith('/api/obras/2/ocorrencias-rapidas', {
      data: '2026-11-27',
      categoria: 'OUTRO',
      descricao: 'X',
    });
  });

  it('envia foto como multipart com arquivo e legenda', async () => {
    http.post.mockResolvedValue({ data: { id: 6 } });
    const arquivo = new Blob(['imagem'], { type: 'image/png' });
    await expect(enviarFoto(2, 4, arquivo, '  Foto da trinca  ')).resolves.toEqual({ id: 6 });
    const [url, form] = http.post.mock.calls[0];
    expect(url).toBe('/api/obras/2/ocorrencias/4/fotos');
    expect(form).toBeInstanceOf(FormData);
    expect(form.get('arquivo')).toBeTruthy();
    expect(form.get('legenda')).toBe('Foto da trinca');
  });

  it('omite legenda vazia no multipart', async () => {
    http.post.mockResolvedValue({ data: { id: 7 } });
    await enviarFoto(2, 4, new Blob(['imagem'], { type: 'image/jpeg' }));
    expect(http.post.mock.calls[0][1].has('legenda')).toBe(false);
  });
});
