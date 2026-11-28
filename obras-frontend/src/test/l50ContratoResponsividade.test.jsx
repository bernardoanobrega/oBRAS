import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('../shared/auth/useAuth.js', () => ({ useAuth: vi.fn() }));
vi.mock('../shared/auth/usePermissao.js', () => ({ usePermissao: vi.fn() }));
vi.mock('../features/obras/hooks/usePainelObra.js', () => ({ usePainelObra: vi.fn() }));
vi.mock('../features/obras/api/diarioApi.js', async (importOriginal) => {
  const atual = await importOriginal();
  return { ...atual, buscarFotoBlob: vi.fn().mockResolvedValue(new Blob()) };
});

import { useAuth } from '../shared/auth/useAuth.js';
import { usePermissao } from '../shared/auth/usePermissao.js';
import { usePainelObra } from '../features/obras/hooks/usePainelObra.js';
import { adaptarPainel } from '../features/obras/adapters/adaptarPainel.js';
import { adaptarOcorrenciasPainel } from '../features/obras/adapters/adaptarSecoesPainel.js';
import AppShell from '../shared/components/layout/AppShell.jsx';
import DetalheObraPage from '../features/obras/pages/DetalheObraPage.jsx';
import { painelComDados, painelComNulos } from '../features/obras/fixtures/painel.fixture.js';

const CAMPOS = {
  painel: ['clima', 'equipe', 'fotos', 'kpis', 'maoDeObra', 'ocorrencias', 'obra'],
  obra: [
    'art',
    'auditavelId',
    'descricao',
    'endereco',
    'fimPrevisto',
    'id',
    'inicioPrevisto',
    'nome',
    'responsavelId',
    'responsavelNome',
    'status',
    'tipologia',
  ],
  clima: ['condicao', 'data'],
  kpis: ['avancoPrevisto', 'avancoRealizado', 'executado', 'fonte', 'limiteAlerta', 'orcamentoTotal', 'spi'],
  equipe: ['cargos', 'dataInicio', 'nome', 'usuarioId'],
  maoDeObra: ['alocados', 'fonte', 'presentesHoje', 'vsOntem'],
  ocorrencia: ['autor', 'categoria', 'dataHora', 'descricao', 'diarioId', 'fotos', 'id', 'paralisacao'],
  membro: ['alocacaoId', 'nome', 'usuarioId'],
  paralisacao: ['fim', 'inicio'],
  foto: ['dataEnvio', 'id', 'legenda', 'nomeArquivo', 'ocorrenciaId'],
};

const TODAS_PERMISSOES = new Set([
  'DETALHES_OBRA:VISUALIZAR',
  'DETALHES_OBRA:CRIAR',
  'DETALHES_OBRA:EDITAR',
]);

function chaves(objeto) {
  return Object.keys(objeto ?? {}).sort();
}

function esperarChaves(objeto, esperado, rotulo) {
  expect(chaves(objeto), rotulo).toEqual([...esperado].sort());
}

function verificarContratoBruto(painel) {
  esperarChaves(painel, CAMPOS.painel, 'painel bruto');
  esperarChaves(painel.obra, CAMPOS.obra, 'obra bruta');
  esperarChaves(painel.clima, CAMPOS.clima, 'clima bruto');
  esperarChaves(painel.kpis, CAMPOS.kpis, 'kpis brutos');
  esperarChaves(painel.maoDeObra, CAMPOS.maoDeObra, 'maoDeObra bruto');

  if (painel.equipe.length > 0) esperarChaves(painel.equipe[0], CAMPOS.equipe, 'equipe bruta');
  if (painel.fotos.length > 0) esperarChaves(painel.fotos[0], CAMPOS.foto, 'foto bruta');
  if (painel.ocorrencias.length > 0) {
    esperarChaves(painel.ocorrencias[0], CAMPOS.ocorrencia, 'ocorrencia bruta');
    esperarChaves(painel.ocorrencias[0].autor, CAMPOS.membro, 'autor bruto');
    esperarChaves(painel.ocorrencias[0].paralisacao, CAMPOS.paralisacao, 'paralisacao bruta');
    esperarChaves(painel.ocorrencias[0].fotos[0], CAMPOS.foto, 'foto da ocorrencia bruta');
  }
}

function verificarContrato(painel) {
  const adaptado = adaptarPainel(painel);

  esperarChaves(adaptado, CAMPOS.painel, 'painel');
  esperarChaves(adaptado.obra, CAMPOS.obra, 'obra');
  esperarChaves(adaptado.clima, CAMPOS.clima, 'clima');
  esperarChaves(adaptado.kpis, CAMPOS.kpis, 'kpis');
  esperarChaves(adaptado.maoDeObra, CAMPOS.maoDeObra, 'maoDeObra');

  if (adaptado.equipe.length > 0) esperarChaves(adaptado.equipe[0], CAMPOS.equipe, 'equipe');
  if (adaptado.fotos.length > 0) esperarChaves(adaptado.fotos[0], CAMPOS.foto, 'foto');
  if (adaptado.ocorrencias.length > 0) {
    esperarChaves(adaptado.ocorrencias[0], CAMPOS.ocorrencia, 'ocorrencia');
    esperarChaves(adaptado.ocorrencias[0].autor, CAMPOS.membro, 'autor');
    esperarChaves(adaptado.ocorrencias[0].paralisacao, CAMPOS.paralisacao, 'paralisacao');
    esperarChaves(adaptado.ocorrencias[0].fotos[0], CAMPOS.foto, 'foto da ocorrencia');
  }

  return adaptado;
}

function renderPagina({
  painel = painelComDados,
  hook = {},
  permissoes = TODAS_PERMISSOES,
  comShell = false,
} = {}) {
  useAuth.mockReturnValue({
    usuario: { id: 1, nome: 'Usuária de teste', login: 'usuaria' },
    iniciando: false,
    permissoesGlobais: new Set(),
    permissoesDaObra: vi.fn().mockResolvedValue(new Set()),
    sair: vi.fn(),
  });
  usePermissao.mockReturnValue({
    pode: (modulo, acao) => permissoes.has(`${modulo}:${acao}`),
    carregando: false,
  });
  usePainelObra.mockReturnValue({
    painel: painel ? adaptarPainel(painel) : null,
    carregando: false,
    erro: null,
    recarregar: vi.fn(),
    ...hook,
  });

  return render(
    <MemoryRouter initialEntries={['/obras/5/resumo']}>
      <Routes>
        {comShell ? (
          <Route element={<AppShell />}>
            <Route path="/obras/:obraId/resumo" element={<DetalheObraPage />} />
          </Route>
        ) : (
          <Route path="/obras/:obraId/resumo" element={<DetalheObraPage />} />
        )}
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  Object.defineProperty(URL, 'createObjectURL', {
    configurable: true,
    value: vi.fn(() => 'blob:foto'),
  });
  Object.defineProperty(URL, 'revokeObjectURL', {
    configurable: true,
    value: vi.fn(),
  });
});

afterEach(cleanup);

describe('L50 — contrato e responsividade', () => {
  it('contratoPainelBateComFrontend', () => {
    for (const painel of [painelComDados, painelComNulos]) {
      verificarContratoBruto(painel);
      verificarContrato(painel);
    }

    const adaptado = adaptarPainel(painelComDados);
    const ocorrencia = adaptarOcorrenciasPainel(adaptado.ocorrencias)[0];
    expect(ocorrencia).toMatchObject({
      autorUsuarioId: 10,
      autorNome: 'Usuária de teste',
      autorAlocacaoId: 11,
    });
  });

  it('painelRenderizaDadosReais', () => {
    const { container } = renderPagina();

    expect(screen.getByRole('heading', { level: 2, name: 'Obra de teste' })).toBeTruthy();
    expect(screen.getAllByText('Usuária de teste').length).toBeGreaterThan(0);
    expect(screen.getByText('Verificação')).toBeTruthy();
    expect(screen.getAllByText('Foto de teste').length).toBeGreaterThan(0);
    expect(container.textContent).toContain('SIMULADO');
    expect(container.textContent).toContain('Paralisação');

    expect(screen.getByText('Gerenciar equipe')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Editar Parâmetros' })).toBeTruthy();
    expect(screen.getByRole('link', { name: 'Nova Ocorrência' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Paralisar obra' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Concluir obra' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Cancelar obra' })).toBeTruthy();
  });

  it('painelRenderizaNulos', () => {
    const { container } = renderPagina({ painel: painelComNulos, permissoes: new Set() });

    expect(screen.getAllByText('Não disponível').length).toBeGreaterThan(0);
    expect(screen.queryByText('0%')).toBeNull();
    expect(screen.getByText(/Sem diário registrado para hoje/)).toBeTruthy();
    expect(screen.getByText('Nenhum profissional alocado nesta obra.')).toBeTruthy();
    expect(screen.getByText('Sem ocorrências registradas neste diário.')).toBeTruthy();
    expect(screen.getByText('Sem fotos registradas neste diário.')).toBeTruthy();
    expect(container.textContent).toContain('SIMULADO');
  });

  it('painelRespeitaPermissao', () => {
    const casos = [
      { rotulo: 'sem permissão', permissoes: new Set(), esperados: [] },
      {
        rotulo: 'somente VISUALIZAR',
        permissoes: new Set(['DETALHES_OBRA:VISUALIZAR']),
        esperados: ['Gerenciar equipe'],
      },
      {
        rotulo: 'CRIAR sem EDITAR',
        permissoes: new Set(['DETALHES_OBRA:VISUALIZAR', 'DETALHES_OBRA:CRIAR']),
        esperados: ['Gerenciar equipe', 'Nova Ocorrência'],
      },
    ];
    const proibidos = ['Editar Parâmetros', 'Paralisar obra', 'Concluir obra', 'Cancelar obra'];

    for (const caso of casos) {
      cleanup();
      renderPagina({ permissoes: caso.permissoes });

      for (const texto of caso.esperados) {
        expect(screen.getByText(texto), `${caso.rotulo}: ${texto}`).toBeTruthy();
      }
      for (const texto of proibidos) {
        expect(screen.queryByText(texto), `${caso.rotulo}: ${texto}`).toBeNull();
      }
      if (!caso.esperados.includes('Nova Ocorrência')) {
        expect(screen.queryByText('Nova Ocorrência'), caso.rotulo).toBeNull();
      }
      if (!caso.esperados.includes('Gerenciar equipe')) {
        expect(screen.queryByText('Gerenciar equipe'), caso.rotulo).toBeNull();
      }
    }
  });

  it('painelPlanejadaComEditarOfereceIniciarObra', () => {
    renderPagina({ painel: painelComNulos, permissoes: TODAS_PERMISSOES });

    expect(screen.getByText('Iniciar obra')).toBeTruthy();
    expect(screen.queryByText('Paralisar obra')).toBeNull();
  });

  it('painelPlanejadaSemEditarNaoOfereceIniciarObra', () => {
    renderPagina({
      painel: painelComNulos,
      permissoes: new Set(['DETALHES_OBRA:VISUALIZAR']),
    });

    expect(screen.queryByText('Iniciar obra')).toBeNull();
    expect(screen.queryByText('Paralisar obra')).toBeNull();
  });

  it('contrato de classes layoutMobileSemOverflow', () => {
    const { container } = renderPagina({ comShell: true });

    const aside = container.querySelector('aside');
    const nav = container.querySelector('nav[aria-label="Navegação principal"]');
    const main = container.querySelector('main');

    expect(aside.className).toContain('hidden');
    expect(aside.className).toContain('md:flex');
    expect(nav.className).toContain('fixed');
    expect(nav.className).toContain('md:hidden');
    expect(main.className).toContain('pb-20');
    expect(main.className).toContain('md:pb-6');
    expect(container.innerHTML).toContain('md:grid-cols-3');
    expect(container.innerHTML).toContain('xl:grid-cols-[1fr_400px]');
  });

  it('contrato de classes layoutTabletSemSidebarFixa', () => {
    const { container } = renderPagina({ comShell: true });
    const aside = container.querySelector('aside');

    expect(aside.className).toContain('w-60');
    expect(aside.className).toContain('shrink-0');
    expect(aside.className).toContain('md:flex');
    expect(aside.className).not.toContain('fixed');
    expect(container.innerHTML).toContain('xl:grid-cols-[1fr_400px]');
  });

  it('erro500PermiteRepetir', async () => {
    const recarregar = vi.fn();
    renderPagina({
      painel: null,
      hook: {
        painel: null,
        erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
        recarregar,
      },
    });

    await userEvent.click(screen.getByRole('button', { name: 'Tentar novamente' }));
    expect(recarregar).toHaveBeenCalledTimes(1);
  });

  it('erro403NaoPermiteRepetir', () => {
    renderPagina({
      painel: null,
      hook: { painel: null, erro: { response: { status: 403 } }, recarregar: vi.fn() },
    });

    expect(screen.getByText('Você não tem permissão para visualizar esta obra.')).toBeTruthy();
    expect(screen.queryByRole('button', { name: 'Tentar novamente' })).toBeNull();
  });

  it('erro404NaoPermiteRepetir', () => {
    renderPagina({
      painel: null,
      hook: { painel: null, erro: { response: { status: 404 } }, recarregar: vi.fn() },
    });

    expect(screen.getByText('Obra não encontrada.')).toBeTruthy();
    expect(screen.queryByRole('button', { name: 'Tentar novamente' })).toBeNull();
  });

  it('mostraSpinnerDuranteCarregamento', () => {
    renderPagina({ painel: null, hook: { painel: null, carregando: true } });
    expect(screen.getByRole('status', { name: 'Carregando' })).toBeTruthy();
  });
});
