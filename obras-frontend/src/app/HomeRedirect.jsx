import { Navigate } from 'react-router-dom';
import { usePermissao } from '../shared/auth/usePermissao.js';
import Spinner from '../shared/components/ui/Spinner.jsx';

/** Direciona ao módulo de Estoque da Obra #1 como tela principal inicial. */
export default function HomeRedirect() {
  const { pode, carregando } = usePermissao(null);
  if (carregando) return <Spinner className="mt-16" />;
  if (pode('ESTOQUE', 'VISUALIZAR')) return <Navigate to="/obras/1/estoque" replace />;
  if (pode('COMPRAS_FORNECEDORES', 'VISUALIZAR')) return <Navigate to="/obras/1/compras" replace />;
  return <Navigate to="/obras" replace />;
}