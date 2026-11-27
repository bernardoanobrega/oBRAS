package br.com.obras.obra.controller;

import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.obra.dto.UsuarioCandidatoDto;
import br.com.obras.obra.service.AlocacaoObraService;
import br.com.obras.shared.exception.GlobalExceptionHandler;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlocacaoObraController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class AlocacaoObraControllerTest {
    private static final String BASE = "/api/obras/7/alocacoes";
    private static final Long OBRA_ID = 7L;
    private static final Long ALOCACAO_ID = 100L;

    @Autowired private MockMvc mvc;
    @MockBean private AlocacaoObraService service;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    @Test
    void getListaRetornaAlocacoes() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(service.listar(OBRA_ID)).thenReturn(List.of(dto()));

        mvc.perform(get(BASE).with(usuarioLogado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ALOCACAO_ID))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void getListaBloqueiaSemPermissao() throws Exception {
        mvc.perform(get(BASE).with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void getCandidatosRetornaUsuariosAtivos() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "CRIAR")).thenReturn(true);
        when(service.candidatos(OBRA_ID)).thenReturn(List.of(new UsuarioCandidatoDto(10L, "Ana")));

        mvc.perform(get(BASE + "/candidatos").with(usuarioLogado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void getCandidatosBloqueiaSemPermissao() throws Exception {
        mvc.perform(get(BASE + "/candidatos").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void postAlocarRetorna201() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "CRIAR")).thenReturn(true);
        when(service.alocar(eq(OBRA_ID), any())).thenReturn(dto());

        mvc.perform(post(BASE).with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"usuarioId":10,"cargoIds":[20]}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ALOCACAO_ID));
    }

    @Test
    void postAlocarRejeitaCargosVazios() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "CRIAR")).thenReturn(true);

        mvc.perform(post(BASE).with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"usuarioId":10,"cargoIds":[]}
                            """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void postAlocarBloqueiaSemPermissao() throws Exception {
        mvc.perform(post(BASE).with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"usuarioId":10,"cargoIds":[20]}
                            """))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void patchEncerrarRetorna204() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "EDITAR")).thenReturn(true);

        mvc.perform(patch(BASE + "/" + ALOCACAO_ID + "/encerrar").with(usuarioLogado()))
                .andExpect(status().isNoContent());
        verify(service).encerrar(OBRA_ID, ALOCACAO_ID);
    }

    @Test
    void patchEncerrarBloqueiaSemPermissao() throws Exception {
        mvc.perform(patch(BASE + "/" + ALOCACAO_ID + "/encerrar").with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void putCargosAtualiza() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "EDITAR")).thenReturn(true);
        when(service.atualizarCargos(eq(OBRA_ID), eq(ALOCACAO_ID), any())).thenReturn(dto());

        mvc.perform(put(BASE + "/" + ALOCACAO_ID + "/cargos").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"cargoIds":[20]}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ALOCACAO_ID));
    }

    @Test
    void putCargosRejeitaCargosVazios() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "EDITAR")).thenReturn(true);

        mvc.perform(put(BASE + "/" + ALOCACAO_ID + "/cargos").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"cargoIds":[]}
                            """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void putCargosBloqueiaSemPermissao() throws Exception {
        mvc.perform(put(BASE + "/" + ALOCACAO_ID + "/cargos").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"cargoIds":[20]}
                            """))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    private static AlocacaoObraDto dto() {
        return new AlocacaoObraDto(ALOCACAO_ID, 10L, "Ana", "ana",
                LocalDate.of(2026, 1, 1), null, List.of());
    }

    private static RequestPostProcessor usuarioLogado() {
        return authentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(10L, "ana"), null, List.of()));
    }
}
