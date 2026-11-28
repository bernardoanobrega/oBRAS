import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';

vi.mock('../shared/auth/useAuth.js', () => ({ useAuth: vi.fn() }));

import { useAuth } from '../shared/auth/useAuth.js';
import Can from '../shared/auth/Can.jsx';
import RotaPrivada from '../shared/auth/RotaPrivada.jsx';
import RotaProtegida from '../shared/auth/RotaProtegida.jsx';

function Login() {
  const local = useLocation();
  return <p>{`Entrar:${local.state?.de ?? ''}`}</p>;
}

function cliente({
  usuario = { id: 1, nome: 'Usuária' },
  iniciando = false,
  globais = new Set(),
  daObra = new Set(),
} = {}) {
  const permissoesDaObra = vi.fn().mockResolvedValue(daObra);
  useAuth.mockReturnValue({
    usuario,
    iniciando,
    permissoesGlobais: globais,
    permissoesDaObra,
    sair: vi.fn(),
  });
  return permissoesDaObra;
}

function renderRotaPrivada() {
  return render(
    <MemoryRouter initialEntries={['/obras/5/resumo']}>
      <Routes>
        <Route element={<RotaPrivada />}>
          <Route path="/obras/:obraId/resumo" element={<p>Área privada</p>} />
        </Route>
        <Route path="/login" element={<Login />} />
      </Routes>
    </MemoryRouter>,
  );
}

function renderRotaProtegida() {
  return render(
    <MemoryRouter initialEntries={['/obras/5/resumo']}>
      <Routes>
        <Route element={<RotaPrivada />}>
          <Route element={<RotaProtegida modulo="DETALHES_OBRA" acao="VISUALIZAR" escopo="obra" />}>
            <Route path="/obras/:obraId/resumo" element={<p>Resumo liberado</p>} />
          </Route>
        </Route>
        <Route path="/login" element={<Login />} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
});

afterEach(cleanup);

describe('L50 — rotas protegidas com guards reais', () => {
  it('rotasProtegidasRedirecionam', () => {
    cliente({ usuario: null });
    renderRotaPrivada();

    expect(screen.getByText('Entrar:/obras/5/resumo')).toBeTruthy();
    expect(screen.queryByText('Área privada')).toBeNull();
  });

  it('iniciandoMostraSpinnerSemRedirecionar', () => {
    cliente({ usuario: null, iniciando: true });
    renderRotaPrivada();

    expect(screen.getByRole('status', { name: 'Carregando' })).toBeTruthy();
    expect(screen.queryByText('Entrar:/obras/5/resumo')).toBeNull();
    expect(screen.queryByText('Área privada')).toBeNull();
  });

  it('rotaPrivadaLiberaComLogin', () => {
    cliente();
    renderRotaPrivada();

    expect(screen.getByText('Área privada')).toBeTruthy();
    expect(screen.queryByText('Entrar:/obras/5/resumo')).toBeNull();
  });

  it('rotaProtegidaLiberaComParExatoDaObra', async () => {
    const permissoesDaObra = cliente({
      daObra: new Set(['DETALHES_OBRA:VISUALIZAR']),
    });
    renderRotaProtegida();

    expect(await screen.findByText('Resumo liberado')).toBeTruthy();
    await waitFor(() => expect(permissoesDaObra).toHaveBeenCalledWith('5'));
  });

  it('rotaProtegidaBloqueiaSemParExatoDaObra', async () => {
    cliente({ daObra: new Set(['DIARIO:VISUALIZAR']) });
    renderRotaProtegida();

    expect(await screen.findByRole('heading', { name: 'Sem permissão' })).toBeTruthy();
    expect(screen.queryByText('Resumo liberado')).toBeNull();
  });

  it('Can renderiza somente com par exato da obra', async () => {
    const permissoesDaObra = cliente({
      daObra: new Set(['DETALHES_OBRA:CRIAR']),
    });
    render(
      <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={5}>
        <button type="button">Nova Ocorrência</button>
      </Can>,
    );

    expect(await screen.findByRole('button', { name: 'Nova Ocorrência' })).toBeTruthy();
    await waitFor(() => expect(permissoesDaObra).toHaveBeenCalledWith(5));
  });

  it('Can esconde ação com par diferente', async () => {
    const permissoesDaObra = cliente({
      daObra: new Set(['DETALHES_OBRA:VISUALIZAR']),
    });
    render(
      <>
        <Can modulo="DETALHES_OBRA" acao="VISUALIZAR" obraId={5}>
          <button type="button">Versão permitida</button>
        </Can>
        <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={5}>
          <button type="button">Editar Parâmetros</button>
        </Can>
      </>,
    );

    expect(await screen.findByRole('button', { name: 'Versão permitida' })).toBeTruthy();
    await waitFor(() => expect(permissoesDaObra).toHaveBeenCalledWith(5));
    expect(screen.queryByRole('button', { name: 'Editar Parâmetros' })).toBeNull();
  });
});
