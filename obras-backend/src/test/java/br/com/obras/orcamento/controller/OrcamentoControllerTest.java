package br.com.obras.orcamento.controller;

import br.com.obras.orcamento.service.OrcamentoService;
import br.com.obras.shared.security.JwtService;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.SecurityConfig;
import br.com.obras.shared.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Teste de permissão: 403 para quem não tem acesso ao módulo na obra. */
@WebMvcTest(controllers = {OrcamentoController.class, OrcamentoListagemController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class OrcamentoControllerTest {
    private static final Long OBRA_ID = 5L;
    private static final String URL_ORCAMENTO = "/api/obras/5/orcamento";
    private static final String ITEM_VALIDO = """
            {"codigo":"01.01","descricao":"Fundação","unidade":"m3","quantidade":10,"custoUnitarioOrcado":100}
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrcamentoService service;

    @MockBean(name = "perm")
    private PermissaoEvaluator perm;

    @MockBean
    private JwtService jwtService;

    private static RequestPostProcessor usuarioLogado() {
        var usuario = new UsuarioAutenticado(1L, "eng");
        return authentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    @Test
    void retorna401SemAutenticacao() throws Exception {
        mvc.perform(get(URL_ORCAMENTO)).andExpect(status().isUnauthorized());
    }

    @Test
    void retorna403SemPermissaoDeVisualizar() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "VISUALIZAR")).thenReturn(false);

        mvc.perform(get(URL_ORCAMENTO).with(usuarioLogado()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(service, never()).buscarPorObra(anyLong());
    }

    @Test
    void retorna403AoCriarItemSemPermissaoDeCriar() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "CRIAR")).thenReturn(false);

        mvc.perform(post(URL_ORCAMENTO + "/itens").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(ITEM_VALIDO))
                .andExpect(status().isForbidden());
        verify(service, never()).adicionarItem(anyLong(), any());
    }

    @Test
    void retorna403AoRegistrarDecisaoSemPermissaoDeAprovar() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "APROVAR")).thenReturn(false);
        String decisao = """
                {"descricao":"Aditivo","justificativa":"Reajuste","valorImpacto":100}
                """;

        mvc.perform(post(URL_ORCAMENTO + "/decisoes").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(decisao))
                .andExpect(status().isForbidden());
    }

    @Test
    void retorna403AoEditarDecisaoSemPermissaoDeAprovar() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "APROVAR")).thenReturn(false);
        String decisao = """
                {"descricao":"Aditivo","justificativa":"Reajuste","valorImpacto":100}
                """;

        mvc.perform(put(URL_ORCAMENTO + "/decisoes/3").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(decisao))
                .andExpect(status().isForbidden());
        verify(service, never()).editarDecisao(anyLong(), anyLong(), any());
    }

    @Test
    void retorna403AoEditarCustoSemPermissaoDeEditar() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "EDITAR")).thenReturn(false);
        String custo = """
                {"descricao":"Concreto","valor":100,"origem":"MATERIAL"}
                """;

        mvc.perform(put(URL_ORCAMENTO + "/itens/1/custos/2").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(custo))
                .andExpect(status().isForbidden());
        verify(service, never()).editarCusto(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void retorna403AoRemoverCustoSemPermissaoDeExcluir() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "EXCLUIR")).thenReturn(false);

        mvc.perform(delete(URL_ORCAMENTO + "/itens/1/custos/2").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verify(service, never()).removerCusto(anyLong(), anyLong(), anyLong());
    }

    @Test
    void retorna204AoRemoverCustoComPermissao() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "EXCLUIR")).thenReturn(true);

        mvc.perform(delete(URL_ORCAMENTO + "/itens/1/custos/2").with(usuarioLogado()))
                .andExpect(status().isNoContent());
        verify(service).removerCusto(OBRA_ID, 1L, 2L);
    }

    @Test
    void retorna201AoCriarItemComPermissao() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "CRIAR")).thenReturn(true);

        mvc.perform(post(URL_ORCAMENTO + "/itens").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(ITEM_VALIDO))
                .andExpect(status().isCreated());
        verify(service).adicionarItem(eq(OBRA_ID), any());
    }

    @Test
    void retorna400ComCamposQuandoItemInvalido() throws Exception {
        when(perm.pode(OBRA_ID, "ORCAMENTO", "CRIAR")).thenReturn(true);
        String itemSemCodigo = """
                {"codigo":"","descricao":"Fundação","unidade":"m3","quantidade":0,"custoUnitarioOrcado":100}
                """;

        mvc.perform(post(URL_ORCAMENTO + "/itens").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON).content(itemSemCodigo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(2));
    }

    @Test
    void listagemExigeAutenticacao() throws Exception {
        mvc.perform(get("/api/orcamentos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orcamentos").with(usuarioLogado())).andExpect(status().isOk());
    }
}
