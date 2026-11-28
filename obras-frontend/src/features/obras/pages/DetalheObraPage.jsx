import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import Can from '../../../shared/auth/Can.jsx';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import {
  adaptarClimaPainel,
  adaptarEquipePainel,
  adaptarFotosPainel,
  adaptarOcorrenciasPainel,
} from '../adapters/adaptarSecoesPainel.js';
import { paraHeader } from '../api/adaptadores.js';
import { usePainelObra } from '../hooks/usePainelObra.js';
import EditarParametrosModal from '../components/EditarParametrosModal.jsx';
import ObraAcoesMenu from '../components/ObraAcoesMenu.jsx';
import ClimaDiaCard from '../components/detalhe/ClimaDiaCard.jsx';
import EquipeResumo from '../components/detalhe/EquipeResumo.jsx';
import { KpiGrid } from '../components/detalhe/KpiCard.jsx';
import MuralFotos from '../components/detalhe/MuralFotos.jsx';
import ObraHeader from '../components/detalhe/ObraHeader.jsx';
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
  const { painel, carregando, erro, recarregar } = usePainelObra(obraId);
  const [editando, setEditando] = useState(false);

  const casca = 'space-y-6 rounded-2xl bg-fundo p-4 font-sans text-texto md:p-6';

  if (carregando) {
    return (
      <div className={casca}>
        <h1 className="sr-only">Detalhe de Obra</h1>
        <Spinner className="py-16" />
      </div>
    );
  }

  if (erro && !painel) {
    const status = erro?.response?.status;
    const podeRepetir = !erro?.mensagem && ![400, 403, 404].includes(status);
    return (
      <div className={casca}>
        <h1 className="sr-only">Detalhe de Obra</h1>
        <ErrorState mensagem={mensagemFalha(erro)} onTentarNovamente={podeRepetir ? recarregar : undefined} />
      </div>
    );
  }

  const obra = paraHeader(painel?.obra);
  const equipe = adaptarEquipePainel(painel?.equipe);
  const ocorrencias = adaptarOcorrenciasPainel(painel?.ocorrencias);
  const fotos = adaptarFotosPainel(painel);
  const clima = adaptarClimaPainel(painel?.clima);

  return (
    <div className={casca}>
      <h1 className="sr-only">Detalhe de Obra</h1>
      <ObraHeader
        obra={obra}
        pode={pode}
        acoes={
          <ObraAcoesMenu
            obra={painel?.obra}
            obraId={obraId}
            onAcao={recarregar}
            onEditar={() => setEditando(true)}
          />
        }
      />

      {erro && (
        <div role="alert" className="flex items-center justify-between gap-3 rounded-lg border border-perigo/40 bg-perigo-fundo p-3 text-xs text-perigo">
          <span>Não foi possível carregar o diário desta obra.</span>
          <button type="button" onClick={recarregar} className="min-h-[44px] px-2 font-semibold text-perigo">
            Tentar novamente
          </button>
        </div>
      )}

      {clima.semDiario && (
        <div role="status" className="rounded-lg border border-borda bg-superficie p-3 text-xs text-texto-fraco">
          Sem diário registrado para hoje. Equipe alocada continua disponível; presença e clima não foram medidos.
        </div>
      )}

      <ClimaDiaCard clima={clima} />
      <KpiGrid kpis={painel?.kpis ?? {}} maoDeObra={painel?.maoDeObra ?? null} />

      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-[1fr_400px]">
        <div className="space-y-6">
          <Can modulo="DETALHES_OBRA" acao="VISUALIZAR" obraId={obraId}>
            <Link to={`/obras/${obraId}/equipe`} className="inline-block text-xs font-semibold text-brand hover:text-brand-claro">
              Gerenciar equipe
            </Link>
          </Can>
          <EquipeResumo equipe={equipe} />
          <OcorrenciasRecentes ocorrencias={ocorrencias} />
        </div>
        <MuralFotos obraId={obraId} fotos={fotos} />
      </div>

      <EditarParametrosModal
        aberto={editando}
        obra={painel?.obra}
        obraId={obraId}
        onFechar={() => setEditando(false)}
        onSalvo={() => {
          setEditando(false);
          recarregar();
        }}
      />
    </div>
  );
}
