import { useParams } from 'react-router-dom';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import { OBRA, CLIMA_DIA, KPIS, EQUIPE, OCORRENCIAS, FOTOS } from '../mocks/detalheObraMock.js';
import ObraHeader from '../components/detalhe/ObraHeader.jsx';
import ClimaDiaCard from '../components/detalhe/ClimaDiaCard.jsx';
import { KpiGrid } from '../components/detalhe/KpiCard.jsx';
import EquipeResumo from '../components/detalhe/EquipeResumo.jsx';
import OcorrenciasRecentes from '../components/detalhe/OcorrenciasRecentes.jsx';
import MuralFotos from '../components/detalhe/MuralFotos.jsx';

/**
 * Painel Detalhes de Obra (Plano de Projeto: módulo central de rotinas diárias e informações gerais).
 * Fatia L43+L44: renderiza dentro de AppShell/ObraLayout, sem Sidebar/Topbar próprios, com dados mockados.
 * Ações são visuais e condicionadas por permissão; nenhuma chamada de API nesta fatia.
 */
export default function DetalheObraPage() {
  const { obraId } = useParams();
  const { pode } = usePermissao(obraId);

  return (
    <div className="space-y-6 rounded-2xl bg-[#111111] p-4 font-sans text-zinc-200 md:p-6">
      <h1 className="sr-only">Detalhe de Obra</h1>
      <ObraHeader obra={OBRA} pode={pode} />
      <ClimaDiaCard clima={CLIMA_DIA} />
      <KpiGrid kpis={KPIS} />

      <div className="grid grid-cols-1 items-start gap-6 xl:grid-cols-[1fr_400px]">
        <div className="space-y-6">
          <EquipeResumo equipe={EQUIPE} />
          <OcorrenciasRecentes ocorrencias={OCORRENCIAS} />
        </div>
        <MuralFotos fotos={FOTOS} />
      </div>
    </div>
  );
}
