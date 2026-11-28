import { describe, expect, it } from 'vitest';
import { adaptarDiario, adaptarListaDiarios } from '../adapters/adaptadoresDiario.js';
import { payloadAlterarDiario, payloadNovoDiario } from '../adapters/validarDiario.js';
import { payloadOcorrencia, payloadOcorrenciaRapida } from '../adapters/validarOcorrencia.js';

describe('contrato real do backend F3', () => {
  it('usa alocacaoId como equipePresenteIds e usuarioId do autor', () => {
    const diario = adaptarDiario({
      id: 3,
      equipePresente: [{ alocacaoId: 11, usuarioId: 7, nome: 'Ana' }],
      ocorrencias: [{ id: 4, autor: { alocacaoId: 11, usuarioId: 7 } }],
    });
    expect(diario.equipePresenteIds).toEqual([11]);
    expect(diario.equipePresenteUsuarioIds).toEqual([7]);
    expect(diario.ocorrencias[0].autorUsuarioId).toBe(7);
    expect(adaptarListaDiarios([diario])[0].ocorrencias[0].autorUsuarioId).toBe(7);
  });

  it('payload de criação usa somente campos do NovaDiarioDto', () => {
    expect(
      payloadNovoDiario({
        data: '2026-11-27',
        clima: 'NUBLADO',
        descricao: 'Diário',
        equipePresenteIds: ['11', ''],
      }),
    ).toEqual({
      data: '2026-11-27',
      clima: 'NUBLADO',
      descricao: 'Diário',
      equipePresenteIds: [11],
    });
  });

  it('payload de edição não envia data', () => {
    const payload = payloadAlterarDiario({
      data: '2026-11-27',
      clima: 'CHUVA_FRACA',
      descricao: 'Ajuste',
      equipePresenteIds: [11],
    });
    expect(Object.keys(payload)).toEqual(['clima', 'descricao', 'equipePresenteIds']);
    expect(payload).not.toHaveProperty('data');
  });

  it('payload de ocorrência mantém os quatro campos esperados no PUT', () => {
    const payload = payloadOcorrencia({
      categoria: 'SEGURANCA',
      descricao: 'Evento',
      dataHora: '2026-11-27T10:00',
      paralisacao: { inicio: '2026-11-27T10:00', fim: '2026-11-27T11:00' },
    });
    expect(Object.keys(payload)).toEqual(['categoria', 'descricao', 'dataHora', 'paralisacao']);
  });

  it('ocorrência rápida usa categoria padrão OUTRO', () => {
    expect(payloadOcorrenciaRapida({ data: '2026-11-27', descricao: 'Fenda' })).toEqual({
      data: '2026-11-27',
      descricao: 'Fenda',
      categoria: 'OUTRO',
    });
  });
});
