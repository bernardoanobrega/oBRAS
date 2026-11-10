import { useEffect } from 'react';
import { X } from 'lucide-react';

export default function Modal({ titulo, aberto, onFechar, largura = 'max-w-lg', children }) {
  useEffect(() => {
    if (!aberto) return undefined;
    const aoTeclar = (e) => e.key === 'Escape' && onFechar();
    window.addEventListener('keydown', aoTeclar);
    return () => window.removeEventListener('keydown', aoTeclar);
  }, [aberto, onFechar]);

  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 p-0 md:items-center md:p-4" onClick={onFechar}>
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className={`max-h-[90vh] w-full overflow-y-auto rounded-t-2xl bg-white p-5 shadow-xl md:rounded-2xl ${largura}`}
        onClick={(e) => e.stopPropagation()}
      >
        <header className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{titulo}</h2>
          <button onClick={onFechar} aria-label="Fechar" className="rounded p-2 hover:bg-slate-100">
            <X size={18} />
          </button>
        </header>
        {children}
      </div>
    </div>
  );
}
