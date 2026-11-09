package br.com.obras.shared.security;

import java.util.List;

/** Implementada pelo módulo obra: evita ciclo usuario <-> obra. */
public interface ContextoAlocacaoPort {
    /** Ids dos cargos da alocação ATIVA (data_fim nula) do usuário na obra. */
    List<Long> cargoIdsAtivos(Long usuarioId, Long obraId);
}
