import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import {
  AlocacaoFormulario,
  estadoDoErroAlocacao,
  paraCorpoAlocacao,
  validarAlocacao,
} from '../features/obras/components/equipe/AlocarUsuarioModal.jsx';
import { alternarCargo } from '../features/obras/components/equipe/CargosSelect.jsx';

const CARGOS = [{ id: 1, nome: 'Gestor da Obra' }, { id: 4, nome: 'Mestre de Obras' }];
const CANDIDATOS = [{ id: 7, nome: 'Ana Souza' }];

const renderForm = (props = {}) =>
  renderToStaticMarkup(
    <AlocacaoFormulario cargos={CARGOS} candidatos={CANDIDATOS} form={{ usuarioId: '', cargoIds: [] }}
                        onMudarUsuario={() => {}} onAlternarCargo={() => {}} {...props} />,
  );

describe('validarAlocacao', () => {
  it('formularioExigeAoMenosUmCargo', () => {
    expect(validarAlocacao({ usuarioId: '7', cargoIds: [] }).cargoIds).toBe('Escolha ao menos um cargo.');
    expect(validarAlocacao({ usuarioId: '7', cargoIds: [1] })).toEqual({});
  });

  it('alocarExigeProfissional', () => {
    expect(validarAlocacao({ usuarioId: '', cargoIds: [1] }).usuarioId).toBe('Escolha quem será alocado.');
  });

  it('edicaoNaoExigeProfissional', () => {
    expect(validarAlocacao({ usuarioId: '', cargoIds: [1] }, true)).toEqual({});
  });
});

describe('paraCorpoAlocacao', () => {
  it('alocarEnviaUsuarioECargos', () => {
    expect(paraCorpoAlocacao({ usuarioId: '7', cargoIds: [1, 4] })).toEqual({ usuarioId: 7, cargoIds: [1, 4] });
  });

  it('alterarCargosEnviaSoCargos', () => {
    expect(paraCorpoAlocacao({ usuarioId: '', cargoIds: [4] }, true)).toEqual({ cargoIds: [4] });
  });
});

describe('alternarCargo', () => {
  it('marcaEDesmarcaSemAlterarAListaOriginal', () => {
    const original = [1];
    expect(alternarCargo(original, 4)).toEqual([1, 4]);
    expect(alternarCargo(original, 1)).toEqual([]);
    expect(original).toEqual([1]);
  });
});

describe('AlocacaoFormulario', () => {
  it('erroDeDuplicidadeExibeMensagem', () => {
    // o backend é quem sabe que a pessoa já está alocada (409/422); o front só mostra
    const err = { response: { status: 409, data: { status: 409, mensagem: 'Usuário já está alocado nesta obra.' } } };
    const html = renderForm(estadoDoErroAlocacao(err));
    expect(html).toContain('role="alert"');
    expect(html).toContain('Usuário já está alocado nesta obra.');
  });

  it('mostraCandidatosECargosParaMarcar', () => {
    const html = renderForm({ form: { usuarioId: '', cargoIds: [4] } });
    expect(html).toContain('Ana Souza');
    expect(html).toContain('type="checkbox"');
    expect(html).toContain('Mestre de Obras');
    // a caixa do cargo 4 vem marcada; a do cargo 1, não
    const caixa = (id) => html.match(new RegExp(`<input[^>]*value="${id}"[^>]*>`))[0];
    expect(caixa(4)).toContain('checked');
    expect(caixa(1)).not.toContain('checked');
  });

  it('edicaoMostraNomeSemSeletorDeProfissional', () => {
    const html = renderForm({ edicao: true, nomeUsuario: 'Ana Souza', form: { usuarioId: '', cargoIds: [1] } });
    expect(html).toContain('Profissional: <span');
    expect(html).not.toContain('name="usuarioId"');
    expect(html).toContain('Salvar cargos');
  });
});
