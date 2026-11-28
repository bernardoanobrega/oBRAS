package br.com.obras.equipamento.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Valida, em um Postgres real, as restrições criadas pela migration de equipamento.
 * As chaves estrangeiras são suspensas na transação do teste (session_replication_role) para
 * isolar as regras das tabelas deste módulo, sem depender dos dados de usuário, obra e auditoria.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MigracaoEquipamentoTest {

    private static final long EQUIPAMENTO_ID = 1L;
    private static final long OBRA_ID = 100L;
    private static final long RESPONSAVEL_ID = 200L;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private final JdbcTemplate jdbc;

    @Autowired
    MigracaoEquipamentoTest(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @BeforeEach
    void suspenderChavesEstrangeirasNaTransacao() {
        jdbc.execute("SET LOCAL session_replication_role = replica");
    }

    @Test
    @DisplayName("Aceita equipamento e alocação com dados válidos")
    void aceitaEquipamentoEAlocacaoValidos() {
        assertThatCode(() -> {
            inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "ALOCADO");
            inserirAlocacao(10L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(7), null, null);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Rejeita código de patrimônio duplicado")
    void rejeitaCodigoPatrimonioDuplicado() {
        inserirEquipamento(1L, "PAT-001", "MAQUINA", "DISPONIVEL");

        assertThatThrownBy(() -> inserirEquipamento(2L, "PAT-001", "FERRAMENTA", "DISPONIVEL"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_equipamento_codigo_patrimonio");
    }

    @Test
    @DisplayName("Rejeita tipo de equipamento inválido")
    void rejeitaTipoInvalido() {
        assertThatThrownBy(() -> inserirEquipamento(1L, "PAT-001", "VEICULO", "DISPONIVEL"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_equipamento_tipo");
    }

    @Test
    @DisplayName("Rejeita status fora de disponível, alocado e inativo")
    void rejeitaStatusInvalido() {
        assertThatThrownBy(() -> inserirEquipamento(1L, "PAT-001", "MAQUINA", "EM_MANUTENCAO"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_equipamento_status");
    }

    @Test
    @DisplayName("Rejeita data prevista de devolução anterior ao início")
    void rejeitaPrevisaoAnteriorAoInicio() {
        inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "ALOCADO");

        assertThatThrownBy(() -> inserirAlocacao(10L, EQUIPAMENTO_ID, hoje(), hoje().minusDays(1), null, null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_alocacao_equipamento_prevista");
    }

    @Test
    @DisplayName("Rejeita data de fim anterior ao início")
    void rejeitaFimAnteriorAoInicio() {
        inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "DISPONIVEL");

        assertThatThrownBy(() -> inserirAlocacao(10L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(7),
                hoje().minusDays(1), null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_alocacao_equipamento_fim");
    }

    @Test
    @DisplayName("Rejeita observação de devolução em alocação ainda ativa")
    void rejeitaObservacaoDevolucaoSemDataFim() {
        inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "ALOCADO");

        assertThatThrownBy(() -> inserirAlocacao(10L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(7), null, "Sem avarias"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_alocacao_equipamento_observacao_devolucao");
    }

    @Test
    @DisplayName("Rejeita segunda alocação ativa para o mesmo equipamento")
    void rejeitaSegundaAlocacaoAtivaDoMesmoEquipamento() {
        inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "ALOCADO");
        inserirAlocacao(10L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(7), null, null);

        assertThatThrownBy(() -> inserirAlocacao(11L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(14), null, null))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_alocacao_equipamento_ativa");
    }

    @Test
    @DisplayName("Aceita nova alocação ativa depois que a anterior foi encerrada")
    void aceitaNovaAlocacaoAtivaAposEncerramentoDaAnterior() {
        inserirEquipamento(EQUIPAMENTO_ID, "PAT-001", "MAQUINA", "ALOCADO");
        inserirAlocacao(10L, EQUIPAMENTO_ID, hoje().minusDays(10), hoje().minusDays(3), hoje().minusDays(2), "Ok");

        assertThatCode(() -> inserirAlocacao(11L, EQUIPAMENTO_ID, hoje(), hoje().plusDays(7), null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cria os índices das chaves estrangeiras e do status")
    void criaIndicesDasChavesEstrangeiras() {
        List<String> indices = jdbc.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename IN ('equipamento', 'alocacao_equipamento')",
                String.class);

        assertThat(indices).contains(
                "ix_equipamento_status",
                "ix_alocacao_equipamento_equipamento",
                "ix_alocacao_equipamento_obra",
                "ix_alocacao_equipamento_responsavel",
                "uk_alocacao_equipamento_ativa");
    }

    private static LocalDate hoje() {
        return LocalDate.now();
    }

    private void inserirEquipamento(long id, String codigoPatrimonio, String tipo, String status) {
        jdbc.update("INSERT INTO equipamento (id, codigo_patrimonio, descricao, tipo, status) VALUES (?, ?, ?, ?, ?)",
                id, codigoPatrimonio, "Equipamento de teste", tipo, status);
    }

    private void inserirAlocacao(long id, long equipamentoId, LocalDate dataInicio, LocalDate dataPrevista,
            LocalDate dataFim, String observacaoDevolucao) {
        jdbc.update("""
                INSERT INTO alocacao_equipamento (id, equipamento_id, obra_id, responsavel_id, data_inicio,
                    data_prevista_devolucao, data_fim, observacao_devolucao)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, equipamentoId, OBRA_ID, RESPONSAVEL_ID, dataInicio, dataPrevista, dataFim,
                observacaoDevolucao);
    }
}