import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import EquipeResumo from '../features/obras/components/detalhe/EquipeResumo.jsx';

describe('EquipeResumo com dados da API', () => {
  const membros = [
    { usuarioId: 5, sigla: 'CM', nome: 'Carlos Mendes', cargo: 'Mestre de Obras', tom: 'neutral', dataInicio: '2026-01-10' },
  ];

  it('lista membro alocado', () => {
    const html = renderToStaticMarkup(<EquipeResumo equipe={membros} />);
    expect(html).toContain('Carlos Mendes');
    expect(html).toContain('Mestre de Obras');
    expect(html).toContain('1 profissional alocado');
  });

  it('mostra estado vazio sem membros', () => {
    const html = renderToStaticMarkup(<EquipeResumo equipe={[]} />);
    expect(html).toContain('Nenhum profissional alocado nesta obra.');
    expect(html).not.toContain('<tbody');
  });

  it('trata lista ausente como vazia', () => {
    const html = renderToStaticMarkup(<EquipeResumo equipe={undefined} />);
    expect(html).toContain('Nenhum profissional alocado nesta obra.');
  });
});
