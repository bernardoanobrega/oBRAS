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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrestadorController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class PrestadorControllerSecurityTest {
    private static final String BASE = "/api/prestadores";
    private static final String DADOS = """
            {"nomeRazaoSocial":"Prestador","cpfCnpj":"12345678901","contato":"contato"}
            """;

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
        return Stream.of(
            Arguments.of("GET /prestadores", get(BASE)),
            Arguments.of("POST /prestadores", post(BASE).contentType(MediaType.APPLICATION_JSON).content(DADOS)),
            Arguments.of("PUT /prestadores/{id}", put(BASE + "/9").contentType(MediaType.APPLICATION_JSON).content(DADOS)),
            Arguments.of("PATCH /desativar", patch(BASE + "/9/desativar")),
            Arguments.of("GET /historico", get(BASE + "/9/historico"))
        );
    }

    private static RequestPostProcessor usuarioLogado() {
        return authentication(new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticado(10L, "ana"), null, List.of()));
    }
}
