import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../features/obras/api/useObras.js', () => ({ useObras: vi.fn() }));
vi.mock('../shared/auth/usePermissao.js', () => ({ usePermissao: vi.fn() }));

import { useObras } from '../features/obras/api/useObras.js';
import { usePermissao } from '../shared/auth/usePermissao.js';
import ObrasPage, { filtrarObras } from '../features/obras/pages/ObrasPage.jsx';
import { paraHeader } from '../features/obras/api/adaptadores.js';

const linha = (id, nome, statusId) => ({ ...paraHeader({ id, nome, status: statusId }), id, statusId });

const linhas = [
  linha(1, 'Edifício Horizon', 'EM_ANDAMENTO'),
  linha(2, 'Galpão Solar', 'PLANEJADA'),
  linha(3, 'Ponte Rio', 'CONCLUIDA'),
];

const OBRA_DTO = {
  id: 1,
  nome: 'Edifício Horizon',
  tipologia: 'RESIDENCIAL',
  endereco: 'Rua A, 100',
  descricao: null,
  inicioPrevisto: '2026-01-10',
  fimPrevisto: '2026-12-20',
  status: 'PLANEJADA',
  art: null,
  responsavelId: null,
};

const estadoLista = (parcial) => ({ carregando: false, obras: null, erro: null, recarregar: vi.fn(), ...parcial });

/** globais e daObra são listas de "MODULO:ACAO". */
function permissoes({ globais = [], daObra = [] } = {}) {
  usePermissao.mockImplementation((obraId = null) => {
    const conjunto = new Set(obraId == null ? globais : daObra);
    return { pode: (m, a) => conjunto.has(`${m}:${a}`), carregando: false };
  });
}

const renderPagina = () => renderToStaticMarkup(<MemoryRouter><ObrasPage /></MemoryRouter>);

beforeEach(() => {
  permissoes();
  useObras.mockReturnValue(estadoLista({ obras: [OBRA_DTO] }));
});

describe('filtrarObras', () => {
  it('sem filtro retorna todas', () => {
    expect(filtrarObras(linhas, '', 'TODOS').map((l) => l.id)).toEqual([1, 2, 3]);
  });

  it('filtra por nome', () => {
    expect(filtrarObras(linhas, 'horizon', 'TODOS').map((l) => l.id)).toEqual([1]);
  });

  it('ignora acento na busca', () => {
    expect(filtrarObras(linhas, 'galpao', 'TODOS').map((l) => l.id)).toEqual([2]);
  });

  it('filtra por status', () => {
    expect(filtrarObras(linhas, '', 'PLANEJADA').map((l) => l.id)).toEqual([2]);
  });

  it('combina nome e status', () => {
    expect(filtrarObras(linhas, 'ponte', 'CONCLUIDA').map((l) => l.id)).toEqual([3]);
    expect(filtrarObras(linhas, 'ponte', 'PLANEJADA')).toEqual([]);
  });
});

describe('ObrasPage', () => {
  it('listaMostraLoadingDepoisDados', () => {
    useObras.mockReturnValue(estadoLista({ carregando: true }));
    const carregando = renderPagina();
    expect(carregando).toContain('aria-label="Carregando"');
    expect(carregando).not.toContain('Edifício Horizon');

    useObras.mockReturnValue(estadoLista({ obras: [OBRA_DTO] }));
    const comDados = renderPagina();
    expect(comDados).not.toContain('aria-label="Carregando"');
    expect(comDados).toContain('Edifício Horizon');
    expect(comDados).toContain('Residencial');
    expect(comDados).toContain('/obras/1/resumo');
  });

  it('listaVaziaMostraEmptyState', () => {
    useObras.mockReturnValue(estadoLista({ obras: [] }));
    const html = renderPagina();
    expect(html).toContain('Nenhuma obra disponível para visualização.');
    expect(html).not.toContain('<table');
  });

  it('erroDeApiMostraErrorState', () => {
    const erro = { response: { status: 500, data: { mensagem: 'Falha ao consultar obras.' } } };
    useObras.mockReturnValue(estadoLista({ erro }));
    const html = renderPagina();
    expect(html).toContain('Falha ao consultar obras.');
    expect(html).toContain('Tentar novamente');
    expect(html).not.toContain('aria-label="Carregando"');
  });

  it('botaoNovaObraOcultoSemPermissao', () => {
    // a permissão de criar é perguntada sem obraId (global, ADR-11); ter só a da obra não basta
    permissoes({ daObra: ['DETALHES_OBRA:CRIAR'] });
    expect(renderPagina()).not.toContain('Nova obra');

    permissoes({ globais: ['DETALHES_OBRA:CRIAR'] });
    expect(renderPagina()).toContain('Nova obra');
  });

  it('botaoEditarSoComPermissaoDaObra', () => {
    // EDITAR global não basta: a pergunta é feita com o obraId da linha (ADR-09)
    permissoes({ globais: ['DETALHES_OBRA:EDITAR'] });
    expect(renderPagina()).not.toContain('aria-label="Editar obra Edifício Horizon"');

    permissoes({ daObra: ['DETALHES_OBRA:EDITAR'] });
    expect(renderPagina()).toContain('aria-label="Editar obra Edifício Horizon"');
    expect(usePermissao).toHaveBeenCalledWith(1);
  });
});
