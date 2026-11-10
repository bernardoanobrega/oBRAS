import { Outlet, useParams } from 'react-router-dom';
import { usePermissao } from './usePermissao.js';
import Spinner from '../components/ui/Spinner.jsx';
import SemPermissao from '../components/feedback/SemPermissao.jsx';

/**
 * Guard de rota. escopo="global" usa permissões globais; escopo="obra" usa :obraId da URL.
 */
export default function RotaProtegida({ modulo, acao = 'VISUALIZAR', escopo = 'global' }) {
  const { obraId } = useParams();
  const { pode, carregando } = usePermissao(escopo === 'obra' ? obraId : null);

  if (carregando) return <Spinner className="mt-16" />;
  if (!pode(modulo, acao)) return <SemPermissao />;
  return <Outlet />;
}
