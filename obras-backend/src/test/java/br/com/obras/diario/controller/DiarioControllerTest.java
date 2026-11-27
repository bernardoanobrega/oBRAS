package br.com.obras.diario.controller;

import br.com.obras.diario.dto.DiarioDto;
import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.MembroDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.service.DiarioService;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.JwtService;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.SecurityConfig;
import br.com.obras.shared.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * L34: contrato HTTP do diário. Sobe só a camada web (controller, segurança,
 * JSON e GlobalExceptionHandler); o service e a permissão são mocks. A regra de
 * negócio e o isolamento real entre obras estão provados nos testes do service.
 */
@WebMvcTest(controllers = DiarioController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class DiarioControllerTest {
    private static final Long OBRA_ID = 5L;
    private static final String OBRA = "/api/obras/5";
    private static final LocalDate DIA = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime DEZ_HORAS = DIA.atTime(10, 0);

    private static final String DIARIO = """
            {"data":"2026-11-18","clima":"ENSOLARADO","descricao":"Concretagem"}
            """;
    private static final String ALTERAR_DIARIO = """
            {"clima":"NUBLADO","descricao":"Concretagem"}
            """;
    private static final String OCORRENCIA = """
            {"categoria":"LOGISTICA","descricao":"Atraso do aço","dataHora":"2026-11-18T10:00:00"}
            """;
    private static final String RAPIDA_SEM_CLIMA = """
            {"data":"2026-11-18","descricao":"Fachada leste","dataHora":"2026-11-18T10:00:00"}
            """;

    @Autowired private MockMvc mvc;
    @MockBean private DiarioService service;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    // ---------- autenticação e permissão ----------

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get(OBRA + "/diarios")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    /**
     * "Testar obra alheia em todos os endpoints": o usuário pode tudo, menos a ação
     * exigida pela rota na obra da URL. Prova que cada rota confere a ação certa,
     * na obra da URL, e que o service nem é chamado. Cobre o {@code postSemPermissaoRetorna403}.
     */
    @ParameterizedTest(name = "{0} sem {1} → 403")
    @MethodSource("rotasDoContrato")
    void semPermissaoRetorna403EmTodasAsRotas(String rota, String acao, MockHttpServletRequestBuilder req)
            throws Exception {
        when(perm.pode(anyLong(), anyString(), anyString())).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", acao)).thenReturn(false);

        mvc.perform(req.with(usuarioLogado()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para esta operação"));
        verifyNoInteractions(service);
    }

    static Stream<Arguments> rotasDoContrato() {
        return Stream.of(
                Arguments.of("GET /diarios", "VISUALIZAR", get(OBRA + "/diarios")),
                Arguments.of("POST /diarios", "CRIAR", json(post(OBRA + "/diarios"), DIARIO)),
                Arguments.of("GET /diarios/{id}", "VISUALIZAR", get(OBRA + "/diarios/1")),
                Arguments.of("PUT /diarios/{id}", "EDITAR", json(put(OBRA + "/diarios/1"), ALTERAR_DIARIO)),
                Arguments.of("POST /diarios/{id}/ocorrencias", "CRIAR",
                        json(post(OBRA + "/diarios/1/ocorrencias"), OCORRENCIA)),
                Arguments.of("PUT /ocorrencias/{id}", "EDITAR", json(put(OBRA + "/ocorrencias/7"), OCORRENCIA)),
                Arguments.of("POST /ocorrencias-rapidas", "CRIAR",
                        json(post(OBRA + "/ocorrencias-rapidas"), RAPIDA_SEM_CLIMA)),
                Arguments.of("POST /ocorrencias/{id}/fotos", "CRIAR",
                        multipart(OBRA + "/ocorrencias/7/fotos").file(foto()))
        );
    }

    /** D2 (a): permissão vale por obra. Poder na obra 6 não abre a obra 5. */
    @Test
    void semPermissaoNaObraRetorna403() throws Exception {
        when(perm.pode(6L, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(false);

        mvc.perform(get(OBRA + "/diarios/1").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void permissaoGlobalNaoSubstituiEscopoDeObraSemRegra() throws Exception {
        when(perm.podeGlobal("DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(false);

        mvc.perform(get(OBRA + "/diarios/1").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void usuarioComVisualizarNaoCria() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "CRIAR")).thenReturn(false);
        when(service.listar(OBRA_ID)).thenReturn(List.of());

        mvc.perform(get(OBRA + "/diarios").with(usuarioLogado()))
                .andExpect(status().isOk());
        mvc.perform(json(post(OBRA + "/diarios"), DIARIO).with(usuarioLogado()))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioComCriarNaoExclui() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "CRIAR")).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "EXCLUIR")).thenReturn(false);

        mvc.perform(delete(OBRA + "/diarios/1").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    /**
     * D2 (b): com permissão na obra 5, pedir pela URL dela um diário de outra obra dá
     * 404, não 403, para não revelar que o id existe. O controller repassa o obraId da
     * URL; quem filtra é o service (provado em OcorrenciaServiceIntegracaoTest.rejeitaDiarioDeOutraObra).
     */
    @Test
    void diarioDeOutraObraRetorna404() throws Exception {
        permitir("VISUALIZAR");
        when(service.buscar(OBRA_ID, 99L)).thenThrow(new RecursoNaoEncontradoException("RegistroDiario", 99L));

        mvc.perform(get(OBRA + "/diarios/99").with(usuarioLogado()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        verify(service).buscar(OBRA_ID, 99L);
    }

    // ---------- status HTTP ----------

    @Test
    void postDiarioRetorna201() throws Exception {
        permitir("CRIAR");
        when(service.criar(eq(OBRA_ID), any())).thenReturn(diarioDto());

        mvc.perform(json(post(OBRA + "/diarios"), DIARIO).with(usuarioLogado()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clima").value("ENSOLARADO"));
    }

    @Test
    void getDiarioInexistenteRetorna404() throws Exception {
        permitir("VISUALIZAR");
        when(service.buscar(OBRA_ID, 404L)).thenThrow(new RecursoNaoEncontradoException("RegistroDiario", 404L));

        mvc.perform(get(OBRA + "/diarios/404").with(usuarioLogado()))
                .andExpect(status().isNotFound());
    }

    @Test
    void postOcorrenciaRetorna201() throws Exception {
        permitir("CRIAR");
        when(service.adicionarOcorrencia(eq(OBRA_ID), eq(1L), any())).thenReturn(ocorrenciaDto());

        mvc.perform(json(post(OBRA + "/diarios/1/ocorrencias"), OCORRENCIA).with(usuarioLogado()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.autor.nome").value("Maria"));
    }

    @Test
    void postOcorrenciaRapidaEFotoRetornam201() throws Exception {
        permitir("CRIAR");
        when(service.ocorrenciaRapida(eq(OBRA_ID), any())).thenReturn(ocorrenciaDto());
        when(service.anexarFoto(eq(OBRA_ID), eq(7L), any(), isNull()))
                .thenReturn(new FotoDto(30L, 7L, "foto.jpg", null, DEZ_HORAS));

        mvc.perform(json(post(OBRA + "/ocorrencias-rapidas"), RAPIDA_SEM_CLIMA).with(usuarioLogado()))
                .andExpect(status().isCreated());
        mvc.perform(multipart(OBRA + "/ocorrencias/7/fotos").file(foto()).with(usuarioLogado()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30));
    }

    @Test
    void postOcorrenciaRapidaSemClimaRetorna422() throws Exception {
        permitir("CRIAR");
        when(service.ocorrenciaRapida(eq(OBRA_ID), any()))
                .thenThrow(new RegraNegocioException("Clima é obrigatório quando não existe diário no dia"));

        mvc.perform(json(post(OBRA + "/ocorrencias-rapidas"), RAPIDA_SEM_CLIMA).with(usuarioLogado()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Clima é obrigatório quando não existe diário no dia"));
    }

    // ---------- erros padronizados ----------

    /** Todo erro sai no mesmo formato (RNF-09): status, mensagem, campos e timestamp. */
    @Test
    void erroSegueApiError() throws Exception {
        permitir("CRIAR");
        when(service.ocorrenciaRapida(eq(OBRA_ID), any()))
                .thenThrow(new RegraNegocioException("Clima é obrigatório quando não existe diário no dia"));

        mvc.perform(json(post(OBRA + "/ocorrencias-rapidas"), RAPIDA_SEM_CLIMA).with(usuarioLogado()))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensagem").value("Clima é obrigatório quando não existe diário no dia"))
                .andExpect(jsonPath("$.campos").isArray())
                .andExpect(jsonPath("$.campos").isEmpty())
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void bodyInvalidoRetorna400ComCampos() throws Exception {
        permitir("CRIAR");

        mvc.perform(json(post(OBRA + "/diarios"), "{}").with(usuarioLogado()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos[*].campo", containsInAnyOrder("data", "clima")));
        verifyNoInteractions(service);
    }

    // ---------- apoio ----------

    private void permitir(String acao) {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", acao)).thenReturn(true);
    }

    private static RequestPostProcessor usuarioLogado() {
        var usuario = new UsuarioAutenticado(1L, "eng");
        return authentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private static MockMultipartFile foto() {
        return new MockMultipartFile("arquivo", "foto.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});
    }

    private static DiarioDto diarioDto() {
        return new DiarioDto(1L, OBRA_ID, DIA, "ENSOLARADO", "Concretagem", List.of(), List.of());
    }

    private static OcorrenciaDto ocorrenciaDto() {
        return new OcorrenciaDto(7L, 1L, "LOGISTICA", "Atraso do aço", DEZ_HORAS,
                new MembroDiarioDto(3L, 1L, "Maria"), null, List.of());
    }
}
