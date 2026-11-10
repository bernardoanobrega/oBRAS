import { NavLink } from 'react-router-dom';
import { useMenu } from './useMenu.js';

/** Navegação do canteiro (celular): até 5 atalhos grandes. */
export default function BottomNav() {
  const itens = useMenu().slice(0, 5);
  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 flex border-t border-slate-200 bg-white md:hidden">
      {itens.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `flex min-h-[56px] flex-1 flex-col items-center justify-center gap-0.5 text-[11px] ${
              isActive ? 'text-brand-700' : 'text-slate-500'
            }`
          }
        >
          <Icon size={20} />
          {label}
        </NavLink>
      ))}
    </nav>
  );
}
