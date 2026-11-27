package br.com.obras.diario.service;

import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.NovaOcorrenciaRapidaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.shared.storage.ArquivoStorageService;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L33: ocorrência rápida pelo service, com banco, auditoria e ObraService reais.
 *
 * <p>A classe inteira roda <b>sem</b> a transação do teste: a ocorrência rápida abre
 * a própria transação (ADR-19), então cada chamada é confirmada ou desfeita de
 * verdade, como numa requisição HTTP. Cada teste usa uma obra nova, por isso os
 * dados gravados por um teste não atrapalham o outro.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DiarioService.class, DiarioMapper.class, AuditoriaService.class, ObraService.class, ObraMapper.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OcorrenciaRapidaIntegracaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime DEZ_HORAS = DIA.atTime(10, 0);

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private DiarioService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transacoes;
    @MockBean private ArquivoStorageService storage;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private long usuario;
    private long obra;
    private long alocacao;

    @BeforeEach
    void setup() {
        usuario = usuario("Maria");
        obra = obra();
        alocacao = alocacao(obra, usuario);
        autenticar();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void criaOcorrenciaRapidaComDiarioExistente() {
        long diario = diario(Clima.ENSOLARADO);

        OcorrenciaDto dto = service.ocorrenciaRapida(obra, rapida(null, "Fachada leste", DEZ_HORAS));

        assertAll(
                () -> assertEquals(diario, dto.diarioId()),
                () -> assertEquals(1L, diariosDaObra(), "nenhum diário novo")
        );
    }

    @Test
    void criaDiarioQuandoNaoExiste() {
        OcorrenciaDto dto = service.ocorrenciaRapida(obra, rapida(Clima.CHUVA_FRACA, "Fachada leste", DEZ_HORAS));

        DiarioDto diario = service.buscar(obra, dto.diarioId());
        assertAll(
                () -> assertEquals(DIA, diario.data()),
                () -> assertEquals("CHUVA_FRACA", diario.clima()),
                () -> assertNull(diario.descricao(), "D4: o diário nasce sem descrição"),
                () -> assertTrue(diario.equipePresente().isEmpty()),
                () -> assertEquals(List.of(dto.id()), diario.ocorrencias().stream().map(OcorrenciaDto::id).toList())
        );
    }

    @Test
    void exigeClimaQuandoDiarioNaoExiste() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.ocorrenciaRapida(obra, rapida(null, "Sem clima", DEZ_HORAS)));

        assertAll(
                () -> assertEquals("Clima é obrigatório quando não existe diário no dia", erro.getMessage()),
                () -> assertEquals(0L, diariosDaObra()),
                () -> assertEquals(0L, ocorrenciasDaObra())
        );
    }

    @Test
    void ignoraClimaQuandoDiarioExiste() {
        long diario = diario(Clima.ENSOLARADO);

        service.ocorrenciaRapida(obra, rapida(Clima.CHUVA_FORTE, "Céu fechou", DEZ_HORAS));

        assertEquals("ENSOLARADO", service.buscar(obra, diario).clima(), "um clima por dia (ADR-01)");
    }

    @Test
    void categoriaPadraoOutro() {
        assertEquals("OUTRO", service.ocorrenciaRapida(obra,
                rapida(Clima.NUBLADO, "Sem categoria", DEZ_HORAS)).categoria(), "ADR-14");
    }

    @Test
    void descricaoObrigatoria() {
        NovaOcorrenciaRapidaDto semDescricao = rapida(Clima.NUBLADO, "   ", DEZ_HORAS);

        assertAll(
                () -> assertEquals(Set.of("descricao"), camposInvalidos(semDescricao), "DTO barra com 400"),
                () -> assertEquals("Descrição da ocorrência é obrigatória",
                        assertThrows(RegraNegocioException.class,
                                () -> service.ocorrenciaRapida(obra, semDescricao)).getMessage(),
                        "se o DTO for contornado, o domínio barra com 422"),
                () -> assertEquals(0L, diariosDaObra(), "e o diário automático é desfeito junto")
        );
    }

    @Test
    void autorEDataHoraSaoDoMomento() {
        LocalDateTime antes = LocalDateTime.now();
        OcorrenciaDto dto = service.ocorrenciaRapida(obra,
                new NovaOcorrenciaRapidaDto(LocalDate.now(), Clima.ENSOLARADO, null, "Agora", null));
        LocalDateTime depois = LocalDateTime.now();

        assertAll(
                () -> assertEquals(alocacao, dto.autor().alocacaoId(), "autor = usuário do token"),
                () -> assertFalse(dto.dataHora().isBefore(antes.withNano(0))),
                () -> assertFalse(dto.dataHora().isAfter(depois))
        );
    }

    @Test
    void auditaCriacao() {
        OcorrenciaDto dto = service.ocorrenciaRapida(obra, rapida(Clima.NUBLADO, "Auditada", DEZ_HORAS));

        assertAll(
                () -> assertEquals(List.of("CRIACAO"), acoesAuditadas(dto.diarioId()), "diário automático"),
                () -> assertEquals(List.of("CRIACAO"), acoesAuditadas(dto.id()), "ocorrência"),
                () -> assertEquals(List.of(usuario), autoresAuditados(dto.id()))
        );
    }

    /**
     * Transação única: o diário é criado e depois a ocorrência é recusada
     * (dia passado sem horário, ADR-18). O diário precisa sumir junto.
     */
    @Test
    void transacaoUnicaDesfazDiarioSeOcorrenciaFalhar() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.ocorrenciaRapida(obra, rapida(Clima.NUBLADO, "Sem horário", null)));

        assertAll(
                () -> assertEquals("A ocorrência deve ser do mesmo dia do diário", erro.getMessage()),
                () -> assertEquals(0L, diariosDaObra(), "rollback: o diário automático não fica"),
                () -> assertEquals(0L, contar("SELECT count(*) FROM registro_auditoria WHERE usuario_id = ?",
                        usuario), "nem os registros de auditoria (o usuário é novo neste teste)")
        );
    }

    /**
     * ADR-19: duas requisições no mesmo dia sem diário. A (thread 1) cria o diário e
     * segura a transação aberta; B (thread 2) faz a ocorrência rápida e fica bloqueada
     * no índice único. Quando A confirma, B recebe a duplicidade, repete numa
     * transação nova e se vincula ao diário de A, sem erro para o usuário.
     */
    @Test
    void corridaSemDiarioVinculaAoDiarioCriado() throws Exception {
        ExecutorService threads = Executors.newFixedThreadPool(2);
        CountDownLatch diarioCriado = new CountDownLatch(1);
        CountDownLatch liberarA = new CountDownLatch(1);
        TransactionTemplate transacaoA = new TransactionTemplate(transacoes);
        try {
            Future<Long> a = threads.submit(() -> comUsuario(() -> transacaoA.execute(status -> {
                long id = service.criar(obra, new NovoDiarioDto(DIA, Clima.ENSOLARADO, "Diário de A", Set.of())).id();
                diarioCriado.countDown();
                aguardar(liberarA);
                return id;
            })));
            assertTrue(diarioCriado.await(10, SECONDS), "A criou o diário (ainda sem confirmar)");

            Future<OcorrenciaDto> b = threads.submit(() -> comUsuario(() ->
                    service.ocorrenciaRapida(obra, rapida(Clima.CHUVA_FORTE, "Registro de B", DEZ_HORAS))));
            aguardarBloqueioNoBanco(b);
            liberarA.countDown();

            long diarioA = a.get(10, SECONDS);
            OcorrenciaDto ocorrenciaB = b.get(10, SECONDS);
            assertAll(
                    () -> assertEquals(diarioA, ocorrenciaB.diarioId(), "B vinculada ao diário de A"),
                    () -> assertEquals(1L, diariosDaObra(), "um diário por dia (RN-07)"),
                    () -> assertEquals("ENSOLARADO", service.buscar(obra, diarioA).clima(), "clima de B ignorado")
            );
        } finally {
            threads.shutdownNow();
        }
    }

    // ---------- apoio ----------

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    /** O usuário logado fica em ThreadLocal: cada thread precisa do seu. */
    private <T> T comUsuario(Callable<T> acao) throws Exception {
        autenticar();
        try {
            return acao.call();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static void aguardar(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) throw new IllegalStateException("tempo esgotado");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** Espera B ficar parada esperando o lock do índice único (ou terminar, se algo der errado). */
    private void aguardarBloqueioNoBanco(Future<?> b) throws InterruptedException {
        long limite = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < limite && !b.isDone()) {
            if (contar("SELECT count(*) FROM pg_locks WHERE NOT granted") > 0) return;
            Thread.sleep(50);
        }
        throw new AssertionError("B não ficou bloqueada no índice único");
    }

    private static NovaOcorrenciaRapidaDto rapida(Clima clima, String descricao, LocalDateTime dataHora) {
        return new NovaOcorrenciaRapidaDto(DIA, clima, null, descricao, dataHora);
    }

    private Set<String> camposInvalidos(Object dto) {
        return validador.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private long diario(Clima clima) {
        return service.criar(obra, new NovoDiarioDto(DIA, clima, "Diário", Set.of())).id();
    }

    private long diariosDaObra() {
        return contar("SELECT count(*) FROM registro_diario WHERE obra_id = ?", obra);
    }

    private long ocorrenciasDaObra() {
        return contar("""
                SELECT count(*) FROM ocorrencia o JOIN registro_diario d ON d.id = o.registro_diario_id
                WHERE d.obra_id = ?
                """, obra);
    }

    private List<String> acoesAuditadas(long auditavelId) {
        return jdbc.queryForList("SELECT acao FROM registro_auditoria WHERE auditavel_id = ? ORDER BY id",
                String.class, auditavelId);
    }

    private List<Long> autoresAuditados(long auditavelId) {
        return jdbc.queryForList("SELECT usuario_id FROM registro_auditoria WHERE auditavel_id = ? ORDER BY id",
                Long.class, auditavelId);
    }

    private long usuario(String nome) {
        return contar("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES (?, 'ATIVO', ? || gen_random_uuid(), 'hash') RETURNING id
                """, nome, nome);
    }

    private long obra() {
        long auditavel = contar("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return contar("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, usuario, auditavel);
    }

    private long alocacao(long obraId, long usuarioId) {
        return contar("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obraId, usuarioId);
    }

    private long contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
