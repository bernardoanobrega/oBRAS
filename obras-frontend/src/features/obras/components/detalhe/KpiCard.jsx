import { CalendarDays, CircleDollarSign, Users } from 'lucide-react';
import { formatarMoeda } from '../../../../shared/utils/formatadores.js';
import { Badge, ProgressBar } from './primitivos.jsx';
import { CARD } from './tokens.js';

export default function KpiCard({ icon: Icon, titulo, badge = 'SIMULADO', children }) {
  return (
    <article className={`${CARD} p-5`}>
      <header className="flex items-center gap-3">
        <span className="grid size-7 place-items-center rounded-md bg-amber-500/10 text-amber-500">
          <Icon size={14} />
        </span>
        <h3 className="text-[10px] tracking-wider text-zinc-400">{titulo}</h3>
      </header>
      <div className="mt-4">{children}</div>
      {badge && (
        <footer className="mt-4 flex items-center justify-end border-t border-zinc-800 pt-3">
          <Badge tom="amber">{badge}</Badge>
        </footer>
      )}
    </article>
  );
}

export function KpiGrid({ kpis }) {
  const consumido = kpis.orcamentoTotal ? (kpis.executado / kpis.orcamentoTotal) * 100 : 0;

  return (
    <section className="grid grid-cols-1 gap-6 md:grid-cols-3">
      <KpiCard icon={CalendarDays} titulo="AVANÇO FÍSICO DO CRONOGRAMA" badge={kpis.fonte}>
        <p className="text-2xl font-semibold text-white">
          {kpis.avancoRealizado}%{' '}
          <span className="text-xs font-normal text-zinc-400">
            realizado vs. {kpis.avancoPrevisto}% previsto
          </span>
        </p>
        <div className="mt-4 space-y-3 text-xs text-zinc-400">
          <div className="flex items-center gap-4">
            <span className="w-16">Realizado</span>
            <ProgressBar value={kpis.avancoRealizado} />
            <span className="w-8 text-right text-white">{kpis.avancoRealizado}%</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="w-16">Previsto</span>
            <ProgressBar value={kpis.avancoPrevisto} color="bg-zinc-500" />
            <span className="w-8 text-right text-zinc-300">{kpis.avancoPrevisto}%</span>
          </div>
        </div>
        <footer className="mt-4 flex items-center justify-between border-t border-zinc-800 pt-3 text-xs text-zinc-400">
          <span>
            Desvio{' '}
            <b className="text-white">
              {kpis.avancoRealizado - kpis.avancoPrevisto > 0 ? '+' : ''}
              {kpis.avancoRealizado - kpis.avancoPrevisto}%
            </b>{' '}
            · SPI <b className="text-white">{kpis.spi.toLocaleString('pt-BR')}</b>
          </span>
          <Badge tom={kpis.spi >= 1 ? 'green' : 'amber'}>{kpis.spi >= 1 ? 'No prazo' : 'Atenção'}</Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={CircleDollarSign} titulo="RESUMO ORÇAMENTÁRIO E FINANCEIRO" badge={kpis.fonte}>
        <p className="text-2xl font-semibold text-white">
          {formatarMoeda(kpis.orcamentoTotal)}{' '}
          <span className="text-xs font-normal text-zinc-400">orçamento total</span>
        </p>
        <ProgressBar value={consumido} className="mt-4" />
        <div className="mt-3 flex justify-between text-xs text-zinc-400">
          <p>
            Executado
            <br />
            <b className="text-white">{formatarMoeda(kpis.executado)}</b>
          </p>
          <p className="text-right">
            Saldo restante
            <br />
            <b className="text-white">{formatarMoeda(kpis.saldo)}</b>
          </p>
        </div>
        <footer className="mt-4 flex items-center justify-between border-t border-zinc-800 pt-3 text-xs text-zinc-400">
          <span>{consumido.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}% do orçamento consumido</span>
          <Badge tom={consumido <= kpis.limite ? 'green' : 'red'} dot>
            {consumido <= kpis.limite ? 'Dentro do Teto' : 'Acima do Teto'}
          </Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={Users} titulo="MÃO DE OBRA NO CANTEIRO" badge={kpis.fonte}>
        <p className="text-2xl font-semibold text-white">
          {kpis.maoDeObraTotal}{' '}
          <span className="text-xs font-normal text-zinc-400">operários ativos hoje</span>
        </p>
        <footer className="mt-4 flex items-center justify-between border-t border-zinc-800 pt-3 text-xs text-zinc-400">
          <span>Total presente no canteiro</span>
          <Badge tom={kpis.vsOntem >= 0 ? 'green' : 'red'}>
            {kpis.vsOntem >= 0 ? '+' : ''}
            {kpis.vsOntem} vs. ontem
          </Badge>
        </footer>
      </KpiCard>
    </section>
  );
}
