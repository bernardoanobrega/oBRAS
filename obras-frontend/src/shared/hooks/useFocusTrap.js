import { useEffect } from 'react';

const SELETORES_FOCAVEIS = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled]):not([type="hidden"])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

export function useFocusTrap(ref, ativo = true, aoFechar = null) {
  useEffect(() => {
    if (!ativo || !ref.current) return undefined;

    const elementoAnterior = document.activeElement;
    const modal = ref.current;
    const focaveis = () => [...modal.querySelectorAll(SELETORES_FOCAVEIS)]
      .filter((elemento) => elemento.getAttribute('aria-hidden') !== 'true');

    (focaveis()[0] ?? modal).focus();

    function onKeyDown(evento) {
      if (evento.key === 'Escape') {
        aoFechar?.();
        return;
      }
      if (evento.key !== 'Tab') return;

      const lista = focaveis();
      if (!lista.length) {
        evento.preventDefault();
        modal.focus();
        return;
      }
      const primeiro = lista[0];
      const ultimo = lista[lista.length - 1];
      if (evento.shiftKey && (document.activeElement === primeiro || !modal.contains(document.activeElement))) {
        evento.preventDefault();
        ultimo.focus();
      } else if (!evento.shiftKey && (document.activeElement === ultimo || !modal.contains(document.activeElement))) {
        evento.preventDefault();
        primeiro.focus();
      }
    }

    modal.addEventListener('keydown', onKeyDown);
    return () => {
      modal.removeEventListener('keydown', onKeyDown);
      if (elementoAnterior instanceof HTMLElement && elementoAnterior.isConnected) {
        elementoAnterior.focus();
      }
    };
  }, [ref, ativo, aoFechar]);
}
