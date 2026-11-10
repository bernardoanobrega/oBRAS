import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth.js';
import Spinner from '../components/ui/Spinner.jsx';

/** Exige usuário autenticado; senão manda para /login guardando o destino. */
export default function RotaPrivada() {
  const { usuario, iniciando } = useAuth();
  const local = useLocation();

  if (iniciando) return <Spinner className="mt-24" />;
  if (!usuario) return <Navigate to="/login" replace state={{ de: local.pathname }} />;
  return <Outlet />;
}
