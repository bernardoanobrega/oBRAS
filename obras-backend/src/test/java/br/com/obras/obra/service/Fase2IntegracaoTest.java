package br.com.obras.obra.service;

import br.com.obras.AbstractPostgresIntegrationTest;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.obra.dto.CargoDto;
import br.com.obras.obra.dto.NovaAlocacaoDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.mapper.AlocacaoObraMapper;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.repository.UsuarioRepository;
import br.com.obras.usuario.service.CargoConsultaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ObraService.class, AlocacaoObraService.class, ObraMapper.class, AlocacaoObraMapper.class,
        AuditoriaService.class, CargoConsultaService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class Fase2IntegracaoTest extends AbstractPostgresIntegrationTest {
    @Autowired private ObraService obraService;
    @Autowired private AlocacaoObraService alocacaoService;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockBean private UsuarioConsultaPort usuarioPort;
    @MockBean private AutorizacaoPort autorizacao;

    private long usuarioId;
    private long inativoId;
    private long cargoId;
    private long obraId;

    @BeforeEach
    void setup() {
        usuarioId = inserir("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Ana', 'ATIVO', 'ana-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        inativoId = inserir("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Bruno', 'INATIVO', 'bruno-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        cargoId = inserir("""
                INSERT INTO cargo (nome, descricao)
                VALUES ('Engenheiro-' || gen_random_uuid(), null) RETURNING id
                """);
        obraId = criarObra("Obra fase 2", usuarioId);

        when(usuarioPort.buscarEntidade(anyLong()))
                .thenAnswer(invocacao -> usuarios.findById(invocacao.getArgument(0, Long.class)));
        when(usuarioPort.listarAtivos()).thenAnswer(invocacao ->
                usuarios.findAll().stream().filter(Usuario::estaAtivo).toList());
        when(usuarioPort.estaAtivo(anyLong())).thenAnswer(invocacao ->
                usuarios.findById(invocacao.getArgument(0, Long.class)).map(Usuario::estaAtivo).orElse(false));
        when(usuarioPort.nome(anyLong())).thenAnswer(invocacao ->
                usuarios.findById(invocacao.getArgument(0, Long.class)).map(Usuario::getNome));
        when(autorizacao.possuiPermissao(any(), any(), any(), any())).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(usuarioId, "ana"), null, List.of()));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void alocaComCargos() {
        AlocacaoObraDto dto = alocacaoService.alocar(obraId, novaAlocacao(usuarioId));

        assertAll(
                () -> assertEquals(usuarioId, dto.usuarioId()),
                () -> assertEquals(List.of(cargoId), dto.cargos().stream().map(CargoDto::id).toList()),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM alocacao_obra_cargo WHERE alocacao_obra_id = ? AND cargo_id = ?",
                        dto.id(), cargoId))
        );
    }

    @Test
    void duplicidadeAtivaSobConcorrencia() throws Exception {
        TransactionTemplate transacao = novaTransacao();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch primeiraInseriu = new CountDownLatch(1);
        CountDownLatch liberarPrimeira = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger conflitos = new AtomicInteger();

        try {
            Future<?> primeira = pool.submit(() -> transacao.executeWithoutResult(status -> {
                alocacaoService.alocar(obraId, novaAlocacao(usuarioId));
                primeiraInseriu.countDown();
                await(liberarPrimeira);
            }));
            assertTrue(primeiraInseriu.await(10, TimeUnit.SECONDS));

            Future<?> segunda = pool.submit(() -> {
                try {
                    alocacaoService.alocar(obraId, novaAlocacao(usuarioId));
                    sucessos.incrementAndGet();
                } catch (DataIntegrityViolationException | RegraNegocioException erro) {
                    conflitos.incrementAndGet();
                }
            });
            liberarPrimeira.countDown();
            primeira.get(20, TimeUnit.SECONDS);
            segunda.get(20, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertAll(
                () -> assertEquals(0, sucessos.get()),
                () -> assertEquals(1, conflitos.get()),
                () -> assertEquals(1L, contar("""
                        SELECT count(*) FROM alocacao_obra
                        WHERE obra_id = ? AND usuario_id = ? AND data_fim IS NULL
                        """, obraId, usuarioId))
        );
    }

    @Test
    void encerraENovaAlocacaoFunciona() {
        long primeira = inserirAlocacao(obraId, usuarioId);

        alocacaoService.encerrar(obraId, primeira);
        AlocacaoObraDto nova = alocacaoService.alocar(obraId, novaAlocacao(usuarioId));

        assertAll(
                () -> assertNotNull(dataFim(primeira)),
                () -> assertEquals(2L, contar("SELECT count(*) FROM alocacao_obra WHERE obra_id = ?", obraId)),
                () -> assertEquals(1L, contar("""
                        SELECT count(*) FROM alocacao_obra
                        WHERE obra_id = ? AND data_fim IS NULL
                        """, obraId)),
                () -> assertTrue(nova.id() != primeira)
        );
    }

    @Test
    void responsavelInativoFalha() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> obraService.definirResponsavel(obraId, inativoId));

        assertEquals("Responsável deve ser um usuário ativo", erro.getMessage());
    }

    @Test
    void cascataConcluir() {
        long alocacao = inserirAlocacao(obraId, usuarioId);

        ObraDto dto = obraService.concluir(obraId);

        assertAll(
                () -> assertEquals("CONCLUIDA", dto.status()),
                () -> assertEquals(LocalDate.now(), dataFim(alocacao))
        );
    }

    @Test
    void cascataCancelar() {
        long alocacao = inserirAlocacao(obraId, usuarioId);

        ObraDto dto = obraService.cancelar(obraId);

        assertAll(
                () -> assertEquals("CANCELADA", dto.status()),
                () -> assertEquals(LocalDate.now(), dataFim(alocacao))
        );
    }

    @Test
    void paralisarPreservaAlocacoes() {
        long alocacao = inserirAlocacao(obraId, usuarioId);

        ObraDto dto = obraService.paralisar(obraId);

        assertAll(
                () -> assertEquals("PARALISADA", dto.status()),
                () -> assertNull(dataFim(alocacao))
        );
    }

    private TransactionTemplate novaTransacao() {
        TransactionTemplate transacao = new TransactionTemplate(transactionManager);
        transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transacao;
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(erro);
        }
    }

    private NovaAlocacaoDto novaAlocacao(long usuario) {
        return new NovaAlocacaoDto(usuario, List.of(cargoId));
    }

    private long criarObra(String nome, long responsavel) {
        long auditavel = inserir("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return inserir("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua 1', DATE '2026-01-01', DATE '2026-12-31',
                        'EM_ANDAMENTO', ?, ?) RETURNING id
                """, nome, responsavel, auditavel);
    }

    private long inserirAlocacao(long obra, long usuario) {
        long alocacao = inserir("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obra, usuario);
        jdbc.update("INSERT INTO alocacao_obra_cargo (alocacao_obra_id, cargo_id) VALUES (?, ?)",
                alocacao, cargoId);
        return alocacao;
    }

    private LocalDate dataFim(long alocacao) {
        return jdbc.queryForObject("SELECT data_fim FROM alocacao_obra WHERE id = ?",
                LocalDate.class, alocacao);
    }

    private long inserir(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private long contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
