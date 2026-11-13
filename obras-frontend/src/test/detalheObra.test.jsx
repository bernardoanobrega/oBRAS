import { describe, expect, it, vi, beforeEach } from 'vitest';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

vi.mock('../shared/auth/usePermissao.js', () => ({
  usePermissao: vi.fn(() => ({ pode: () => false, carregando: false })),
}));

vi.mock('../shared/auth/useAuth.js', () => ({
  useAuth: () => ({ usuario: { nome: 'Teste' }, sair: () => {} }),
}));

import { usePermissao } from '../shared/auth/usePermissao.js';
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
    expect(html).toContain('Obra #1');
    expect(html).toContain('>Resumo<');
    expect(html).toContain('>Equipe<');
    expect(html).not.toContain('>Cronograma<');
    expect(html).not.toContain('>Orçamento<');
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
  });

  it('MuralFotos usa placeholders locais sem upload', () => {
    const html = renderToStaticMarkup(<MuralFotos fotos={FOTOS} />);
    expect(html).toContain(FOTOS[0].legenda);
    expect(html).not.toContain('/img/');
    expect(html).not.toContain('Anexar Registro Fotográfico');
    expect(html).not.toContain('type="file"');
  });
});

describe('contrato de arquivos', () => {
  it('nenhumImportAbsolutoForaDoSrc', () => {
    const raiz = new URL('../features/obras', import.meta.url).pathname;
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
