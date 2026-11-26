import { formatarDataIso } from '../../../shared/utils/formatadores.js';
import { sigla, tomEquipe } from './adaptadores.js';

/** AlocacaoDto[] (L22) → linhas da tabela de equipe. */
export function paraAlocacoes(dtos) {
  if (!Array.isArray(dtos)) return [];
  return dtos.map((a) => ({
    id: a.id,
    usuarioId: a.usuarioId,
    nome: a.nome ?? '—',
    sigla: sigla(a.nome),
    tom: tomEquipe(a.usuarioId),
    cargos: Array.isArray(a.cargos) ? a.cargos : [],
    desde: formatarDataIso(a.dataInicio),
  }));
}

/**
 * Responsável técnico para o cabeçalho da equipe (ADR-05 e ADR-20).
 * O nome vem do ObraDto (responsavelNome, PENDENTE BACKEND); se não vier, tenta achar na equipe.
 * A ART é texto da obra (ADR-20), não do usuário.
 */
export function responsavelTecnico(obraDto, alocacoes) {
  const art = obraDto?.art ? String(obraDto.art) : null;
  const id = obraDto?.responsavelId ?? null;
  if (id == null) return { definido: false, nome: null, naEquipe: false, art };

  const naEquipe = (alocacoes ?? []).find((a) => a.usuarioId === id);
  const nome = obraDto.responsavelNome ?? naEquipe?.nome ?? null;
  return { definido: true, nome, naEquipe: !!naEquipe, art };
}
