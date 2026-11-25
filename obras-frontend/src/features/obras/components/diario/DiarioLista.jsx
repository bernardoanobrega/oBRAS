import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import { paraResumoDiario } from '../../api/adaptadoresDiario.js';
import { Badge } from '../detalhe/primitivos.jsx';
import { CARD } from '../detalhe/tokens.js';

export default function DiarioLista({ resumos = [], selecionadoId, onSelecionar }) {
  const itens = resumos.map(paraResumoDiario).filter(Boolean);

  return (
    <section className={`${CARD} p-4`}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-sm font-semibold text-texto">Diários registrados</h2>
          <p className="text-xs text-texto-fraco">Selecione um registro</p>
        </div>
        <Badge tom="neutral">{itens.length}</Badge>
      </header>

      {itens.length === 0 ? (
        <div className="mt-4">
          <EmptyState mensagem="Nenhum diário registrado para esta obra." />
        </div>
      ) : (
        <div className="mt-4 flex gap-2 overflow-x-auto pb-2 md:grid md:grid-cols-3 lg:grid-cols-4">
          {itens.map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => onSelecionar?.(item.id)}
              className={`min-h-[44px] shrink-0 rounded-lg border px-3 py-2 text-left ${
                selecionadoId === item.id
                  ? 'border-brand bg-brand-fundo'
                  : 'border-borda bg-superficie hover:bg-superficie-alta'
              }`}
            >
              <span className="block text-sm font-semibold text-texto">{item.data}</span>
              <span className="mt-1 block text-xs text-texto-suave">{item.clima}</span>
              <span className="mt-2 block">
                <Badge tom={item.tom}>{item.totalOcorrencias} ocorrência(s)</Badge>
              </span>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}
