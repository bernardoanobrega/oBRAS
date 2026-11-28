package br.com.obras.diario.service;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.shared.storage.ArquivoStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DiarioService.class, DiarioMapper.class, AuditoriaService.class, ObraService.class, ObraMapper.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ExclusaoDiarioAtomicidadeIntegracaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime DEZ_HORAS = DIA.atTime(10, 0);

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private DiarioService service;
    @Autowired private JdbcTemplate jdbc;
    @SpyBean private AuditoriaService auditoria;
    @MockBean private ArquivoStorageService storage;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private final AtomicInteger chamadas = new AtomicInteger();
    private long usuario;
    private long obra;
    private long diario;
    private long ocorrencia;
    private long foto;

    @BeforeEach
    void setup() {
        chamadas.set(0);
        usuario = id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Maria', 'ATIVO', 'maria-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        long auditavelObra = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        obra = id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra', 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?)
                RETURNING id
                """, usuario, auditavelObra);
        id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obra, usuario);
        autenticar();

        diario = service.criar(obra, new NovoDiarioDto(DIA, Clima.ENSOLARADO, "Diário", Set.of())).id();
        ocorrencia = service.adicionarOcorrencia(obra, diario, new NovaOcorrenciaDto(
                CategoriaOcorrencia.OUTRO, "Com foto", DEZ_HORAS, null)).id();
        foto = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        jdbc.update("INSERT INTO registro_fotografico (id, ocorrencia_id, arquivo) VALUES (?, ?, ?)",
                foto, ocorrencia, "2026/11/foto.jpg");

        doAnswer(invocacao -> {
            if (chamadas.incrementAndGet() == 2) {
                throw new IllegalStateException("falha injetada na cascata");
            }
            invocacao.callRealMethod();
            return null;
        }).when(auditoria).excluir(any(Auditavel.class));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void falhaNaCascataReverteDiarioOcorrenciasEFotos() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.excluir(obra, diario));

        assertAll(
                () -> assertEquals("falha injetada na cascata", erro.getMessage()),
                () -> assertEquals(2, chamadas.get()),
                () -> assertFalse(booleano("SELECT excluido FROM auditavel WHERE id = ?", diario)),
                () -> assertFalse(booleano("SELECT excluido FROM auditavel WHERE id = ?", ocorrencia)),
                () -> assertFalse(booleano("SELECT excluido FROM auditavel WHERE id = ?", foto)),
                () -> assertFalse(booleano("SELECT excluido FROM registro_diario WHERE id = ?", diario)),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_diario WHERE id = ?", diario)),
                () -> assertEquals(1L, contar("SELECT count(*) FROM ocorrencia WHERE id = ?", ocorrencia)),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_fotografico WHERE id = ?", foto)),
                () -> assertEquals(0L, contar("""
                        SELECT count(*) FROM registro_auditoria
                        WHERE acao = 'EXCLUSAO' AND auditavel_id IN (?, ?, ?)
                        """, diario, ocorrencia, foto))
        );
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private long contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private boolean booleano(String sql, Object... parametros) {
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, parametros));
    }
}
