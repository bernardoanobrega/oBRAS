import { NavLink } from 'react-router-dom';
import { HardHat } from 'lucide-react';
import { useMenu } from './useMenu.js';

export default function Sidebar() {
  const itens = useMenu();
  return (
    <aside className="hidden w-60 shrink-0 border-r border-slate-800/80 bg-[#0d1322] text-slate-300 md:flex md:flex-col">
      <div className="flex items-center gap-3 px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500 text-slate-950 shadow-md">
          <HardHat size={20} strokeWidth={2.3} />
        </div>
        <span className="text-lg font-bold tracking-tight text-white">oBRAS</span>
      </div>

      <nav className="flex flex-1 flex-col gap-1 px-3 py-2">
        {itens.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition ${
                isActive
                  ? 'bg-[#1e293b] text-white shadow-sm'
                  : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
              }`
            }
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}