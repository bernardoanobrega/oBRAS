package br.com.obras.obra.repository;

import br.com.obras.obra.domain.Obra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Obra excluída logicamente (auditável marcado) não é listada nem encontrada. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ObraRepositoryTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private ObraRepository obras;
    @Autowired private JdbcTemplate jdbc;

    private long responsavel;

    @BeforeEach
    void setup() {
        responsavel = jdbc.queryForObject("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Resp', 'ATIVO', 'resp-' || gen_random_uuid(), 'hash') RETURNING id
                """, Long.class);
    }

    @Test
    void buscarAtivaNaoEncontraObraExcluida() {
        long ativa = obra("Ativa", false);
        long excluida = obra("Excluída", true);

        assertAll(
                () -> assertTrue(obras.buscarAtiva(ativa).isPresent()),
                () -> assertTrue(obras.buscarAtiva(excluida).isEmpty())
        );
    }

    @Test
    void listarAtivasIgnoraExcluidaEOrdenaPorNome() {
        obra("Bloco B", false);
        obra("Bloco A", false);
        obra("Bloco C", true);

        assertEquals(List.of("Bloco A", "Bloco B"), obras.listarAtivas().stream().map(Obra::getNome).toList());
    }

    private long obra(String nome, boolean excluida) {
        long auditavel = jdbc.queryForObject(
                "INSERT INTO auditavel (excluido) VALUES (?) RETURNING id", Long.class, excluida);
        return jdbc.queryForObject("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, Long.class, nome, responsavel, auditavel);
    }
}
