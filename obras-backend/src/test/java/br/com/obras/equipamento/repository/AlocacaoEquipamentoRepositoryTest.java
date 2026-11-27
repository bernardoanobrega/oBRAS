package br.com.obras.equipamento.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * As chaves estrangeiras para usuário e canteiro são suspensas na transação do teste
 * (session_replication_role), isolando as regras deste módulo.
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AlocacaoEquipamentoRepositoryTest {

    private static final Long CANTEIRO_A = 100L;
    private static final Long CANTEIRO_B = 200L;
    private static final Long RESPONSAVEL = 5L;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private final AlocacaoEquipamentoRepository repository;
    private final TestEntityManager entityManager;

    @Autowired
    AlocacaoEquipamentoRepositoryTest(AlocacaoEquipamentoRepository repository, TestEntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void suspenderChavesEstrangeirasNaTransacao() {
        entityManager.getEntityManager()
                .createNativeQuery("SET LOCAL session_replication_role = replica")
                .executeUpdate();
    }

    private Equipamento persistirEquipamento(String codigo) {
        return entityManager.persistAndFlush(new Equipamento(codigo, "Equipamento " + codigo, TipoEquipamento.MAQUINA));
    }

    private AlocacaoEquipamento alocar(Equipamento equipamento, Long canteiroId, long diasAteDevolucao) {
        LocalDate dataPrevista = LocalDate.now().plusDays(diasAteDevolucao);
        return entityManager.persistAndFlush(equipamento.alocar(canteiroId, dataPrevista, RESPONSAVEL, null));
    }

    @Test
    @DisplayName("Lista o histórico do equipamento da alocação mais recente para a mais antiga")
    void listaHistoricoDaMaisRecenteParaAMaisAntiga() {
        Equipamento equipamento = persistirEquipamento("PAT-001");
        AlocacaoEquipamento primeira = alocar(equipamento, CANTEIRO_A, 5);
        equipamento.devolver("Sem avarias");
        entityManager.flush();
        AlocacaoEquipamento segunda = alocar(equipamento, CANTEIRO_B, 5);
        entityManager.clear();

        List<AlocacaoEquipamento> historico =
                repository.findByEquipamentoIdOrderByDataInicioDescIdDesc(equipamento.getId());

        assertThat(historico).extracting(AlocacaoEquipamento::getId)
                .containsExactly(segunda.getId(), primeira.getId());
        assertThat(historico.get(1).estaAtiva()).isFalse();
    }

    @Test
    @DisplayName("Lista apenas alocações ativas dos canteiros informados, por data prevista")
    void listaApenasAtivasDosCanteirosInformadosPorDataPrevista() {
        Equipamento tardio = persistirEquipamento("PAT-001");
        Equipamento cedo = persistirEquipamento("PAT-002");
        Equipamento emOutroCanteiro = persistirEquipamento("PAT-003");
        Equipamento devolvido = persistirEquipamento("PAT-004");
        alocar(tardio, CANTEIRO_A, 10);
        alocar(cedo, CANTEIRO_A, 3);
        alocar(emOutroCanteiro, CANTEIRO_B, 5);
        alocar(devolvido, CANTEIRO_A, 5);
        devolvido.devolver(null);
        entityManager.flush();
        entityManager.clear();

        List<AlocacaoEquipamento> doCanteiroA = repository.listarAtivasPorCanteiros(List.of(CANTEIRO_A));
        List<AlocacaoEquipamento> dosDoisCanteiros =
                repository.listarAtivasPorCanteiros(List.of(CANTEIRO_A, CANTEIRO_B));

        assertThat(doCanteiroA).extracting(a -> a.getEquipamento().getCodigoPatrimonio())
                .containsExactly("PAT-002", "PAT-001");
        assertThat(dosDoisCanteiros).hasSize(3);
        assertThat(Hibernate.isInitialized(doCanteiroA.get(0).getEquipamento())).isTrue();
    }

    @Test
    @DisplayName("Retorna lista vazia quando não há canteiros informados")
    void retornaListaVaziaQuandoNaoHaCanteiros() {
        alocar(persistirEquipamento("PAT-001"), CANTEIRO_A, 5);

        List<AlocacaoEquipamento> ativas = repository.listarAtivasPorCanteiros(List.of());

        assertThat(ativas).isEmpty();
    }

    @Test
    @DisplayName("Persiste a transferência quando o encerramento é gravado antes da nova alocação")
    void persisteTransferenciaQuandoEncerramentoEGravadoAntesDaNovaAlocacao() {
        Equipamento equipamento = persistirEquipamento("PAT-001");
        alocar(equipamento, CANTEIRO_A, 5);

        AlocacaoEquipamento nova = equipamento.transferir(
                CANTEIRO_B, LocalDate.now().plusDays(14), RESPONSAVEL, null);
        entityManager.flush();
        entityManager.persistAndFlush(nova);
        entityManager.clear();

        assertThat(repository.listarAtivasPorCanteiros(List.of(CANTEIRO_A))).isEmpty();
        assertThat(repository.listarAtivasPorCanteiros(List.of(CANTEIRO_B))).hasSize(1);
        assertThat(repository.findByEquipamentoIdOrderByDataInicioDescIdDesc(equipamento.getId())).hasSize(2);
    }
}