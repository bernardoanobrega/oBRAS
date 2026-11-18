import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

vi.mock('../features/obras/api/useDetalheObra.js', async () => {
  const { OBRA, EQUIPE } = await import('../features/obras/mocks/detalheObraMock.js');
  return {
    useDetalheObra: vi.fn(() => ({
      obra: OBRA,
      equipe: EQUIPE,
      carregando: false,
      erro: null,
      recarregar: vi.fn(),
    })),
  };
});

import { useDetalheObra } from '../features/obras/api/useDetalheObra.js';
import EquipeObraPage from '../features/obras/pages/EquipeObraPage.jsx';
import { EQUIPE, OBRA } from '../features/obras/mocks/detalheObraMock.js';

function renderPagina() {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={['/obras/1/equipe']}>
      <Routes>
        <Route path="/obras/:obraId/equipe" element={<EquipeObraPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('EquipeObraPage', () => {
  beforeEach(() => {
    useDetalheObra.mockReturnValue({
      obra: OBRA,
      equipe: EQUIPE,
      carregando: false,
      erro: null,
      recarregar: vi.fn(),
    });
  });

  it('mostra obra e equipe real', () => {
    const html = renderPagina();
    expect(html).toContain('Equipe da Obra');
    expect(html).toContain(OBRA.nome);
    expect(html).toContain(EQUIPE[0].nome);
    expect(html).toContain(EQUIPE[0].cargo);
  });

  it('mostra spinner durante carregamento', () => {
    useDetalheObra.mockReturnValue({ obra: null, equipe: null, carregando: true, erro: null, recarregar: vi.fn() });
    expect(renderPagina()).toContain('Equipe da Obra');
  });

  it('403 sem retry', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: null,
      carregando: false,
      erro: { response: { status: 403 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Você não tem permissão para visualizar esta obra.');
    expect(html).not.toContain('Tentar novamente');
  });

  it('404 sem retry', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: null,
      carregando: false,
      erro: { response: { status: 404 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Obra não encontrada.');
    expect(html).not.toContain('Tentar novamente');
  });

  it('500 com retry', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: null,
      carregando: false,
      erro: { response: { status: 500 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Tentar novamente');
  });
});
