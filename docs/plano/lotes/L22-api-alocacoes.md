# L22 — API de alocações da equipe

**Fase:** 2 · **Commit:** `feat(equipe): expor alocacoes`  
**Status:** Em revisão  
**Depende de:** L21 · **RF/UC:** RF-16 a RF-19; UC05 · **ADR:** ADR-09

## 1. Objetivo

Expor listagem, criação, atualização de cargos e encerramento de alocações.

## 2. Escopo

**Inclui:** endpoints de alocação, DTOs, `@PreAuthorize`, validação, erros.  
**Não inclui:** tela de equipe (L25), cascata (L23).

## 3. Contrato API

| Método | Rota | Permissão |
|---|---|---|
| GET | `/api/obras/{obraId}/alocacoes` | `DETALHES_OBRA:VISUALIZAR` |
| POST | `/api/obras/{obraId}/alocacoes` | `DETALHES_OBRA:CRIAR` |
| PUT | `/api/obras/{obraId}/alocacoes/{id}/cargos` | `DETALHES_OBRA:EDITAR` |
| DELETE | `/api/obras/{obraId}/alocacoes/{id}` | `DETALHES_OBRA:EDITAR` |

Payload de criação: `{ "usuarioId": 10, "cargoIds": [1, 2] }`.

## 4. Testes primeiro (RED)

- `postAlocaRetorna201`.
- `postComCargosVaziosRetorna400`.
- `postDuplicadoRetorna409Ou422`.
- `getListaSomenteDaObra`.
- `putCargosAtualiza`.
- `deleteEncerraRetorna204`.
- `semPermissaoRetorna403`.
- `obraDeOutroUsuarioRetorna403`.

## 5. Implementação

1. Criar `AlocacaoObraController`.
2. Aplicar `@PreAuthorize` por obra.
3. Mapear exceções para `ApiError`.
4. Garantir que `id` da alocação pertence à obra da URL.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Alocação pertence à obra da URL | controller/serviço | 404/403 |
| Cargos não vazios | DTO | 400 |
| Duplicidade ativa bloqueada | serviço/banco | 409/422 |
| Encerramento não apaga | serviço | integração |

## 7. Segurança

Não aceitar `obraId` divergente do path. Testar usuário alocado em outra obra.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Rotas conforme contrato.
- [ ] Status HTTP corretos.
- [ ] Permissões testadas.
- [ ] Isolamento por obra.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de controller.

## 11. Nota do frontend (L25, 2026-11-26)

O front do L25 já chama estas rotas e espera os formatos listados em [`L25-frontend-equipe.md`, seção 11](L25-frontend-equipe.md#pendências-para-o-backend-o-que-o-front-já-chama), incluindo duas rotas que não estão neste contrato: `GET /api/cargos` e `GET /api/obras/{obraId}/alocacoes/candidatos`. Combinar com o Cauã antes de mudar algum formato.
