import { NavLink } from 'react-router-dom';
import { useMenu } from './useMenu.js';

/** Navegação do canteiro (celular): todos os módulos, com rolagem horizontal. */
export default function BottomNav() {
  const itens = useMenu();
  return (
    <nav
      aria-label="Navegação principal"
      className="fixed inset-x-0 bottom-0 z-40 flex overflow-x-auto border-t border-menu-ativo bg-menu md:hidden"
    >
      {itens.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `flex min-h-[56px] min-w-[82px] shrink-0 flex-1 flex-col items-center justify-center gap-0.5 text-[11px] ${
              isActive ? 'text-brand' : 'text-menu-texto'
            }`
          }
        >
          <Icon size={20} aria-hidden="true" />
          {label}
        </NavLink>
      ))}
    </nav>
  );
}
