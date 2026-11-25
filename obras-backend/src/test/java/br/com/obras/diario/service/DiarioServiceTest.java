package br.com.obras.diario.service;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.diario.dto.NovaOcorrenciaDto;
import br.com.obras.diario.dto.NovaOcorrenciaRapidaDto;
import br.com.obras.diario.dto.NovoDiarioDto;
import br.com.obras.diario.mapper.DiarioMapper;
import br.com.obras.diario.repository.ContagemOcorrencias;
import br.com.obras.diario.repository.OcorrenciaRepository;
import br.com.obras.diario.repository.ParalisacaoRepository;
import br.com.obras.diario.repository.RegistroDiarioRepository;
import br.com.obras.diario.repository.RegistroFotograficoRepository;
import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.shared.storage.ArquivoArmazenado;
import br.com.obras.shared.storage.ArquivoStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiarioServiceTest {
    private static final Long OBRA_ID = 2L;
    private static final Long USUARIO_ID = 10L;
    private static final LocalDate DATA = LocalDate.of(2026, 11, 18);

    @Mock private RegistroDiarioRepository diarios;
    @Mock private OcorrenciaRepository ocorrencias;
    @Mock private ParalisacaoRepository paralisacoes;
    @Mock private RegistroFotograficoRepository fotos;
    @Mock private ObraService obras;
    @Mock private AuditoriaService auditoria;
    @Mock private ArquivoStorageService storage;
    @Spy private DiarioMapper mapper = new DiarioMapper();
    @InjectMocks private DiarioService service;

    @BeforeEach
    void autenticar() {
        var auth = new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticado(USUARIO_ID, "admin"), null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void criarDiarioSalvaEquipePresente() {
        Obra obra = obra();
        when(obras.obterEntidade(OBRA_ID)).thenReturn(obra);
        when(diarios.buscarAtivoPorData(OBRA_ID, DATA)).thenReturn(Optional.empty());
        AlocacaoObra alocacao = alocacao(7L);
        when(obras.alocacoesAtivasPorIds(OBRA_ID, Set.of(7L))).thenReturn(List.of(alocacao));
        when(auditoria.criar(any(AcaoAuditoria.class), isNull())).thenAnswer(i -> auditavel(100L));
        when(diarios.saveAndFlush(any(RegistroDiario.class))).thenAnswer(i -> i.getArgument(0));

        var dto = new NovoDiarioDto(DATA, Clima.ENSOLARADO, "Concretagem", Set.of(7L));
        var result = service.criar(OBRA_ID, dto);

        assertEquals(Clima.ENSOLARADO.name(), result.clima());
        assertEquals(7L, result.equipePresente().get(0).alocacaoId());
        assertEquals("Maria", result.equipePresente().get(0).nome());
        verify(auditoria).criar(AcaoAuditoria.CRIACAO, null);
    }

    @Test
    void criarDiarioDuplicadoLancaRegraNegocio() {
        Obra obra = obra();
        when(obras.obterEntidade(OBRA_ID)).thenReturn(obra);
        when(diarios.buscarAtivoPorData(OBRA_ID, DATA)).thenReturn(Optional.of(diario(auditavel(1L), obra)));

        var dto = new NovoDiarioDto(DATA, Clima.NUBLADO, "Duplicado", Set.of());

        RegraNegocioException e = assertThrows(
            RegraNegocioException.class, () -> service.criar(OBRA_ID, dto)
        );
        assertEquals("Já existe registro para o dia", e.getMessage());
        verify(diarios, never()).saveAndFlush(any());
    }

    @Test
    void ocorrenciaRapidaSemDiarioExigeClima() {
        Obra obra = obra();
        when(obras.obterEntidade(OBRA_ID)).thenReturn(obra);
        when(diarios.buscarAtivoPorData(OBRA_ID, DATA)).thenReturn(Optional.empty());

        var dto = new NovaOcorrenciaRapidaDto(
            DATA, null, CategoriaOcorrencia.OUTRO, "Fachada leste", "Fachada leste", DATA.atTime(9, 0), null
        );

        RegraNegocioException e = assertThrows(
            RegraNegocioException.class, () -> service.ocorrenciaRapida(OBRA_ID, dto)
        );
        assertEquals("Clima é obrigatório quando não existe diário no dia", e.getMessage());
        verifyNoInteractions(auditoria);
    }

    @Test
    void ocorrenciaRapidaComCategoriaNulaUsaOutro() {
        prepararOcorrenciaRapidaEmDiarioExistente();

        var dto = new NovaOcorrenciaRapidaDto(
            DATA, null, null, "Fachada leste", null, DATA.atTime(9, 0), null
        );

        assertEquals(CategoriaOcorrencia.OUTRO.name(), service.ocorrenciaRapida(OBRA_ID, dto).categoria());
    }

    @Test
    void ocorrenciaRapidaMantemCategoriaInformada() {
        prepararOcorrenciaRapidaEmDiarioExistente();

        var dto = new NovaOcorrenciaRapidaDto(
            DATA, null, CategoriaOcorrencia.SEGURANCA, "Andaime sem guarda-corpo", null, DATA.atTime(9, 0), null
        );

        assertEquals(CategoriaOcorrencia.SEGURANCA.name(), service.ocorrenciaRapida(OBRA_ID, dto).categoria());
    }

    /** L32: o autor é a alocação do usuário do token; o payload nem tem campo de autor. */
    @Test
    void autorNaoVemDoPayload() {
        Obra obra = obra();
        when(diarios.buscar(OBRA_ID, 100L)).thenReturn(Optional.of(diario(auditavel(100L), obra)));
        AlocacaoObra doToken = alocacao(7L);
        when(obras.alocacaoAtiva(OBRA_ID, USUARIO_ID)).thenReturn(Optional.of(doToken));
        when(auditoria.criar(any(AcaoAuditoria.class), isNull())).thenAnswer(i -> auditavel(200L));
        when(ocorrencias.saveAndFlush(any(Ocorrencia.class))).thenAnswer(i -> i.getArgument(0));

        var dto = new NovaOcorrenciaDto(CategoriaOcorrencia.OUTRO, "Registro", DATA.atTime(9, 0), null);
        var result = service.adicionarOcorrencia(OBRA_ID, 100L, dto);

        var salva = org.mockito.ArgumentCaptor.forClass(Ocorrencia.class);
        verify(ocorrencias).saveAndFlush(salva.capture());
        verify(obras).alocacaoAtiva(OBRA_ID, USUARIO_ID);
        assertSame(doToken, salva.getValue().getAlocacaoObra(), "autor = alocação do usuário autenticado");
        assertEquals(7L, result.autor().alocacaoId());
        assertTrue(java.util.Arrays.stream(NovaOcorrenciaDto.class.getRecordComponents())
                .noneMatch(c -> c.getName().toLowerCase().contains("autor")
                        || c.getName().toLowerCase().contains("usuario")),
            "o DTO de entrada não aceita autor nem usuário");
    }

    private void prepararOcorrenciaRapidaEmDiarioExistente() {
        Obra obra = obra();
        when(obras.obterEntidade(OBRA_ID)).thenReturn(obra);
        when(diarios.buscarAtivoPorData(OBRA_ID, DATA)).thenReturn(Optional.of(diario(auditavel(100L), obra)));
        AlocacaoObra autor = alocacao(7L);
        when(obras.alocacaoAtiva(OBRA_ID, USUARIO_ID)).thenReturn(Optional.of(autor));
        when(auditoria.criar(any(AcaoAuditoria.class), isNull())).thenAnswer(i -> auditavel(200L));
        when(ocorrencias.saveAndFlush(any(Ocorrencia.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void anexarFotoRejeitaPdfEDescarta() {
        Obra obra = obra();
        RegistroDiario diario = diario(auditavel(100L), obra);
        Ocorrencia ocorrencia = Ocorrencia.criar(
            auditavel(200L), diario, alocacao(7L), DATA.atTime(9, 0), CategoriaOcorrencia.OUTRO, "Fachada"
        );
        ReflectionTestUtils.setField(ocorrencia, "id", 300L);
        when(ocorrencias.buscar(OBRA_ID, 300L)).thenReturn(Optional.of(ocorrencia));

        MultipartFile arquivo = mock(MultipartFile.class);
        when(storage.armazenar(arquivo)).thenReturn(
            new ArquivoArmazenado("fachada.pdf", "PDF", 10, "2026/11/x.pdf")
        );

        RegraNegocioException e = assertThrows(
            RegraNegocioException.class, () -> service.anexarFoto(OBRA_ID, 300L, arquivo, "Fachada")
        );
        assertEquals("Fotos: use PNG ou JPG", e.getMessage());
        verify(storage).descartar("2026/11/x.pdf");
        verifyNoInteractions(fotos);
    }

    @Test
    void excluirDiarioMarcaAuditavelEExcluido() {
        Auditavel auditavel = auditavel(100L);
        RegistroDiario diario = diario(auditavel, obra());
        when(diarios.buscar(OBRA_ID, 100L)).thenReturn(Optional.of(diario));
        doAnswer(i -> {
            i.getArgument(0, Auditavel.class).excluir();
            return null;
        }).when(auditoria).excluir(any(Auditavel.class));

        service.excluir(OBRA_ID, 100L);

        assertTrue(auditavel.isExcluido());
        assertTrue(diario.isExcluido());
        verify(auditoria).excluir(auditavel);
        verify(diarios).saveAndFlush(diario);
    }

    @Test
    void listarContaOcorrenciasNumaConsultaSo() {
        Obra obra = obra();
        RegistroDiario comOcorrencias = diario(auditavel(100L), obra);
        RegistroDiario semOcorrencias = diario(auditavel(101L), obra);
        when(diarios.listarPorObra(OBRA_ID)).thenReturn(List.of(comOcorrencias, semOcorrencias));
        when(ocorrencias.contarPorDiarios(List.of(100L, 101L)))
            .thenReturn(List.of(new ContagemOcorrencias(100L, 3L)));

        var resumo = service.listar(OBRA_ID);

        assertEquals(3L, resumo.get(0).totalOcorrencias());
        assertEquals(0L, resumo.get(1).totalOcorrencias(), "diário sem ocorrência não volta na contagem");
        verify(ocorrencias, times(1)).contarPorDiarios(anyCollection());
    }

    private Obra obra() {
        Obra obra = mock(Obra.class);
        lenient().when(obra.getId()).thenReturn(OBRA_ID);
        return obra;
    }

    private Auditavel auditavel(Long id) {
        Auditavel auditavel = Auditavel.novo();
        ReflectionTestUtils.setField(auditavel, "id", id);
        return auditavel;
    }

    private AlocacaoObra alocacao(Long id) {
        Usuario usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(USUARIO_ID);
        lenient().when(usuario.getNome()).thenReturn("Maria");
        AlocacaoObra alocacao = mock(AlocacaoObra.class);
        lenient().when(alocacao.getId()).thenReturn(id);
        lenient().when(alocacao.getUsuario()).thenReturn(usuario);
        return alocacao;
    }

    private RegistroDiario diario(Auditavel auditavel, Obra obra) {
        RegistroDiario diario = RegistroDiario.criar(
            auditavel, obra, DATA, Clima.NUBLADO, "Registro", Set.of()
        );
        ReflectionTestUtils.setField(diario, "id", auditavel.getId());
        return diario;
    }
}
