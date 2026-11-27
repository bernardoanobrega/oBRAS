package br.com.obras.diario.service;

import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.dto.ParalisacaoDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.shared.storage.ArquivoStorageService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L32: ocorrências e paralisações pelo service, com banco, auditoria e
 * ObraService reais. No fim, os testes de cascata lógica do L36.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DiarioService.class, DiarioMapper.class, AuditoriaService.class, ObraService.class, ObraMapper.class})
@Testcontainers(disabledWithoutDocker = true)
class OcorrenciaServiceIntegracaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime DEZ_HORAS = DIA.atTime(10, 0);

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private DiarioService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager em;
    @MockBean private ArquivoStorageService storage;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private long usuario;
    private long obraA;
    private long obraB;
    private long alocacaoA;

    @BeforeEach
    void setup() {
        usuario = usuario("Maria");
        obraA = obra();
        obraB = obra();
        alocacaoA = alocacao(obraA, usuario);
        alocacao(obraB, usuario);
        autenticar();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    // ---------- L32 ----------

    @Test
    void criaOcorrenciaNoDiarioDaObra() {
        long diario = diario(obraA);

        OcorrenciaDto dto = service.adicionarOcorrencia(obraA, diario,
                nova(CategoriaOcorrencia.LOGISTICA, "Atraso na entrega de aço", DEZ_HORAS, null));

        assertAll(
                () -> assertEquals(diario, dto.diarioId()),
                () -> assertEquals("LOGISTICA", dto.categoria()),
                () -> assertEquals(DEZ_HORAS, dto.dataHora()),
                () -> assertEquals(alocacaoA, dto.autor().alocacaoId()),
                () -> assertEquals("Maria", dto.autor().nome()),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM ocorrencia WHERE registro_diario_id = ? AND alocacao_obra_id = ?",
                        diario, alocacaoA))
        );
    }

    @Test
    void rejeitaDiarioDeOutraObra() {
        long diarioB = diario(obraB);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarOcorrencia(obraA, diarioB,
                nova(CategoriaOcorrencia.OUTRO, "Tentativa pela obra errada", DEZ_HORAS, null)));

        assertEquals(0L, contar("SELECT count(*) FROM ocorrencia WHERE registro_diario_id = ?", diarioB));
    }

    @Test
    void rejeitaCategoriaNula() {
        NovaOcorrenciaDto semCategoria = nova(null, "Sem categoria", DEZ_HORAS, null);
        long diario = diario(obraA);

        assertAll(
                () -> assertEquals(Set.of("categoria"), camposInvalidos(semCategoria), "DTO barra com 400"),
                () -> assertEquals("Categoria da ocorrência é obrigatória",
                        assertThrows(RegraNegocioException.class,
                                () -> service.adicionarOcorrencia(obraA, diario, semCategoria)).getMessage(),
                        "se o DTO for contornado, o domínio barra com 422")
        );
    }

    @Test
    void rejeitaDescricaoVazia() {
        NovaOcorrenciaDto semDescricao = nova(CategoriaOcorrencia.OUTRO, "   ", DEZ_HORAS, null);
        long diario = diario(obraA);

        assertAll(
                () -> assertEquals(Set.of("descricao"), camposInvalidos(semDescricao), "DTO barra com 400"),
                () -> assertEquals("Descrição da ocorrência é obrigatória",
                        assertThrows(RegraNegocioException.class,
                                () -> service.adicionarOcorrencia(obraA, diario, semDescricao)).getMessage(),
                        "se o DTO for contornado, o domínio barra com 422")
        );
    }

    /** A paralisação não tem categoria própria: nasce com a ocorrência e herda a dela (L29). */
    @Test
    void criaParalisacaoComCategoria() {
        long diario = diario(obraA);

        OcorrenciaDto dto = service.adicionarOcorrencia(obraA, diario, nova(CategoriaOcorrencia.INTEMPERIE,
                "Chuva forte, obra parada", DEZ_HORAS, new ParalisacaoDto(DEZ_HORAS, DEZ_HORAS.plusHours(3))));

        assertAll(
                () -> assertEquals("INTEMPERIE", dto.categoria()),
                () -> assertEquals(DEZ_HORAS, dto.paralisacao().inicio()),
                () -> assertEquals(DEZ_HORAS.plusHours(3), dto.paralisacao().fim()),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM paralisacao WHERE id = ?", dto.id()),
                        "paralisação compartilha o id da ocorrência")
        );
    }

    /** Sem a transação do teste: o service abre a própria e o rollback é o real. */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rejeitaParalisacaoComFimMenorQueInicio() {
        long diario = diario(obraA);
        ParalisacaoDto invertida = new ParalisacaoDto(DEZ_HORAS, DEZ_HORAS.minusMinutes(1));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.adicionarOcorrencia(
                obraA, diario, nova(CategoriaOcorrencia.INTEMPERIE, "Período invertido", DEZ_HORAS, invertida)));

        assertAll(
                () -> assertEquals("Fim da paralisação não pode ser antes do início", erro.getMessage()),
                () -> assertEquals(0L, contar("SELECT count(*) FROM ocorrencia WHERE registro_diario_id = ?", diario),
                        "rollback: nem a ocorrência fica gravada")
        );

        OcorrenciaDto valida = service.adicionarOcorrencia(obraA, diario,
                nova(CategoriaOcorrencia.INTEMPERIE, "Chuva", DEZ_HORAS, null));
        assertThrows(RegraNegocioException.class, () -> service.editarOcorrencia(obraA, valida.id(),
                nova(CategoriaOcorrencia.INTEMPERIE, "Chuva", DEZ_HORAS, invertida)), "também na edição");
    }

    @Test
    void atualizacaoAudita() {
        OcorrenciaDto criada = service.adicionarOcorrencia(obraA, diario(obraA),
                nova(CategoriaOcorrencia.OUTRO, "Texto original", DEZ_HORAS, null));

        OcorrenciaDto editada = service.editarOcorrencia(obraA, criada.id(),
                nova(CategoriaOcorrencia.SEGURANCA, "Texto corrigido", DEZ_HORAS, null));
        em.flush();

        assertAll(
                () -> assertEquals("SEGURANCA", editada.categoria()),
                () -> assertEquals("Texto corrigido", editada.descricao()),
                () -> assertEquals(List.of("CRIACAO", "EDICAO"), acoesAuditadas(criada.id()))
        );
    }

    // ---------- L36: cascata lógica (contrato do L36, seção 3) ----------

    @Test
    void excluirDiarioMarcaOcorrenciasEFotos() {
        long diario = diario(obraA);
        long ocorrencia = service.adicionarOcorrencia(obraA, diario,
                nova(CategoriaOcorrencia.OUTRO, "Com foto", DEZ_HORAS, null)).id();
        long foto = foto(ocorrencia);

        service.excluir(obraA, diario);
        em.flush();

        assertAll(
                () -> assertTrue(excluido(diario) && excluido(ocorrencia) && excluido(foto),
                        "diário, ocorrência e foto marcados como excluídos"),
                () -> assertEquals(List.of("EXCLUSAO"), acoesAuditadas(foto)),
                () -> assertEquals(List.of("CRIACAO", "EXCLUSAO"), acoesAuditadas(ocorrencia)),
                () -> assertEquals(1L, contar("SELECT count(*) FROM ocorrencia WHERE id = ?", ocorrencia),
                        "nada é apagado fisicamente"),
                () -> assertThrows(RecursoNaoEncontradoException.class, () -> service.editarOcorrencia(obraA,
                        ocorrencia, nova(CategoriaOcorrencia.OUTRO, "Editar depois", DEZ_HORAS, null)),
                        "ocorrência de diário excluído não pode mais ser editada")
        );
    }

    @Test
    void excluirOcorrenciaMarcaFotos() {
        long diario = diario(obraA);
        long ocorrencia = service.adicionarOcorrencia(obraA, diario,
                nova(CategoriaOcorrencia.OUTRO, "Com foto", DEZ_HORAS, null)).id();
        long foto = foto(ocorrencia);

        service.excluirOcorrencia(obraA, ocorrencia);
        em.flush();

        assertAll(
                () -> assertTrue(excluido(ocorrencia) && excluido(foto)),
                () -> assertTrue(!excluido(diario), "o diário continua ativo"),
                () -> assertEquals(List.of("EXCLUSAO"), acoesAuditadas(foto))
        );
    }

    // ---------- apoio ----------

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    private static NovaOcorrenciaDto nova(CategoriaOcorrencia categoria, String descricao,
                                          LocalDateTime dataHora, ParalisacaoDto paralisacao) {
        return new NovaOcorrenciaDto(categoria, descricao, dataHora, paralisacao);
    }

    private Set<String> camposInvalidos(Object dto) {
        return validador.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    private long diario(long obra) {
        DiarioDto dto = service.criar(obra, new NovoDiarioDto(DIA, Clima.ENSOLARADO, "Diário", Set.of()));
        assertNotNull(dto.id());
        return dto.id();
    }

    private long foto(long ocorrencia) {
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        jdbc.update("INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo) VALUES (?, ?, 'obras/foto.jpg')",
                auditavel, ocorrencia);
        return auditavel;
    }

    private boolean excluido(long auditavelId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT excluido FROM auditavel WHERE id = ?", Boolean.class, auditavelId));
    }

    private List<String> acoesAuditadas(long auditavelId) {
        return jdbc.queryForList("SELECT acao FROM registro_auditoria WHERE auditavel_id = ? ORDER BY id",
                String.class, auditavelId);
    }

    private long usuario(String nome) {
        return id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES (?, 'ATIVO', ? || gen_random_uuid(), 'hash') RETURNING id
                """, nome, nome);
    }

    private long obra() {
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, usuario, auditavel);
    }

    private long alocacao(long obra, long usuarioId) {
        return id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obra, usuarioId);
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private long contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
