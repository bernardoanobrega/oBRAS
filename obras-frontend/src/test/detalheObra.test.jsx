import { describe, expect, it, vi, beforeEach } from 'vitest';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

vi.mock('../shared/auth/usePermissao.js', () => ({
  usePermissao: vi.fn(() => ({ pode: () => false, carregando: false })),
}));

vi.mock('../shared/auth/useAuth.js', () => ({
  useAuth: () => ({ usuario: { nome: 'Teste' }, sair: () => {} }),
}));

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

vi.mock('../features/obras/api/useDiario.js', () => ({
  useDiario: vi.fn(),
}));

vi.mock('../features/obras/hooks/usePainelObra.js', () => ({
  usePainelObra: vi.fn(),
}));

import { usePermissao } from '../shared/auth/usePermissao.js';
import { useDetalheObra } from '../features/obras/api/useDetalheObra.js';
import { useDiario } from '../features/obras/api/useDiario.js';
import { usePainelObra } from '../features/obras/hooks/usePainelObra.js';
import { adaptarPainel } from '../features/obras/adapters/adaptarPainel.js';
import AppShell from '../shared/components/layout/AppShell.jsx';
import ObraLayout from '../features/obras/pages/ObraLayout.jsx';
import DetalheObraPage from '../features/obras/pages/DetalheObraPage.jsx';
import ObraHeader from '../features/obras/components/detalhe/ObraHeader.jsx';
import ClimaDiaCard from '../features/obras/components/detalhe/ClimaDiaCard.jsx';
import KpiCard, { KpiGrid } from '../features/obras/components/detalhe/KpiCard.jsx';
import EquipeResumo from '../features/obras/components/detalhe/EquipeResumo.jsx';
import OcorrenciasRecentes from '../features/obras/components/detalhe/OcorrenciasRecentes.jsx';
import MuralFotos from '../features/obras/components/detalhe/MuralFotos.jsx';
import { OBRA, CLIMA_DIA, KPIS, EQUIPE, OCORRENCIAS, FOTOS } from '../features/obras/mocks/detalheObraMock.js';
import { painelComDados } from '../features/obras/fixtures/painel.fixture.js';

const DIARIO_REAL = {
  id: 3,
  obraId: 1,
  data: '2026-11-18',
  clima: 'ENSOLARADO',
  descricao: 'Diário real da obra',
  equipePresenteIds: [7],
  ocorrencias: [
    {
      id: 4,
      diarioId: 3,
      categoria: 'LOGISTICA',
      descricao: 'Ocorrência real da obra',
      dataHora: '2026-11-18T10:30:00',
      autorUsuarioId: 1,
      paralisacao: null,
      fotos: [{ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: 'Foto real da obra', dataEnvio: null }],
    },
  ],
};

function painelPadrao() {
  return adaptarPainel({
    obra: {
      ...OBRA,
      status: 'EM_ANDAMENTO',
      endereco: OBRA.localizacao,
      inicioPrevisto: '2026-02-01',
      fimPrevisto: '2027-11-30',
    },
    clima: { data: '2026-09-22', condicao: 'ENSOLARADO' },
    kpis: KPIS,
    equipe: EQUIPE,
    maoDeObra: { alocados: EQUIPE.length, presentesHoje: 38, vsOntem: 3, fonte: 'DIARIO' },
    ocorrencias: OCORRENCIAS,
    fotos: FOTOS.map((foto) => ({ ...foto, ocorrenciaId: 1 })),
  });
}

function painelComDiarioReal() {
  return adaptarPainel({
    ...painelComDados,
    clima: { data: '2026-11-18', condicao: 'ENSOLARADO' },
    ocorrencias: [{ ...painelComDados.ocorrencias[0], descricao: 'Ocorrência real da obra' }],
    fotos: [{ ...painelComDados.fotos[0], legenda: 'Foto real da obra' }],
  });
}

function renderPagina() {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={['/obras/1/resumo']}>
      <Routes>
        <Route path="/obras/:obraId/resumo" element={<DetalheObraPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.stubGlobal('navigator', { onLine: true });
  usePermissao.mockReturnValue({ pode: () => false, carregando: false });
  useDetalheObra.mockReturnValue({
    obra: OBRA,
    equipe: EQUIPE,
    carregando: false,
    erro: null,
    recarregar: vi.fn(),
  });
  useDiario.mockReturnValue({
    resumos: [],
    diario: null,
    selecionadoId: null,
    carregando: false,
    erro: null,
    recarregar: vi.fn(),
    selecionarDiario: vi.fn(),
  });
  usePainelObra.mockReturnValue({
    painel: painelPadrao(),
    carregando: false,
    erro: null,
    recarregar: vi.fn(),
  });
});

function renderShell() {
  return renderToStaticMarkup(
    <MemoryRouter initialEntries={['/obras/1/resumo']}>
      <Routes>
        <Route element={<AppShell />}>
          <Route path="/obras/:obraId" element={<ObraLayout />}>
            <Route path="resumo" element={<DetalheObraPage />} />
          </Route>
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('DetalheObraPage', () => {
  it('detalheObraRenderizaSemErro', () => {
    const html = renderPagina();
    expect(html).toContain('Detalhe de Obra');
    expect(html).toContain(OBRA.nome);
  });

  it('carregaPainelERenderizaObra', () => {
    const html = renderPagina();
    expect(html).toContain(OBRA.nome);
    expect(html).toContain(EQUIPE[0].nome);
    expect(html).toContain('SIMULADO');
  });

  it('loadingInicial', () => {
    useDetalheObra.mockReturnValue({ obra: null, equipe: [], carregando: true, erro: null, recarregar: vi.fn() });
    usePainelObra.mockReturnValue({ painel: null, carregando: true, erro: null, recarregar: vi.fn() });
    const html = renderPagina();
    expect(html).toContain('aria-label="Carregando"');
    expect(html).not.toContain(OBRA.nome);
  });

  it('erroMostraErrorState', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: [],
      carregando: false,
      erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
      recarregar: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: null,
      carregando: false,
      erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Erro interno. Tente novamente.');
    expect(html).toContain('Tentar novamente');
  });

  it('linkEquipeRespeitaPermissao', () => {
    usePermissao.mockReturnValue({ pode: () => false, carregando: false });
    expect(renderPagina()).not.toContain('Gerenciar equipe');

    usePermissao.mockReturnValue({ pode: () => true, carregando: false });
    expect(renderPagina()).toContain('Gerenciar equipe');
  });

  it('painelRespeitaPermissao', () => {
    usePermissao.mockReturnValue({ pode: () => false, carregando: false });
    const sem = renderPagina();
    expect(sem).not.toContain('Editar Parâmetros');
    expect(sem).not.toContain('Nova Ocorrência');

    usePermissao.mockReturnValue({ pode: () => true, carregando: false });
    const com = renderPagina();
    expect(com).toContain('Editar Parâmetros');
    expect(com).toContain('Nova Ocorrência');
  });

  it('layoutMobileSemOverflow', () => {
    const html = renderPagina();
    expect(html).not.toMatch(/min-w-\[/);
    expect(html).not.toMatch(/w-\[\d+px\]/);
  });

  it('layoutTabletSemSidebarFixa', () => {
    const html = renderPagina();
    expect(html).toContain('md:grid-cols-3');
    expect(html).toContain('xl:grid-cols-[1fr_400px]');
    expect(html).not.toContain('<aside');
  });

  it('naoRenderizaSidebarPropria', () => {
    const html = renderPagina();
    expect(html).not.toContain('MENU PRINCIPAL');
    expect(html).not.toContain('Configurações');
    expect(html).not.toContain('Sair');
  });

  it('naoRenderizaCortesDeEscopo', () => {
    const html = renderPagina();
    for (const cortado of [
      'Exportar Relatório',
      'TURNO DA MANHÃ',
      'TURNO DA TARDE',
      'Temperatura',
      'Sincronização',
      'Última sincronização',
      'Check-in',
      'CREA',
      'Todas as frentes',
      'Gestão do canteiro',
      'Estrutura · 4º Pav.',
      'Anexar Registro Fotográfico',
      'Próprios',
      'Terceirizados',
    ]) {
      expect(html).not.toContain(cortado);
    }
  });

  it('layoutResponsivoSemSidebarFixa', () => {
    const html = renderPagina();
    expect(html).toContain('md:grid-cols-3');
    expect(html).toContain('xl:grid-cols-[1fr_400px]');
    expect(html).not.toContain('<aside');
  });

  it('rotaDetalheRenderizaDentroDoAppShell', () => {
    usePermissao.mockReturnValue({ pode: () => true, carregando: false });
    const html = renderShell();
    expect(html).toContain(OBRA.nome);
    expect(html).toContain('Obra #1');
    expect(html).not.toContain('MENU PRINCIPAL');
  });

  it('usaUsePermissaoComObraId', () => {
    renderPagina();
    expect(usePermissao).toHaveBeenCalledWith('1');
  });

  it('tabsDoObraLayoutFuncionam', () => {
    usePermissao.mockImplementation(() => ({
      pode: (modulo, acao) => modulo === 'DETALHES_OBRA' && acao === 'VISUALIZAR',
      carregando: false,
    }));
    const html = renderShell();
    // O menu lateral também tem Cronograma e Orçamento; a regra vale só para as abas da obra.
    const abas = html.match(/<nav[^>]*aria-label="Abas da obra"[^>]*>.*?<\/nav>/s)[0];
    expect(html).toContain('Obra #1');
    expect(abas).toContain('>Resumo<');
    expect(abas).toContain('>Equipe<');
    expect(abas).not.toContain('>Cronograma<');
    expect(abas).not.toContain('>Orçamento<');
  });

  it('acaoSemPermissaoNaoAparece', () => {
    const html = renderPagina();
    expect(html).not.toContain('Editar Parâmetros');
    expect(html).not.toContain('Nova Ocorrência');
  });

  it('acaoComPermissaoAparece', () => {
    usePermissao.mockReturnValue({ pode: () => true, carregando: false });
    const html = renderPagina();
    expect(html).toContain('Editar Parâmetros');
    expect(html).toContain('Nova Ocorrência');
  });

  it('kpisExibemBadgeSimulado', () => {
    usePermissao.mockReturnValue({ pode: () => false, carregando: false });
    const html = renderPagina();
    expect(html).toContain('SIMULADO');
  });

  it('usa dados reais do diário no painel', () => {
    useDiario.mockReturnValue({
      resumos: [{ id: 3, data: '2026-11-18' }],
      diario: DIARIO_REAL,
      selecionadoId: 3,
      carregando: false,
      erro: null,
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: painelComDiarioReal(),
      carregando: false,
      erro: null,
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('18/11/2026');
    expect(html).toContain('Ensolarado');
    expect(html).toContain('Ocorrência real da obra');
    expect(html).toContain('Foto real da obra');
  });

  it('mantém obra e equipe quando diário falha', () => {
    useDiario.mockReturnValue({
      resumos: [],
      diario: null,
      selecionadoId: null,
      carregando: false,
      erro: { response: { status: 500 } },
      recarregar: vi.fn(),
      selecionarDiario: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: painelPadrao(),
      carregando: false,
      erro: { response: { status: 500 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain(OBRA.nome);
    expect(html).toContain(EQUIPE[0].nome);
    expect(html).toContain('Não foi possível carregar o diário desta obra.');
    expect(html).toContain('Tentar novamente');
  });

  it('mostraCarregandoEnquantoApiResponde', () => {
    useDetalheObra.mockReturnValue({ obra: null, equipe: [], carregando: true, erro: null, recarregar: vi.fn() });
    usePainelObra.mockReturnValue({ painel: null, carregando: true, erro: null, recarregar: vi.fn() });
    const html = renderPagina();
    expect(html).toContain('aria-label="Carregando"');
    expect(html).not.toContain(OBRA.nome);
  });

  it('mostraErroDaApiComRepeticao', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: [],
      carregando: false,
      erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
      recarregar: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: null,
      carregando: false,
      erro: { response: { status: 500, data: { mensagem: 'Erro interno. Tente novamente.' } } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Erro interno. Tente novamente.');
    expect(html).toContain('Tentar novamente');
  });

  it('naoOfereceRepeticaoPara403', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: [],
      carregando: false,
      erro: { response: { status: 403 } },
      recarregar: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: null,
      carregando: false,
      erro: { response: { status: 403 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Você não tem permissão para visualizar esta obra.');
    expect(html).not.toContain('Tentar novamente');
  });

  it('mostraMensagemDeObraInexistente', () => {
    useDetalheObra.mockReturnValue({
      obra: null,
      equipe: [],
      carregando: false,
      erro: { response: { status: 404 } },
      recarregar: vi.fn(),
    });
    usePainelObra.mockReturnValue({
      painel: null,
      carregando: false,
      erro: { response: { status: 404 } },
      recarregar: vi.fn(),
    });
    const html = renderPagina();
    expect(html).toContain('Obra não encontrada.');
    expect(html).not.toContain('Tentar novamente');
  });
});

describe('componentes extraídos', () => {
  it('ObraHeader renderiza dados e ações conforme permissão', () => {
    const semAcao = renderToStaticMarkup(<ObraHeader obra={OBRA} pode={() => false} />);
    expect(semAcao).toContain(OBRA.nome);
    expect(semAcao).toContain(OBRA.art);
    expect(semAcao).not.toContain('Editar Parâmetros');

    const comAcao = renderToStaticMarkup(<ObraHeader obra={OBRA} pode={() => true} />);
    expect(comAcao).toContain('Editar Parâmetros');
    expect(comAcao).toContain('Nova Ocorrência');
  });

  it('ClimaDiaCard exibe clima único sem turno/temperatura', () => {
    const html = renderToStaticMarkup(<ClimaDiaCard clima={CLIMA_DIA} />);
    expect(html).toContain(CLIMA_DIA.condicao);
    expect(html).toContain(CLIMA_DIA.data);
    expect(html).not.toContain('TURNO');
    expect(html).not.toContain('°C');
    expect(html).not.toContain('SIMULADO');
  });

  it('KpiCard e KpiGrid exibem valores e badge SIMULADO', () => {
    const card = renderToStaticMarkup(
      <KpiCard icon={() => null} titulo="TESTE KPI">
        conteúdo
      </KpiCard>,
    );
    expect(card).toContain('TESTE KPI');
    expect(card).toContain('SIMULADO');

    const grid = renderToStaticMarkup(<KpiGrid kpis={KPIS} />);
    expect(grid).toContain(`${KPIS.avancoRealizado}%`);
    expect(grid).toContain(`${KPIS.maoDeObraTotal}`);
    expect(grid).toContain(`${KPIS.vsOntem}`);
  });

  it('EquipeResumo lista profissionais sem frente/CREA', () => {
    const html = renderToStaticMarkup(<EquipeResumo equipe={EQUIPE} />);
    expect(html).toContain(EQUIPE[0].nome);
    expect(html).toContain(EQUIPE[0].cargo);
    expect(html).not.toContain('CREA');
    expect(html).not.toContain('FRENTE DE TRABALHO');
  });

  it('OcorrenciasRecentes lista ocorrências sem link morto', () => {
    const html = renderToStaticMarkup(<OcorrenciasRecentes ocorrencias={OCORRENCIAS} />);
    expect(html).toContain(OCORRENCIAS[0].texto);
    expect(html).not.toContain('Ver todos');
    expect(html).not.toContain('href="#"');
    expect(html).not.toContain('SIMULADO');
  });

  it('MuralFotos usa fotos do diário sem upload', () => {
    const html = renderToStaticMarkup(<MuralFotos obraId="1" fotos={FOTOS} />);
    expect(html).toContain(FOTOS[0].legenda);
    expect(html).not.toContain('/img/');
    expect(html).not.toContain('Anexar Registro Fotográfico');
    expect(html).not.toContain('type="file"');
    expect(html).not.toContain('SIMULADO');
  });
});

describe('contrato de arquivos', () => {
  it('nenhumImportAbsolutoForaDoSrc', () => {
    const raiz = fileURLToPath(new URL('../features/obras', import.meta.url));
    const arquivos = [];
    const varrer = (dir) => {
      for (const nome of readdirSync(dir)) {
        const p = join(dir, nome);
        if (statSync(p).isDirectory()) varrer(p);
        else if (/\.(jsx?|mjs)$/.test(nome)) arquivos.push(p);
      }
    };
    varrer(raiz);

    for (const arquivo of arquivos) {
      const conteudo = readFileSync(arquivo, 'utf8');
      for (const [, spec] of conteudo.matchAll(/from\s+['"]([^'"]+)['"]/g)) {
        expect(spec.startsWith('/'), `${arquivo} importa ${spec}`).toBe(false);
        expect(/^(\.\.\/){5,}/.test(spec), `${arquivo} escapa de src: ${spec}`).toBe(false);
      }
    }
  });
});
