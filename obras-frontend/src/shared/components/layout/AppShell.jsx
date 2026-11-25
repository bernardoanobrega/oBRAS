import { Outlet, useLocation } from 'react-router-dom';
import { HardHat, LogOut } from 'lucide-react';
import Sidebar from './Sidebar.jsx';
import BottomNav from './BottomNav.jsx';
import OfflineBanner from '../feedback/OfflineBanner.jsx';
import { useAuth } from '../../auth/useAuth.js';
import { tituloDaRota } from '../../../app/nav.js';

const QUANTIDADE_INICIAIS = 2;

const iniciais = (nome = '') =>
  nome
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, QUANTIDADE_INICIAIS)
    .map((parte) => parte[0].toUpperCase())
    .join('');

export default function AppShell() {
  const { usuario, sair } = useAuth();
  const { pathname } = useLocation();

  return (
    <div className="flex h-full flex-col">
      <OfflineBanner />
      <div className="flex min-h-0 flex-1">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <header className="flex h-14 shrink-0 items-center gap-3 border-b border-borda bg-superficie px-4 md:px-6">
            <span
              className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-fundo md:hidden"
              aria-hidden="true"
            >
              <HardHat size={18} />
            </span>
            <h1 className="truncate text-lg font-semibold text-texto md:text-xl">
              {tituloDaRota(pathname)}
            </h1>
            <div className="ml-auto hidden text-right sm:block">
              <p className="text-xs font-semibold text-texto">{usuario?.nome}</p>
              <p className="text-xs text-texto-suave">{usuario?.login}</p>
            </div>
            <span
              className="hidden h-8 w-8 shrink-0 items-center justify-center rounded-full bg-superficie-alta text-xs font-semibold text-texto sm:flex"
              aria-hidden="true"
            >
              {iniciais(usuario?.nome)}
            </span>
            <button
              type="button"
              onClick={sair}
              className="ml-auto flex min-h-[44px] items-center gap-1 rounded-lg px-3 text-sm text-texto-suave hover:bg-superficie-alta hover:text-texto sm:ml-0"
            >
              <LogOut size={16} aria-hidden="true" /> Sair
            </button>
          </header>
          {/* pb-20: espaço para a BottomNav no celular */}
          <main className="min-h-0 flex-1 overflow-y-auto bg-fundo p-4 pb-20 md:p-6 md:pb-6">
            <Outlet />
          </main>
        </div>
      </div>
      <BottomNav />
    </div>
  );
}
