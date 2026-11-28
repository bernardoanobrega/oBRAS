import { describe, expect, it } from 'vitest';
import {
  normalizarDiarioDto,
  normalizarOcorrenciaDto,
  paraFormularioDiario,
  paraFormularioOcorrencia,
  temDiarioParaData,
} from '../features/obras/adapters/diarioF3.js';
import { payloadAlterarDiario, payloadNovoDiario, validarDiario } from '../features/obras/adapters/validarDiario.js';

describe('formulário de diário', () => {
  it('formularioRejeitaClimaVazio', () => {
    expect(validarDiario({ data: '2026-11-27', clima: '' }).erros.clima).toBe('Informe as condições climáticas.');
    expect(() => payloadNovoDiario({ data: '2026-11-27', clima: '', descricao: 'x' })).toThrow('Dados inválidos');
  });

  it('formularioRejeitaDataVazia', () => {
    expect(validarDiario({ data: '', clima: 'SOL' }).erros.data).toBe('Informe a data do diário.');
  });

  it('edicaoCarregaValores', () => {
    const dto = normalizarDiarioDto({
      id: 3,
      data: '2026-11-18',
      clima: 'ENSOLARADO',
      descricao: 'Diário real',
      equipePresente: [{ alocacaoId: 11, usuarioId: 7, nome: 'Ana' }],
      ocorrencias: [],
    });
    expect(dto.equipePresenteIds).toEqual([11]);
    expect(dto.equipePresenteUsuarioIds).toEqual([7]);
    expect(paraFormularioDiario(dto)).toEqual({
      data: '2026-11-18',
      clima: 'ENSOLARADO',
      descricao: 'Diário real',
      equipePresenteIds: ['11'],
    });
    expect(payloadAlterarDiario(paraFormularioDiario(dto))).toEqual({
      clima: 'ENSOLARADO',
      descricao: 'Diário real',
      equipePresenteIds: [11],
    });
  });

  it('normaliza autor da ocorrência para autorUsuarioId', () => {
    const ocorrencia = normalizarOcorrenciaDto({
      id: 4,
      autor: { alocacaoId: 11, usuarioId: 7, nome: 'Ana' },
      fotos: [],
    });
    expect(ocorrencia.autorUsuarioId).toBe(7);
    expect(ocorrencia.autor.alocacaoId).toBe(11);
  });

  it('pede clima apenas sem diário na data', () => {
    const resumos = [{ id: 3, data: '2026-11-18', clima: 'ENSOLARADO' }];
    expect(temDiarioParaData(resumos, '2026-11-18')).toBe(true);
    expect(temDiarioParaData(resumos, '2026-11-19')).toBe(false);
  });

  it('novo diário envia todos os campos do DTO', () => {
    expect(payloadNovoDiario({
      data: '2026-11-18',
      clima: 'NUBLADO',
      descricao: '  Dia nublado  ',
      equipePresenteIds: ['11', ''],
    })).toEqual({
      data: '2026-11-18',
      clima: 'NUBLADO',
      descricao: 'Dia nublado',
      equipePresenteIds: [11],
    });
  });

  it('pede data quando o diário não tem ocorrências válidas', () => {
    expect(() => normalizarDiarioDto({ equipePresente: ' Ana ' })).toThrow('Campo equipePresente deve ser uma lista');
  });
});

describe('formulário de ocorrência', () => {
  it('edicaoCarregaOcorrencia', () => {
    const form = paraFormularioOcorrencia({
      id: 4,
      categoria: 'LOGISTICA',
      descricao: 'Atraso na entrega',
      dataHora: '2026-11-18T10:30:00',
      paralisacao: { inicio: '2026-11-18T10:30:00', fim: '2026-11-18T12:00:00' },
    });
    expect(form).toEqual({
      categoria: 'LOGISTICA',
      descricao: 'Atraso na entrega',
      dataHora: '2026-11-18T10:30',
      paralisacao: { inicio: '2026-11-18T10:30', fim: '2026-11-18T12:00' },
    });
  });
});
