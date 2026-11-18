import { Outlet, useNavigate, useParams } from 'react-router-dom';
import { HardHat, LogOut } from 'lucide-react';
import Sidebar from './Sidebar.jsx';
import BottomNav from './BottomNav.jsx';
import OfflineBanner from '../feedback/OfflineBanner.jsx';
import { useAuth } from '../../auth/useAuth.js';

export default function AppShell() {
  const { usuario, sair } = useAuth();
  const { obraId } = useParams();
  const navigate = useNavigate();

  return (
    <div className="flex h-full flex-col bg-[#0b0d12] text-slate-100">
      <OfflineBanner />
      <div className="flex min-h-0 flex-1">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex items-center justify-between border-b border-slate-800/80 bg-[#0f1218] px-4 py-2.5 md:px-6">
            <span className="font-bold text-amber-500 md:hidden">oBRAS</span>

            <div className="ml-auto flex items-center gap-3">
              {/* Seletor Contextual de Obra Ativa (conforme topo direito do SVG) */}
              <div className="flex items-center gap-2 rounded-lg border border-slate-800 bg-[#181b22] px-3 py-1.5 text-xs text-slate-200">
                <HardHat size={15} className="text-amber-500" />
                <select
                  aria-label="Obra ativa"
                  value={obraId || '1'}
                  onChange={(e) => navigate(`/obras/${e.target.value}/estoque`)}
                  className="bg-transparent font-medium text-slate-200 outline-none"
                >
                  <option value="1" className="bg-[#181b22]">
                    Obra #1 — Residencial Parque das Águas
                  </option>
                </select>
              </div>

              <span className="hidden text-xs font-medium text-slate-400 sm:inline">
                {usuario?.nome}
              </span>
              <button
                onClick={sair}
                className="flex items-center gap-1.5 rounded-lg border border-slate-800 bg-[#181b22] px-3 py-1.5 text-xs font-medium text-slate-300 hover:bg-slate-800"
              >
                <LogOut size={14} /> Sair
              </button>
            </div>
          </header>

          <main className="min-h-0 flex-1 overflow-y-auto p-4 pb-20 md:p-6 md:pb-6">
            <Outlet />
          </main>
        </div>
      </div>
      <BottomNav />
    </div>
  );
}