package br.com.obras.obra.controller;

import br.com.obras.diario.dto.FotoDto;
import br.com.obras.diario.dto.MembroDiarioDto;
import br.com.obras.diario.dto.OcorrenciaDto;
import br.com.obras.diario.dto.ParalisacaoDto;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.obra.service.PainelObraService;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.security.JwtService;
import br.com.obras.shared.security.PermissaoEvaluator;
import br.com.obras.shared.security.SecurityConfig;
import br.com.obras.shared.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PainelObraController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class PainelObraControllerTest {
    private static final Long OBRA_ID = 5L;
    private static final String PAINEL = "/api/obras/5/painel";
    private static final String MSG_403 = "Você não tem permissão para esta operação";

    @Autowired private MockMvc mvc;
    @MockBean private PainelObraService service;
    @MockBean(name = "perm") private PermissaoEvaluator perm;
    @MockBean private JwtService jwtService;

    private static RequestPostProcessor usuarioLogado() {
        UsuarioAutenticado principal = new UsuarioAutenticado(1L, "eng");
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private void permitir() {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
    }

    @Test
    void getPainelRetorna200() throws Exception {
        permitir();
        when(service.painel(OBRA_ID)).thenReturn(painelComDiario());

        mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isOk());

        verify(service).painel(OBRA_ID);
    }

    @Test
    void painelDeObraInexistenteRetorna404() throws Exception {
        permitir();
        when(service.painel(OBRA_ID)).thenThrow(new RecursoNaoEncontradoException("Obra", OBRA_ID));

        mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void painelSemPermissaoRetorna403() throws Exception {
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(false);

        mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.mensagem").value(MSG_403));

        verifyNoInteractions(service);
    }

    @Test
    void painelDeOutraObraRetorna403() throws Exception {
        when(perm.pode(6L, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(true);
        when(perm.pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR")).thenReturn(false);

        mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isForbidden());

        verify(perm).pode(OBRA_ID, "DETALHES_OBRA", "VISUALIZAR");
        verifyNoInteractions(service);
    }

    @Test
    void contratoTemCamposObrigatorios() throws Exception {
        permitir();
        when(service.painel(OBRA_ID)).thenReturn(painelSemDiario());

        String corpo = mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode n = new ObjectMapper().readTree(corpo);

        assertAll(
            () -> assertEquals(Set.of("obra", "clima", "kpis", "equipe", "maoDeObra", "ocorrencias", "fotos"), nomes(n)),
            () -> assertEquals(Set.of("data", "condicao"), nomes(n.get("clima"))),
            () -> assertEquals(Set.of("avancoRealizado", "avancoPrevisto", "spi", "orcamentoTotal",
                    "executado", "limiteAlerta", "fonte"), nomes(n.get("kpis"))),
            () -> assertEquals(Set.of("alocados", "presentesHoje", "vsOntem", "fonte"), nomes(n.get("maoDeObra"))),
            () -> assertTrue(n.get("clima").has("data") && n.get("clima").get("data").isNull()),
            () -> assertTrue(n.get("clima").has("condicao") && n.get("clima").get("condicao").isNull()),
            () -> assertTrue(n.get("maoDeObra").has("vsOntem") && n.get("maoDeObra").get("vsOntem").isNull()),
            () -> assertEquals("DIARIO", n.get("maoDeObra").get("fonte").asText()),
            () -> assertTrue(n.get("equipe").isArray()),
            () -> assertTrue(n.get("ocorrencias").isArray()),
            () -> assertTrue(n.get("fotos").isArray())
        );
    }

    @Test
    void contratoPainelPopuladoTemChavesExatas() throws Exception {
        permitir();
        when(service.painel(OBRA_ID)).thenReturn(painelPopulado());

        String corpo = mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode n = new ObjectMapper().readTree(corpo);
        JsonNode ocorrencia = n.get("ocorrencias").get(0);
        JsonNode ocorrenciaSemExtras = n.get("ocorrencias").get(1);

        assertAll(
            () -> assertEquals(Set.of("id", "nome", "descricao", "tipologia", "endereco", "inicioPrevisto",
                    "fimPrevisto", "status", "art", "responsavelId", "responsavelNome", "auditavelId"),
                nomes(n.get("obra"))),
            () -> assertEquals(Set.of("usuarioId", "nome", "cargos", "dataInicio"),
                nomes(n.get("equipe").get(0))),
            () -> assertEquals(Set.of("id", "diarioId", "categoria", "descricao", "dataHora", "autor",
                    "paralisacao", "fotos"), nomes(ocorrencia)),
            () -> assertEquals(Set.of("alocacaoId", "usuarioId", "nome"),
                nomes(ocorrencia.get("autor"))),
            () -> assertEquals(Set.of("inicio", "fim"), nomes(ocorrencia.get("paralisacao"))),
            () -> assertEquals(Set.of("id", "ocorrenciaId", "nomeArquivo", "legenda", "dataEnvio"),
                nomes(ocorrencia.get("fotos").get(0))),
            () -> assertEquals(Set.of("id", "ocorrenciaId", "nomeArquivo", "legenda", "dataEnvio"),
                nomes(n.get("fotos").get(0))),
            () -> assertEquals(2, n.get("ocorrencias").size()),
            () -> assertEquals(Set.of("id", "diarioId", "categoria", "descricao", "dataHora", "autor",
                    "paralisacao", "fotos"), nomes(ocorrenciaSemExtras)),
            () -> assertTrue(ocorrenciaSemExtras.get("paralisacao").isNull()),
            () -> assertTrue(ocorrenciaSemExtras.get("fotos").isArray()),
            () -> assertEquals(0, ocorrenciaSemExtras.get("fotos").size()),
            () -> assertEquals("SEGURANCA", ocorrencia.get("categoria").asText()),
            () -> assertEquals("Ana", ocorrencia.get("autor").get("nome").asText())
        );
    }

    @Test
    void kpisTemFonteSimuladoQuandoNulo() throws Exception {
        permitir();
        when(service.painel(OBRA_ID)).thenReturn(painelComDiario());

        String corpo = mvc.perform(get(PAINEL).with(usuarioLogado()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kpis.fonte").value("SIMULADO"))
            .andReturn().getResponse().getContentAsString();
        JsonNode kpis = new ObjectMapper().readTree(corpo).get("kpis");

        assertAll(nulos(kpis,
            "avancoRealizado", "avancoPrevisto", "spi", "orcamentoTotal", "executado", "limiteAlerta"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mvc.perform(get(PAINEL))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private static Executable[] nulos(JsonNode no, String... campos) {
        return Arrays.stream(campos)
            .<Executable>map(c ->
                () -> assertTrue(no.has(c) && no.get(c).isNull(), "campo deve existir e ser null: " + c))
            .toArray(Executable[]::new);
    }

    private static Set<String> nomes(JsonNode no) {
        Set<String> r = new HashSet<>();
        no.fieldNames().forEachRemaining(r::add);
        return r;
    }

    private static ObraDto obra() {
        return new ObraDto(5L, "Obra A", "desc", "RESIDENCIAL", "Rua X",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 10, 1), "EM_ANDAMENTO", "ART-1", 1L,
            "Responsável A", 77L);
    }

    private static PainelObraDto painelComDiario() {
        return new PainelObraDto(
            obra(),
            new PainelObraDto.ClimaDto(LocalDate.of(2026, 11, 11), "ENSOLARADO"),
            PainelObraDto.KpisDto.simulados(),
            List.of(),
            PainelObraDto.MaoDeObraDto.de(4, 3, 1),
            List.of(),
            List.of()
        );
    }

    private static PainelObraDto painelSemDiario() {
        return new PainelObraDto(
            obra(),
            new PainelObraDto.ClimaDto(null, null),
            PainelObraDto.KpisDto.simulados(),
            List.of(),
            PainelObraDto.MaoDeObraDto.de(4, 0, null),
            List.of(),
            List.of()
        );
    }

    private static PainelObraDto painelPopulado() {
        FotoDto foto = new FotoDto(12L, 9L, "foto.png", "Foto da obra",
            LocalDateTime.of(2026, 11, 11, 10, 31));
        OcorrenciaDto ocorrencia = new OcorrenciaDto(
            9L, 8L, "SEGURANCA", "Ocorrência da obra",
            LocalDateTime.of(2026, 11, 11, 10, 30),
            new MembroDiarioDto(11L, 10L, "Ana"),
            new ParalisacaoDto(
                LocalDateTime.of(2026, 11, 11, 10, 30),
                LocalDateTime.of(2026, 11, 11, 11, 15)),
            List.of(foto));
        OcorrenciaDto ocorrenciaSemExtras = new OcorrenciaDto(
            10L, 8L, "OUTRO", "Ocorrência sem paralisação",
            LocalDateTime.of(2026, 11, 11, 11, 30),
            new MembroDiarioDto(11L, 10L, "Ana"),
            null,
            List.of());
        return new PainelObraDto(
            obra(),
            new PainelObraDto.ClimaDto(LocalDate.of(2026, 11, 11), "ENSOLARADO"),
            PainelObraDto.KpisDto.simulados(),
            List.of(new EquipeMembroDto(10L, "Ana", List.of("ENGENHEIRO"), LocalDate.of(2026, 1, 1))),
            PainelObraDto.MaoDeObraDto.de(4, 3, 1),
            List.of(ocorrencia, ocorrenciaSemExtras),
            List.of(foto)
        );
    }
}