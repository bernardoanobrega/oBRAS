package br.com.obras.obra.repository;

import br.com.obras.obra.domain.AlocacaoObra;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * As consultas de alocação usadas pelo diário devolvem o usuário já carregado
 * (join fetch), para o nome sair sem uma consulta por pessoa (N+1).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class AlocacaoObraRepositoryTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private AlocacaoObraRepository alocacoes;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager em;

    private long obra;
    private long outraObra;
    private long ana;
    private long bruno;

    @BeforeEach
    void setup() {
        ana = usuario("Ana");
        bruno = usuario("Bruno");
        obra = obra(ana);
        outraObra = obra(ana);
    }

    @Test
    void ativasPorIdsJaTrazemUsuarioCarregado() {
        long a1 = alocacao(obra, ana);
        long a2 = alocacao(obra, bruno);
        em.clear(); // garante que nada vem do cache da sessão: tudo sai da consulta

        List<AlocacaoObra> lista = alocacoes.findAtivasPorIds(obra, Set.of(a1, a2));

        assertAll(
                () -> assertEquals(2, lista.size()),
                () -> assertTrue(lista.stream().allMatch(a -> Hibernate.isInitialized(a.getUsuario())),
                        "usuário deve vir carregado, não como proxy vazio")
        );
    }

    @Test
    void ativasPorIdsContinuaFiltrandoObraEAtivas() {
        long ativa = alocacao(obra, ana);
        long encerrada = alocacao(obra, bruno);
        jdbc.update("UPDATE alocacao_obra SET data_fim = DATE '2026-06-30' WHERE id = ?", encerrada);
        long deOutraObra = alocacao(outraObra, bruno);
        em.clear();

        List<Long> ids = alocacoes.findAtivasPorIds(obra, Set.of(ativa, encerrada, deOutraObra)).stream()
                .map(AlocacaoObra::getId).toList();

        assertEquals(List.of(ativa), ids);
    }

    @Test
    void ativaPorUsuarioJaTrazUsuarioCarregado() {
        alocacao(obra, bruno);
        em.clear();

        AlocacaoObra autor = alocacoes.findAtivaPorUsuario(obra, bruno).orElseThrow();

        assertAll(
                () -> assertTrue(Hibernate.isInitialized(autor.getUsuario())),
                () -> assertEquals("Bruno", autor.getUsuario().getNome()),
                () -> assertTrue(alocacoes.findAtivaPorUsuario(outraObra, bruno).isEmpty(), "outra obra")
        );
    }

    @Test
    void repositoryListaAlocacoesDaObra() {
        long ativa = alocacao(obra, ana);
        long encerrada = alocacao(obra, bruno);
        jdbc.update("UPDATE alocacao_obra SET data_fim = DATE '2026-06-30' WHERE id = ?", encerrada);
        long deOutraObra = alocacao(outraObra, bruno);
        em.clear();

        List<Long> ids = alocacoes.listarPorObra(obra).stream()
                .map(AlocacaoObra::getId)
                .toList();

        assertEquals(List.of(ativa), ids);
        assertFalse(ids.contains(deOutraObra));
    }

    @Test
    void repositoryBuscaAlocacaoAtiva() {
        long ativa = alocacao(obra, ana);
        long encerrada = alocacao(obra, bruno);
        jdbc.update("UPDATE alocacao_obra SET data_fim = DATE '2026-06-30' WHERE id = ?", encerrada);
        em.clear();

        assertAll(
                () -> assertTrue(alocacoes.buscarAtiva(obra, ativa).isPresent()),
                () -> assertTrue(alocacoes.buscarAtiva(obra, encerrada).isEmpty()),
                () -> assertTrue(alocacoes.buscarAtiva(outraObra, ativa).isEmpty())
        );
    }

    private long usuario(String nome) {
        return id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES (?, 'ATIVO', ? || gen_random_uuid(), 'hash') RETURNING id
                """, nome, nome);
    }

    private long obra(long responsavel) {
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, responsavel, auditavel);
    }

    private long alocacao(long obraId, long usuarioId) {
        return id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obraId, usuarioId);
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
