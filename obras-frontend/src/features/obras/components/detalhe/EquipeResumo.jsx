import { Phone, Mail } from 'lucide-react';
import { Avatar, Badge, IconButton } from './primitivos.jsx';
import { CARD } from './tokens.js';

/** Equipe técnica e responsáveis. Cortes L46: sem frente de trabalho e sem sub/CREA. */
export default function EquipeResumo({ equipe }) {
  return (
    <section className={`${CARD} p-5`}>
      <h3 className="text-sm font-semibold text-white">Equipe técnica e responsáveis</h3>
      <p className="text-xs text-zinc-500">{equipe.length} profissionais alocados</p>

      <div className="mt-4 overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="text-[10px] tracking-wider text-zinc-500">
            <tr>
              <th className="py-2 font-normal">PROFISSIONAL</th>
              <th className="font-normal">CARGO</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {equipe.map((p) => (
              <tr key={p.nome} className="h-12">
                <td>
                  <div className="flex items-center gap-3">
                    <Avatar tom={p.tom}>{p.sigla}</Avatar>
                    <p className="whitespace-nowrap text-white">{p.nome}</p>
                  </div>
                </td>
                <td>
                  <Badge tom={p.tom}>{p.cargo}</Badge>
                </td>
                <td>
                  <div className="flex justify-end gap-2">
                    <IconButton icon={Phone} label={`Ligar para ${p.nome}`} />
                    <IconButton icon={Mail} label={`Enviar e-mail para ${p.nome}`} />
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
