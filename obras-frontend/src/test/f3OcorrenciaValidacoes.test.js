import { describe, expect, it } from 'vitest';
import { estadoDoErro } from '../features/obras/adapters/estadoDoErro.js';
import {
  payloadOcorrencia,
  payloadOcorrenciaRapida,
  validarOcorrencia,
  validarOcorrenciaRapida,
} from '../features/obras/adapters/validarOcorrencia.js';

describe('ocorrências e paralisações', () => {
  it('formularioRejeitaCategoriaVazia', () => {
    expect(validarOcorrencia({ categoria: '', descricao: 'Texto' }).erros.categoria).toBe('Selecione uma categoria.');
  });

  it('formularioRejeitaDescricaoVazia', () => {
    expect(validarOcorrencia({ categoria: 'OUTRO', descricao: '  ' }).erros.descricao).toBe(
      'Informe a descrição da ocorrência.',
    );
  });

  it('paralisacaoRejeitaFimMenorQueInicio', () => {
    const resultado = validarOcorrencia({
      categoria: 'OUTRO',
      descricao: 'Teste',
      paralisacao: { inicio: '2026-11-28T10:00', fim: '2026-11-27T10:00' },
    });
    expect(resultado.erros['paralisacao.fim']).toBe('O fim da paralisação não pode ser anterior ao início.');
  });

  it('edicaoSemMexerNaParalisacaoReenviaParalisacao', () => {
    const paralisacao = { inicio: '2026-11-27T08:00', fim: '2026-11-27T12:00' };
    expect(payloadOcorrencia({ categoria: 'OUTRO', descricao: 'Teste', paralisacao }).paralisacao).toEqual(paralisacao);
  });

  it('edicaoEnviaTodosOsCamposNoPut', () => {
    const payload = payloadOcorrencia({
      categoria: 'LOGISTICA',
      descricao: 'Atraso',
      dataHora: '2026-11-18T10:30',
      paralisacao: { inicio: '2026-11-18T10:30', fim: '2026-11-18T12:00' },
    });
    expect(payload).toEqual({
      categoria: 'LOGISTICA',
      descricao: 'Atraso',
      dataHora: '2026-11-18T10:30',
      paralisacao: { inicio: '2026-11-18T10:30', fim: '2026-11-18T12:00' },
    });
    expect(Object.keys(payload)).toEqual(['categoria', 'descricao', 'dataHora', 'paralisacao']);
  });

  it('removerParalisacaoEnviaParalisacaoNula', () => {
    expect(payloadOcorrencia({ categoria: 'OUTRO', descricao: 'Teste' }).paralisacao).toBeNull();
    expect(payloadOcorrencia({ categoria: 'OUTRO', descricao: 'Teste', paralisacao: null }).paralisacao).toBeNull();
  });
});

describe('ocorrência rápida', () => {
  it('abreOcorrenciaRapidaComCategoriaOutro', () => {
    const payload = payloadOcorrenciaRapida({ data: '2026-11-27', descricao: 'Trinca' });
    expect(payload.categoria).toBe('OUTRO');
    expect(payload).not.toHaveProperty('clima');
  });

  it('exigeDescricaoNaOcorrenciaRapida', () => {
    expect(validarOcorrenciaRapida({ data: '2026-11-27', descricao: '' }).erros.descricao).toBeTruthy();
    expect(() => payloadOcorrenciaRapida({ data: '2026-11-27', descricao: '' })).toThrow('Dados inválidos');
  });

  it('pede clima quando não há diário na data', () => {
    expect(validarOcorrenciaRapida({ data: '2026-11-27', descricao: 'X' }, { requerClima: true }).erros.clima).toBeTruthy();
    expect(payloadOcorrenciaRapida({ data: '2026-11-27', descricao: 'X', clima: 'NUBLADO' }, { requerClima: true }))
      .toMatchObject({ clima: 'NUBLADO', categoria: 'OUTRO' });
  });

  it('não pede clima quando diário existe', () => {
    const validacao = validarOcorrenciaRapida({ data: '2026-11-27', descricao: 'X' }, { requerClima: false });
    expect(validacao.valido).toBe(true);
    expect(validacao.erros.clima).toBeUndefined();
  });
});

describe('erros do backend', () => {
  it('erroDoBackendEhExibido', () => {
    const estado = estadoDoErro({
      response: {
        status: 422,
        data: {
          mensagem: 'Clima é obrigatório quando não existe diário no dia',
          campos: [{ campo: 'clima', mensagem: 'Campo obrigatório' }],
        },
      },
    });
    expect(estado.erroGeral).toBe('Clima é obrigatório quando não existe diário no dia');
    expect(estado.erros).toEqual({ clima: 'Campo obrigatório' });
  });
});
