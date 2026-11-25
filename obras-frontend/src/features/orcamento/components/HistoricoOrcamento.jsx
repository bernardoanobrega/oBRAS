import { Pencil } from 'lucide-react';
import Button from '../../../shared/components/ui/Button.jsx';
import { formatarMoedaComSinal } from '../../../shared/utils/formatadores.js';
import { formatarDataLocal } from '../utils/orcamento.js';

// Como no protótipo: decisão mais recente em verde, anteriores em cinza e a elaboração (base) em âmbar.
const MARCADOR = 'absolute -left-[9px] top-0.5 h-4 w-4 rounded';

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
    <section className="flex flex-col rounded-xl border border-borda bg-superficie shadow-sm">
      <header className="flex flex-wrap items-start justify-between gap-2 p-4">
        <div>
          <h2 className="font-semibold text-texto">Histórico do orçamento</h2>
          <p className="text-xs text-texto-suave">
            {decisoes.length} decisões · {quantidadeAlertas} alertas
          </p>
        </div>
        {quantidadeAlertas > 0 && (
          <span className="rounded-full bg-perigo-forte px-2 py-0.5 text-xs font-medium uppercase text-texto">
            {quantidadeAlertas} {quantidadeAlertas === 1 ? 'alerta' : 'alertas'}
          </span>
        )}
      </header>

      <ol className="flex-1 space-y-5 px-4 pb-4">
        {decisoes.map((decisao, indice) => (
          <li key={decisao.id} className="relative border-l-2 border-borda pl-5">
            <span
              className={`${MARCADOR} ${indice === 0 ? 'bg-sucesso-marcador' : 'bg-neutro'}`}
              aria-hidden="true"
            />
            <div className="flex items-start justify-between gap-2">
              <div className="flex min-w-0 flex-1 flex-wrap items-baseline justify-between gap-x-3">
                <p className="text-sm font-medium text-texto">{decisao.descricao}</p>
                <p className="text-sm font-semibold text-texto">
                  {formatarMoedaComSinal(decisao.valorImpacto)}
                </p>
              </div>
              {podeRegistrar && (
                <button
                  type="button"
                  aria-label={`Editar decisão ${decisao.descricao}`}
                  onClick={() => onEditarDecisao(decisao)}
                  className="-mt-2 flex h-11 w-11 shrink-0 items-center justify-center rounded-lg text-texto-suave hover:bg-superficie-alta md:-mt-1 md:h-8 md:w-8"
                >
                  <Pencil size={15} />
                </button>
              )}
            </div>
            <p className="mt-0.5 text-xs text-texto-suave">{decisao.justificativa}</p>
            <p className="mt-0.5 text-xs text-texto-fraco">
              {formatarDataLocal(decisao.dataDecisao)}
            </p>
          </li>
        ))}
        <li className="relative border-l-2 border-borda pl-5">
          <span className={`${MARCADOR} bg-brand`} aria-hidden="true" />
          <p className="text-sm font-medium text-texto">Orçamento elaborado</p>
          <p className="mt-0.5 text-xs text-texto-fraco">{formatarDataLocal(dataElaboracao)}</p>
        </li>
      </ol>

      {podeRegistrar && (
        <footer className="border-t border-borda p-4">
          <Button variante="secundario" className="w-full" onClick={onRegistrarDecisao}>
            Registrar decisão
          </Button>
        </footer>
      )}
    </section>
  );
}
