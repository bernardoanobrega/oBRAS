package br.com.obras.equipamento.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Roda com ddl-auto=validate: se o mapeamento JPA divergir das migrations, o contexto não sobe. */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class EquipamentoRepositoryTest {

    private static final PageRequest PRIMEIRA_PAGINA = PageRequest.of(0, 10, Sort.by("codigoPatrimonio"));

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    private final EquipamentoRepository repository;
    private final TestEntityManager entityManager;

    @Autowired
    EquipamentoRepositoryTest(EquipamentoRepository repository, TestEntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    private Equipamento persistir(String codigo, String descricao, TipoEquipamento tipo) {
        return entityManager.persistAndFlush(new Equipamento(codigo, descricao, tipo));
    }

    @Test
    @DisplayName("Persiste equipamento com id gerado e o encontra pelo id")
    void persisteEquipamentoComIdGerado() {
        Equipamento salvo = persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        entityManager.clear();

        Optional<Equipamento> encontrado = repository.findById(salvo.getId());

        assertThat(salvo.getId()).isNotNull();
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getCodigoPatrimonio()).isEqualTo("PAT-001");
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("Informa se o código de patrimônio já existe")
    void informaSeCodigoPatrimonioJaExiste() {
        persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);

        assertThat(repository.existsByCodigoPatrimonio("PAT-001")).isTrue();
        assertThat(repository.existsByCodigoPatrimonio("PAT-999")).isFalse();
    }

    @Test
    @DisplayName("Busca todos os equipamentos quando não há filtros")
    void buscaTodosQuandoSemFiltros() {
        persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        persistir("PAT-002", "Furadeira de impacto", TipoEquipamento.FERRAMENTA);
        persistir("PAT-003", "Andaime fachadeiro", TipoEquipamento.OUTRO);

        Page<Equipamento> pagina = repository.buscar(null, null, "", PRIMEIRA_PAGINA);

        assertThat(pagina.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Filtra equipamentos por status")
    void filtraPorStatus() {
        persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        Equipamento inativo = persistir("PAT-002", "Furadeira", TipoEquipamento.FERRAMENTA);
        inativo.desativar();
        entityManager.flush();

        Page<Equipamento> pagina = repository.buscar(StatusEquipamento.INATIVO, null, "", PRIMEIRA_PAGINA);

        assertThat(pagina.getContent()).extracting(Equipamento::getCodigoPatrimonio).containsExactly("PAT-002");
    }

    @Test
    @DisplayName("Filtra equipamentos por tipo")
    void filtraPorTipo() {
        persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        persistir("PAT-002", "Furadeira", TipoEquipamento.FERRAMENTA);

        Page<Equipamento> pagina = repository.buscar(null, TipoEquipamento.FERRAMENTA, "", PRIMEIRA_PAGINA);

        assertThat(pagina.getContent()).extracting(Equipamento::getCodigoPatrimonio).containsExactly("PAT-002");
    }

    @Test
    @DisplayName("Busca por trecho do código ou da descrição sem diferenciar maiúsculas")
    void buscaPorTrechoDoCodigoOuDaDescricao() {
        persistir("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        persistir("PAT-002", "Furadeira de impacto", TipoEquipamento.FERRAMENTA);

        Page<Equipamento> porDescricao = repository.buscar(null, null, "BETONEIRA", PRIMEIRA_PAGINA);
        Page<Equipamento> porCodigo = repository.buscar(null, null, "pat-002", PRIMEIRA_PAGINA);

        assertThat(porDescricao.getContent()).extracting(Equipamento::getCodigoPatrimonio)
                .containsExactly("PAT-001");
        assertThat(porCodigo.getContent()).extracting(Equipamento::getCodigoPatrimonio)
                .containsExactly("PAT-002");
    }

    @Test
    @DisplayName("Pagina e ordena os resultados")
    void paginaEOrdenaOsResultados() {
        persistir("PAT-003", "Andaime", TipoEquipamento.OUTRO);
        persistir("PAT-001", "Betoneira", TipoEquipamento.MAQUINA);
        persistir("PAT-002", "Furadeira", TipoEquipamento.FERRAMENTA);

        Page<Equipamento> pagina = repository.buscar(null, null, "", PageRequest.of(0, 2, Sort.by("codigoPatrimonio")));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent()).extracting(Equipamento::getCodigoPatrimonio)
                .containsExactly("PAT-001", "PAT-002");
    }
}