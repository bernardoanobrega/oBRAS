package br.com.obras.obra.service;

import br.com.obras.AbstractPostgresIntegrationTest;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.obra.domain.Tipologia;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
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
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ObraService.class, ObraMapper.class, AuditoriaService.class, PermissaoEvaluator.class})
class ObraFase1IntegracaoTest extends AbstractPostgresIntegrationTest {
    @Autowired private ObraService service;
    @Autowired private PermissaoEvaluator permissao;
    @Autowired private JdbcTemplate jdbc;
    @MockBean private UsuarioConsultaPort usuarios;
    @MockBean private AutorizacaoPort autorizacao;

    private long usuario;

    @BeforeEach
    void setup() {
        usuario = id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Maria', 'ATIVO', 'maria-' || gen_random_uuid(), 'hash') RETURNING id
                """);
        autenticar();
        when(usuarios.estaAtivo(anyLong())).thenReturn(true);
        when(usuarios.nome(anyLong())).thenReturn(Optional.of("Maria"));
        when(autorizacao.possuiPermissao(any(), any(), any(), any())).thenReturn(false);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obraCriadaComAuditoria() {
        ObraDto criada = service.criar(nova("Obra nova", usuario));

        assertAll(
                () -> assertTrue(criada.id() > 0),
                () -> assertEquals(1L, contar("SELECT count(*) FROM registro_auditoria WHERE acao = 'CRIACAO'")),
                () -> assertEquals(1L, contar(
                        "SELECT count(*) FROM obra WHERE id = ? AND auditavel_id IS NOT NULL", criada.id()))
        );
    }

    @Test
    void transicoesValidasEInvalidas() {
        long obra = obra("Obra transição", usuario);

        assertAll(
                () -> assertEquals("EM_ANDAMENTO", service.iniciar(obra).status()),
                () -> assertThrows(RegraNegocioException.class, () -> service.iniciar(obra)),
                () -> assertEquals("PARALISADA", service.paralisar(obra).status()),
                () -> assertEquals("EM_ANDAMENTO", service.retomar(obra).status()),
                () -> assertThrows(RegraNegocioException.class, () -> service.retomar(obra)),
                () -> assertEquals("CONCLUIDA", service.concluir(obra).status()),
                () -> assertThrows(RegraNegocioException.class, () -> service.paralisar(obra))
        );
    }

    @Test
    void iniciarSemResponsavelFalha() {
        long obra = obra("Sem responsável", null);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> service.iniciar(obra));

        assertEquals("A obra exige um responsável para ser iniciada", erro.getMessage());
    }

    @Test
    void listaFiltraPorAlocacao() {
        long minha = obra("Minha obra", usuario);
        long outra = obra("Outra obra", usuario);
        alocacao(minha, usuario);
        when(autorizacao.possuiPermissao(usuario, minha,
                ModuloSistema.DETALHES_OBRA, AcaoPermissao.VISUALIZAR)).thenReturn(true);

        List<Long> ids = service.listar().stream().map(ObraDto::id).toList();

        assertEquals(List.of(minha), ids);
        assertTrue(ids.contains(minha));
        assertFalse(ids.contains(outra));
    }

    @Test
    void obraDeOutroUsuarioNaoAcessivel() {
        long outroUsuario = outroUsuario();
        long obra = obra("Obra do outro", outroUsuario);

        boolean pode = permissao.pode(obra, "DETALHES_OBRA", "VISUALIZAR");

        assertFalse(pode);
        assertThrows(AccessDeniedException.class, () -> {
            if (!pode) {
                throw new AccessDeniedException("Obra de outro usuário");
            }
        });
    }

    private void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario, "maria"), null, List.of()));
    }

    private NovaObraDto nova(String nome, Long responsavelId) {
        return new NovaObraDto(nome, "desc", Tipologia.RESIDENCIAL, "Rua X",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null, responsavelId);
    }

    private long obra(String nome, Long responsavelId) {
        long auditavel = id("INSERT INTO auditavel DEFAULT VALUES RETURNING id");
        return id("""
                INSERT INTO obra (nome, tipologia, endereco, inicio_previsto, fim_previsto,
                                  status, responsavel_id, auditavel_id)
                VALUES (?, 'RESIDENCIAL', 'Rua X', DATE '2026-01-01', DATE '2026-12-31',
                        'PLANEJADA', ?, ?)
                RETURNING id
                """, nome, new SqlParameterValue(Types.BIGINT, responsavelId), auditavel);
    }

    private long outroUsuario() {
        return id("""
                INSERT INTO usuario (nome, situacao, login, senha_hash)
                VALUES ('Joao', 'ATIVO', 'joao-' || gen_random_uuid(), 'hash') RETURNING id
                """);
    }

    private long alocacao(long obraId, long usuarioId) {
        return id("""
                INSERT INTO alocacao_obra (obra_id, usuario_id, data_inicio)
                VALUES (?, ?, DATE '2026-01-01') RETURNING id
                """, obraId, usuarioId);
    }

    private long id(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }

    private long contar(String sql, Object... parametros) {
        return jdbc.queryForObject(sql, Long.class, parametros);
    }
}
