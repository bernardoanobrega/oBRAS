package br.com.obras.orcamento.repository;

import br.com.obras.orcamento.domain.ItemOrcado;
import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.OrigemCusto;
import br.com.obras.orcamento.domain.StatusOrcamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Valida a migration de orçamento e o mapeamento JPA contra um Postgres real (ddl-auto: validate). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OrcamentoRepositoryTest {
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrcamentoRepository orcamentos;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    private Long obraId;
    private Long usuarioId;

    @BeforeEach
    void inserirObraEUsuario() {
        usuarioId = jdbc.queryForObject("INSERT INTO usuario (nome, situacao, login, senha_hash) "
                + "VALUES ('Eng', 'ATIVO', 'eng', 'hash') RETURNING id", Long.class);
        obraId = jdbc.queryForObject("INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto, "
                + "status, responsavel_id) VALUES ('Obra', 'RESIDENCIAL', 'Rua A', DATE '2026-01-01', "
                + "DATE '2026-12-31', 'PLANEJADA', ?) RETURNING id", Long.class, usuarioId);
    }

    @Test
    void persisteOrcamentoComItensCustosEDecisoes() {
        Orcamento orcamento = Orcamento.elaborar(obraId, LIMITE_ALERTA);
        ItemOrcado item = orcamento.adicionarItem("01.01", "Fundação", "m3", new BigDecimal("12.5"),
                new BigDecimal("100"));
        item.registrarCusto("Concreto", new BigDecimal("1300"), OrigemCusto.MATERIAL, 10L, usuarioId);
        orcamento.registrarDecisao("Aditivo", "Reajuste", new BigDecimal("100"), usuarioId);
        orcamentos.saveAndFlush(orcamento);
        entityManager.clear();

        Orcamento salvo = orcamentos.findByObraId(obraId).orElseThrow();

        assertEquals(StatusOrcamento.ATENCAO, salvo.getStatus());
        assertEquals(new BigDecimal("1350.00"), salvo.calcularTotalOrcado());
        assertEquals(new BigDecimal("1300.00"), salvo.calcularTotalRealizado());
        assertEquals(1, salvo.getItens().get(0).getCustos().size());
    }

    @Test
    void encontraOrcamentoPeloIdDoItem() {
        Orcamento orcamento = Orcamento.elaborar(obraId, LIMITE_ALERTA);
        ItemOrcado item = orcamento.adicionarItem("01.01", "Fundação", "m3", BigDecimal.ONE, BigDecimal.TEN);
        orcamentos.saveAndFlush(orcamento);
        entityManager.clear();

        Orcamento encontrado = orcamentos.findByItensId(item.getId()).orElseThrow();

        assertEquals(obraId, encontrado.getObraId());
        assertTrue(orcamentos.findByItensId(item.getId() + 1).isEmpty());
    }

    @Test
    void lancaExcecaoQuandoObraJaPossuiOrcamento() {
        orcamentos.saveAndFlush(Orcamento.elaborar(obraId, LIMITE_ALERTA));

        assertThrows(DataIntegrityViolationException.class,
                () -> orcamentos.saveAndFlush(Orcamento.elaborar(obraId, LIMITE_ALERTA)));
    }
}
