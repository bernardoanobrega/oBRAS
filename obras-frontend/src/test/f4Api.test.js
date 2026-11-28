import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../shared/api/http.js', () => ({
  http: { get: vi.fn(), post: vi.fn(), put: vi.fn() },
}));

import { http } from '../shared/api/http.js';
import { buscarPainelObra } from '../features/obras/api/painelApi.js';
import {
  ACOES_OBRA,
  cancelarObra,
  concluirObra,
  definirResponsavelObra,
  editarObra,
  iniciarObra,
  paralisarObra,
  retomarObra,
} from '../features/obras/api/obrasApi.js';

const resposta = { data: { ok: true } };

describe('wrappers F4', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('busca painel na rota real', async () => {
    http.get.mockResolvedValue(resposta);
    await expect(buscarPainelObra(7)).resolves.toEqual({ ok: true });
    expect(http.get).toHaveBeenCalledWith('/api/obras/7/painel');
  });

  it('buscaPainelNaoDevolveAxiosResponse', async () => {
    http.get.mockResolvedValue(resposta);
    expect(await buscarPainelObra(7)).not.toHaveProperty('data');
  });

  it('editar obra continua no PUT simples', async () => {
    http.put.mockResolvedValue(resposta);
    const dto = { nome: 'Novo nome' };
    await editarObra(7, dto);
    expect(http.put).toHaveBeenCalledWith('/api/obras/7', dto);
  });

  it('responsável usa PUT escopo EDITAR', async () => {
    http.put.mockResolvedValue(resposta);
    await definirResponsavelObra(7, 42);
    expect(http.put).toHaveBeenCalledWith('/api/obras/7/responsavel', { usuarioId: 42 });
  });

  it.each([
    ['iniciar', iniciarObra],
    ['paralisar', paralisarObra],
    ['retomar', retomarObra],
    ['concluir', concluirObra],
    ['cancelar', cancelarObra],
  ])('%s usa POST escopo EDITAR', async (acao, funcao) => {
    http.post.mockResolvedValue(resposta);
    await funcao(7);
    expect(http.post).toHaveBeenCalledWith(`/api/obras/7/${acao}`);
    expect(ACOES_OBRA[acao]).toBe(funcao);
  });
});
