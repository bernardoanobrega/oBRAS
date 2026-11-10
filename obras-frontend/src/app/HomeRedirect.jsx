import { Navigate } from 'react-router-dom';
import { usePermissao } from '../shared/auth/usePermissao.js';
import Spinner from '../shared/components/ui/Spinner.jsx';

/** Administrador/Diretoria cai no Dashboard; os demais, nas obras. */
export default function HomeRedirect() {
  const { pode, carregando } = usePermissao(null);
  if (carregando) return <Spinner className="mt-16" />;
  return <Navigate to={pode('DASHBOARD', 'VISUALIZAR') ? '/dashboard' : '/obras'} replace />;
}
