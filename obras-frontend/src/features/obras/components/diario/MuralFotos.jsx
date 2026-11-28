import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import FotoReal from '../detalhe/FotoReal.jsx';
import { fotosDeOcorrencias, fotoParaExibir, legendaEfetiva } from '../../adapters/adaptadoresFoto.js';
import { CARD } from '../detalhe/tokens.js';

export default function MuralFotos({ obraId, ocorrencias = [] }) {
  const fotos = fotosDeOcorrencias(ocorrencias).filter(fotoParaExibir);

  return (
    <section className={`${CARD} p-5`}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-texto">Mural de evidências fotográficas</h3>
          <p className="text-xs text-texto-fraco">{fotos.length} fotos de ocorrências</p>
        </div>
      </header>
      {fotos.length === 0 ? (
        <div className="mt-4">
          <EmptyState mensagem="Sem fotos registradas neste diário." />
        </div>
      ) : (
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {fotos.map((foto) => (
            <div key={foto.id} className="min-w-0">
              <FotoReal
                obraId={obraId}
                foto={{ ...foto, legenda: legendaEfetiva(foto.legenda, foto.descricaoOcorrencia) }}
                className="aspect-[4/3]"
              />
              <span className="sr-only">Ocorrência {foto.ocorrenciaId}</span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
