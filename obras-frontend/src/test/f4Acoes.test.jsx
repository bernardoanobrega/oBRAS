import { beforeEach, describe, expect, it, vi } from 'vitest';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../shared/auth/usePermissao.js', () => ({ usePermissao: vi.fn() }));

import { usePermissao } from '../shared/auth/usePermissao.js';
import {
  confirmarAcao,
  mensagemErroAcao,
} from '../features/obras/components/ObraAcoesMenu.jsx';
import ObraAcoesMenu from '../features/obras/components/ObraAcoesMenu.jsx';
import {
  estadoAposErro,
  salvarParametros,
} from '../features/obras/components/EditarParametrosModal.jsx';
import {
  ObraFormulario,
  formularioInicial,
} from '../features/obras/components/ObraFormModal.jsx';

const form = {
  nome: 'Edifício Horizon Prime',
  tipologia: 'RESIDENCIAL',
  endereco: 'Rua Augusta, 1000',
  descricao: 'Obra de teste',
  inicioPrevisto: '2026-02-01',
  fimPrevisto: '2027-11-30',
};

function html(elemento) {
  return renderToStaticMarkup(React.createElement(MemoryRouter, null, elemento));
}

describe('ações L47', () => {
  beforeEach(() => {
    usePermissao.mockReturnValue({ pode: () => true });
  });

  it('editarParametrosAtualizaObra', async () => {
    const salvar = vi.fn().mockResolvedValue({ id: 7 });
    const corpo = await salvarParametros(7, form, salvar);
    expect(salvar).toHaveBeenCalledWith(7, form);
    expect(corpo.nome).toBe('Edifício Horizon Prime');
    expect(corpo).not.toHaveProperty('status');
  });

  it('iniciarSemResponsavelExplicaBloqueio', () => {
    const erro = { response: { status: 422, data: { mensagem: 'Responsável técnico obrigatório.' } } };
    expect(mensagemErroAcao('iniciar', erro)).toBe('Responsável técnico obrigatório.');
  });

  it('paralisarNaoEncerraEquipeNaUI', () => {
    const markup = html(React.createElement(ObraAcoesMenu, { obra: { id: 7, status: 'EM_ANDAMENTO' } }));
    expect(markup).toContain('Paralisar obra');
    expect(markup).not.toContain('A equipe será encerrada');
  });

  it('concluirPedeConfirmacao', () => {
    const perguntas = [];
    const resultado = confirmarAcao('concluir', (pergunta) => {
      perguntas.push(pergunta);
      return false;
    });
    expect(resultado).toBe(false);
    expect(perguntas[0]).toMatch(/equipe será encerrada/i);
  });

  it('cancelarPedeConfirmacao', () => {
    const perguntas = [];
    confirmarAcao('cancelar', (pergunta) => perguntas.push(pergunta));
    expect(perguntas[0]).toMatch(/equipe será encerrada/i);
  });

  it('acaoSemPermissaoNaoAparece', () => {
    usePermissao.mockReturnValue({ pode: () => false });
    const sem = html(React.createElement(ObraAcoesMenu, { obra: { id: 7, status: 'PLANEJADA' } }));
    expect(sem).not.toContain('Editar Parâmetros');
    expect(sem).not.toContain('Nova Ocorrência');

    usePermissao.mockReturnValue({ pode: () => true });
    const com = html(React.createElement(ObraAcoesMenu, { obra: { id: 7, status: 'PLANEJADA' } }));
    expect(com).toContain('Editar Parâmetros');
    expect(com).toContain('Nova Ocorrência');
  });

  it('erro422EhExibido', () => {
    const erro = {
      response: {
        status: 422,
        data: { mensagem: 'Dados inválidos.', campos: [{ campo: 'nome', mensagem: 'Obrigatório' }] },
      },
    };
    const estado = estadoAposErro(formularioInicial(null), erro);
    expect(estado.manterAberto).toBe(true);
    expect(estado.erros.nome).toBe('Obrigatório');
    const markup = renderToStaticMarkup(React.createElement(ObraFormulario, estado));
    expect(markup).toContain('role="alert"');
    expect(markup).toContain('Dados inválidos.');
    expect(markup).toContain('Obrigatório');
  });
});
