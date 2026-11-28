import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import DiarioFormModal from '../features/obras/components/diario/DiarioFormModal.jsx';
import FotoUpload from '../features/obras/components/diario/FotoUpload.jsx';
import Modal from '../shared/components/ui/Modal.jsx';
import OcorrenciaFormModal from '../features/obras/components/diario/OcorrenciaFormModal.jsx';

const DIARIO = {
  id: 3,
  data: '2026-11-18',
  clima: 'ENSOLARADO',
  descricao: 'Diário real',
  equipePresente: [],
};

function temLabelAssociado(html, id) {
  const explicito = html.includes(`for="${id}"`) && html.includes(`id="${id}"`);
  const envolvido = new RegExp(`<label[^>]*>(?:(?!<\\/label>)[\\s\\S])*id="${id}"[\\s\\S]*?<\\/label>`).test(html);
  return explicito || envolvido;
}

function htmlModal() {
  return renderToStaticMarkup(
    <Modal aberto titulo="Título do modal" onFechar={() => {}}>
      <span>Conteúdo</span>
    </Modal>,
  );
}

describe('acessibilidade do detalhe de obra', () => {
  it('camposTemLabelAssociado', () => {
    const diario = renderToStaticMarkup(
      <DiarioFormModal aberto obraId="2" diario={DIARIO} onFechar={() => {}} onSalvar={async () => {}} />,
    );
    const ocorrencia = renderToStaticMarkup(
      <OcorrenciaFormModal aberto onFechar={() => {}} onSalvar={async () => {}} />,
    );
    const foto = renderToStaticMarkup(
      <FotoUpload aberto obraId="2" ocorrenciaId="4" onFechar={() => {}} onSalvo={() => {}} />,
    );

    expect(temLabelAssociado(diario, 'diario-data')).toBe(true);
    expect(temLabelAssociado(diario, 'diario-clima')).toBe(true);
    expect(temLabelAssociado(diario, 'diario-descricao')).toBe(true);
    expect(temLabelAssociado(diario, 'diario-equipe')).toBe(true);
    expect(temLabelAssociado(ocorrencia, 'ocorrencia-categoria')).toBe(true);
    expect(temLabelAssociado(ocorrencia, 'ocorrencia-descricao')).toBe(true);
    expect(temLabelAssociado(ocorrencia, 'ocorrencia-data-hora')).toBe(true);
    expect(temLabelAssociado(ocorrencia, 'ocorrencia-com-paralisacao')).toBe(true);
    expect(temLabelAssociado(foto, 'foto-arquivo')).toBe(true);
    expect(temLabelAssociado(foto, 'foto-legenda')).toBe(true);
  });

  it('botoesDeIconeTemAriaLabel', () => {
    const html = htmlModal();
    const botoes = [...html.matchAll(/<button\b([^>]*)>([\s\S]*?)<\/button>/g)];

    expect(botoes.length).toBeGreaterThan(0);
    for (const [, atributos, conteudo] of botoes) {
      if (conteudo.includes('<svg')) {
        expect(atributos).toContain('aria-label=');
      }
    }
    expect(html).toContain('aria-label="Fechar"');
    expect(html).toContain('role="dialog"');
    expect(html).toContain('aria-modal="true"');
  });
});
