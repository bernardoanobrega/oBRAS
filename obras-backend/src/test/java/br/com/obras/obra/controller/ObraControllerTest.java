package br.com.obras.obra.controller;

import br.com.obras.obra.dto.AtualizarObraDto;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.service.ObraService;
import br.com.obras.shared.exception.GlobalExceptionHandler;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ObraController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class ObraControllerTest {
    private static final String BASE = "/api/obras";
    private static final Long OBRA_ID = 7L;

    @Autowired private MockMvc mvc;
    @MockBean private ObraService service;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    @Test
    void postCriaObraRetorna201() throws Exception {
        when(perm.podeGlobal("DETALHES_OBRA", "CRIAR")).thenReturn(true);
        when(service.criar(any(NovaObraDto.class))).thenReturn(dto());

        mvc.perform(post(BASE).with(usuarioLogado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Residencial Aurora","descricao":"Bloco A",
                     "tipologia":"RESIDENCIAL","endereco":"Rua Central, 10",
                     "inicioPrevisto":"2026-01-01","fimPrevisto":"2026-12-31",
                     "art":"ART-10","responsavelId":null}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(OBRA_ID))
            .andExpect(jsonPath("$.nome").value("Residencial Aurora"));
    }

    @Test
    void postSemNomeRetorna400ComCampo() throws Exception {
        when(perm.podeGlobal("DETALHES_OBRA", "CRIAR")).thenReturn(true);

        mvc.perform(post(BASE).with(usuarioLogado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"","tipologia":"RESIDENCIAL","endereco":"Rua Central",
                     "inicioPrevisto":"2026-01-01","fimPrevisto":"2026-12-31"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.campos[0].campo").value("nome"));
        verifyNoInteractions(service);
    }

    @Test
    void getListaRetorna200() throws Exception {
        when(service.listar()).thenReturn(List.of(dto()));
        mvc.perform(get(BASE).with(usuarioLogado()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(OBRA_ID));
    }

    @Test
    void getInexistenteRetorna404() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(service.buscar(OBRA_ID)).thenThrow(new RecursoNaoEncontradoException("Obra", OBRA_ID));
        mvc.perform(get(BASE + "/" + OBRA_ID).with(usuarioLogado()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void putAtualizaRetorna200() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "EDITAR")).thenReturn(true);
        when(service.atualizar(eq(OBRA_ID), any(AtualizarObraDto.class))).thenReturn(dto());
        mvc.perform(put(BASE + "/" + OBRA_ID).with(usuarioLogado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Residencial Aurora","descricao":"Atualizada",
                     "tipologia":"RESIDENCIAL","endereco":"Rua Central, 10",
                     "inicioPrevisto":"2026-01-01","fimPrevisto":"2026-12-31",
                     "art":"ART-10","responsavelId":null,"status":null}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Residencial Aurora"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void semPermissaoRetorna403() throws Exception {
        when(perm.podeGlobal("DETALHES_OBRA", "CRIAR")).thenReturn(false);
        mvc.perform(post(BASE).with(usuarioLogado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Residencial Aurora","tipologia":"RESIDENCIAL","endereco":"Rua Central",
                     "inicioPrevisto":"2026-01-01","fimPrevisto":"2026-12-31"}
                    """))
            .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void erroSegueFormatoApiError() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(service.buscar(OBRA_ID)).thenThrow(new RecursoNaoEncontradoException("Obra", OBRA_ID));
        mvc.perform(get(BASE + "/" + OBRA_ID).with(usuarioLogado()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.mensagem").isNotEmpty())
            .andExpect(jsonPath("$.campos").isArray())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    private static ObraDto dto() {
        return new ObraDto(OBRA_ID, "Residencial Aurora", "Bloco A", "RESIDENCIAL",
            "Rua Central, 10", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
            "PLANEJADA", "ART-10", null, null, null);
    }

    private static RequestPostProcessor usuarioLogado() {
        return authentication(new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticado(10L, "ana"), null, List.of()));
    }
}
