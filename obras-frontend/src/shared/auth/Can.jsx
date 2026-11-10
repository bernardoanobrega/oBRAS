import { usePermissao } from './usePermissao.js';

/** Renderiza os filhos só se o usuário tiver a permissão. <Can modulo="ESTOQUE" acao="CRIAR" obraId={1}> */
export default function Can({ modulo, acao, obraId = null, children }) {
  const { pode } = usePermissao(obraId);
  return pode(modulo, acao) ? children : null;
}
