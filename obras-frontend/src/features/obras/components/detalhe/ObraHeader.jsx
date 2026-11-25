import { SlidersHorizontal, Plus } from 'lucide-react';
import { Badge } from './primitivos.jsx';
import { BOTAO_PRIMARIO, BOTAO_SECUNDARIO } from './tokens.js';

/** Cabeçalho do Painel Detalhes de Obra. Ações visuais, condicionadas por permissão; nada é persistido nesta fatia. */
export default function ObraHeader({ obra, pode }) {
  const campos = [
    ['ART:', obra.art],
    ['Tipologia:', obra.tipologia],
    ['Localização:', obra.localizacao],
    ['Previsão Contratual:', obra.previsao],
  ];

  return (
    <section className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
      <div>
        <div className="flex flex-wrap items-center gap-3">
          <h2 className="text-xl font-semibold text-texto">{obra.nome}</h2>
          <Badge tom={obra.statusTom ?? 'green'} dot>
            {obra.status}
          </Badge>
        </div>
        <dl className="mt-2 flex max-w-xl flex-wrap gap-x-4 gap-y-1 text-xs text-texto-fraco">
          {campos.map(([k, v]) => (
            <div key={k} className="flex gap-1.5">
              <dt>{k}</dt>
              <dd className="font-medium text-texto">{v}</dd>
            </div>
          ))}
        </dl>
      </div>

      <div className="flex flex-wrap gap-2">
        {pode('DETALHES_OBRA', 'EDITAR') && (
          <button type="button" className={BOTAO_SECUNDARIO}>
            <SlidersHorizontal size={13} /> Editar Parâmetros
          </button>
        )}
        {pode('DETALHES_OBRA', 'CRIAR') && (
          <button type="button" className={BOTAO_PRIMARIO}>
            <Plus size={13} /> Nova Ocorrência
          </button>
        )}
      </div>
    </section>
  );
}
