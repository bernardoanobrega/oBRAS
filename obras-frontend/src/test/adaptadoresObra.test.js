import { describe, expect, it } from 'vitest';
import { paraEquipe, paraHeader } from '../features/obras/api/adaptadores.js';

const dto = {
  id: 1,
  nome: 'Edifício Horizon Prime',
  descricao: 'Descrição',
  tipologia: 'RESIDENCIAL',
  endereco: 'Av. Beira Mar, 1040',
  inicioPrevisto: '2026-02-01',
  fimPrevisto: '2027-11-30',
  status: 'EM_ANDAMENTO',
  art: '2026/8941',
  responsavelId: 9,
};

describe('paraHeader', () => {
  it('mapeia DTO da API para o contrato do cabecalho', () => {
    expect(paraHeader(dto)).toEqual({
      nome: 'Edifício Horizon Prime',
      status: 'Em Andamento',
      statusTom: 'green',
      art: '2026/8941',
      tipologia: 'Residencial',
      localizacao: 'Av. Beira Mar, 1040',
      previsao: 'Fev/2026 a Nov/2027',
    });
  });

  it('mapeia todos os status', () => {
    const casos = {
      PLANEJADA: ['Planejada', 'blue'],
      EM_ANDAMENTO: ['Em Andamento', 'green'],
      PARALISADA: ['Paralisada', 'amber'],
      CANCELADA: ['Cancelada', 'red'],
      CONCLUIDA: ['Concluída', 'blue'],
    };
    for (const [chave, [rotulo, tom]] of Object.entries(casos)) {
      expect(paraHeader({ ...dto, status: chave })).toMatchObject({ status: rotulo, statusTom: tom });
    }
  });

  it('mapeia todas as tipologias', () => {
    const casos = {
      RESIDENCIAL: 'Residencial',
      COMERCIAL: 'Comercial',
      INDUSTRIAL: 'Industrial',
      INFRAESTRUTURA: 'Infraestrutura',
      REFORMA: 'Reforma',
      RESTAURO: 'Restauro',
    };
    for (const [chave, rotulo] of Object.entries(casos)) {
      expect(paraHeader({ ...dto, tipologia: chave }).tipologia).toBe(rotulo);
    }
  });

  it('usa traco para campos ausentes e previsao incompleta', () => {
    expect(paraHeader({})).toMatchObject({
      nome: '—',
      status: '—',
      statusTom: 'neutral',
      tipologia: '—',
      localizacao: '—',
      previsao: '—',
    });
    expect(paraHeader({ ...dto, fimPrevisto: null }).previsao).toBe('—');
  });

  it('capitaliza enum desconhecido', () => {
    expect(paraHeader({ ...dto, status: 'EM_REVISAO', tipologia: 'MISTA' })).toMatchObject({
      status: 'Em Revisao',
      tipologia: 'Mista',
    });
  });

  it('retorna null sem DTO', () => {
    expect(paraHeader(null)).toBeNull();
  });
});

describe('paraEquipe', () => {
  const membros = [
    { usuarioId: 5, nome: 'Carlos Mendes', cargos: ['Mestre de Obras', 'Engenheiro Residente'], dataInicio: '2026-01-10' },
    { usuarioId: 6, nome: 'Ana', cargos: [], dataInicio: null },
  ];

  it('mapeia membro para o contrato da tabela', () => {
    expect(paraEquipe(membros)).toEqual([
      {
        usuarioId: 5,
        sigla: 'CM',
        nome: 'Carlos Mendes',
        cargo: 'Mestre de Obras / Engenheiro Residente',
        tom: 'neutral',
        dataInicio: '2026-01-10',
      },
      {
        usuarioId: 6,
        sigla: 'A',
        nome: 'Ana',
        cargo: '—',
        tom: 'amber',
        dataInicio: null,
      },
    ]);
  });

  it('tom e estavel independente da ordem', () => {
    const original = paraEquipe(membros);
    const invertido = paraEquipe([...membros].reverse());
    expect(invertido.find((m) => m.usuarioId === 5).tom).toBe(original[0].tom);
    expect(['amber', 'blue', 'neutral']).toContain(original[0].tom);
  });

  it('retorna lista vazia sem membros', () => {
    expect(paraEquipe(null)).toEqual([]);
    expect(paraEquipe([])).toEqual([]);
  });
});
