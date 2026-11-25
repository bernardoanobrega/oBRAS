package br.com.obras.diario.dto;

import br.com.obras.shared.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validação dos DTOs de entrada do diário pelo caminho real do Spring MVC:
 * JSON → Jackson → Bean Validation → GlobalExceptionHandler.
 * Usa um controller de teste mínimo para não depender de segurança nem de service.
 */
class DiarioDtoValidacaoTest {

    @RestController
    static class ControllerDeTeste {
        @PostMapping("/teste/diario")
        void criar(@Valid @RequestBody NovoDiarioDto dto) {}

        @PostMapping("/teste/diario-alterar")
        void alterar(@Valid @RequestBody AlterarDiarioDto dto) {}
    }

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void dtoDiarioValidoPassa() throws Exception {
        enviar("/teste/diario", """
                {"data":"2026-11-18","clima":"ENSOLARADO","descricao":"ok"}
                """).andExpect(status().isOk());
    }

    @Test
    void dtoDiarioRejeitaClimaNulo() throws Exception {
        enviar("/teste/diario", """
                {"data":"2026-11-18","descricao":"sem clima"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("clima"));
    }

    @Test
    void dtoDiarioRejeitaClimaInvalido() throws Exception {
        enviar("/teste/diario", """
                {"data":"2026-11-18","clima":"NEVOA"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos[0].campo").value("clima"));
    }

    @Test
    void dtoAlterarDiarioRejeitaClimaInvalido() throws Exception {
        enviar("/teste/diario-alterar", """
                {"clima":"NEVOA"}
                """).andExpect(status().isBadRequest());
    }

    @Test
    void jsonQuebradoDevolve400ENao500() throws Exception {
        enviar("/teste/diario", "{ isto não é json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
