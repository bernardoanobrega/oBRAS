import { createBrowserRouter } from 'react-router-dom';
import RotaPrivada from '../shared/auth/RotaPrivada.jsx';
import RotaProtegida from '../shared/auth/RotaProtegida.jsx';
import AppShell from '../shared/components/layout/AppShell.jsx';
import NotFound from '../shared/components/feedback/NotFound.jsx';
import HomeRedirect from './HomeRedirect.jsx';

import LoginPage from '../features/auth/pages/LoginPage.jsx';
import UsuariosPage from '../features/usuarios/pages/UsuariosPage.jsx';
import DashboardPage from '../features/dashboard/pages/DashboardPage.jsx';
import ObrasPage from '../features/obras/pages/ObrasPage.jsx';
import ObraLayout from '../features/obras/pages/ObraLayout.jsx';
import IndexObra from '../features/obras/pages/IndexObra.jsx';
import ResumoObraPage from '../features/obras/pages/ResumoObraPage.jsx';
import EquipeObraPage from '../features/obras/pages/EquipeObraPage.jsx';
import DiarioPage from '../features/obras/pages/DiarioPage.jsx';
import CronogramaPage from '../features/cronograma/pages/CronogramaPage.jsx';
import OrcamentoPage from '../features/orcamento/pages/OrcamentoPage.jsx';
import ComprasPage from '../features/compras/pages/ComprasPage.jsx';
import FornecedoresPage from '../features/compras/pages/FornecedoresPage.jsx';
import EstoquePage from '../features/estoque/pages/EstoquePage.jsx';
import MateriaisPage from '../features/estoque/pages/MateriaisPage.jsx';
import EquipamentosObraPage from '../features/equipamentos/pages/EquipamentosObraPage.jsx';
import EquipamentosPage from '../features/equipamentos/pages/EquipamentosPage.jsx';
import DocumentosPage from '../features/documentos/pages/DocumentosPage.jsx';
import PrestadoresPage from '../features/documentos/pages/PrestadoresPage.jsx';

// Guard global: permissão individual no escopo sem obra.
const global = (modulo, element) => ({
  element: <RotaProtegida modulo={modulo} />,
  children: [{ index: true, element }],
});

// Guard por obra: usa :obraId da URL.
const daObra = (path, modulo, element) => ({
  path,
  element: <RotaProtegida modulo={modulo} escopo="obra" />,
  children: [{ index: true, element }],
});

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    element: <RotaPrivada />,
    children: [
      {
        element: <AppShell />,
        children: [
          { index: true, element: <HomeRedirect /> },
          { path: 'dashboard', ...global('DASHBOARD', <DashboardPage />) },
          { path: 'usuarios', ...global('USUARIOS', <UsuariosPage />) },
          { path: 'fornecedores', ...global('COMPRAS_FORNECEDORES', <FornecedoresPage />) },
          { path: 'materiais', ...global('ESTOQUE', <MateriaisPage />) },
          { path: 'equipamentos', ...global('MAQUINAS_EQUIPAMENTOS', <EquipamentosPage />) },
          { path: 'prestadores', ...global('CONTRATOS_DOCUMENTOS', <PrestadoresPage />) },
          { path: 'obras', element: <ObrasPage /> },
          {
            path: 'obras/:obraId',
            element: <ObraLayout />,
            children: [
              { index: true, element: <IndexObra /> },
              daObra('resumo', 'DETALHES_OBRA', <ResumoObraPage />),
              daObra('equipe', 'DETALHES_OBRA', <EquipeObraPage />),
              daObra('diario', 'DETALHES_OBRA', <DiarioPage />),
              daObra('cronograma', 'CRONOGRAMA', <CronogramaPage />),
              daObra('orcamento', 'ORCAMENTO', <OrcamentoPage />),
              daObra('compras', 'COMPRAS_FORNECEDORES', <ComprasPage />),
              daObra('estoque', 'ESTOQUE', <EstoquePage />),
              daObra('equipamentos', 'MAQUINAS_EQUIPAMENTOS', <EquipamentosObraPage />),
              daObra('documentos', 'CONTRATOS_DOCUMENTOS', <DocumentosPage />),
            ],
          },
          { path: '*', element: <NotFound /> },
        ],
      },
    ],
  },
]);
