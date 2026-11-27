package br.com.obras.obra.controller;

import br.com.obras.shared.exception.GlobalExceptionHandler;
import br.com.obras.shared.port.CargoConsultaPort;
import br.com.obras.shared.security.JwtService;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.SecurityConfig;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.usuario.domain.Cargo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CargoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class CargoControllerTest {
    private static final String BASE = "/api/cargos";

    @Autowired private MockMvc mvc;
    @MockBean private CargoConsultaPort cargos;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    @Test
    void getCargoRetornaCargos() throws Exception {
        permissao();
        Cargo cargo = new Cargo("Engenheiro", null);
        ReflectionTestUtils.setField(cargo, "id", 20L);
        when(cargos.listar()).thenReturn(List.of(cargo));

        mvc.perform(get(BASE).with(usuarioLogado()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].nome").value("Engenheiro"));
    }

    @Test
    void getCargoBloqueiaSemPermissao() throws Exception {
        mvc.perform(get(BASE).with(usuarioLogado()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(cargos);
    }

    @Test
    void postCargoNaoImplementado() throws Exception {
        permissao();

        mvc.perform(post(BASE).with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void postCargoBloqueiaSemPermissao() throws Exception {
        mvc.perform(post(BASE).with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void putCargoNaoImplementado() throws Exception {
        permissao();

        mvc.perform(put(BASE + "/20").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void putCargoBloqueiaSemPermissao() throws Exception {
        mvc.perform(put(BASE + "/20").with(usuarioLogado())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCargoNaoImplementado() throws Exception {
        permissao();

        mvc.perform(delete(BASE + "/20").with(usuarioLogado()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCargoBloqueiaSemPermissao() throws Exception {
        mvc.perform(delete(BASE + "/20").with(usuarioLogado()))
                .andExpect(status().isForbidden());
    }

    private void permissao() {
        when(perm.podeGlobal("DETALHES_OBRA", "EDITAR")).thenReturn(true);
        when(perm.podeEmAlgumaObra("DETALHES_OBRA", "EDITAR")).thenReturn(true);
    }

    private static RequestPostProcessor usuarioLogado() {
        return authentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(10L, "ana"), null, List.of()));
    }
}
