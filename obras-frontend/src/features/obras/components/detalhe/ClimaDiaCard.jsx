import { CalendarDays, Cloud, CloudRain, Sun } from 'lucide-react';
import { Badge } from './primitivos.jsx';
import { CARD, ICON_BOX } from './tokens.js';

const ICONES = {
  sun: Sun,
  cloud: Cloud,
  cloudRain: CloudRain,
  calendar: CalendarDays,
};

export default function ClimaDiaCard({ clima }) {
  const info = clima ?? {};
  const Icon = ICONES[info.icone] ?? Sun;

  return (
    <section className={`${CARD} flex items-center justify-between gap-3 p-4`}>
      <div className="flex items-center gap-3">
        <span className={`${ICON_BOX} size-10`}>
          <Icon size={18} />
        </span>
        <div>
          <p className="text-sm font-semibold text-texto">{info.condicao ?? '—'}</p>
          <p className="text-xs text-texto-fraco">{info.data ?? '—'}</p>
        </div>
      </div>
      <Badge tom={info.tom ?? 'neutral'} dot>
        {info.badge ?? '—'}
      </Badge>
    </section>
  );
}
