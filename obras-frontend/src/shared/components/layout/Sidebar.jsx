import { NavLink } from 'react-router-dom';
import { useMenu } from './useMenu.js';

export default function Sidebar() {
  const itens = useMenu();
  return (
    <aside className="hidden w-60 shrink-0 border-r border-slate-200 bg-white md:block">
      <div className="px-5 py-4 text-xl font-bold text-brand-700">oBRAS</div>
      <nav className="flex flex-col gap-1 px-3">
        {itens.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium ${
                isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100'
              }`
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
