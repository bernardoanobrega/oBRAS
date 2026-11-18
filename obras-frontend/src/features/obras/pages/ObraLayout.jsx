import { NavLink, Outlet, useParams } from 'react-router-dom';
import { ABAS_OBRA } from '../../../app/nav.js';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import Spinner from '../../../shared/components/ui/Spinner.jsx';

export default function ObraLayout() {
  const { obraId } = useParams();
  const { pode, carregando } = usePermissao(obraId);

  if (carregando) return <Spinner className="mt-16" />;
  const abas = ABAS_OBRA.filter((a) => pode(a.modulo, 'VISUALIZAR'));

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-lg font-semibold text-slate-100">
          Obra #{obraId} — Residencial Parque das Águas
        </h1>
      </div>
      <nav
        className="flex gap-1 overflow-x-auto border-b border-slate-800"
        aria-label="Abas da obra"
      >
        {abas.map((a) => (
          <NavLink
            key={a.path}
            to={a.path}
            className={({ isActive }) =>
              `whitespace-nowrap px-4 py-2.5 text-xs font-semibold uppercase tracking-wider transition ${
                isActive
                  ? 'border-b-2 border-amber-500 text-amber-400'
                  : 'text-slate-400 hover:text-slate-200'
              }`
            }
          >
            {a.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  );
}