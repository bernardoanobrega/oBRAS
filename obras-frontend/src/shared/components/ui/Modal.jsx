import { useRef } from 'react';
import { X } from 'lucide-react';
import { useFocusTrap } from '../../hooks/useFocusTrap.js';

export default function Modal({ titulo, aberto, onFechar, largura = 'max-w-lg', children }) {
  const referencia = useRef(null);
  useFocusTrap(referencia, aberto, onFechar);

  if (!aberto) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-fundo/80 p-0 md:items-center md:p-4" onClick={onFechar}>
      <div
        ref={referencia}
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        tabIndex={-1}
        className={`max-h-[90vh] w-full overflow-y-auto rounded-t-2xl border border-borda bg-superficie p-5 shadow-xl md:rounded-2xl ${largura}`}
        onClick={(e) => e.stopPropagation()}
      >
        <header className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{titulo}</h2>
          <button onClick={onFechar} aria-label="Fechar" className="rounded p-2 hover:bg-superficie-alta">
            <X size={18} />
          </button>
        </header>
        {children}
      </div>
    </div>
  );
}
