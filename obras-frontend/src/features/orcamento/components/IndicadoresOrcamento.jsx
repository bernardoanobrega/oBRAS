import { AlertTriangle, BarChart3, CheckCircle2 } from 'lucide-react';
import Badge from '../../../shared/components/ui/Badge.jsx';
import {
  formatarMoeda,
  formatarMoedaComSinal,
  formatarPercentual,
  formatarPercentualComSinal,
} from '../../../shared/utils/formatadores.js';
import SituacaoBadge from './SituacaoBadge.jsx';

// Quadrado atrás do ícone, na cor da situação (como no protótipo).
const TONS_ICONE = {
  brand: 'bg-brand-fundo text-brand',
  sucesso: 'bg-sucesso-fundo text-sucesso',
  perigo: 'bg-perigo-fundo text-perigo',
};

const TOM_DA_SAUDE = { SAUDAVEL: 'sucesso', ATENCAO: 'brand', CRITICA: 'perigo' };

function Indicador({ icone: Icone, tom, titulo, valor, detalhe, selo }) {
  return (
    <div className="flex flex-col gap-3 p-5 md:p-6">
      <div className="flex flex-wrap items-center gap-3">
        <span
          className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${TONS_ICONE[tom]}`}
        >
          <Icone size={18} aria-hidden="true" />
        </span>
        <p className="text-xs font-medium uppercase tracking-wide text-texto-suave">{titulo}</p>
        <span className="ml-auto">{selo}</span>
      </div>
      <p className="text-2xl font-semibold text-texto md:text-3xl">{valor}</p>
      <p className="text-sm text-texto-suave">{detalhe}</p>
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
      className="grid divide-y divide-borda rounded-xl border border-borda bg-superficie shadow-sm md:grid-cols-3 md:divide-x md:divide-y-0"
    >
      <Indicador
        icone={BarChart3}
        tom="brand"
        titulo="Orçamento aprovado"
        valor={formatarMoeda(indicadores.totalOrcado)}
        detalhe={`${itens.length} itens · alerta em ${formatarPercentual(percentualLimiteAlerta)}`}
        selo={<SituacaoBadge codigo={status} />}
      />
      <Indicador
        icone={CheckCircle2}
        tom={TOM_DA_SAUDE[indicadores.saude] ?? 'sucesso'}
        titulo="Realizado"
        valor={formatarMoeda(indicadores.totalRealizado)}
        detalhe={`${formatarPercentual(indicadores.percentualExecutado)} do total orçado`}
        selo={<SituacaoBadge codigo={indicadores.saude} />}
      />
      <Indicador
        icone={AlertTriangle}
        tom={estaAcimaDoOrcado ? 'perigo' : 'sucesso'}
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
