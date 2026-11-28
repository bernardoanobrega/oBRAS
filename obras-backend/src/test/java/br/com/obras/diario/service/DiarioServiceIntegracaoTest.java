package br.com.obras.diario.service;

import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.dto.AlterarDiarioDto;
import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.diario.repository.RegistroDiarioRepository;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.RegraNegocioException;
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
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * L31: service do diário com banco real (Flyway, índice RN-07, triggers),
 * auditoria real e o ObraService real. Só storage e autorização são mock,
 * porque o diário não usa nenhum dos dois nestas operações.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DiarioService.class, DiarioMapper.class, AuditoriaService.class, ObraService.class,
        ObraMapper.class, DiarioServiceIntegracaoTest.PausaNaChecagem.class})
@Testcontainers(disabledWithoutDocker = true)
class DiarioServiceIntegracaoTest {
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final String MSG_DUPLICADO = "Já existe registro para o dia";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Autowired private DiarioService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transacoes;
    @MockBean private ArquivoStorageService storage;
    @MockBean private AutorizacaoPort autorizacao;
    @MockBean private UsuarioConsultaPort usuarios;

    private long usuario;
    private long obraA;
    private long obraB;
    private long alocacaoA;

    @BeforeEach
    void setup() {
        usuario = id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Maria', 'ATIVO', 'maria-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        obraA = obra();
        obraB = obra();
        alocacaoA = alocacao(obraA, usuario);
        autenticar();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void criaDiarioDoDia() {
        DiarioDto dto = service.criar(obraA, novo(DIA, Set.of(alocacaoA)));

        assertAll(
                () -> assertEquals(DIA, dto.data()),
                () -> assertEquals("ENSOLARADO", dto.clima()),
                () -> assertEquals(List.of(alocacaoA), dto.equipePresente().stream().map(m -> m.alocacaoId()).toList()),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_diario WHERE obra_id = ?", obraA)),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM registro_diario_equipe WHERE registro_diario_id = ?", dto.id()))
        );
    }

    @Test
    void rejeitaDiarioDuplicadoAtivo() {
        service.criar(obraA, novo(DIA, Set.of()));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.criar(obraA, novo(DIA, Set.of())));

        assertEquals(MSG_DUPLICADO, erro.getMessage());
    }

    @Test
    void permiteDiarioAposExclusaoLogica() {
        DiarioDto primeiro = service.criar(obraA, novo(DIA, Set.of()));
        service.excluir(obraA, primeiro.id());

        DiarioDto segundo = service.criar(obraA, novo(DIA, Set.of()));

        assertAll(
                () -> assertTrue(segundo.id() > primeiro.id()),
                () -> assertEquals(2L, contar(
                        "SELECT count(*) FROM registro_diario WHERE obra_id = ? AND data = ?", obraA, DIA)),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM registro_diario WHERE obra_id = ? AND data = ? AND NOT excluido",
                        obraA, DIA))
        );
    }

    @Test
    void atualizaClimaEDescricao() {
        DiarioDto criado = service.criar(obraA, novo(DIA, Set.of()));

        service.editar(obraA, criado.id(), new AlterarDiarioDto(Clima.CHUVA_FORTE, "Concretagem adiada", Set.of()));
        DiarioDto lido = service.buscar(obraA, criado.id());

        assertAll(
                () -> assertEquals("CHUVA_FORTE", lido.clima()),
                () -> assertEquals("Concretagem adiada", lido.descricao()),
                () -> assertEquals(DIA, lido.data(), "a data do diário não muda na edição")
        );
    }

    @Test
    void equipePresenteUsaSomenteAlocacoesAtivas() {
        long encerrada = alocacao(obraA, outroUsuario());
        jdbc.update("UPDATE alocacao_obra SET data_fim = DATE '2026-06-30' WHERE id = ?", encerrada);

        assertThrows(RegraNegocioException.class,
                () -> service.criar(obraA, novo(DIA, Set.of(alocacaoA, encerrada))));
        assertEquals(0L, contar("SELECT count(*) FROM registro_diario WHERE obra_id = ?", obraA),
                "nada é gravado quando a equipe é inválida");

        DiarioDto criado = service.criar(obraA, novo(DIA, Set.of(alocacaoA)));
        assertThrows(RegraNegocioException.class,
                () -> service.editar(obraA, criado.id(), new AlterarDiarioDto(Clima.NUBLADO, null, Set.of(encerrada))));
    }

    @Test
    void rejeitaEquipeDeOutraObra() {
        long alocacaoB = alocacao(obraB, usuario);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.criar(obraA, novo(DIA, Set.of(alocacaoB))));

        assertAll(
                () -> assertEquals("Equipe presente contém alocação inválida ou inativa", erro.getMessage()),
                () -> assertEquals(0L, contar("SELECT count(*) FROM registro_diario WHERE obra_id = ?", obraA))
        );
    }

    /**
     * Duas requisições criam o diário do mesmo dia ao mesmo tempo. A barreira faz
     * as duas passarem pela checagem do service ("não existe diário") antes de
     * qualquer uma inserir: é a corrida real. Só o índice único da V4 barra a
     * segunda, e o service traduz a violação para a mesma mensagem 422.
     * Cada thread tem a própria transação, como duas requisições HTTP.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void umDiarioPorDiaSobConcorrencia() throws Exception {
        PausaNaChecagem.barreira = new CyclicBarrier(2);

        TransactionTemplate transacao = new TransactionTemplate(transacoes);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        List<Throwable> erros = Collections.synchronizedList(new ArrayList<>());
        try {
            List<Future<?>> tarefas = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                tarefas.add(threads.submit(() -> {
                    autenticar();
                    try {
                        transacao.executeWithoutResult(s -> service.criar(obraA, novo(DIA, Set.of())));
                    } catch (Throwable e) {
                        erros.add(e);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }));
            }
            for (Future<?> tarefa : tarefas) tarefa.get(30, TimeUnit.SECONDS);
        } finally {
            threads.shutdownNow();
            PausaNaChecagem.barreira = null;
        }

        assertAll(
                () -> assertEquals(1, erros.size(), () -> "uma das duas deve falhar: " + erros),
                () -> assertTrue(erros.get(0) instanceof RegraNegocioException,
                        () -> "deve ser 422 (RegraNegocioException), veio: " + erros.get(0)),
                () -> assertEquals(MSG_DUPLICADO, erros.get(0).getMessage()),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_diario WHERE obra_id = ?", obraA))
        );
    }

    @Test
    void excluirDiarioNaoRemoveLinhaFisicamente() {
        DiarioDto criado = service.criar(obraA, novo(DIA, Set.of()));

        service.excluir(obraA, criado.id());

        assertAll(
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_diario WHERE id = ?", criado.id())),
                () -> assertTrue(booleano("SELECT excluido FROM registro_diario WHERE id = ?", criado.id())),
                () -> assertTrue(booleano("SELECT excluido FROM auditavel WHERE id = ?", criado.id()))
        );
    }

    @Test
    void diarioExcluidoNaoApareceNaLista() {
        DiarioDto criado = service.criar(obraA, novo(DIA, Set.of()));
        assertEquals(1, service.listar(obraA).size());

        service.excluir(obraA, criado.id());

        assertAll(
                () -> assertTrue(service.listar(obraA).isEmpty()),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_diario WHERE id = ?", criado.id()))
        );
    }

    @Test
    void criacaoEEdicaoAuditam() {
        DiarioDto criado = service.criar(obraA, novo(DIA, Set.of()));
        service.editar(obraA, criado.id(), new AlterarDiarioDto(Clima.NUBLADO, "Editado", Set.of()));

        List<String> acoes = jdbc.queryForList("""
                SELECT acao FROM registro_auditoria
                WHERE auditavel_id = ? AND usuario_id = ?
                ORDER BY id
                """, String.class, criado.id(), usuario);

        assertEquals(List.of("CRIACAO", "EDICAO"), acoes,
                "criação e edição registradas no auditável do diário, com o usuário autenticado");
    }

    // ---------- apoio ----------

    /**
     * Envolve o RegistroDiarioRepository real: quando uma barreira está ativa,
     * cada chamada a {@code buscarAtivoPorData} espera a outra thread também
     * conferir antes de seguir para o insert. Fora do teste de concorrência,
     * a barreira é nula e o repository funciona normalmente.
     */
    @TestConfiguration
    static class PausaNaChecagem {
        static volatile CyclicBarrier barreira;

        @Bean
        static BeanPostProcessor pausarChecagemDoDiario() {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String nome) {
                    if (!(bean instanceof RegistroDiarioRepository alvo)) return bean;
                    return Proxy.newProxyInstance(
                            RegistroDiarioRepository.class.getClassLoader(),
                            new Class<?>[]{RegistroDiarioRepository.class},
                            (proxy, metodo, args) -> {
                                Object resultado;
                                try {
                                    resultado = metodo.invoke(alvo, args);
                                } catch (InvocationTargetException e) {
                                    throw e.getCause();
                                }
                                CyclicBarrier b = barreira;
                                if (b != null && metodo.getName().equals("buscarAtivoPorData")) {
                                    b.await(10, TimeUnit.SECONDS);
                                }
                                return resultado;
                            });
                }
            };
        }
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    private static NovoDiarioDto novo(LocalDate data, Set<Long> equipe) {
        return new NovoDiarioDto(data, Clima.ENSOLARADO, "Diário de teste", equipe);
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

    private long outroUsuario() {
        return id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Joao', 'ATIVO', 'joao-' || gen_random_uuid(), 'hash') RETURNING id
                """);
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

    private boolean booleano(String sql, Object... parametros) {
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, parametros));
    }
}
