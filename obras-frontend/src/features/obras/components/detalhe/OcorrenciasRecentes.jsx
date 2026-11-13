import { CloudRain, Truck, ShieldCheck } from 'lucide-react';
import { Badge } from './primitivos.jsx';
import { CARD, TOM } from './tokens.js';

const ICONE_POR_TAG = {
  'Clima / Paralisação': CloudRain,
  Logística: Truck,
  Segurança: ShieldCheck,
};

/** Ocorrências recentes. Ícone mapeado aqui (mock sem componentes). Sem link morto "Ver todos". */
export default function OcorrenciasRecentes({ ocorrencias }) {
  return (
    <section className={`${CARD} p-5`}>
      <header>
        <h3 className="text-sm font-semibold text-white">Registro de ocorrências e imprevistos</h3>
        <p className="text-xs text-zinc-500">Últimos 7 dias · ordem cronológica</p>
      </header>

      <ol className="mt-5 space-y-4">
        {ocorrencias.map(({ data, tag, tom, texto }) => {
          const Icon = ICONE_POR_TAG[tag] || CloudRain;
          return (
            <li key={data} className="flex gap-3">
              <span className={`grid size-8 shrink-0 place-items-center rounded-lg border ${TOM[tom]}`}>
                <Icon size={14} />
              </span>
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-mono text-[11px] text-zinc-500">{data}</span>
                  <Badge tom={tom} dot>
                    {tag}
                  </Badge>
                </div>
                <p className="mt-1.5 text-sm text-zinc-200">{texto}</p>
              </div>
            </li>
          );
        })}
      </ol>
    </section>
  );
}
