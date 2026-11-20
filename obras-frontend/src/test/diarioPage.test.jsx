import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

vi.mock('../shared/auth/usePermissao.js', () => ({
  usePermissao: vi.fn(),
}));

vi.mock('../features/obras/api/useDiario.js', () => ({
  useDiario: vi.fn(),
}));

import { usePermissao } from '../shared/auth/usePermissao.js';
import { useDiario } from '../features/obras/api/useDiario.js';
import DiarioPage from '../features/obras/pages/DiarioPage.jsx';

const RESUMOS = [
  { id: 3, data: '2026-11-18', clima: 'ENSOLARADO', descricao: 'Diário real', totalOcorrencias: 1 },
];

const DIARIO = {
  id: 3,
  obraId: 2,
  data: '2026-11-18',
  clima: 'ENSOLARADO',
  descricao: 'Diário real',
  equipePresenteIds: [7],
  ocorrencias: [
    {
      id: 4,
      diarioId: 3,
      categoria: 'LOGISTICA',
      descricao: 'Ocorrência real',
      dataHora: '2026-11-18T10:30:00',
      autorUsuarioId: 1,
      paralisacao: null,
      fotos: [{ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: 'Foto real', dataEnvio: '2026-11-18T10:31:00' }],
    },
  ],
};

function renderPagina() {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={['/obras/2/diario']}>
      <Routes>
        <Route path="/obras/:obraId/diario" element={<DiarioPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  usePermissao.mockReturnValue({ pode: () => true, carregando: false });
  useDiario.mockReturnValue({
    resumos: RESUMOS,
    diario: DIARIO,
    selecionadoId: 3,
    carregando: false,
    erro: null,
    recarregar: vi.fn(),
    selecionarDiario: vi.fn(),
  });
});

describe('DiarioPage', () => {
  it('usa obraId nos hooks', () => {
    renderPagina();
    expect(usePermissao).toHaveBeenCalledWith('2');
    expect(useDiario).toHaveBeenCalledWith('2');
  });

  it('renderiza diário, ocorrência e foto reais', () => {
    const html = renderPagina();
    expect(html).toContain('Diário de ocorrências');
    expect(html).toContain('18/11/2026');
    expect(html).toContain('Ensolarado');
    expect(html).toContain('Ocorrência real');
    expect(html).toContain('Foto real');
    expect(html).not.toContain('SIMULADO');
  });

  it('mostra permissão de edição quando pode criar', () => {
    expect(renderPagina()).toContain('Permissão de edição');
  });

  it('mostra somente leitura sem permissão de criar', () => {
    usePermissao.mockReturnValue({
      pode: (modulo, acao) => modulo === 'DETALHES_OBRA' && acao === 'VISUALIZAR',
      carregando: false,
    });
    expect(renderPagina()).toContain('Somente leitura');
  });

  it('mostra spinner durante carregamento', () => {
    useDiario.mockReturnValue({
      resumos: [],
      diario: null,
      selecionadoId: null,
      carregando: true,
      erro: null,
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    expect(renderPagina()).toContain('aria-label="Carregando"');
  });

  it('mostra estado vazio sem diários', () => {
    useDiario.mockReturnValue({
      resumos: [],
      diario: null,
      selecionadoId: null,
      carregando: false,
      erro: null,
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Nenhum diário registrado para esta obra.');
    expect(html).toContain('Sem diário registrado para esta obra.');
  });

  it('mostra erro com repetição para 500', () => {
    useDiario.mockReturnValue({
      resumos: [],
      diario: null,
      selecionadoId: null,
      carregando: false,
      erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Erro interno. Tente novamente.');
    expect(html).toContain('Tentar novamente');
  });

  it('não oferece repetição para 403', () => {
    useDiario.mockReturnValue({
      resumos: [],
      diario: null,
      selecionadoId: null,
      carregando: false,
      erro: { response: { status: 403, data: { mensagem: 'Sem acesso.' } } },
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Você não tem permissão para visualizar o diário desta obra.');
    expect(html).not.toContain('Tentar novamente');
  });
});
