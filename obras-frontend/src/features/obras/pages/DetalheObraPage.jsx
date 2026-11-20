import { useParams } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { paraClimaDia, paraDiario } from '../api/adaptadoresDiario.js';
import { useDetalheObra } from '../api/useDetalheObra.js';
import { useDiario } from '../api/useDiario.js';
import { KPIS } from '../mocks/detalheObraMock.js';
import ObraHeader from '../components/detalhe/ObraHeader.jsx';
import ClimaDiaCard from '../components/detalhe/ClimaDiaCard.jsx';
import { KpiGrid } from '../components/detalhe/KpiCard.jsx';
import EquipeResumo from '../components/detalhe/EquipeResumo.jsx';
import MuralFotos from '../components/detalhe/MuralFotos.jsx';
import OcorrenciasRecentes from '../components/detalhe/OcorrenciasRecentes.jsx';

function mensagemFalha(erro) {
  if (erro?.mensagem) return erro.mensagem;
  const status = erro?.response?.status;
  if (status === 403) return 'Você não tem permissão para visualizar esta obra.';
  if (status === 404) return 'Obra não encontrada.';
  return mensagemErro(erro);
}

export default function DetalheObraPage() {
  const { obraId } = useParams();
  const { pode } = usePermissao(obraId);
  const { obra, equipe, carregando, erro, recarregar } = useDetalheObra(obraId);
  const { diario, erro: erroDiario, recarregar: recarregarDiario } = useDiario(obraId);
  const dadosDiario = paraDiario(diario);

  const casca = 'space-y-6 rounded-2xl bg-[#111111] p-4 font-sans text-zinc-200 md:p-6';

  if (carregando) {
    return (
      <div className={casca}>
        <h1 className="sr-only">Detalhe de Obra</h1>
        <Spinner className="py-16" />
      </div>
    );
  }

  if (erro) {
    const status = erro?.response?.status;
    const podeRepetir = !erro?.mensagem && ![400, 403, 404].includes(status);
    return (
      <div className={casca}>
        <h1 className="sr-only">Detalhe de Obra</h1>
        <ErrorState mensagem={mensagemFalha(erro)} onTentarNovamente={podeRepetir ? recarregar : undefined} />
      </div>
    );
  }

  return (
    <div className={casca}>
      <h1 className="sr-only">Detalhe de Obra</h1>
      <ObraHeader obra={obra} pode={pode} />
      <ClimaDiaCard clima={paraClimaDia(diario)} />
      {erroDiario && (
        <div role="alert" className="flex items-center justify-between gap-3 rounded-lg border border-red-500/40 bg-red-500/10 p-3 text-xs text-red-300">
          <span>Não foi possível carregar o diário desta obra.</span>
          <button type="button" onClick={recarregarDiario} className="min-h-[44px] px-2 font-semibold text-red-200">
            Tentar novamente
          </button>
        </div>
      )}
      <KpiGrid kpis={KPIS} />

      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-[1fr_400px]">
        <div className="space-y-6">
          <EquipeResumo equipe={equipe} />
          <OcorrenciasRecentes ocorrencias={dadosDiario?.ocorrencias ?? []} />
        </div>
        <MuralFotos obraId={obraId} fotos={dadosDiario?.fotos ?? []} />
      </div>
    </div>
  );
}
