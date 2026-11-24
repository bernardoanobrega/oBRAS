# L12 — API CRUD de Obra

**Fase:** 1 · **Commit:** `feat(obra): expor crud de obra`  
**Status:** Em andamento  
**Depende de:** L11 · **RF/UC:** RF-14, RF-15; UC04 · **ADR:** ADR-04, ADR-05

## 1. Objetivo

Expor os endpoints de criação, listagem, busca e edição de obra com validação e permissão.

## 2. Escopo

**Inclui:** `POST /api/obras`, `GET /api/obras`, `GET /api/obras/{obraId}`, `PUT /api/obras/{obraId}`; `@PreAuthorize`; `ApiError`.  
**Não inclui:** transições (L13), filtro por alocação (L14), canteiro (L15).

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
