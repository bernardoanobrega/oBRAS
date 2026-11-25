import { Pencil } from 'lucide-react';
import Badge from '../../../shared/components/ui/Badge.jsx';
import Button from '../../../shared/components/ui/Button.jsx';
import { formatarMoedaComSinal } from '../../../shared/utils/formatadores.js';
import { formatarDataLocal } from '../utils/orcamento.js';

/** Histórico de decisões financeiras (RF-74), da mais recente para a elaboração do orçamento. */
export default function HistoricoOrcamento({
  orcamento,
  podeRegistrar,
  onRegistrarDecisao,
  onEditarDecisao,
}) {
  const { decisoes, indicadores, dataElaboracao } = orcamento;
  const quantidadeAlertas = indicadores.quantidadeItensEmEstouro;

  return (
    <section className="flex flex-col rounded-xl border border-slate-200 bg-white shadow-sm">
      <header className="flex flex-wrap items-start justify-between gap-2 p-4">
        <div>
          <h2 className="font-semibold text-slate-800">Histórico do orçamento</h2>
          <p className="text-xs text-slate-500">
            {decisoes.length} decisões · {quantidadeAlertas} alertas
          </p>
        </div>
        {quantidadeAlertas > 0 && (
          <Badge tom="vermelho">
            {quantidadeAlertas} {quantidadeAlertas === 1 ? 'alerta' : 'alertas'}
          </Badge>
        )}
      </header>

      <ol className="flex-1 space-y-5 px-4 pb-4">
        {decisoes.map((decisao) => (
          <li key={decisao.id} className="relative border-l-2 border-slate-200 pl-5">
            <span
              className={`absolute -left-[7px] top-1 h-3 w-3 rounded-full ${
                decisao.valorImpacto >= 0 ? 'bg-brand-500' : 'bg-slate-500'
              }`}
              aria-hidden="true"
            />
            <div className="flex items-start justify-between gap-2">
              <div className="flex min-w-0 flex-1 flex-wrap items-baseline justify-between gap-x-3">
                <p className="text-sm font-medium text-slate-800">{decisao.descricao}</p>
                <p className="text-sm font-semibold text-slate-700">
                  {formatarMoedaComSinal(decisao.valorImpacto)}
                </p>
              </div>
              {podeRegistrar && (
                <button
                  type="button"
                  aria-label={`Editar decisão ${decisao.descricao}`}
                  onClick={() => onEditarDecisao(decisao)}
                  className="-mt-2 flex h-11 w-11 shrink-0 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 md:-mt-1 md:h-8 md:w-8"
                >
                  <Pencil size={15} />
                </button>
              )}
            </div>
            <p className="mt-0.5 text-xs text-slate-500">{decisao.justificativa}</p>
            <p className="mt-0.5 text-xs text-slate-400">
              {formatarDataLocal(decisao.dataDecisao)}
            </p>
          </li>
        ))}
        <li className="relative border-l-2 border-slate-200 pl-5">
          <span
            className="absolute -left-[7px] top-1 h-3 w-3 rounded-full bg-slate-300"
            aria-hidden="true"
          />
          <p className="text-sm font-medium text-slate-800">Orçamento elaborado</p>
          <p className="mt-0.5 text-xs text-slate-400">{formatarDataLocal(dataElaboracao)}</p>
        </li>
      </ol>

      {podeRegistrar && (
        <footer className="border-t border-slate-100 p-4">
          <Button variante="secundario" className="w-full" onClick={onRegistrarDecisao}>
            Registrar decisão
          </Button>
        </footer>
      )}
    </section>
  );
}
