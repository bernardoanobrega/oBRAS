import {
  Boxes,
  Building2,
  FileText,
  LayoutDashboard,
  Package,
  ShoppingCart,
  Truck,
  Users,
  Wrench,
} from 'lucide-react';

// modulo === null => sempre visível a qualquer usuário autenticado.
// Itens com modulo exigem permissão GLOBAL VISUALIZAR (individual).
export const ITENS_MENU = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, modulo: 'DASHBOARD' },
  { to: '/obras', label: 'Obras', icon: Building2, modulo: null },
  { to: '/obras/1/estoque', label: 'Estoque (Obra)', icon: Boxes, modulo: 'ESTOQUE' },
  { to: '/obras/1/compras', label: 'Compras (Kanban)', icon: ShoppingCart, modulo: 'COMPRAS_FORNECEDORES' },
  { to: '/materiais', label: 'Catálogo Materiais', icon: Package, modulo: 'ESTOQUE' },
  { to: '/fornecedores', label: 'Fornecedores', icon: Truck, modulo: 'COMPRAS_FORNECEDORES' },
  { to: '/equipamentos', label: 'Equipamentos', icon: Wrench, modulo: 'MAQUINAS_EQUIPAMENTOS' },
  { to: '/prestadores', label: 'Prestadores', icon: FileText, modulo: 'CONTRATOS_DOCUMENTOS' },
  { to: '/usuarios', label: 'Usuários', icon: Users, modulo: 'USUARIOS' },
];

// Abas do painel da obra: path, rótulo e módulo exigido (permissão da obra).
export const ABAS_OBRA = [
  { path: 'resumo', label: 'Resumo', modulo: 'DETALHES_OBRA' },
  { path: 'equipe', label: 'Equipe', modulo: 'DETALHES_OBRA' },
  { path: 'diario', label: 'Diário', modulo: 'DETALHES_OBRA' },
  { path: 'cronograma', label: 'Cronograma', modulo: 'CRONOGRAMA' },
  { path: 'orcamento', label: 'Orçamento', modulo: 'ORCAMENTO' },
  { path: 'estoque', label: 'Estoque', modulo: 'ESTOQUE' },
  { path: 'compras', label: 'Compras', modulo: 'COMPRAS_FORNECEDORES' },
  { path: 'equipamentos', label: 'Equipamentos', modulo: 'MAQUINAS_EQUIPAMENTOS' },
  { path: 'documentos', label: 'Documentos', modulo: 'CONTRATOS_DOCUMENTOS' },
];