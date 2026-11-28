import Can from '../../../../shared/auth/Can.jsx';
import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import Button from '../../../../shared/components/ui/Button.jsx';
import { paraResumoDiario } from '../../api/adaptadoresDiario.js';
import { Badge } from '../detalhe/primitivos.jsx';
import { CARD } from '../detalhe/tokens.js';

export default function DiarioLista({ obraId, resumos = [], selecionadoId, onSelecionar, onEditar }) {
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
          {itens.map((item) => {
            const resumo = resumos.find((valor) => valor.id === item.id);
            return (
              <div key={item.id} className="flex min-w-[180px] shrink-0 flex-col gap-2 md:min-w-0">
                <button
                  type="button"
                  onClick={() => onSelecionar?.(item.id)}
                  className={`min-h-[44px] w-full rounded-lg border px-3 py-2 text-left ${
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
                <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={obraId}>
                  <Button type="button" variante="secundario" className="w-full" onClick={() => onEditar?.(resumo)}>
                    Editar diário
                  </Button>
                </Can>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}
