import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import {
  ObraFormulario,
  estadoDoErro,
  formularioInicial,
  paraCorpoObra,
  validarObra,
} from '../features/obras/components/ObraFormModal.jsx';

const VALIDO = {
  nome: 'Edifício Horizon',
  tipologia: 'RESIDENCIAL',
  endereco: 'Rua A, 100',
  descricao: '',
  inicioPrevisto: '2026-01-10',
  fimPrevisto: '2026-12-20',
};

const semAcao = () => () => {};
const renderFormulario = (props) =>
  renderToStaticMarkup(<ObraFormulario form={VALIDO} onMudar={semAcao} {...props} />);

describe('validarObra', () => {
  it('formularioValidoNaoTemErros', () => {
    expect(validarObra(VALIDO)).toEqual({});
  });

  it('formularioExigeCamposObrigatorios', () => {
    const erros = validarObra(formularioInicial(null));
    expect(Object.keys(erros).sort()).toEqual(['endereco', 'fimPrevisto', 'inicioPrevisto', 'nome', 'tipologia']);
  });

  it('formularioRejeitaPeriodoInvertido', () => {
    const erros = validarObra({ ...VALIDO, inicioPrevisto: '2026-12-20', fimPrevisto: '2026-01-10' });
    expect(erros.fimPrevisto).toBe('O fim previsto não pode ser antes do início.');
    // mesmo dia é permitido (fim >= início)
    expect(validarObra({ ...VALIDO, fimPrevisto: VALIDO.inicioPrevisto })).toEqual({});
  });

  it('formularioPermiteResponsavelVazio', () => {
    // ADR-05: a obra nasce sem responsável; ele é definido depois pelo PUT /responsavel (L13)
    const form = { ...VALIDO, responsavelId: null };
    expect(validarObra(form)).toEqual({});
    const corpo = paraCorpoObra(form);
    expect(corpo).not.toHaveProperty('responsavelId');
    expect(corpo).not.toHaveProperty('status');
  });

  it('nomeAcimaDoLimiteDoBancoEhRejeitado', () => {
    expect(validarObra({ ...VALIDO, nome: 'x'.repeat(201) }).nome).toBe('Máximo de 200 caracteres.');
  });
});

describe('paraCorpoObra', () => {
  it('apara espacos e manda descricao vazia como null', () => {
    expect(paraCorpoObra({ ...VALIDO, nome: '  Obra X  ', descricao: '   ' })).toMatchObject({
      nome: 'Obra X',
      descricao: null,
    });
  });

  it('edicao abre preenchida com o ObraDto', () => {
    const dto = { ...VALIDO, id: 9, descricao: null, status: 'PLANEJADA' };
    expect(formularioInicial(dto)).toEqual({ ...VALIDO, descricao: '' });
  });
});

describe('ObraFormulario', () => {
  it('mensagemDeErroDoBackendEhExibida', () => {
    // formato do ApiError do backend: {status, mensagem, campos[]}
    const err = {
      response: {
        status: 422,
        data: {
          status: 422,
          mensagem: 'Já existe uma obra com esse nome.',
          campos: [{ campo: 'endereco', mensagem: 'tamanho deve ser entre 0 e 300' }],
        },
      },
    };
    const html = renderFormulario(estadoDoErro(err));
    expect(html).toContain('Já existe uma obra com esse nome.');
    expect(html).toContain('role="alert"');
    expect(html).toContain('tamanho deve ser entre 0 e 300');
  });

  it('semConexaoMostraMensagemAmigavel', () => {
    const html = renderFormulario(estadoDoErro({ message: 'Network Error' }));
    expect(html).toContain('Sem conexão com o servidor.');
  });

  it('formularioNaoTemCampoDeResponsavelNemStatus', () => {
    const html = renderFormulario({});
    expect(html).not.toContain('name="responsavelId"');
    expect(html).not.toContain('name="status"');
    expect(html).toContain('name="tipologia"');
  });
});
