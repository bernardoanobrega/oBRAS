import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import { paraClimaDia, paraDiario } from '../../api/adaptadoresDiario.js';
import ClimaDiaCard from '../detalhe/ClimaDiaCard.jsx';
import MuralFotos from '../detalhe/MuralFotos.jsx';
import OcorrenciasRecentes from '../detalhe/OcorrenciasRecentes.jsx';
import { Badge } from '../detalhe/primitivos.jsx';
import { CARD } from '../detalhe/tokens.js';

export default function DiarioDetalhe({ obraId, diario }) {
  const dados = paraDiario(diario);

  if (!dados) {
    return (
      <section className={CARD}>
        <header className="p-5 pb-0">
          <h2 className="text-sm font-semibold text-white">Registro do dia</h2>
        </header>
        <div className="p-5">
          <EmptyState mensagem="Sem diário registrado para esta obra." />
        </div>
      </section>
    );
  }

  return (
    <section className="space-y-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-sm font-semibold text-white">Registro de {dados.data}</h2>
          <p className="mt-1 text-xs text-zinc-500">{dados.descricao}</p>
        </div>
        <Badge tom="neutral">{dados.equipePresenteIds.length} na equipe</Badge>
      </div>
      <ClimaDiaCard clima={paraClimaDia(diario)} />
      <OcorrenciasRecentes ocorrencias={dados.ocorrencias} />
      <MuralFotos obraId={obraId} fotos={dados.fotos} />
    </section>
  );
}
