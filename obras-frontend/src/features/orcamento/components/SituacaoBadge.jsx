import Badge from '../../../shared/components/ui/Badge.jsx';
import { situacao } from '../utils/orcamento.js';

const COR_PONTO = {
  verde: 'bg-sucesso',
  amarelo: 'bg-brand',
  vermelho: 'bg-perigo-forte',
  azul: 'bg-info',
  cinza: 'bg-neutro',
};

/** compacto: ponto colorido + texto (linhas da planilha); padrão: pílula (cabeçalhos). */
export default function SituacaoBadge({ codigo, compacto = false }) {
  const { rotulo, tom } = situacao(codigo);
  if (!compacto) return <Badge tom={tom}>{rotulo}</Badge>;
  return (
    <span className="inline-flex items-center gap-1.5 text-xs text-texto">
      <span className={`h-2 w-2 rounded-full ${COR_PONTO[tom]}`} aria-hidden="true" />
      {rotulo}
    </span>
  );
}
