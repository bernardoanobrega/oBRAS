import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import FotoReal from './FotoReal.jsx';
import { CARD } from './tokens.js';

export default function MuralFotos({ obraId, fotos = [] }) {
  const itens = Array.isArray(fotos) ? fotos : [];

  return (
    <section className={`${CARD} p-5`}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-texto">Mural de evidências fotográficas</h3>
          <p className="text-xs text-texto-fraco">{itens.length} registros de ocorrências</p>
        </div>
      </header>

      {itens.length === 0 ? (
        <div className="mt-4">
          <EmptyState mensagem="Sem fotos registradas neste diário." />
        </div>
      ) : (
        <>
          <div className="mt-4">
            <FotoReal obraId={obraId} foto={itens[0]} className="aspect-[1408/768]" />
          </div>
          <div className="mt-4 grid grid-cols-2 gap-4">
            {itens.slice(1).map((foto) => (
              <FotoReal key={foto.id ?? foto.nomeArquivo} obraId={obraId} foto={foto} />
            ))}
          </div>
        </>
      )}
    </section>
  );
}
