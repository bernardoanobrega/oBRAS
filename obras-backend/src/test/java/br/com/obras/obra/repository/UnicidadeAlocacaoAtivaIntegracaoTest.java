package br.com.obras.obra.repository;

import br.com.obras.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UnicidadeAlocacaoAtivaIntegracaoTest extends AbstractPostgresIntegrationTest {
    @Autowired private JdbcTemplate jdbc;

    @Test
    void indiceUnicoDeAlocacaoAtivaExiste() {
        Integer indiceExiste = jdbc.queryForObject("""
                SELECT count(*) FROM pg_indexes
                WHERE schemaname = current_schema() AND indexname = 'uk_alocacao_ativa'
                """, Integer.class);

        assertEquals(1, indiceExiste);
    }
}
