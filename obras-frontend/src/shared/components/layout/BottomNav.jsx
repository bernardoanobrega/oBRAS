import { NavLink } from 'react-router-dom';
import { useMenu } from './useMenu.js';

const ATALHOS_CELULAR = 5;

/** Navegação do canteiro (celular): até 5 atalhos grandes. */
export default function BottomNav() {
  const itens = useMenu().slice(0, ATALHOS_CELULAR);
  return (
    <nav
      aria-label="Atalhos"
      className="fixed inset-x-0 bottom-0 z-40 flex border-t border-menu-ativo bg-menu md:hidden"
    >
      {itens.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `flex min-h-[56px] flex-1 flex-col items-center justify-center gap-0.5 text-[11px] ${
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
