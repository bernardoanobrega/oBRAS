package br.com.obras.equipamento.controller;

import static br.com.obras.equipamento.controller.PermissoesEquipamento.MODULO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.obras.equipamento.domain.TipoEquipamento;
import br.com.obras.equipamento.dto.EquipamentoCriacaoDto;
import br.com.obras.equipamento.service.EquipamentoService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(EquipamentoController.class)
@Import(MetodoSegurancaTestConfig.class)
@WithMockUser
class EquipamentoControllerTest {

    private static final String CORPO_CRIACAO =
            "{\"codigoPatrimonio\":\"PAT-001\",\"descricao\":\"Betoneira 400L\",\"tipo\":\"MAQUINA\"}";
    private static final String CORPO_EDICAO = "{\"descricao\":\"Betoneira 600L\",\"tipo\":\"MAQUINA\"}";

    private final MockMvc mockMvc;

    @MockBean
    private EquipamentoService equipamentoService;

    @MockBean(name = "perm")
    private PermissaoFalsa perm;

    @Autowired
    EquipamentoControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    static Stream<Arguments> requisicoes() {
        return Stream.of(
                Arguments.of("GET", "/api/equipamentos", null),
                Arguments.of("GET", "/api/equipamentos/7", null),
                Arguments.of("POST", "/api/equipamentos", CORPO_CRIACAO),
                Arguments.of("PUT", "/api/equipamentos/7", CORPO_EDICAO),
                Arguments.of("PATCH", "/api/equipamentos/7/desativar", null),
                Arguments.of("PATCH", "/api/equipamentos/7/reativar", null));
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
    @DisplayName("Lista equipamentos quando tem permissão de visualizar")
    void listaEquipamentosQuandoTemPermissaoDeVisualizar() throws Exception {
        when(perm.podeGlobal(MODULO, "VISUALIZAR")).thenReturn(true);
        var paginaPadrao = PageRequest.of(0, 20, Sort.by("codigoPatrimonio"));
        when(equipamentoService.listar(null, null, null, paginaPadrao)).thenReturn(Page.empty());

        mockMvc.perform(requisicao("GET", "/api/equipamentos", null)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cadastra equipamento quando tem permissão de criar")
    void cadastraEquipamentoQuandoTemPermissaoDeCriar() throws Exception {
        when(perm.podeGlobal(MODULO, "CRIAR")).thenReturn(true);

        mockMvc.perform(requisicao("POST", "/api/equipamentos", CORPO_CRIACAO)).andExpect(status().isCreated());

        verify(equipamentoService).cadastrar(
                new EquipamentoCriacaoDto("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA));
    }

    @Test
    @DisplayName("Retorna 400 quando o corpo do cadastro é inválido")
    void retorna400QuandoCorpoDoCadastroInvalido() throws Exception {
        when(perm.podeGlobal(MODULO, "CRIAR")).thenReturn(true);

        mockMvc.perform(requisicao("POST", "/api/equipamentos", "{}")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Desativa equipamento apenas com permissão de excluir")
    void desativaEquipamentoApenasComPermissaoDeExcluir() throws Exception {
        when(perm.podeGlobal(MODULO, "EDITAR")).thenReturn(true);

        mockMvc.perform(requisicao("PATCH", "/api/equipamentos/7/desativar", null))
                .andExpect(status().isForbidden());

        when(perm.podeGlobal(MODULO, "EXCLUIR")).thenReturn(true);

        mockMvc.perform(requisicao("PATCH", "/api/equipamentos/7/desativar", null)).andExpect(status().isOk());
        verify(equipamentoService).desativar(7L);
    }
}