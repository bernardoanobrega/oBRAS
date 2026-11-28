import { beforeEach, describe, expect, it, vi } from 'vitest';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('../shared/auth/useAuth.js', () => ({ useAuth: vi.fn() }));

vi.mock('react-router-dom', async (importOriginal) => {
  const atual = await importOriginal();
  const React = await import('react');
  return {
    ...atual,
    Navigate: ({ to }) => React.createElement('span', null, `Redirecionar:${to}`),
  };
});

import { useAuth } from '../shared/auth/useAuth.js';
import Can from '../shared/auth/Can.jsx';
import RotaPrivada from '../shared/auth/RotaPrivada.jsx';

const cliente = (permutacoes = []) => ({
  usuario: { id: 1, nome: 'Usuário' },
  iniciando: false,
  permissoesGlobais: new Set(permutacoes),
  permissoesDaObra: vi.fn().mockResolvedValue(new Set()),
});

describe('permissões e rota real F4', () => {
  beforeEach(() => {
    useAuth.mockReturnValue(cliente(['DETALHES_OBRA:VISUALIZAR']));
  });

  it('CanComPermissaoGlobalRenderiza', () => {
    const markup = renderToStaticMarkup(
      React.createElement(Can, { modulo: 'DETALHES_OBRA', acao: 'VISUALIZAR' }, 'Conteúdo liberado'),
    );
    expect(markup).toBe('Conteúdo liberado');
  });

  it('CanSemPermissaoNaoRenderiza', () => {
    useAuth.mockReturnValue(cliente([]));
    const markup = renderToStaticMarkup(
      React.createElement(Can, { modulo: 'DETALHES_OBRA', acao: 'VISUALIZAR' }, 'Conteúdo liberado'),
    );
    expect(markup).toBe('');
  });

  it('rotaPrivadaRedirecionaSemLogin', () => {
    useAuth.mockReturnValue({
      usuario: null,
      iniciando: false,
      permissoesGlobais: new Set(),
      permissoesDaObra: vi.fn(),
    });
    const markup = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        { initialEntries: ['/obras'] },
        React.createElement(
          Routes,
          null,
          React.createElement(
            Route,
            { element: React.createElement(RotaPrivada) },
            React.createElement(Route, { path: '/obras', element: React.createElement('p', null, 'Área privada') }),
          ),
          React.createElement(Route, { path: '/login', element: React.createElement('p', null, 'Entrar') }),
        ),
      ),
    );
    expect(markup).toContain('Redirecionar:/login');
    expect(markup).not.toContain('Área privada');
  });

  it('rotasProtegidasRedirecionam', () => {
    useAuth.mockReturnValue({
      usuario: null,
      iniciando: false,
      permissoesGlobais: new Set(),
      permissoesDaObra: vi.fn(),
    });
    const markup = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        { initialEntries: ['/obras'] },
        React.createElement(
          Routes,
          null,
          React.createElement(
            Route,
            { element: React.createElement(RotaPrivada) },
            React.createElement(Route, { path: '/obras', element: React.createElement('p', null, 'Área privada') }),
          ),
          React.createElement(Route, { path: '/login', element: React.createElement('p', null, 'Entrar') }),
        ),
      ),
    );
    expect(markup).toContain('Redirecionar:/login');
    expect(markup).not.toContain('Área privada');
  });

  it('rotaPrivadaLiberaComLogin', () => {
    const markup = renderToStaticMarkup(
      React.createElement(
        MemoryRouter,
        { initialEntries: ['/obras'] },
        React.createElement(
          Routes,
          null,
          React.createElement(
            Route,
            { element: React.createElement(RotaPrivada) },
            React.createElement(Route, { path: '/obras', element: React.createElement('p', null, 'Área privada') }),
          ),
          React.createElement(Route, { path: '/login', element: React.createElement('p', null, 'Entrar') }),
        ),
      ),
    );
    expect(markup).toContain('Área privada');
    expect(markup).not.toContain('Entrar');
  });
});
