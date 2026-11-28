import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

vi.mock('../shared/auth/usePermissao.js', () => ({ usePermissao: vi.fn() }));
vi.mock('../features/obras/api/useDiario.js', () => ({ useDiario: vi.fn() }));

import { usePermissao } from '../shared/auth/usePermissao.js';
import { useDiario } from '../features/obras/api/useDiario.js';
import DiarioPage from '../features/obras/pages/DiarioPage.jsx';
import DiarioFormModal from '../features/obras/components/diario/DiarioFormModal.jsx';
import DiarioLista from '../features/obras/components/diario/DiarioLista.jsx';
import ErroFormulario from '../features/obras/components/diario/ErroFormulario.jsx';
import MuralFotos from '../features/obras/components/diario/MuralFotos.jsx';
import OcorrenciaFormModal from '../features/obras/components/diario/OcorrenciaFormModal.jsx';
import OcorrenciaList from '../features/obras/components/diario/OcorrenciaList.jsx';
import OcorrenciaRapidaModal from '../features/obras/components/diario/OcorrenciaRapidaModal.jsx';

const RESUMOS = [
  { id: 3, data: '2026-11-18', clima: 'ENSOLARADO', descricao: 'Diário real', totalOcorrencias: 1 },
];

const OCORRENCIA = {
  id: 4,
  categoria: 'LOGISTICA',
  descricao: 'Ocorrência real',
  dataHora: '2026-11-18T10:30:00',
  autor: { alocacaoId: 11, usuarioId: 7, nome: 'Ana' },
  paralisacao: { inicio: '2026-11-18T10:30:00', fim: '2026-11-18T12:00:00' },
  fotos: [{ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: 'Foto real', dataEnvio: '2026-11-18T10:31:00' }],
};

const DIARIO = {
  id: 3,
  obraId: 2,
  data: '2026-11-18',
  clima: 'ENSOLARADO',
  descricao: 'Diário real',
  equipePresente: [{ alocacaoId: 11, usuarioId: 7, nome: 'Ana' }],
  ocorrencias: [OCORRENCIA],
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

describe('tela do diário', () => {
  it('listaDiariosMostraDataClima', () => {
    const html = renderToStaticMarkup(<DiarioLista resumos={RESUMOS} selecionadoId={3} />);
    expect(html).toContain('18/11/2026');
    expect(html).toContain('Ensolarado');
  });

  it('listaVaziaMostraEmptyState', () => {
    const html = renderToStaticMarkup(<DiarioLista resumos={[]} />);
    expect(html).toContain('Nenhum diário registrado para esta obra.');
  });

  it('edicaoCarregaValores', () => {
    const html = renderToStaticMarkup(
      <DiarioFormModal aberto diario={DIARIO} obraId="2" onFechar={() => {}} onSalvar={async () => {}} />,
    );
    expect(html).toContain('Editar diário');
    expect(html).toContain('value="2026-11-18"');
    expect(html).toContain('Ensolarado');
    expect(html).toContain('Diário real');
  });

  it('semPermissaoOcultaCriarEditar', () => {
    usePermissao.mockReturnValue({ pode: () => false, carregando: false });
    const html = renderPagina();
    expect(html).not.toContain('Novo diário');
    expect(html).not.toContain('Editar diário');
    expect(html).not.toContain('Nova ocorrência');
    expect(html).toContain('Somente leitura');
  });

  it('layoutNaoUsaTabelaFixaEmMobile', () => {
    const html = renderPagina();
    expect(html).not.toContain('<table');
    expect(html).toContain('flex-col');
    expect(html).toContain('md:grid');
  });

  it('diarioMobileFirst', () => {
    const html = renderPagina();
    expect(html).not.toContain('<table');
    expect(html).toContain('grid-cols-1');
    expect(html).toContain('min-h-[44px]');
    expect(html).toContain('18/11/2026');
  });
});

describe('ocorrências na tela', () => {
  it('listaOcorrenciasMostraCategoriaEDescricao', () => {
    const html = renderToStaticMarkup(
      <OcorrenciaList obraId="2" ocorrencias={[OCORRENCIA]} onEditar={() => {}} onExcluir={() => {}} />,
    );
    expect(html).toContain('Logística');
    expect(html).toContain('Ocorrência real');
  });

  it('semPermissaoOcultaAcoes', () => {
    usePermissao.mockReturnValue({ pode: () => false, carregando: false });
    const html = renderToStaticMarkup(<OcorrenciaList obraId="2" ocorrencias={[OCORRENCIA]} />);
    expect(html).not.toContain('Anexar foto');
    expect(html).not.toContain('Editar');
    expect(html).not.toContain('Excluir');
  });

  it('edicaoCarregaOcorrencia', () => {
    const html = renderToStaticMarkup(
      <OcorrenciaFormModal aberto ocorrencia={OCORRENCIA} onFechar={() => {}} onSalvar={async () => {}} />,
    );
    expect(html).toContain('Editar ocorrência');
    expect(html).toContain('Ocorrência real');
    expect(html).toContain('value="2026-11-18T10:30"');
    expect(html).toContain('Logística');
  });

  it('erroDoBackendEhExibido', () => {
    const html = renderToStaticMarkup(<ErroFormulario erroGeral="Clima é obrigatório quando não existe diário no dia" />);
    expect(html).toContain('role="alert"');
    expect(html).toContain('Clima é obrigatório quando não existe diário no dia');
  });
});

describe('ocorrência rápida', () => {
  it('abreOcorrenciaRapidaComCategoriaOutro', () => {
    const html = renderToStaticMarkup(
      <OcorrenciaRapidaModal aberto data="2026-11-18" resumos={[]} onFechar={() => {}} onSalvar={async () => {}} />,
    );
    expect(html).toContain('Ocorrência rápida');
    expect(html).toContain('value="OUTRO"');
    expect(html).toContain('Outro');
    expect(html).not.toContain('Selecione uma categoria');
  });

  it('pedeClimaQuandoNaoHaDiario', () => {
    const html = renderToStaticMarkup(
      <OcorrenciaRapidaModal aberto data="2026-11-18" resumos={[]} onFechar={() => {}} onSalvar={async () => {}} />,
    );
    expect(html).toContain('id="rapida-clima"');
    expect(html).toContain('Clima *');
  });

  it('naoPedeClimaQuandoDiarioExiste', () => {
    const html = renderToStaticMarkup(
      <OcorrenciaRapidaModal
        aberto
        data="2026-11-18"
        resumos={RESUMOS}
        onFechar={() => {}}
        onSalvar={async () => {}}
      />,
    );
    expect(html).not.toContain('id="rapida-clima"');
    expect(html).toContain('value="OUTRO"');
  });
});

describe('mural', () => {
  it('muralMostraSomenteFotosDeOcorrencias', () => {
    const html = renderToStaticMarkup(<MuralFotos obraId="2" ocorrencias={[OCORRENCIA]} />);
    expect(html).toContain('Foto real');
    expect(html).not.toContain('type="file"');
    expect(html).not.toContain('Anexar foto');
  });
});
