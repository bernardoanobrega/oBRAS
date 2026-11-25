import { AlertTriangle, BarChart3, CheckCircle2 } from 'lucide-react';
import Badge from '../../../shared/components/ui/Badge.jsx';
import {
  formatarMoeda,
  formatarMoedaComSinal,
  formatarPercentual,
  formatarPercentualComSinal,
} from '../../../shared/utils/formatadores.js';
import SituacaoBadge from './SituacaoBadge.jsx';

function Indicador({ icone: Icone, titulo, valor, detalhe, selo }) {
  return (
    <div className="flex flex-col gap-2 p-4 md:p-5">
      <div className="flex items-start justify-between gap-2">
        <span className="rounded-lg bg-brand-50 p-2 text-brand-700">
          <Icone size={18} aria-hidden="true" />
        </span>
        {selo}
      </div>
      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{titulo}</p>
      <p className="text-2xl font-semibold text-slate-900 md:text-3xl">{valor}</p>
      <p className="text-sm text-slate-600">{detalhe}</p>
    </div>
  );
}

/** Três cartões do topo do protótipo: orçado, realizado e desvio (RF-33, RF-34). */
export default function IndicadoresOrcamento({ orcamento }) {
  const { indicadores, itens, percentualLimiteAlerta, status } = orcamento;
  const estaAcimaDoOrcado = indicadores.desvioAbsoluto > 0;

  return (
    <section
      aria-label="Indicadores da saúde financeira"
      className="grid divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white shadow-sm md:grid-cols-3 md:divide-x md:divide-y-0"
    >
      <Indicador
        icone={BarChart3}
        titulo="Orçamento aprovado"
        valor={formatarMoeda(indicadores.totalOrcado)}
        detalhe={`${itens.length} itens · alerta em ${formatarPercentual(percentualLimiteAlerta)}`}
        selo={<SituacaoBadge codigo={status} />}
      />
      <Indicador
        icone={CheckCircle2}
        titulo="Realizado"
        valor={formatarMoeda(indicadores.totalRealizado)}
        detalhe={`${formatarPercentual(indicadores.percentualExecutado)} do total orçado`}
        selo={<SituacaoBadge codigo={indicadores.saude} />}
      />
      <Indicador
        icone={AlertTriangle}
        titulo="Desvio (realizado − orçado)"
        valor={formatarMoedaComSinal(indicadores.desvioAbsoluto)}
        detalhe={`${formatarPercentualComSinal(indicadores.desvioPercentual)} sobre o orçado · ${
          indicadores.quantidadeItensEmEstouro
        } itens em estouro`}
        selo={
          <Badge tom={estaAcimaDoOrcado ? 'vermelho' : 'verde'}>
            {estaAcimaDoOrcado ? 'Acima do orçado' : 'Dentro do orçado'}
          </Badge>
        }
      />
    </section>
  );
}
