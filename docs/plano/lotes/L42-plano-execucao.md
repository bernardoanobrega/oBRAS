# L42 — Plano de implementação (API do painel e contrato)

**Lote:** [L42](./L42-api-painel.md) · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-07, ADR-10  
**Responsável:** Thierry · **Fase:** 4 · **Branch:** `feat/painel-obra` · **HEAD:** `69b7740`  
**Status atual no repo:** `Pendente` · **Commit alvo:** `feat(painel): expor endpoint agregador`

> Plano de implementação. Importantes: imports marcados como `CONFIRMAR` na resposta original foram conferidos no repositório e ajustados. Nenhum status foi alterado neste arquivo.

## 1. Estado atual

- L41 concluído em `69b7740 feat(painel): agregar dados da obra`.
- `PainelObraService.painel(Long)` e `PainelObraDto` já existem em `br.com.obras.obra.{service,dto}`.
- Não existe `PainelObraController` nem rota `/painel` em `obras-backend/src`.
- `L42-api-painel.md:4` e `DIVISAO-TAREFAS-THIERRY-CAUA.md:74` seguem `Pendente`.
- Sem migration, sem frontend, sem mudança de contrato do DTO.

## 2. Decisões congeladas

1. Domínio permanece em Detalhe de Obra; não criar `br.com.obras.painel` nem subpacote `obra.painel`.
2. Controller somente expõe o service; agregação, filtros e exceção de obra inexistente já estão no L41.
3. Permissão por obra no controller: `@PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")` com `@P("obraId")`.
4. Nulos do contrato devem aparecer no JSON. Não configurar `default-property-inclusion=non_null`.
5. DTOs aninhados de `ObraDto`, `EquipeMembroDto`, `OcorrenciaDto` e `FotoDto` permanecem como entregues no L41. Exemplo `{}`/`[]` do documento L42 é ilustrativo, não autorização para remover campos.
6. `fotos` no raiz e `ocorrencias[].fotos` coexistem; não "corrigir" essa duplicação neste lote.
7. `vsOntem` = diferença para o diário ativo imediatamente anterior, não D-1 literal.
8. Sem diário: `clima {data:null, condicao:null}`, `presentesHoje=0`, `vsOntem=null`, `fonte="DIARIO"`.

## 3. Contrato API

`GET /api/obras/{obraId}/painel` → `200` com:

```json
{
  "obra": {},
  "clima": { "data": "2026-11-11", "condicao": "ENSOLARADO" },
  "kpis": {
    "avancoRealizado": null,
    "avancoPrevisto": null,
    "spi": null,
    "orcamentoTotal": null,
    "executado": null,
    "limiteAlerta": null,
    "fonte": "SIMULADO"
  },
  "equipe": [],
  "maoDeObra": { "alocados": 4, "presentesHoje": 3, "vsOntem": 1, "fonte": "DIARIO" },
  "ocorrencias": [],
  "fotos": []
}
```

Status: `200`, `401` sem token, `403` sem permissão na obra da URL, `404` obra inexistente.  
Erros usam `ApiError` existente: `{"status": ..., "mensagem": ..., "campos": [], "timestamp": ...}`.

## 4. Arquivos a criar

| Arquivo | Conteúdo |
|---|---|
| `obras-backend/src/main/java/br/com/obras/obra/controller/PainelObraController.java` | Controller somente leitura com `@PreAuthorize` e OpenAPI |
| `obras-backend/src/test/java/br/com/obras/obra/controller/PainelObraControllerTest.java` | 7 testes de contrato HTTP/permissão/JSON |
| `docs/plano/lotes/L42-plano-execucao.md` | Este plano |

## 5. Arquivos a alterar

| Arquivo | Alteração |
|---|---|
| `docs/plano/lotes/L42-api-painel.md` | Status e evidência da seção 10 |
| `docs/plano/lotes/DIVISAO-TAREFAS-THIERRY-CAUA.md:74` | Mesmo status da linha L42 |

Status: `Em andamento` ao começar; `Concluído` somente depois do DoD e da auditagem do dono.

## 6. Imports confirmados

Não restam imports `CONFIRMAR`:

- `br.com.obras.shared.security.SecurityConfig`
- `br.com.obras.shared.security.PermissaoEvaluator`
- `br.com.obras.shared.security.JwtService`
- `br.com.obras.shared.security.UsuarioAutenticado`
- `br.com.obras.shared.exception.RecursoNaoEncontradoException`

`UsuarioAutenticado` é record `(Long id, String login)`; `new UsuarioAutenticado(1L, "eng")` é válido.  
`RecursoNaoEncontradoException(String recurso, Object id)` aceita `OBRA_ID` como `Long`.

## 7. Snippet — controller

`obras-backend/src/main/java/br/com/obras/obra/controller/PainelObraController.java`

```java
package br.com.obras.obra.controller;

import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.obra.service.PainelObraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/obras/{obraId}/painel")
@Tag(name = "Painel da obra", description = "Dados agregados da obra para o painel (somente leitura)")
public class PainelObraController {
    private final PainelObraService service;

    public PainelObraController(PainelObraService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    @Operation(summary = "Painel agregado da obra",
        description = "Obra, clima do dia, KPIs (simulados), equipe, mão de obra, ocorrências e fotos do diário de hoje")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Painel da obra",
            content = @Content(schema = @Schema(implementation = PainelObraDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão DETALHES_OBRA/VISUALIZAR na obra", content = @Content),
        @ApiResponse(responseCode = "404", description = "Obra não encontrada", content = @Content)
    })
    public PainelObraDto painel(@PathVariable @P("obraId") Long obraId) {
        return service.painel(obraId);
    }
}
```

## 8. Snippet — teste de contrato HTTP

`obras-backend/src/test/java/br/com/obras/obra/controller/PainelObraControllerTest.java`

```java
package br.com.obras.obra.controller;

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
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 10, 1), "EM_ANDAMENTO", "ART-1", 1L);
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
}
```

## 9. Ordem de execução

1. Marcar `Em andamento` nos dois lugares.
2. RED: criar `PainelObraControllerTest` sem o controller; quebra de compilação é a falha esperada.
3. Criar `PainelObraController` com snippet da seção 7.
4. Rodar `mvn test -Dtest=PainelObraControllerTest`.
5. Rodar suíte completa `mvn test`.
6. Conferir Swagger e capturar JSON real com/sem diário.
7. Preencher `L42-api-painel.md:10`.
8. Revisão cruzada com Cauã — dispensada por decisão do usuário (ver §12); marcar `Concluído` nos dois lugares.
9. Commit único: `feat(painel): expor endpoint agregador`.

## 10. Verificação

```bash
cd obras-backend && mvn test -Dtest=PainelObraControllerTest
cd obras-backend && mvn test
```

Se `OBRAS_TEST_DB_URL` não estiver definida, testes de integração ignorados é comportamento esperado; os unitários precisam passar.

Checklist antes de rodar:

- `perm.pode(...)` aceita `(Long, String, String)`; os stubs dos testes 3 e 4 casam com o SpEL.
- `SecurityConfig` tem `@EnableMethodSecurity` (`SecurityConfig.java:24`). Sem isso `@PreAuthorize` não age e o teste 3 retornaria 200.
- Corpo de 403 vem do `GlobalExceptionHandler` para `AccessDeniedException`; teste 3 exige `$.mensagem` exata.
- 401 depende de `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` em `SecurityConfig.java:37`.
- `grep -rn "default-property-inclusion\|non_null" obras-backend/src/main/resources` deve ser vazio; qualquer ocorrência quebra o contrato de `null`.
- Existe só um `@RequestMapping` para `/api/obras/{obraId}/painel`.
- `/swagger-ui.html` mostra a rota e o cadeado do bearer.
- Nenhuma migration, nenhum frontend, nenhuma mudança em `PainelObraDto`/`PainelObraService`.

## 11. Evidência a registrar em `L42-api-painel.md`

```md
## 10. Verificação e evidência

- `cd obras-backend && mvn test -Dtest=PainelObraControllerTest` → <colar saída>
- `cd obras-backend && mvn test` (suíte completa) → <colar saída>
- Testes: getPainelRetorna200, painelDeObraInexistenteRetorna404, painelSemPermissaoRetorna403,
  painelDeOutraObraRetorna403, contratoTemCamposObrigatorios, kpisTemFonteSimuladoQuandoNulo, semTokenRetorna401.
- JSON real (obra com diário): <colar corpo do GET /api/obras/{id}/painel>
- JSON real (sem diário): clima {data:null, condicao:null}, presentesHoje 0, vsOntem null, fonte "DIARIO".
- Swagger: GET /api/obras/{obraId}/painel visível em /swagger-ui.html, bearer global aplicado.
- Divergência registrada: L42-api-painel.md:28 mostra clima preenchido; sem diário o contrato devolve campos null (decisão L41).
```

## 12. Riscos e ressalvas

- `jsonPath(...).exists()` falha para valor `null`; usar `JsonNode.has` + `isNull` como nos snippets.
- `assertAll` com array de `Executable` funciona via varargs; não trocar por `Stream` solto.
- O teste de outra obra prova apenas autorização na obra da URL; isolamento real de dados é do service, já coberto no L41.
- `ObraDto` tem 10 componentes; não usar construtor reduzido.
- L43-L46 consomem este contrato; mudança de nomes aqui quebra o frontend em revisão.
- Revisão cruzada com Cauã dispensada por decisão do usuário em 2026-11-27; registrada como ressalva de processo, não técnica.
