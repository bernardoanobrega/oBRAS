package br.com.obras.diario.service;

import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.FotoFixtures;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DiarioService.class, DiarioMapper.class, AuditoriaService.class, ObraService.class,
        ObraMapper.class, ArquivoStorageService.class})
class FotoServiceIntegracaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime DEZ_HORAS = DIA.atTime(10, 0);
    static final Path STORAGE = criarDiretorio();

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void storage(DynamicPropertyRegistry registry) {
        registry.add("app.storage.base-path", STORAGE::toString);
    }

    @Autowired private DiarioService service;
    @Autowired private ArquivoStorageService storage;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager em;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private long usuario;
    private long obraA;
    private long obraB;

    @BeforeEach
    void setup() throws IOException {
        limparStorage();
        usuario = usuario("Maria");
        obraA = obra();
        obraB = obra();
        alocacao(obraA, usuario);
        alocacao(obraB, usuario);
        autenticar();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadInvalidoFalha() throws Exception {
        long ocorrencia = ocorrencia(obraA, diario(obraA));

        RegraNegocioException pdf = assertThrows(RegraNegocioException.class,
                () -> service.anexarFoto(obraA, ocorrencia, FotoFixtures.pdf("invalido.pdf"), null));
        RegraNegocioException extensaoFalsa = assertThrows(RegraNegocioException.class,
                () -> service.anexarFoto(obraA, ocorrencia,
                        FotoFixtures.extensaoFalsa("extensao-falsa.jpg"), null));
        em.flush();

        assertAll(
                () -> assertEquals("Fotos: use PNG ou JPG", pdf.getMessage()),
                () -> assertEquals("Formato não permitido. Use PDF, PNG ou JPG", extensaoFalsa.getMessage()),
                () -> assertEquals(0L, contar("SELECT count(*) FROM registro_fotografico")),
                () -> assertTrue(arquivos().isEmpty(), "PDF foi descartado e extensão falsa nunca foi gravada")
        );
    }

    @Test
    void uploadValidoGravaEBuscaBytes() throws Exception {
        long ocorrencia = ocorrencia(obraA, diario(obraA));
        byte[] bytes = FotoFixtures.jpg();

        var foto = service.anexarFoto(obraA, ocorrencia,
                FotoFixtures.multipart("fachada.jpg", "image/jpeg", bytes), "Fachada leste");
        em.flush();
        var download = service.buscarFoto(obraA, foto.id());

        assertAll(
                () -> assertTrue(foto.nomeArquivo().endsWith(".jpg")),
                () -> assertEquals("Fachada leste", foto.legenda()),
                () -> assertEquals(MediaType.IMAGE_JPEG, download.mediaType()),
                () -> assertArrayEquals(bytes, download.resource().getContentAsByteArray()),
                () -> assertEquals(1, arquivos().size())
        );
    }

    @Test
    void rejeitaOcorrenciaDeOutraObraNoService() throws Exception {
        long ocorrenciaB = ocorrencia(obraB, diario(obraB));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.anexarFoto(obraA, ocorrenciaB, FotoFixtures.jpg("fachada.jpg"), null));

        assertAll(
                () -> assertEquals(0L, contar("SELECT count(*) FROM registro_fotografico")),
                () -> assertTrue(arquivos().isEmpty(), "nenhum arquivo é gravado para ocorrência de outra obra")
        );
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    private long diario(long obra) {
        return service.criar(obra, new NovoDiarioDto(DIA, Clima.ENSOLARADO, "Diário", Set.of())).id();
    }

    private long ocorrencia(long obra, long diario) {
        return service.adicionarOcorrencia(obra, diario,
                new NovaOcorrenciaDto(CategoriaOcorrencia.OUTRO, "Com foto", DEZ_HORAS, null)).id();
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

    private void alocacao(long obra, long usuarioId) {
        id("""
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

    private List<Path> arquivos() throws IOException {
        try (var fluxo = Files.walk(STORAGE)) {
            return fluxo.filter(Files::isRegularFile).toList();
        }
    }

    private void limparStorage() throws IOException {
        try (var fluxo = Files.walk(STORAGE)) {
            for (Path caminho : fluxo.sorted(Comparator.reverseOrder()).toList()) {
                if (!caminho.equals(STORAGE)) Files.deleteIfExists(caminho);
            }
        }
    }

    private static Path criarDiretorio() {
        try {
            return Files.createTempDirectory("obras-fotos-service-");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
