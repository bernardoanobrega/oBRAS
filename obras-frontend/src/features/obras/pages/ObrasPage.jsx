import { Link } from 'react-router-dom';
import { Boxes, Building2, MapPin, ShoppingCart } from 'lucide-react';

export default function ObrasPage() {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-2">
        <Building2 className="text-amber-500" size={22} />
        <h1 className="text-xl font-semibold text-slate-100">Obras Cadastradas</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="rounded-2xl border border-slate-800 bg-[#161920] p-5 shadow-lg">
          <div className="flex items-start justify-between">
            <div>
              <span className="rounded bg-emerald-950/80 px-2.5 py-0.5 text-xs font-medium text-emerald-300 border border-emerald-500/30">
                EM_ANDAMENTO
              </span>
              <h2 className="mt-2 text-lg font-bold text-white">
                Obra #1 — Residencial Parque das Águas
              </h2>
              <p className="mt-1 flex items-center gap-1.5 text-xs text-slate-400">
                <MapPin size={14} className="text-amber-500" />
                Av. Beira Mar, 1200 — Aracaju/SE • 2 Canteiros Ativos
              </p>
            </div>
          </div>

          <div className="mt-5 flex flex-wrap gap-3 border-t border-slate-800 pt-4">
            <Link
              to="/obras/1/estoque"
              className="flex flex-1 items-center justify-center gap-2 rounded-xl bg-amber-500 px-4 py-2.5 text-xs font-semibold text-slate-950 hover:bg-amber-400"
            >
              <Boxes size={16} /> Abrir Painel de Estoque
            </Link>
            <Link
              to="/obras/1/compras"
              className="flex flex-1 items-center justify-center gap-2 rounded-xl border border-slate-700 bg-[#1b1f28] px-4 py-2.5 text-xs font-semibold text-slate-100 hover:bg-slate-800"
            >
              <ShoppingCart size={16} /> Abrir Kanban de Compras
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}