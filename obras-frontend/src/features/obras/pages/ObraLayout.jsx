import { NavLink, Outlet, useParams } from 'react-router-dom';
import { ABAS_OBRA } from '../../../app/nav.js';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import Spinner from '../../../shared/components/ui/Spinner.jsx';

/** Painel da obra: abas filtradas pelas permissões efetivas do usuário NESTA obra. */
export default function ObraLayout() {
  const { obraId } = useParams();
  const { pode, carregando } = usePermissao(obraId);

  if (carregando) return <Spinner className="mt-16" />;
  const abas = ABAS_OBRA.filter((a) => pode(a.modulo, 'VISUALIZAR'));

  return (
    <div>
      <h1 className="mb-3 text-xl font-semibold">Obra #{obraId}</h1>
      <nav className="mb-4 flex gap-1 overflow-x-auto border-b border-slate-200" aria-label="Abas da obra">
        {abas.map((a) => (
          <NavLink
            key={a.path}
            to={a.path}
            className={({ isActive }) =>
              `whitespace-nowrap px-4 py-3 text-sm font-medium ${
                isActive ? 'border-b-2 border-brand-600 text-brand-700' : 'text-slate-600 hover:text-slate-900'
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
