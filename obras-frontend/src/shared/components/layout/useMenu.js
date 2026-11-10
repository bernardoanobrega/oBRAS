import { ITENS_MENU } from '../../../app/nav.js';
import { usePermissao } from '../../auth/usePermissao.js';

export function useMenu() {
  const { pode } = usePermissao(null);
  return ITENS_MENU.filter((i) => i.modulo === null || pode(i.modulo, 'VISUALIZAR'));
}
