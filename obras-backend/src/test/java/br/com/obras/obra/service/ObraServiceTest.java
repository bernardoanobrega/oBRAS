package br.com.obras.obra.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.domain.RegistroAuditoria;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.domain.StatusObra;
import br.com.obras.obra.domain.Tipologia;
import br.com.obras.obra.dto.AtualizarObraDto;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.mapper.ObraMapper;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {
    private static final Long USUARIO_ID = 10L;
    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);
    
    @Mock private ObraRepository obras;
    @Mock private AlocacaoObraRepository alocacoes;
    @Mock private AutorizacaoPort autorizacao;
    @Spy private ObraMapper mapper = new ObraMapper();
    @Mock private AuditoriaService auditoria;
    @Mock private UsuarioConsultaPort usuarios;
    @InjectMocks private ObraService service;

    @BeforeEach
    void autenticar() {
        var auth = new UsernamePasswordAuthenticationToken( new UsuarioAutenticado(USUARIO_ID, "ana"), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarRetornaSomenteObrasComPermissao() {
        Obra permitida = obraMock(1L);
        Obra negada = mock(Obra.class);
        when(negada.getId()).thenReturn(2L);

        when(obras.listarAtivas()).thenReturn(List.of(permitida, negada));
        when(autorizacao.possuiPermissao(USUARIO_ID, 1L, ModuloSistema.DETALHES_OBRA, AcaoPermissao.VISUALIZAR)).thenReturn(true);
        when(autorizacao.possuiPermissao(USUARIO_ID, 2L, ModuloSistema.DETALHES_OBRA, AcaoPermissao.VISUALIZAR)).thenReturn(false);

        List<ObraDto> result = service.listar();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Obra 1", result.get(0).nome());
    }

    @Test
    void buscarRetornaDetalhe() {
        Obra obra = obraMock(1L);
        when(obras.buscarAtiva(1L)).thenReturn(Optional.of(obra));

        ObraDto dto = service.buscar(1L);

        assertEquals(1L, dto.id());
        assertEquals("RESIDENCIAL", dto.tipologia());
        assertEquals("PLANEJADA", dto.status());
        assertEquals(99L, dto.responsavelId());
    }

    @Test
    void buscaObraInexistenteRetorna404() {
        when(obras.buscarAtiva(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L));
    }

    @Test
    void equipeRetornaMembrosAtivos() {
        Obra obra = mock(Obra.class);
        when(obras.buscarAtiva(1L)).thenReturn(Optional.of(obra));

        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(5L);
        when(usuario.getNome()).thenReturn("Carlos Mendes");

        Cargo cargo = mock(Cargo.class);
        when(cargo.getNome()).thenReturn("Engenheiro Residente");

        AlocacaoObra alocacao = mock(AlocacaoObra.class);
        when(alocacao.getUsuario()).thenReturn(usuario);
        when(alocacao.getCargos()).thenReturn(Set.of(cargo));
        when(alocacao.getDataInicio()).thenReturn(LocalDate.of(2026, 1, 10));
        
        when(alocacoes.findEquipeAtiva(1L)).thenReturn(List.of(alocacao));

        List<EquipeMembroDto> equipe = service.equipe(1L);

        assertEquals(1, equipe.size());
        assertEquals(5L, equipe.get(0).usuarioId());
        assertEquals("Carlos Mendes", equipe.get(0).nome());
        assertEquals(List.of("Engenheiro Residente"), equipe.get(0).cargos());
    }

    @Test
    void equipeDeObraInexistenteLanca404() {
        when(obras.buscarAtiva(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.equipe(99L));
        verifyNoInteractions(alocacoes);
    }

    @Test
    void criaObraComAuditoriaDeCriacao() {
        Auditavel auditavel = Auditavel.novo();
        NovaObraDto dto = novaObraDto(null);
        when(auditoria.criar(AcaoAuditoria.CRIACAO, null)).thenReturn(auditavel);
        when(obras.save(any(Obra.class))).thenAnswer(invocation -> (Obra) invocation.getArgument(0));

        ObraDto resultado = service.criar(dto);

        assertAll(
                () -> assertNotNull(resultado),
                () -> assertEquals("Obra nova", resultado.nome()),
                () -> assertEquals(StatusObra.PLANEJADA.name(), resultado.status())
        );
        verify(auditoria).criar(AcaoAuditoria.CRIACAO, null);
        verify(auditoria, never()).registrar(any(RegistroAuditoria.class));
        verify(auditoria, never()).registrar(any(Auditavel.class), any(AcaoAuditoria.class), any());
        verify(obras).save(any(Obra.class));
    }

    @Test
    void atualizaParametrosEAudita() {
        Obra obra = obraReal(5L);
        when(obras.buscarAtiva(1L)).thenReturn(Optional.of(obra));

        ObraDto resultado = service.atualizar(1L, atualizarObraDto(null));

        assertAll(
                () -> assertEquals("Obra atualizada", resultado.nome()),
                () -> assertEquals("desc nova", resultado.descricao()),
                () -> assertEquals(Tipologia.COMERCIAL.name(), resultado.tipologia())
        );
        verify(auditoria).registrar(argThat(r -> r.getDetalhe().contains("\"campos\":\"parametros\"")));
        verify(obras).save(obra);
    }

    @Test
    void rejeitaAtualizarComResponsavel() {
        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.atualizar(1L, atualizarObraDto(7L)));

        assertEquals("Responsável muda por definirResponsavel", ex.getMessage());
        verifyNoInteractions(auditoria);
        verifyNoInteractions(obras);
    }

    @Test
    void defineResponsavelAtivo() {
        Obra obra = obraReal(5L);
        when(usuarios.estaAtivo(7L)).thenReturn(true);
        when(obras.buscarAtiva(1L)).thenReturn(Optional.of(obra));

        ObraDto resultado = service.definirResponsavel(1L, 7L);

        assertEquals(7L, resultado.responsavelId());
        verify(usuarios).estaAtivo(7L);
        verify(auditoria).registrar(argThat(r -> r.getDetalhe().contains("\"responsavelNovo\":7")));
        verify(obras).save(obra);
    }

    @Test
    void rejeitaResponsavelInativo() {
        when(usuarios.estaAtivo(8L)).thenReturn(false);

        RegraNegocioException ex = assertThrows(RegraNegocioException.class,
                () -> service.definirResponsavel(1L, 8L));

        assertEquals("Responsável deve ser um usuário ativo", ex.getMessage());
        verify(auditoria, never()).registrar(any(RegistroAuditoria.class));
        verify(obras, never()).save(any(Obra.class));
    }

    @Test
    void naoAcessaRepositoryDeUsuario() {
        Class<?>[] tipos = ObraService.class.getConstructors()[0].getParameterTypes();

        boolean temUsuarioRepository = Arrays.stream(tipos)
                .anyMatch(tipo -> tipo.getSimpleName().equals("UsuarioRepository"));

        assertFalse(temUsuarioRepository);
    }

    private static NovaObraDto novaObraDto(Long responsavelId) {
        return new NovaObraDto("Obra nova", "desc", Tipologia.RESIDENCIAL, "Rua nova",
                INICIO, FIM, null, responsavelId);
    }

    private static AtualizarObraDto atualizarObraDto(Long responsavelId) {
        return new AtualizarObraDto("Obra atualizada", "desc nova", Tipologia.COMERCIAL,
                "Rua nova", INICIO, FIM, "ART-2", responsavelId, null);
    }

    private static Obra obraReal(Long responsavelId) {
        Obra obra = Obra.cadastrar("Obra antiga", null, Tipologia.RESIDENCIAL, "Rua antiga",
                INICIO, FIM, null, responsavelId, Auditavel.novo());
        ReflectionTestUtils.setField(obra, "id", 1L);
        return obra;
    }

    private Obra obraMock(Long id) {
        Obra o = mock(Obra.class);
        when(o.getId()).thenReturn(id);
        when(o.getNome()).thenReturn("Obra " + id);
        when(o.getDescricao()).thenReturn("Descrição " + id);
        when(o.getTipologia()).thenReturn(Tipologia.RESIDENCIAL);
        when(o.getEndereco()).thenReturn("Rua " + id);
        when(o.getInicioPrevisto()).thenReturn(LocalDate.of(2026, 1, 1));
        when(o.getFimPrevisto()).thenReturn(LocalDate.of(2026, 12, 31));
        when(o.getStatus()).thenReturn(StatusObra.PLANEJADA);
        when(o.getArt()).thenReturn("ART-" + id);
        when(o.getResponsavelId()).thenReturn(99L);
        return o;
    }
}
