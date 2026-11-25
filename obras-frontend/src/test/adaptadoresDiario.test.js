import { describe, expect, it } from 'vitest';
import {
  escolherDiario,
  paraClimaDia,
  paraDiario,
  paraFoto,
  paraOcorrencia,
  paraResumoDiario,
} from '../features/obras/api/adaptadoresDiario.js';

const resumos = [
  { id: 1, data: '2026-11-16', clima: 'NUBLADO', descricao: 'Anterior', totalOcorrencias: 1 },
  { id: 2, data: '2026-11-18', clima: 'ENSOLARADO', descricao: 'Hoje', totalOcorrencias: 3 },
  { id: 3, data: '2026-11-17', clima: 'CHUVA_FRACA', descricao: 'Ontem', totalOcorrencias: 2 },
];

describe('escolherDiario', () => {
  it('prefere o diário de hoje', () => {
    expect(escolherDiario(resumos, '2026-11-18')).toBe(resumos[1]);
  });

  it('usa o mais recente quando não há diário hoje', () => {
    expect(escolherDiario(resumos, '2026-11-19')).toBe(resumos[1]);
  });

  it('retorna null sem resumos', () => {
    expect(escolherDiario([], '2026-11-18')).toBeNull();
    expect(escolherDiario(null, '2026-11-18')).toBeNull();
  });
});

describe('paraResumoDiario', () => {
  it('mapeia DTO da API para a lista', () => {
    expect(paraResumoDiario(resumos[1])).toEqual({
      id: 2,
      dataIso: '2026-11-18',
      data: '18/11/2026',
      clima: 'Ensolarado',
      tom: 'green',
      totalOcorrencias: 3,
      descricao: 'Hoje',
    });
  });

  it('usa fallback para enum desconhecido e campos ausentes', () => {
    expect(paraResumoDiario({ clima: 'TEMPESTADE' })).toMatchObject({
      clima: 'Tempestade',
      tom: 'neutral',
      totalOcorrencias: 0,
      descricao: '—',
    });
  });
});

describe('paraClimaDia', () => {
  it('mapeia clima conhecido', () => {
    expect(paraClimaDia({ data: '2026-11-18', clima: 'CHUVA_FORTE' })).toMatchObject({
      codigo: 'CHUVA_FORTE',
      condicao: 'Chuva forte',
      data: '18/11/2026',
      tom: 'red',
      icone: 'cloudRain',
    });
  });

  it('mostra estado sem diário', () => {
    expect(paraClimaDia(null)).toMatchObject({
      codigo: null,
      condicao: 'Sem diário registrado',
      badge: '—',
      tom: 'neutral',
      icone: 'calendar',
    });
  });
});

describe('paraFoto e paraOcorrencia', () => {
  it('usa nome do arquivo quando não há legenda', () => {
    expect(paraFoto({ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: null })).toEqual({
      id: 6,
      ocorrenciaId: 4,
      nomeArquivo: 'foto.png',
      legenda: 'foto.png',
      dataEnvio: '—',
    });
  });

  it('mapeia categoria, data, paralisação e fotos', () => {
    const dto = {
      id: 4,
      diarioId: 3,
      categoria: 'LOGISTICA',
      descricao: 'Atraso na entrega',
      dataHora: '2026-11-18T10:30:00',
      autorUsuarioId: 1,
      paralisacao: { inicio: '2026-11-18T10:30:00', fim: '2026-11-18T12:00:00' },
      fotos: [{ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: 'Foto', dataEnvio: '2026-11-18T10:31:00' }],
    };
    const resultado = paraOcorrencia(dto);
    expect(resultado).toMatchObject({
      id: 4,
      data: '18/11/2026 · 10:30',
      tag: 'Logística',
      tom: 'amber',
      icone: 'truck',
      texto: 'Atraso na entrega',
      paralisacao: { inicio: '18/11/2026 · 10:30', fim: '18/11/2026 · 12:00' },
    });
    expect(resultado.fotos).toHaveLength(1);
  });
});

describe('paraDiario', () => {
  it('achata fotos das ocorrências e mantém equipe', () => {
    const dto = {
      id: 3,
      obraId: 2,
      data: '2026-11-18',
      clima: 'ENSOLARADO',
      descricao: 'Diário real',
      equipePresenteIds: [7, 9],
      ocorrencias: [
        {
          id: 4,
          categoria: 'OUTRO',
          descricao: 'Registro',
          dataHora: '2026-11-18T10:30:00',
          fotos: [{ id: 6, ocorrenciaId: 4, nomeArquivo: 'a.png', legenda: 'A' }],
        },
        {
          id: 5,
          categoria: 'SEGURANCA',
          descricao: 'Inspeção',
          dataHora: '2026-11-18T11:00:00',
          fotos: [],
        },
      ],
    };
    const resultado = paraDiario(dto);
    expect(resultado.equipePresenteIds).toEqual([7, 9]);
    expect(resultado.ocorrencias).toHaveLength(2);
    expect(resultado.fotos.map((f) => f.id)).toEqual([6]);
  });

  it('retorna null sem DTO', () => {
    expect(paraDiario(null)).toBeNull();
  });
});
