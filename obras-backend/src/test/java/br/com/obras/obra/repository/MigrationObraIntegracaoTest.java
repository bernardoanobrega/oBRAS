package br.com.obras.obra.repository;

import br.com.obras.AbstractPostgresIntegrationTest;
import br.com.obras.obra.mapper.ObraMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ObraMapper.class)
class MigrationObraIntegracaoTest extends AbstractPostgresIntegrationTest {
    @Autowired private JdbcTemplate jdbc;

    @Test
    void migrationV3AplicaDoZero() {
        assertEquals(1L, colunaExiste("obra", "auditavel_id"));
        assertEquals(1L, colunaExiste("auditavel", "id"));
        assertEquals(1L, tabelaExiste("registro_auditoria"));
    }

    private long colunaExiste(String tabela, String coluna) {
        return jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_name = ? AND column_name = ?
                """, Long.class, tabela, coluna);
    }

    private long tabelaExiste(String tabela) {
        return jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_name = ?
                """, Long.class, tabela);
    }
}
