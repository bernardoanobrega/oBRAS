import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

vi.mock('../features/obras/api/useEquipe.js', () => ({ useEquipe: vi.fn() }));
vi.mock('../shared/auth/usePermissao.js', () => ({ usePermissao: vi.fn() }));

import { useEquipe } from '../features/obras/api/useEquipe.js';
import { usePermissao } from '../shared/auth/usePermissao.js';
import { paraAlocacoes, responsavelTecnico } from '../features/obras/api/adaptadoresEquipe.js';
import EquipeObraPage, { encerrarComConfirmacao } from '../features/obras/pages/EquipeObraPage.jsx';

const OBRA = { nome: 'Edifício Horizon', status: 'Em Andamento', statusTom: 'green' };
const ALOCACOES = paraAlocacoes([
  { id: 10, usuarioId: 7, nome: 'Ana Souza', cargos: [{ id: 1, nome: 'Gestor da Obra' }, { id: 4, nome: 'Mestre de Obras' }], dataInicio: '2026-03-01' },
  { id: 11, usuarioId: 8, nome: 'Bruno Lima', cargos: [{ id: 3, nome: 'Estoquista' }], dataInicio: '2026-04-15' },
]);

const estado = (parcial = {}) => ({
  obra: OBRA,
  alocacoes: ALOCACOES,
  responsavel: { definido: false, nome: null, naEquipe: false, art: null },
  carregando: false,
  erro: null,
  recarregar: vi.fn(),
  ...parcial,
});

/** daObra = lista de "MODULO:ACAO" que o usuário tem nesta obra. */
function permissoes(daObra = []) {
  const conjunto = new Set(daObra);
  usePermissao.mockReturnValue({ pode: (m, a) => conjunto.has(`${m}:${a}`), carregando: false });
}

function renderPagina() {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={['/obras/1/equipe']}>
      <Routes>
        <Route path="/obras/:obraId/equipe" element={<EquipeObraPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  permissoes();
  useEquipe.mockReturnValue(estado());
});

describe('EquipeObraPage', () => {
  it('listaEquipeMostraAlocacoes', () => {
    const html = renderPagina();
    expect(html).toContain('Equipe · Edifício Horizon');
    expect(html).toContain('Ana Souza');
    // cargos múltiplos aparecem separados
    expect(html).toContain('Gestor da Obra');
    expect(html).toContain('Mestre de Obras');
    expect(html).toContain('01/03/2026');
    expect(html).toContain('2 profissionais');
  });

  it('listaVaziaMostraEmptyState', () => {
    useEquipe.mockReturnValue(estado({ alocacoes: [] }));
    const html = renderPagina();
    expect(html).toContain('Nenhum profissional alocado nesta obra.');
    expect(html).not.toContain('<table');
  });

  it('botaoAlocarOcultoSemPermissao', () => {
    permissoes(['DETALHES_OBRA:EDITAR']);
    expect(renderPagina()).not.toContain('Alocar profissional');

    permissoes(['DETALHES_OBRA:CRIAR']);
    expect(renderPagina()).toContain('Alocar profissional');
    // a pergunta é feita com o obraId da rota (permissão da obra, ADR-09)
    expect(usePermissao).toHaveBeenCalledWith('1');
  });

  it('acoesDeEditarEncerrarSoComEditar', () => {
    permissoes(['DETALHES_OBRA:CRIAR']);
    const semEditar = renderPagina();
    expect(semEditar).not.toContain('Encerrar alocação de Ana Souza');
    expect(semEditar).not.toContain('Alterar cargos de Ana Souza');

    permissoes(['DETALHES_OBRA:EDITAR']);
    const comEditar = renderPagina();
    expect(comEditar).toContain('aria-label="Encerrar alocação de Ana Souza"');
    expect(comEditar).toContain('aria-label="Alterar cargos de Ana Souza"');
  });

  it('responsavelTecnicoEhExibido', () => {
    useEquipe.mockReturnValue(estado({
      responsavel: { definido: true, nome: 'Carla Menezes', naEquipe: false, art: 'ART-2026-000123' },
    }));
    const html = renderPagina();
    expect(html).toContain('Responsável técnico');
    expect(html).toContain('Carla Menezes');
    expect(html).toContain('fora da equipe');
    expect(html).toContain('ART-2026-000123');
  });

  it('responsavelNaoDefinido', () => {
    expect(renderPagina()).toContain('Não definido');
  });

  it('mostra spinner durante carregamento', () => {
    useEquipe.mockReturnValue(estado({ obra: null, carregando: true }));
    const html = renderPagina();
    expect(html).toContain('aria-label="Carregando"');
    expect(html).not.toContain('Ana Souza');
  });

  it('403 sem retry', () => {
    useEquipe.mockReturnValue(estado({ obra: null, erro: { response: { status: 403 } } }));
    const html = renderPagina();
    expect(html).toContain('Você não tem permissão para visualizar esta obra.');
    expect(html).not.toContain('Tentar novamente');
  });

  it('404 sem retry', () => {
    useEquipe.mockReturnValue(estado({ obra: null, erro: { response: { status: 404 } } }));
    const html = renderPagina();
    expect(html).toContain('Obra não encontrada.');
    expect(html).not.toContain('Tentar novamente');
  });

  it('500 com retry', () => {
    useEquipe.mockReturnValue(estado({ obra: null, erro: { response: { status: 500, data: { mensagem: 'Falhou.' } } } }));
    expect(renderPagina()).toContain('Tentar novamente');
  });
});

describe('encerrarComConfirmacao', () => {
  const ana = ALOCACOES[0];

  it('encerrarPedeConfirmacao', async () => {
    const confirmar = vi.fn(() => false);
    const encerrar = vi.fn();
    expect(await encerrarComConfirmacao(ana, { confirmar, encerrar })).toBe(false);
    expect(confirmar).toHaveBeenCalledWith(expect.stringContaining('Ana Souza'));
    // cancelou: a API não é chamada
    expect(encerrar).not.toHaveBeenCalled();
  });

  it('confirmadoChamaApiComIdDaAlocacao', async () => {
    const encerrar = vi.fn(async () => {});
    expect(await encerrarComConfirmacao(ana, { confirmar: () => true, encerrar })).toBe(true);
    expect(encerrar).toHaveBeenCalledWith(10);
  });
});

describe('responsavelTecnico', () => {
  it('usaNomeDoBackendMesmoForaDaEquipe', () => {
    const r = responsavelTecnico({ responsavelId: 99, responsavelNome: 'Carla Menezes', art: 'ART-1' }, ALOCACOES);
    expect(r).toEqual({ definido: true, nome: 'Carla Menezes', naEquipe: false, art: 'ART-1' });
  });

  it('semNomeDoBackendBuscaNaEquipe', () => {
    const r = responsavelTecnico({ responsavelId: 7 }, ALOCACOES);
    expect(r.nome).toBe('Ana Souza');
    expect(r.naEquipe).toBe(true);
  });

  it('semResponsavelEhNaoDefinido', () => {
    expect(responsavelTecnico({ responsavelId: null, art: 'ART-1' }, ALOCACOES)).toMatchObject({ definido: false, art: 'ART-1' });
  });
});
