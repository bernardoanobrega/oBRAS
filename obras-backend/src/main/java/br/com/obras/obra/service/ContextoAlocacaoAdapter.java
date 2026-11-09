package br.com.obras.obra.service;

import br.com.obras.shared.security.ContextoAlocacaoPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementa o port de shared para o módulo usuario não depender de obra.
 * Troque o SQL por AlocacaoObraRepository quando a entidade AlocacaoObra existir.
 */
@Component
public class ContextoAlocacaoAdapter implements ContextoAlocacaoPort {
    private final JdbcClient jdbc;

    public ContextoAlocacaoAdapter(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Long> cargoIdsAtivos(Long usuarioId, Long obraId) {
        return jdbc.sql("""
                SELECT DISTINCT aoc.cargo_id
                  FROM alocacao_obra ao
                  JOIN alocacao_obra_cargo aoc ON aoc.alocacao_obra_id = ao.id
                 WHERE ao.usuario_id = :usuario AND ao.obra_id = :obra AND ao.data_fim IS NULL
                """)
                .param("usuario", usuarioId)
                .param("obra", obraId)
                .query(Long.class)
                .list();
    }
}
