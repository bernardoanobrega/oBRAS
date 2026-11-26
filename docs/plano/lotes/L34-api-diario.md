# L34 — API de diário e ocorrências

**Fase:** 3 · **Commit:** `feat(diario): expor diario e ocorrencias`  
**Status:** Concluído  
**Depende de:** L31–L33 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02, ADR-08, ADR-14

## 1. Objetivo

Expor os endpoints de diário, ocorrências, ocorrência rápida e fotos (metadados), com permissão por obra.

## 2. Escopo

**Inclui:** rotas da seção 6.3 do `PLANO.md`, DTOs, `@PreAuthorize`, validação e erros.  
**Não inclui:** upload binário (L35), exclusão (L36), UI (L37–L39).

## 3. Contrato API

| Método | Rota | Permissão | Sucesso |
|---|---|---|---|
| GET | `/api/obras/{obraId}/diarios` | `DETALHES_OBRA:VISUALIZAR` | 200 |
| POST | `/api/obras/{obraId}/diarios` | `DETALHES_OBRA:CRIAR` | 201 |
| GET | `/api/obras/{obraId}/diarios/{diarioId}` | `DETALHES_OBRA:VISUALIZAR` | 200 |
| PUT | `/api/obras/{obraId}/diarios/{diarioId}` | `DETALHES_OBRA:EDITAR` | 200 |
| POST | `/api/obras/{obraId}/diarios/{diarioId}/ocorrencias` | `DETALHES_OBRA:CRIAR` | 201 |
| PUT | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}` | `DETALHES_OBRA:EDITAR` | 200 |
| POST | `/api/obras/{obraId}/ocorrencias-rapidas` | `DETALHES_OBRA:CRIAR` | 201 |
| POST | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}/fotos` | `DETALHES_OBRA:CRIAR` | 201 |

Erros: 400 (`Dados inválidos`, com `campos`), 401 sem token, 403 sem permissão na obra da URL, 404 para id inexistente **ou de outra obra**, 422 regra de negócio. Todos no formato `ApiError`. Os `DELETE` e o `GET` de foto, que também estão no `DiarioController`, são do L35/L36.

## 4. Testes primeiro (RED)

- `postDiarioRetorna201`.
- `getDiarioInexistenteRetorna404`.
- `postOcorrenciaRetorna201`.
- `postOcorrenciaRapidaSemClimaRetorna422`.
- `getDiarioDeOutraObraRetorna403` (dividido em `semPermissaoNaObraRetorna403` e `diarioDeOutraObraRetorna404`).
- `postSemPermissaoRetorna403`.
- `erroSegueApiError`.

**Onde ficaram:** `DiarioControllerTest` (`@WebMvcTest`, service e permissão mockados). Além dos acima: `semTokenRetorna401`, `postOcorrenciaRapidaEFotoRetornam201`, `bodyInvalidoRetorna400ComCampos` e `semPermissaoRetorna403EmTodasAsRotas`, parametrizado com as 8 rotas, que cobre o `postSemPermissaoRetorna403` e confere a ação exigida por rota.

## 5. Implementação

1. Um só `DiarioController`, coerente com o `DiarioService` único.
2. Aplicar `@PreAuthorize` por obra.
3. Validar que ids de diário/ocorrência pertencem à obra (no service: id de outra obra → 404).
4. Não aceitar autor do payload (os DTOs não têm o campo). `dataHora` é aceita, opcional (ADR-18).

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Todo endpoint protegido | controller | 401/403 |
| IDs pertencem à obra | controller/serviço | 403/404 |
| Erros padronizados | handler | contrato |
| Clima condicional | serviço | 422 |

## 7. Segurança

Nunca confiar em `obraId` do body. Testar obra alheia em todos os endpoints.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [x] Rotas conforme contrato.
- [x] Status HTTP corretos.
- [x] Permissões e isolamento.
- [x] Erros padronizados.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de controller.

**Evidência (26/11/2026):** `DiarioControllerTest` 18/18. `mvn test`: 269 testes, 3 erros, todos em `OrcamentoRepositoryTest` (módulo Orçamento: insere obra sem `auditavel_id`), alheios a este lote.
