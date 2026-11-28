package br.com.obras.documento.controller;

import br.com.obras.documento.service.DocumentoService;
import br.com.obras.shared.security.JwtService;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.SecurityConfig;
import br.com.obras.shared.security.UsuarioAutenticado;
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

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentoController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class DocumentoControllerSecurityTest {
    private static final String BASE = "/api/documentos";
    private static final Long OBRA_ID = 5L;

    @Autowired private MockMvc mvc;
    @MockBean private DocumentoService service;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    @ParameterizedTest(name = "{0} sem token → 401")
    @MethodSource("rotas")
    void semTokenRetorna401EmTodasAsRotas(String nome, MockHttpServletRequestBuilder requisicao) throws Exception {
        mvc.perform(requisicao).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @ParameterizedTest(name = "{0} sem permissão → 403")
    @MethodSource("rotas")
    void semPermissaoRetorna403EmTodasAsRotas(String nome, MockHttpServletRequestBuilder requisicao) throws Exception {
        mvc.perform(requisicao.with(usuarioLogado())).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    static Stream<Arguments> rotas() {
        String obra = BASE + "/obras/" + OBRA_ID;
        return Stream.of(
            Arguments.of("GET /obras", get(BASE + "/obras")),
            Arguments.of("GET /obras/{obraId}", get(obra)),
            Arguments.of("GET /prestadores", get(obra + "/prestadores")),
            Arguments.of("GET /documento", get(obra + "/9")),
            Arguments.of("POST /obras/{obraId}", multipart(obra)
                .file(pdf())
                .param("codigo", "CT-1")
                .param("titulo", "Contrato")
                .param("categoria", "CONTRATO")
                .param("descricao", "Inicial")),
            Arguments.of("POST /versoes", multipart(obra + "/9/versoes")
                .file(pdf())
                .param("descricao", "Revisão")),
            Arguments.of("PUT /prestador", put(obra + "/9/prestador")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prestadorId\":9}")),
            Arguments.of("GET /arquivo", get(obra + "/9/versoes/1/arquivo"))
        );
    }

    private static MockMultipartFile pdf() {
        return new MockMultipartFile("arquivo", "projeto.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4".getBytes());
    }

    private static RequestPostProcessor usuarioLogado() {
        return authentication(new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticado(10L, "ana"), null, List.of()));
    }
}
