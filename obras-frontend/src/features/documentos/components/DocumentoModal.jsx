import { useEffect, useRef } from "react";
import { X } from "lucide-react";

export default function DocumentoModal({
  titulo,
  fechar,
  children,
  amplo = false,
}) {
  const ref = useRef(null);
  useEffect(() => {
    const dialog = ref.current;
    dialog.showModal();
    return () => dialog.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className={`doc-modal ${amplo ? "doc-modal-amplo" : ""}`}
      onCancel={(e) => {
        e.preventDefault();
        fechar();
      }}
    >
      <header>
        <h2>{titulo}</h2>
        <button
          type="button"
          className="doc-icon"
          onClick={fechar}
          aria-label="Fechar"
        >
          <X size={20} />
        </button>
      </header>
      {children}
    </dialog>
  );
}
