import { useParams } from 'react-router-dom';
import PainelOrcamento from '../components/PainelOrcamento.jsx';

/** Aba "Orçamento" dentro do painel da obra (UC10). */
export default function OrcamentoPage() {
  const { obraId } = useParams();
  return <PainelOrcamento key={obraId} obraId={obraId} />;
}
