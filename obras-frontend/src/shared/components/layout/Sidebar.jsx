import { NavLink } from 'react-router-dom';
import { HardHat } from 'lucide-react';
import { version } from '../../../../package.json';
import { useMenu } from './useMenu.js';

const AMBIENTE = import.meta.env.PROD ? 'Produção' : 'Desenvolvimento';
const VERSAO_PRINCIPAL = `v${version.split('.')[0]}`;

/** Menu lateral do desktop, no visual do protótipo do Figma. */
export default function Sidebar() {
  const itens = useMenu();
  return (
    <aside className="hidden w-60 shrink-0 flex-col bg-menu md:flex">
      <div className="flex h-14 shrink-0 items-center gap-3 border-b border-menu-ativo px-5">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-fundo">
          <HardHat size={18} aria-hidden="true" />
        </span>
        <span className="font-semibold text-texto">Gestor de Obras</span>
      </div>

      <p className="px-7 pb-4 pt-7 text-sm font-semibold uppercase tracking-wide text-menu-texto">
        Menu principal
      </p>
      <nav aria-label="Menu principal" className="flex flex-1 flex-col gap-2 overflow-y-auto px-3">
        {itens.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex min-h-[44px] items-center gap-3 rounded-lg px-3 text-sm font-medium transition ${
                isActive
                  ? 'bg-menu-ativo text-texto'
                  : 'text-menu-texto hover:bg-menu-ativo hover:text-texto'
              }`
            }
          >
            <Icon size={18} aria-hidden="true" />
            {label}
          </NavLink>
        ))}
      </nav>

      <footer className="flex items-center gap-3 border-t border-menu-ativo px-6 py-4">
        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-menu-destaque text-xs font-medium text-texto">
          {VERSAO_PRINCIPAL}
        </span>
        <div>
          <p className="text-xs font-medium text-texto">Versão {version}</p>
          <p className="text-xs text-menu-texto">{AMBIENTE}</p>
        </div>
      </footer>
    </aside>
  );
}
