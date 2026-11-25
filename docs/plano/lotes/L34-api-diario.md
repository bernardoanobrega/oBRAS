# L34 — API de diário e ocorrências

**Fase:** 3 · **Commit:** `feat(diario): expor diario e ocorrencias`  
**Status:** Em revisão  
**Depende de:** L31–L33 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02, ADR-08

## 1. Objetivo

Expor os endpoints de diário, ocorrências, ocorrência rápida e fotos (metadados), com permissão por obra.

## 2. Escopo

**Inclui:** rotas da seção 6.3 do `PLANO.md`, DTOs, `@PreAuthorize`, validação e erros.  
**Não inclui:** upload binário (L35), exclusão (L36), UI (L37–L39).

## 3. Contrato API

| Método | Rota | Permissão |
|---|---|---|
| GET | `/api/obras/{obraId}/diarios` | `DETALHES_OBRA:VISUALIZAR` |
| POST | `/api/obras/{obraId}/diarios` | `DETALHES_OBRA:CRIAR` |
| GET | `/api/obras/{obraId}/diarios/{diarioId}` | `DETALHES_OBRA:VISUALIZAR` |
| PUT | `/api/obras/{obraId}/diarios/{diarioId}` | `DETALHES_OBRA:EDITAR` |
| POST | `/api/obras/{obraId}/diarios/{diarioId}/ocorrencias` | `DETALHES_OBRA:CRIAR` |
| PUT | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}` | `DETALHES_OBRA:EDITAR` |
| POST | `/api/obras/{obraId}/ocorrencias-rapidas` | `DETALHES_OBRA:CRIAR` |
| POST | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}/fotos` | `DETALHES_OBRA:CRIAR` |

## 4. Testes primeiro (RED)

- `postDiarioRetorna201`.
- `getDiarioInexistenteRetorna404`.
- `postOcorrenciaRetorna201`.
- `postOcorrenciaRapidaSemClimaRetorna422`.
- `getDiarioDeOutraObraRetorna403`.
- `postSemPermissaoRetorna403`.
- `erroSegueApiError`.

## 5. Implementação

1. Criar `RegistroDiarioController` e `OcorrenciaController`.
2. Aplicar `@PreAuthorize` por obra.
3. Validar que ids de diário/ocorrência pertencem à obra.
4. Não aceitar autor/dataHora do payload quando proibido.

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

- [ ] Rotas conforme contrato.
- [ ] Status HTTP corretos.
- [ ] Permissões e isolamento.
- [ ] Erros padronizados.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de controller.
