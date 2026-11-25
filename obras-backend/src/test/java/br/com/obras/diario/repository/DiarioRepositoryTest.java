package br.com.obras.diario.repository;

import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.diario.domain.RegistroFotografico;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Consultas do diário contra um PostgreSQL real (Flyway aplica as migrations).
 * Cada teste roda numa transação que é desfeita no fim (@DataJpaTest).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class DiarioRepositoryTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private RegistroDiarioRepository diarios;
    @Autowired private OcorrenciaRepository ocorrencias;
    @Autowired private RegistroFotograficoRepository fotos;
    @Autowired private JdbcTemplate jdbc;

    private long obraA;
    private long obraB;
    private long alocacaoA;

    @BeforeEach
    void setup() {
        long usuario = id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Maria Engenheira', 'ATIVO', 'maria-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        obraA = inserirObra(usuario, "Obra A");
        obraB = inserirObra(usuario, "Obra B");
        alocacaoA = id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obraA, usuario);
    }

    @Test
    void repositoryBuscaDiarioAtivoPorObraData() {
        long diario = inserirDiario(obraA, DIA);

        RegistroDiario achado = diarios.buscarAtivoPorData(obraA, DIA).orElseThrow();

        assertAll(
                () -> assertEquals(diario, achado.getId()),
                () -> assertTrue(diarios.buscarAtivoPorData(obraA, DIA.plusDays(1)).isEmpty(), "outra data")
        );
    }

    @Test
    void repositoryNaoRetornaDiarioExcluido() {
        long excluido = inserirDiario(obraA, DIA);
        excluirLogicamente(excluido);
        long ativo = inserirDiario(obraA, DIA.plusDays(1));

        assertAll(
                () -> assertTrue(diarios.buscarAtivoPorData(obraA, DIA).isEmpty()),
                () -> assertTrue(diarios.buscar(obraA, excluido).isEmpty()),
                () -> assertEquals(List.of(ativo), ids(diarios.listarPorObra(obraA)))
        );
    }

    @Test
    void repositoryNaoRetornaDiarioDeOutraObra() {
        long diarioA = inserirDiario(obraA, DIA);
        long diarioB = inserirDiario(obraB, DIA);

        assertAll(
                () -> assertTrue(diarios.buscar(obraB, diarioA).isEmpty(), "id certo, obra errada"),
                () -> assertEquals(diarioB, diarios.buscarAtivoPorData(obraB, DIA).orElseThrow().getId()),
                () -> assertEquals(List.of(diarioA), ids(diarios.listarPorObra(obraA)))
        );
    }

    @Test
    void listarPorObraOrdenaDoMaisRecente() {
        long antigo = inserirDiario(obraA, DIA);
        long novo = inserirDiario(obraA, DIA.plusDays(2));

        assertEquals(List.of(novo, antigo), ids(diarios.listarPorObra(obraA)));
    }

    @Test
    void ocorrenciaExcluidaNaoSaiNaListaNemNaContagem() {
        long diario = inserirDiario(obraA, DIA);
        long ativa = inserirOcorrencia(diario);
        long excluida = inserirOcorrencia(diario);
        excluirLogicamente(excluida);

        assertAll(
                () -> assertEquals(List.of(ativa),
                        ocorrencias.listarPorDiario(diario).stream().map(Ocorrencia::getId).toList()),
                () -> assertEquals(List.of(new ContagemOcorrencias(diario, 1L)),
                        ocorrencias.contarPorDiarios(List.of(diario))),
                () -> assertTrue(ocorrencias.buscar(obraA, excluida).isEmpty())
        );
    }

    @Test
    void ocorrenciaNaoEhEncontradaPorOutraObra() {
        long ocorrencia = inserirOcorrencia(inserirDiario(obraA, DIA));

        assertAll(
                () -> assertTrue(ocorrencias.buscar(obraA, ocorrencia).isPresent()),
                () -> assertTrue(ocorrencias.buscar(obraB, ocorrencia).isEmpty())
        );
    }

    @Test
    void fotoExcluidaNaoSaiNaListaNemNaBusca() {
        long ocorrencia = inserirOcorrencia(inserirDiario(obraA, DIA));
        long ativa = inserirFoto(ocorrencia);
        long excluida = inserirFoto(ocorrencia);
        excluirLogicamente(excluida);

        assertAll(
                () -> assertEquals(List.of(ativa),
                        fotos.listarPorOcorrencia(ocorrencia).stream().map(RegistroFotografico::getId).toList()),
                () -> assertTrue(fotos.buscar(obraA, excluida).isEmpty()),
                () -> assertTrue(fotos.buscar(obraB, ativa).isEmpty(), "foto de outra obra")
        );
    }

    @Test
    void contarPorDiariosAgrupaNumaConsultaEIgnoraExcluidas() {
        long diario1 = inserirDiario(obraA, DIA);
        long diario2 = inserirDiario(obraA, DIA.plusDays(1));
        long semOcorrencia = inserirDiario(obraA, DIA.plusDays(2));
        inserirOcorrencia(diario1);
        inserirOcorrencia(diario1);
        excluirLogicamente(inserirOcorrencia(diario1));
        inserirOcorrencia(diario2);

        var totais = ocorrencias.contarPorDiarios(List.of(diario1, diario2, semOcorrencia)).stream()
                .collect(java.util.stream.Collectors.toMap(ContagemOcorrencias::diarioId, ContagemOcorrencias::total));

        assertEquals(java.util.Map.of(diario1, 2L, diario2, 1L), totais,
                "diário sem ocorrência não aparece; o service usa 0");
    }

    @Test
    void listarFotosPorDiarioIgnoraFotoExcluidaEOcorrenciaExcluida() {
        long diario = inserirDiario(obraA, DIA);
        long ocorrenciaAtiva = inserirOcorrencia(diario);
        long ocorrenciaExcluida = inserirOcorrencia(diario);
        long fotoAtiva = inserirFoto(ocorrenciaAtiva);
        excluirLogicamente(inserirFoto(ocorrenciaAtiva));
        inserirFoto(ocorrenciaExcluida);
        excluirLogicamente(ocorrenciaExcluida);
        inserirFoto(inserirOcorrencia(inserirDiario(obraA, DIA.plusDays(1)))); // outro diário

        assertEquals(List.of(fotoAtiva),
                fotos.listarPorDiario(diario).stream().map(RegistroFotografico::getId).toList());
    }

    @Test
    void buscarComEquipeTrazNomesDosPresentes() {
        long diario = inserirDiario(obraA, DIA);
        jdbc.update("INSERT INTO registro_diario_equipe (registro_diario_id, alocacao_obra_id) VALUES (?, ?)",
                diario, alocacaoA);

        RegistroDiario achado = diarios.buscarComEquipe(obraA, diario).orElseThrow();

        assertAll(
                () -> assertEquals(1, achado.getEquipePresente().size()),
                () -> assertEquals("Maria Engenheira",
                        achado.getEquipePresente().iterator().next().getUsuario().getNome()),
                () -> assertTrue(diarios.buscarComEquipe(obraB, diario).isEmpty(), "outra obra")
        );
    }

    @Test
    void listarOcorrenciasPorDiarioJaTrazAutor() {
        long diario = inserirDiario(obraA, DIA);
        inserirOcorrencia(diario);

        Ocorrencia o = ocorrencias.listarPorDiario(diario).get(0);

        assertEquals("Maria Engenheira", o.getAlocacaoObra().getUsuario().getNome());
    }

    // ---------- dados de teste (SQL direto, como nos testes de migration) ----------

    private long inserirObra(long responsavel, String nome) {
        return id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, nome, responsavel, auditavel());
    }

    private long inserirDiario(long obra, LocalDate data) {
        return id("""
                INSERT INTO registro_diario (id, obra_id, data, clima)
                VALUES (?, ?, ?, 'ENSOLARADO') RETURNING id
                """, auditavel(), obra, data);
    }

    private long inserirOcorrencia(long diario) {
        return id("""
                INSERT INTO ocorrencia (id, registro_diario_id, alocacao_obra_id, data_hora, categoria, descricao)
                VALUES (?, ?, ?, TIMESTAMP '2026-11-18 10:00:00', 'OUTRO', 'Ocorrência de teste') RETURNING id
                """, auditavel(), diario, alocacaoA);
    }

    private long inserirFoto(long ocorrencia) {
        return id("""
                INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo)
                VALUES (?, ?, 'obras/1/foto.jpg') RETURNING id
                """, auditavel(), ocorrencia);
    }

    /** Exclusão lógica pelo auditável; no diário, o trigger da V4 espelha o flag. */
    private void excluirLogicamente(long auditavelId) {
        jdbc.update("UPDATE auditavel SET excluido = TRUE WHERE id = ?", auditavelId);
    }

    private long auditavel() {
        return id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private static List<Long> ids(List<RegistroDiario> lista) {
        return lista.stream().map(RegistroDiario::getId).toList();
    }
}
