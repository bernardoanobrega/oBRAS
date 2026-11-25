import {
  BriefcaseBusiness,
  Building2,
  Calculator,
  CalendarDays,
  FileText,
  LayoutGrid,
  Package,
  Settings,
  ShoppingCart,
  Users,
  Wrench,
} from 'lucide-react';

// Ordem e rótulos do menu do protótipo do Figma.
// modulo === null => sempre visível a qualquer usuário autenticado.
// Itens com modulo exigem permissão GLOBAL VISUALIZAR (individual).
// Cronograma, Orçamento e Documentos são por obra: o item aparece para todos e a página trata as obras
// permitidas. Prestadores não está no Figma; fica junto de Documentos (mesmo módulo).
export const ITENS_MENU = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutGrid, modulo: 'DASHBOARD' },
  { to: '/obras', label: 'Detalhe de Obra', icon: Building2, modulo: null },
  { to: '/cronograma', label: 'Cronograma', icon: CalendarDays, modulo: null },
  { to: '/orcamento', label: 'Orçamento', icon: Calculator, modulo: null },
  { to: '/materiais', label: 'Estoque', icon: Package, modulo: 'ESTOQUE' },
  { to: '/fornecedores', label: 'Compras', icon: ShoppingCart, modulo: 'COMPRAS_FORNECEDORES' },
  { to: '/equipamentos', label: 'Equipamentos', icon: Wrench, modulo: 'MAQUINAS_EQUIPAMENTOS' },
  { to: '/documentos', label: 'Documentos', icon: FileText, modulo: null },
  {
    to: '/prestadores',
    label: 'Prestadores',
    icon: BriefcaseBusiness,
    modulo: 'CONTRATOS_DOCUMENTOS',
  },
  { to: '/usuarios', label: 'Usuários', icon: Users, modulo: 'USUARIOS' },
  { to: '/configuracoes', label: 'Configurações', icon: Settings, modulo: null },
];

// Abas do painel da obra: path, rótulo e módulo exigido (permissão da obra).
export const ABAS_OBRA = [
  { path: 'resumo', label: 'Resumo', modulo: 'DETALHES_OBRA' },
  { path: 'equipe', label: 'Equipe', modulo: 'DETALHES_OBRA' },
  { path: 'diario', label: 'Diário', modulo: 'DETALHES_OBRA' },
  { path: 'cronograma', label: 'Cronograma', modulo: 'CRONOGRAMA' },
  { path: 'orcamento', label: 'Orçamento', modulo: 'ORCAMENTO' },
  { path: 'compras', label: 'Compras', modulo: 'COMPRAS_FORNECEDORES' },
  { path: 'estoque', label: 'Estoque', modulo: 'ESTOQUE' },
  { path: 'equipamentos', label: 'Equipamentos', modulo: 'MAQUINAS_EQUIPAMENTOS' },
  { path: 'documentos', label: 'Documentos', modulo: 'CONTRATOS_DOCUMENTOS' },
];

/** Título do cabeçalho: rótulo do item do menu cuja rota contém a página atual. */
export function tituloDaRota(pathname) {
  const item = ITENS_MENU.find(({ to }) => pathname === to || pathname.startsWith(`${to}/`));
  return item?.label ?? 'oBRAS';
}
