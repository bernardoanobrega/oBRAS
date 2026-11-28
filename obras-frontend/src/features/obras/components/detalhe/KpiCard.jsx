import { CalendarDays, CircleDollarSign, Users } from 'lucide-react';
import { formatarMoeda } from '../../../../shared/utils/formatadores.js';
import { apresentarKpi, calcularKpi, tooltipFonte } from '../../adapters/adaptarKpi.js';
import { Badge, ProgressBar } from './primitivos.jsx';
import { CARD } from './tokens.js';

function textoPercentual(apresentacao) {
  if (!apresentacao.disponivel) return 'Não disponível';
  return `${Number(apresentacao.valor).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
}

function textoMoeda(apresentacao) {
  return apresentacao.disponivel ? formatarMoeda(apresentacao.valor) : 'Não disponível';
}

export default function KpiCard({ icon: Icon, titulo, badge = 'SIMULADO', tooltip, children }) {
  const tituloTooltip = tooltip ?? tooltipFonte(badge === 'SIMULADO' ? 'SIMULADO' : null);

  return (
    <article className={`${CARD} p-5`}>
      <header className="flex items-center gap-3">
        <span className="grid size-7 place-items-center rounded-md bg-brand-fundo text-brand">
          <Icon size={14} />
        </span>
        <h3 className="text-[10px] tracking-wider text-texto-suave">{titulo}</h3>
      </header>
      <div className="mt-4">{children}</div>
      {badge && (
        <footer className="mt-4 flex items-center justify-end border-t border-borda pt-3">
          <span title={tituloTooltip}>
            <Badge tom={badge === 'SIMULADO' ? 'amber' : 'neutral'}>{badge}</Badge>
          </span>
        </footer>
      )}
    </article>
  );
}

export function KpiGrid({ kpis = {}, maoDeObra = null }) {
  const fonte = kpis.fonte ?? null;
  const realizado = apresentarKpi(kpis.avancoRealizado, fonte);
  const previsto = apresentarKpi(kpis.avancoPrevisto, fonte);
  const spi = apresentarKpi(kpis.spi, fonte);
  const orcamento = apresentarKpi(kpis.orcamentoTotal, fonte);
  const executado = apresentarKpi(kpis.executado, fonte);
  const saldo = apresentarKpi(calcularKpi((total, gasto) => total - gasto, kpis.orcamentoTotal, kpis.executado), fonte);
  const limite = kpis.limiteAlerta ?? kpis.limite;
  const consumido = apresentarKpi(
    calcularKpi((gasto, total) => (total === 0 ? null : (gasto / total) * 100), kpis.executado, kpis.orcamentoTotal),
    fonte,
  );
  const desvio = apresentarKpi(calcularKpi((real, planejado) => real - planejado, kpis.avancoRealizado, kpis.avancoPrevisto), fonte);
  const fonteMaoDeObra = maoDeObra?.fonte ?? fonte;
  const presentes = apresentarKpi(maoDeObra?.presentesHoje ?? kpis.maoDeObraTotal, fonteMaoDeObra);
  const vsOntem = apresentarKpi(maoDeObra?.vsOntem ?? kpis.vsOntem, fonteMaoDeObra);
  const noPrazo = spi.disponivel && Number(spi.valor) >= 1;

  return (
    <section className="grid grid-cols-1 gap-6 md:grid-cols-3">
      <KpiCard icon={CalendarDays} titulo="AVANÇO FÍSICO DO CRONOGRAMA" badge={fonte}>
        <p className="text-2xl font-semibold text-texto">
          {textoPercentual(realizado)}{' '}
          <span className="text-xs font-normal text-texto-suave">
            realizado vs. {textoPercentual(previsto)} previsto
          </span>
        </p>
        <div className="mt-4 space-y-3 text-xs text-texto-suave">
          <div className="flex items-center gap-4">
            <span className="shrink-0">Realizado</span>
            {realizado.disponivel ? <ProgressBar value={Number(realizado.valor)} /> : <span>—</span>}
            <span className="shrink-0 text-right text-texto">{textoPercentual(realizado)}</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="shrink-0">Previsto</span>
            {previsto.disponivel ? <ProgressBar value={Number(previsto.valor)} color="bg-neutro" /> : <span>—</span>}
            <span className="shrink-0 text-right text-texto-suave">{textoPercentual(previsto)}</span>
          </div>
        </div>
        <footer className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-borda pt-3 text-xs text-texto-suave">
          <span>
            Desvio <b className="text-texto">{desvio.disponivel ? textoPercentual(desvio) : '—'}</b> · SPI{' '}
            <b className="text-texto">{spi.disponivel ? Number(spi.valor).toLocaleString('pt-BR') : 'Não disponível'}</b>
          </span>
          <Badge tom={!spi.disponivel ? 'neutral' : noPrazo ? 'green' : 'amber'}>
            {!spi.disponivel ? 'Não disponível' : noPrazo ? 'No prazo' : 'Atenção'}
          </Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={CircleDollarSign} titulo="RESUMO ORÇAMENTÁRIO E FINANCEIRO" badge={fonte}>
        <p className="text-2xl font-semibold text-texto">
          {textoMoeda(orcamento)}{' '}
          <span className="text-xs font-normal text-texto-suave">orçamento total</span>
        </p>
        {consumido.disponivel ? <ProgressBar value={Number(consumido.valor)} className="mt-4" /> : <p className="mt-4 text-xs text-texto-suave">—</p>}
        <div className="mt-3 flex justify-between gap-3 text-xs text-texto-suave">
          <p>
            Executado
            <br />
            <b className="text-texto">{textoMoeda(executado)}</b>
          </p>
          <p className="text-right">
            Saldo restante
            <br />
            <b className="text-texto">{textoMoeda(saldo)}</b>
          </p>
        </div>
        <footer className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-borda pt-3 text-xs text-texto-suave">
          <span>{consumido.disponivel ? textoPercentual(consumido) : '—'} do orçamento consumido</span>
          <Badge
            tom={!consumido.disponivel || limite == null ? 'neutral' : Number(consumido.valor) <= Number(limite) ? 'green' : 'red'}
            dot
          >
            {!consumido.disponivel || limite == null
              ? 'Não disponível'
              : Number(consumido.valor) <= Number(limite) ? 'Dentro do Teto' : 'Acima do Teto'}
          </Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={Users} titulo="MÃO DE OBRA NO CANTEIRO" badge={fonteMaoDeObra}>
        <p className="text-2xl font-semibold text-texto">
          {presentes.disponivel ? presentes.valor : 'Não disponível'}{' '}
          <span className="text-xs font-normal text-texto-suave">operários presentes hoje</span>
        </p>
        <footer className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-borda pt-3 text-xs text-texto-suave">
          <span>Total presente no canteiro</span>
          <Badge tom={!vsOntem.disponivel ? 'neutral' : Number(vsOntem.valor) >= 0 ? 'green' : 'red'}>
            {vsOntem.disponivel
              ? `${Number(vsOntem.valor) >= 0 ? '+' : ''}${vsOntem.valor} vs. ontem`
              : 'Sem comparação'}
          </Badge>
        </footer>
      </KpiCard>
    </section>
  );
}
