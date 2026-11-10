import { Outlet } from 'react-router-dom';
import { LogOut } from 'lucide-react';
import Sidebar from './Sidebar.jsx';
import BottomNav from './BottomNav.jsx';
import OfflineBanner from '../feedback/OfflineBanner.jsx';
import { useAuth } from '../../auth/useAuth.js';

export default function AppShell() {
  const { usuario, sair } = useAuth();
  return (
    <div className="flex h-full flex-col">
      <OfflineBanner />
      <div className="flex min-h-0 flex-1">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-2">
            <span className="font-bold text-brand-700 md:hidden">oBRAS</span>
            <span className="ml-auto mr-3 text-sm text-slate-600">{usuario?.nome}</span>
            <button onClick={sair} className="flex items-center gap-1 rounded p-2 text-sm text-slate-600 hover:bg-slate-100">
              <LogOut size={16} /> Sair
            </button>
          </header>
          {/* pb-20: espaço para a BottomNav no celular */}
          <main className="min-h-0 flex-1 overflow-y-auto p-4 pb-20 md:p-6 md:pb-6">
            <Outlet />
          </main>
        </div>
      </div>
      <BottomNav />
    </div>
  );
}
