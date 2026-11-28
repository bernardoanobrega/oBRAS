package br.com.obras.equipamento.controller;

import static br.com.obras.equipamento.controller.PermissoesEquipamento.MODULO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.obras.equipamento.dto.AlocarEquipamentoDto;
import br.com.obras.equipamento.dto.DevolverEquipamentoDto;
import br.com.obras.equipamento.dto.ProrrogarAlocacaoDto;
import br.com.obras.equipamento.dto.TransferirEquipamentoDto;
import br.com.obras.equipamento.service.AlocacaoEquipamentoService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(AlocacaoEquipamentoController.class)
@Import(MetodoSegurancaTestConfig.class)
@WithMockUser
class AlocacaoEquipamentoControllerTest {

    private static final Long OBRA = 1L;
    private static final String CORPO_ALOCAR =
            "{\"canteiroId\":10,\"responsavelId\":5,\"dataPrevistaDevolucao\":\"2030-01-10\"}";
    private static final String CORPO_DEVOLVER = "{\"observacao\":\"Sem avarias\"}";
    private static final String CORPO_TRANSFERIR =
            "{\"canteiroDestinoId\":20,\"responsavelId\":5,\"dataPrevistaDevolucao\":\"2030-01-20\"}";
    private static final String CORPO_PRORROGAR = "{\"novaDataPrevista\":\"2030-02-01\"}";

    private final MockMvc mockMvc;

    @MockBean
    private AlocacaoEquipamentoService alocacaoService;

    @MockBean(name = "perm")
    private PermissaoFalsa perm;

    @Autowired
    AlocacaoEquipamentoControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    static Stream<Arguments> requisicoes() {
        return Stream.of(
                Arguments.of("GET", "/api/obras/1/equipamentos", null),
                Arguments.of("POST", "/api/obras/1/equipamentos/7/alocacoes", CORPO_ALOCAR),
                Arguments.of("PATCH", "/api/obras/1/equipamentos/7/devolver", CORPO_DEVOLVER),
                Arguments.of("PATCH", "/api/obras/1/equipamentos/7/transferir", CORPO_TRANSFERIR),
                Arguments.of("PATCH", "/api/obras/1/alocacoes/9/prorrogar", CORPO_PRORROGAR),
                Arguments.of("GET", "/api/equipamentos/7/alocacoes", null));
    }

    private MockHttpServletRequestBuilder requisicao(String metodo, String url, String corpo) {
        MockHttpServletRequestBuilder requisicao = request(HttpMethod.valueOf(metodo), url)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON);
        return corpo == null ? requisicao : requisicao.content(corpo);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("requisicoes")
    @DisplayName("Retorna 403 quando o usuário não tem permissão")
    void retorna403QuandoSemPermissao(String metodo, String url, String corpo) throws Exception {
        mockMvc.perform(requisicao(metodo, url, corpo)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Lista alocações ativas quando tem permissão de visualizar na obra")
    void listaAlocacoesAtivasQuandoTemPermissaoDeVisualizarNaObra() throws Exception {
        when(perm.pode(OBRA, MODULO, "VISUALIZAR")).thenReturn(true);
        when(alocacaoService.listarAtivasDaObra(OBRA)).thenReturn(List.of());

        mockMvc.perform(requisicao("GET", "/api/obras/1/equipamentos", null)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aloca equipamento quando tem permissão de criar na obra")
    void alocaEquipamentoQuandoTemPermissaoDeCriarNaObra() throws Exception {
        when(perm.pode(OBRA, MODULO, "CRIAR")).thenReturn(true);

        mockMvc.perform(requisicao("POST", "/api/obras/1/equipamentos/7/alocacoes", CORPO_ALOCAR))
                .andExpect(status().isCreated());

        verify(alocacaoService).alocar(OBRA, 7L,
                new AlocarEquipamentoDto(10L, 5L, LocalDate.of(2030, 1, 10), null));
    }

    @Test
    @DisplayName("Devolve equipamento quando tem permissão de editar na obra")
    void devolveEquipamentoQuandoTemPermissaoDeEditarNaObra() throws Exception {
        when(perm.pode(OBRA, MODULO, "EDITAR")).thenReturn(true);

        mockMvc.perform(requisicao("PATCH", "/api/obras/1/equipamentos/7/devolver", CORPO_DEVOLVER))
                .andExpect(status().isOk());

        verify(alocacaoService).devolver(OBRA, 7L, new DevolverEquipamentoDto("Sem avarias"));
    }

    @Test
    @DisplayName("Transfere equipamento quando tem permissão de editar na obra")
    void transfereEquipamentoQuandoTemPermissaoDeEditarNaObra() throws Exception {
        when(perm.pode(OBRA, MODULO, "EDITAR")).thenReturn(true);

        mockMvc.perform(requisicao("PATCH", "/api/obras/1/equipamentos/7/transferir", CORPO_TRANSFERIR))
                .andExpect(status().isOk());

        verify(alocacaoService).transferir(any(), any(), any(TransferirEquipamentoDto.class));
    }

    @Test
    @DisplayName("Prorroga alocação quando tem permissão de editar na obra")
    void prorrogaAlocacaoQuandoTemPermissaoDeEditarNaObra() throws Exception {
        when(perm.pode(OBRA, MODULO, "EDITAR")).thenReturn(true);

        mockMvc.perform(requisicao("PATCH", "/api/obras/1/alocacoes/9/prorrogar", CORPO_PRORROGAR))
                .andExpect(status().isOk());

        verify(alocacaoService).prorrogar(OBRA, 9L, new ProrrogarAlocacaoDto(LocalDate.of(2030, 2, 1)));
    }

    @Test
    @DisplayName("Não permite alocar com permissão apenas de visualizar na obra")
    void retorna403AoAlocarComApenasPermissaoDeVisualizar() throws Exception {
        when(perm.pode(OBRA, MODULO, "VISUALIZAR")).thenReturn(true);

        mockMvc.perform(requisicao("POST", "/api/obras/1/equipamentos/7/alocacoes", CORPO_ALOCAR))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Retorna 400 quando o corpo da alocação é inválido")
    void retorna400QuandoCorpoDaAlocacaoInvalido() throws Exception {
        when(perm.pode(OBRA, MODULO, "CRIAR")).thenReturn(true);

        mockMvc.perform(requisicao("POST", "/api/obras/1/equipamentos/7/alocacoes", "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Lista o histórico quando tem permissão global de visualizar")
    void listaHistoricoQuandoTemPermissaoGlobalDeVisualizar() throws Exception {
        when(perm.podeGlobal(MODULO, "VISUALIZAR")).thenReturn(true);

        mockMvc.perform(requisicao("GET", "/api/equipamentos/7/alocacoes", null)).andExpect(status().isOk());

        verify(alocacaoService).listarHistorico(7L);
    }
}