import { CloudRain, FileText, ShieldCheck, Truck } from 'lucide-react';
import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import { Badge } from './primitivos.jsx';
import { CARD, TOM } from './tokens.js';

const ICONES = {
  cloudRain: CloudRain,
  truck: Truck,
  shield: ShieldCheck,
  clipboard: FileText,
};

export default function OcorrenciasRecentes({ ocorrencias = [] }) {
  const itens = Array.isArray(ocorrencias) ? ocorrencias : [];

  return (
    <section className={`${CARD} p-5`}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-texto">Registro de ocorrências e imprevistos</h3>
          <p className="text-xs text-texto-fraco">Registro do diário selecionado</p>
        </div>
      </header>

      {itens.length === 0 ? (
        <div className="mt-5">
          <EmptyState mensagem="Sem ocorrências registradas neste diário." />
        </div>
      ) : (
        <ol className="mt-5 space-y-4">
          {itens.map((item, indice) => {
            const Icon = ICONES[item.icone] ?? FileText;
            return (
              <li key={item.id ?? `${item.data}-${indice}`} className="flex gap-3">
                <span className={`grid size-8 shrink-0 place-items-center rounded-lg border ${TOM[item.tom] ?? TOM.neutral}`}>
                  <Icon size={14} />
                </span>
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-mono text-[11px] text-texto-fraco">{item.data}</span>
                    <Badge tom={item.tom ?? 'neutral'} dot>
                      {item.tag}
                    </Badge>
                  </div>
                  <p className="mt-1.5 text-sm text-texto">{item.texto}</p>
                  {item.paralisacao && (
                    <p className="mt-1 text-[11px] text-texto-fraco">
                      Paralisação: {item.paralisacao.inicio} → {item.paralisacao.fim}
                    </p>
                  )}
                </div>
              </li>
            );
          })}
        </ol>
      )}
    </section>
  );
}
