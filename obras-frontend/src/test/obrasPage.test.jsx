import { describe, expect, it } from 'vitest';
import { filtrarObras } from '../features/obras/pages/ObrasPage.jsx';
import { paraHeader } from '../features/obras/api/adaptadores.js';

const linha = (id, nome, statusId) => ({ ...paraHeader({ id, nome, status: statusId }), id, statusId });

const linhas = [
  linha(1, 'Edifício Horizon', 'EM_ANDAMENTO'),
  linha(2, 'Galpão Solar', 'PLANEJADA'),
  linha(3, 'Ponte Rio', 'CONCLUIDA'),
];

describe('filtrarObras', () => {
  it('sem filtro retorna todas', () => {
    expect(filtrarObras(linhas, '', 'TODOS').map((l) => l.id)).toEqual([1, 2, 3]);
  });

  it('filtra por nome', () => {
    expect(filtrarObras(linhas, 'horizon', 'TODOS').map((l) => l.id)).toEqual([1]);
  });

  it('ignora acento na busca', () => {
    expect(filtrarObras(linhas, 'galpao', 'TODOS').map((l) => l.id)).toEqual([2]);
  });

  it('filtra por status', () => {
    expect(filtrarObras(linhas, '', 'PLANEJADA').map((l) => l.id)).toEqual([2]);
  });

  it('combina nome e status', () => {
    expect(filtrarObras(linhas, 'ponte', 'CONCLUIDA').map((l) => l.id)).toEqual([3]);
    expect(filtrarObras(linhas, 'ponte', 'PLANEJADA')).toEqual([]);
  });
});
