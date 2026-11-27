# L12 — API CRUD de Obra

**Fase:** 1 · **Commit:** `feat(obra): expor crud de obra`  
**Status:** Em andamento  
**Depende de:** L11 · **RF/UC:** RF-14, RF-15; UC04 · **ADR:** ADR-04, ADR-05

## 1. Objetivo

Expor os endpoints de criação, listagem, busca e edição de obra com validação e permissão.

## 2. Escopo

**Inclui:** `POST /api/obras`, `GET /api/obras`, `GET /api/obras/{obraId}`, `PUT /api/obras/{obraId}`; `@PreAuthorize`; `ApiError`.  
**Não inclui:** transições (L13), filtro por alocação (L14), canteiro (L15 abandonado).

## 3. Contrato API

| Método | Rota | Permissão | Status |
|---|---|---|---|
| POST | `/api/obras` | `DETALHES_OBRA:CRIAR` | 201 |
| GET | `/api/obras` | `DETALHES_OBRA:VISUALIZAR` | 200 |
| GET | `/api/obras/{obraId}` | `DETALHES_OBRA:VISUALIZAR` | 200/404 |
| PUT | `/api/obras/{obraId}` | `DETALHES_OBRA:EDITAR` | 200/400/404/422 |

`responsavelId` opcional no POST. `status` não aceito no PUT.

## 4. Testes primeiro (RED)

- `postCriaObraRetorna201`.
- `postSemNomeRetorna400ComCampo`.
- `getListaRetorna200`.
- `getInexistenteRetorna404`.
- `putAtualizaRetorna200`.
- `semTokenRetorna401`.
- `semPermissaoRetorna403`.
- `erroSegueFormatoApiError`.

## 5. Implementação

1. Criar `ObraController`.
2. Usar `@Valid` nos DTOs.
3. Aplicar `@PreAuthorize`.
4. Usar `CurrentUser.id()` para auditoria.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Todo endpoint tem permissão | controller | 401/403 |
| Status só por transição | controller/DTO | 400 |
| Erro no formato `ApiError` | handler | contrato |

## 7. Segurança

`@PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'ACAO')")`; POST usa `podeGlobal` ou `pode(null, ...)` conforme decisão de criação. Não confiar no frontend.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Rotas conforme contrato.
- [ ] 201/200/400/401/403/404 cobertos.
- [ ] DTOs validados.
- [ ] Erros padronizados.

## 10. Verificação e evidência

`mvn test` com MockMvc/Testcontainers. Evidência: testes de controller.

## 11. Auditoria do código (Cauã, 2026-11-25)

Leitura do código em `feat/obra-read-api` contra este contrato. **Nada foi implementado**; sugestões para o Thierry decidir.

**Já existe**
- `ObraController` com `GET /api/obras` (`isAuthenticated()`), `GET /{obraId}` e `GET /{obraId}/equipe` (`@perm.pode(#obraId,'DETALHES_OBRA','VISUALIZAR')`).
- `@perm.podeGlobal(modulo, acao)` em `PermissaoEvaluator`, pronto para o POST (ADR-11).
- `GlobalExceptionHandler` já cobre o contrato: 400 com `campos` (`MethodArgumentNotValidException` e enum inválido), 403, 404, 422 e 409 (integridade). **Não deve precisar de alteração** neste lote.
- `SecurityConfig` devolve 401 sem token.

**Falta**
- `POST /api/obras` (201) e `PUT /api/obras/{obraId}`. Dependem do L11 (service) e do L10 (DTOs).
- Os 8 testes da seção 4. **Não há nenhum teste de controller no projeto**; será a primeira infraestrutura de MockMvc.

**Pontos de atenção (sugestões)**
1. **`status` no PUT:** o Spring Boot ignora campos desconhecidos no JSON por padrão (`FAIL_ON_UNKNOWN_PROPERTIES=false`). Enviar `status` no PUT **não dá 400**: é ignorado em silêncio. Para cumprir o invariante "Status só por transição → 400", uma opção é um campo `status` com `@Null(message = "status só muda por transição")` no `AtualizarObraDto`.
2. **Permissão do `GET /api/obras`:** a tabela diz `DETALHES_OBRA:VISUALIZAR`, mas como `podeGlobal` isso bloquearia quem só tem acesso **por obra**. O código atual (`isAuthenticated()` + filtro por obra no service) parece o comportamento certo; o filtro definitivo é do L14.
3. **Testes sem Docker:** `@WebMvcTest(ObraController.class)` com o service simulado e a segurança carregada cobre 201/400/401/403/404 e o formato `ApiError` sem banco. Pode exigir simular `JwtService` e o `AutorizacaoPort`.
4. **Impacto no L17 (Cauã):** o frontend de cadastro e edição está sendo construído contra **este contrato** (rotas, status HTTP, `responsavelId` opcional, `status` recusado no PUT). Qualquer mudança de rota, campo ou código HTTP precisa ser avisada ao Cauã.
