import Badge from '../../../shared/components/ui/Badge.jsx';
import { situacao } from '../utils/orcamento.js';

const COR_PONTO = {
  verde: 'bg-green-500',
  amarelo: 'bg-yellow-500',
  vermelho: 'bg-red-500',
  azul: 'bg-blue-500',
  cinza: 'bg-slate-400',
};

/** compacto: ponto colorido + texto (linhas da planilha); padrão: pílula (cabeçalhos). */
export default function SituacaoBadge({ codigo, compacto = false }) {
  const { rotulo, tom } = situacao(codigo);
  if (!compacto) return <Badge tom={tom}>{rotulo}</Badge>;
  return (
    <span className="inline-flex items-center gap-1.5 text-xs text-slate-700">
      <span className={`h-2 w-2 rounded-full ${COR_PONTO[tom]}`} aria-hidden="true" />
      {rotulo}
    </span>
  );
}
