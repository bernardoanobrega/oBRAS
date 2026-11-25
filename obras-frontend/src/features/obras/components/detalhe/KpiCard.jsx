import { CalendarDays, CircleDollarSign, Users } from 'lucide-react';
import { formatarMoeda } from '../../../../shared/utils/formatadores.js';
import { Badge, ProgressBar } from './primitivos.jsx';
import { CARD } from './tokens.js';

export default function KpiCard({ icon: Icon, titulo, badge = 'SIMULADO', children }) {
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
        <p className="text-2xl font-semibold text-texto">
          {kpis.avancoRealizado}%{' '}
          <span className="text-xs font-normal text-texto-suave">
            realizado vs. {kpis.avancoPrevisto}% previsto
          </span>
        </p>
        <div className="mt-4 space-y-3 text-xs text-texto-suave">
          <div className="flex items-center gap-4">
            <span className="w-16">Realizado</span>
            <ProgressBar value={kpis.avancoRealizado} />
            <span className="w-8 text-right text-texto">{kpis.avancoRealizado}%</span>
          </div>
          <div className="flex items-center gap-4">
            <span className="w-16">Previsto</span>
            <ProgressBar value={kpis.avancoPrevisto} color="bg-neutro" />
            <span className="w-8 text-right text-texto-suave">{kpis.avancoPrevisto}%</span>
          </div>
        </div>
        <footer className="mt-4 flex items-center justify-between border-t border-borda pt-3 text-xs text-texto-suave">
          <span>
            Desvio{' '}
            <b className="text-texto">
              {kpis.avancoRealizado - kpis.avancoPrevisto > 0 ? '+' : ''}
              {kpis.avancoRealizado - kpis.avancoPrevisto}%
            </b>{' '}
            · SPI <b className="text-texto">{kpis.spi.toLocaleString('pt-BR')}</b>
          </span>
          <Badge tom={kpis.spi >= 1 ? 'green' : 'amber'}>{kpis.spi >= 1 ? 'No prazo' : 'Atenção'}</Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={CircleDollarSign} titulo="RESUMO ORÇAMENTÁRIO E FINANCEIRO" badge={kpis.fonte}>
        <p className="text-2xl font-semibold text-texto">
          {formatarMoeda(kpis.orcamentoTotal)}{' '}
          <span className="text-xs font-normal text-texto-suave">orçamento total</span>
        </p>
        <ProgressBar value={consumido} className="mt-4" />
        <div className="mt-3 flex justify-between text-xs text-texto-suave">
          <p>
            Executado
            <br />
            <b className="text-texto">{formatarMoeda(kpis.executado)}</b>
          </p>
          <p className="text-right">
            Saldo restante
            <br />
            <b className="text-texto">{formatarMoeda(kpis.saldo)}</b>
          </p>
        </div>
        <footer className="mt-4 flex items-center justify-between border-t border-borda pt-3 text-xs text-texto-suave">
          <span>{consumido.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}% do orçamento consumido</span>
          <Badge tom={consumido <= kpis.limite ? 'green' : 'red'} dot>
            {consumido <= kpis.limite ? 'Dentro do Teto' : 'Acima do Teto'}
          </Badge>
        </footer>
      </KpiCard>

      <KpiCard icon={Users} titulo="MÃO DE OBRA NO CANTEIRO" badge={kpis.fonte}>
        <p className="text-2xl font-semibold text-texto">
          {kpis.maoDeObraTotal}{' '}
          <span className="text-xs font-normal text-texto-suave">operários ativos hoje</span>
        </p>
        <footer className="mt-4 flex items-center justify-between border-t border-borda pt-3 text-xs text-texto-suave">
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
