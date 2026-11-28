import Can from '../../../../shared/auth/Can.jsx';
import EmptyState from '../../../../shared/components/feedback/EmptyState.jsx';
import Button from '../../../../shared/components/ui/Button.jsx';
import { paraClimaDia, paraDiario } from '../../api/adaptadoresDiario.js';
import ClimaDiaCard from '../detalhe/ClimaDiaCard.jsx';
import { Badge } from '../detalhe/primitivos.jsx';
import { CARD } from '../detalhe/tokens.js';
import ErroFormulario from './ErroFormulario.jsx';
import MuralFotos from './MuralFotos.jsx';
import OcorrenciaList from './OcorrenciaList.jsx';

export default function DiarioDetalhe({
  obraId,
  diario,
  erroAcao = null,
  onEditar,
  onNovaOcorrencia,
  onOcorrenciaRapida,
  onEditarOcorrencia,
  onExcluirOcorrencia,
  onRecarregar,
}) {
  const dados = paraDiario(diario);

  if (!dados) {
    return (
      <section className={CARD}>
        <header className="p-5 pb-0">
          <h2 className="text-sm font-semibold text-texto">Registro do dia</h2>
        </header>
        <div className="p-5">
          <EmptyState mensagem="Sem diário registrado para esta obra." />
          <div className="mt-4 flex flex-wrap gap-2">
            <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
              <Button type="button" onClick={onOcorrenciaRapida}>Ocorrência rápida</Button>
            </Can>
          </div>
          <ErroFormulario erroGeral={erroAcao} />
        </div>
      </section>
    );
  }

  return (
    <section className="space-y-4">
      <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div>
          <h2 className="text-sm font-semibold text-texto">Registro de {dados.data}</h2>
          <p className="mt-1 text-xs text-texto-fraco">{dados.descricao}</p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Badge tom="neutral">{dados.equipePresenteIds.length} na equipe</Badge>
          <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={obraId}>
            <Button type="button" variante="secundario" onClick={() => onEditar?.(diario)}>Editar diário</Button>
          </Can>
          <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
            <Button type="button" onClick={onNovaOcorrencia}>Nova ocorrência</Button>
          </Can>
        </div>
      </div>
      <ErroFormulario erroGeral={erroAcao} />
      <ClimaDiaCard clima={paraClimaDia(diario)} />
      <OcorrenciaList
        obraId={obraId}
        ocorrencias={diario.ocorrencias}
        onEditar={onEditarOcorrencia}
        onExcluir={onExcluirOcorrencia}
        onRecarregar={onRecarregar}
      />
      <MuralFotos obraId={obraId} ocorrencias={diario.ocorrencias} />
    </section>
  );
}
