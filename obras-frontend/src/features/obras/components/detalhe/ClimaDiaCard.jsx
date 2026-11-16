import { Sun } from 'lucide-react';
import { Badge } from './primitivos.jsx';
import { CARD, ICON_BOX } from './tokens.js';

/** Clima único do dia. Cortes L46: sem turno, sem temperatura, sem sincronização. */
export default function ClimaDiaCard({ clima }) {
  return (
    <section className={`${CARD} flex items-center justify-between gap-3 p-4`}>
      <div className="flex items-center gap-3">
        <span className={`${ICON_BOX} size-10`}>
          <Sun size={18} />
        </span>
        <div>
          <p className="text-sm font-semibold text-white">{clima.condicao}</p>
          <p className="text-xs text-zinc-500">{clima.data}</p>
        </div>
      </div>
      <Badge tom="green" dot>
        {clima.badge}
      </Badge>
    </section>
  );
}
