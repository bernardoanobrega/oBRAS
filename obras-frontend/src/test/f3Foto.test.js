import { describe, expect, it } from 'vitest';
import { fotosDeOcorrencias, legendaEfetiva } from '../features/obras/adapters/adaptadoresFoto.js';
import { validarFoto } from '../features/obras/adapters/validarFoto.js';

const arquivo = (bytes, { size = bytes.length, type = 'image/jpeg', name = 'foto.jpg' } = {}) => ({
  size,
  type,
  name,
  slice: (inicio, fim) => ({ arrayBuffer: async () => Uint8Array.from(bytes.slice(inicio, fim)).buffer }),
});

describe('upload de fotos', () => {
  it('uploadRejeitaPdf', async () => {
    const pdf = arquivo([0x25, 0x50, 0x44, 0x46], { type: 'application/pdf', name: 'planta.pdf' });
    expect((await validarFoto(pdf)).valido).toBe(false);
    expect((await validarFoto(pdf)).mensagem).toBe('Envie somente imagens JPG ou PNG.');
  });

  it('uploadRejeitaArquivoGrande', async () => {
    const grande = { size: 10 * 1024 * 1024 + 1, type: 'image/jpeg', name: 'grande.jpg' };
    expect((await validarFoto(grande)).valido).toBe(false);
    expect((await validarFoto(grande)).mensagem).toBe('A imagem deve ter no máximo 10 MB.');
  });

  it('aceita jpg e png pela assinatura', async () => {
    const jpg = await validarFoto(arquivo([0xff, 0xd8, 0xff, 0x00]));
    const png = await validarFoto(
      arquivo([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a], { type: 'image/png', name: 'foto.png' }),
    );
    expect(jpg.valido).toBe(true);
    expect(png.valido).toBe(true);
  });

  it('rejeita extensão jpg com bytes de texto', async () => {
    expect((await validarFoto(arquivo([0x00, 0x01, 0x02]))).valido).toBe(false);
  });
});

describe('mural de fotos', () => {
  it('muralMostraSomenteFotosDeOcorrencias', () => {
    const fotos = fotosDeOcorrencias([
      { id: 1, descricao: 'Ocorrência', fotos: [{ id: 10, ocorrenciaId: 1 }] },
      { id: 2, descricao: 'Sem foto', fotos: [] },
    ]);
    expect(fotos).toEqual([{ id: 10, ocorrenciaId: 1, descricaoOcorrencia: 'Ocorrência' }]);
  });

  it('legendaPreencheDescricaoQuandoVazia', () => {
    expect(legendaEfetiva('  ', 'Trinca na parede')).toBe('Trinca na parede');
    expect(legendaEfetiva('Foto 1', 'Trinca na parede')).toBe('Foto 1');
  });

  it('muralSemFotoAvulsa', () => {
    const mural = fotosDeOcorrencias([
      { id: 20, fotos: [{ id: 1, url: '/foto/1' }] },
      { id: 21 },
      { fotos: [{ id: 99, ocorrenciaId: 99 }] },
    ]);
    expect(mural.every((foto) => foto.ocorrenciaId != null)).toBe(true);
    expect(mural).toHaveLength(1);
    expect(mural[0].ocorrenciaId).toBe(20);
  });
});
