package br.com.obras.obra.service;

import br.com.obras.AbstractPostgresIntegrationTest;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ObraService.class, ObraMapper.class, PermissaoEvaluator.class})
class ObraCascataIntegracaoTest extends AbstractPostgresIntegrationTest {
    @Autowired private ObraService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockBean private AuditoriaService auditoria;
    @MockBean private UsuarioConsultaPort usuarios;
    @MockBean private AutorizacaoPort autorizacao;

    private long obraId;
    private long alocacaoId;

    @BeforeEach
    void setup() {
        long usuario = inserir("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Ana', 'ATIVO', 'ana-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        long auditavel = inserir("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        obraId = inserir("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES ('Obra cascata', 'RESIDENCIAL', 'Rua 1', DATE '2026-01-01',
                        DATE '2026-12-31', 'EM_ANDAMENTO', ?, ?) RETURNING id
                """, usuario, auditavel);
        alocacaoId = inserir("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obraId, usuario);

        Auditavel entidade = Auditavel.novo();
        ReflectionTestUtils.setField(entidade, "id", auditavel);
        when(auditoria.buscar(anyLong())).thenReturn(Optional.of(entidade));
        when(usuarios.estaAtivo(anyLong())).thenReturn(true);
        when(autorizacao.possuiPermissao(any(), any(), any(), any())).thenReturn(false);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(usuario, "ana"), null, List.of()));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void falhaNaCascataNaoMudaStatus() {
        doThrow(new RuntimeException("falha na auditoria")).when(auditoria).registrar(any());
        TransactionTemplate transacao = new TransactionTemplate(transactionManager);
        transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThrows(RuntimeException.class,
                () -> transacao.executeWithoutResult(status -> service.concluir(obraId)));

        assertEquals("EM_ANDAMENTO",
                jdbc.queryForObject("SELECT status FROM obra WHERE id = ?", String.class, obraId));
        assertNull(jdbc.queryForObject("SELECT data_fim FROM alocacao_obra WHERE id = ?",
                LocalDate.class, alocacaoId));
    }

    private long inserir(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
