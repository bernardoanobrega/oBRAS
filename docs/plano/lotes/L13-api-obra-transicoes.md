# L13 — API de transições e responsável técnico

**Fase:** 1 · **Commit:** `feat(obra): expor transicoes e responsavel`  
**Status:** Em revisão  
**Depende de:** L12 · **RF/UC:** RF-14, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-05, ADR-08

## 1. Objetivo

Expor as ações de ciclo de vida da obra e a definição de responsável técnico.

## 2. Escopo

**Inclui:** `PUT /responsavel`, `POST /iniciar`, `/paralisar`, `/retomar`, `/concluir`, `/cancelar`; permissão `DETALHES_OBRA:EDITAR`; auditoria.  
**Não inclui:** cascata de alocação (L23), paralisação automática por diário.

## 3. Contrato API

| Método | Rota | Regra |
|---|---|---|
| PUT | `/api/obras/{obraId}/responsavel` | body `{usuarioId}`; usuário ativo |
| POST | `/api/obras/{obraId}/iniciar` | exige responsável |
| POST | `/api/obras/{obraId}/paralisar` | não encerra alocação |
| POST | `/api/obras/{obraId}/retomar` | só de `PARALISADA` |
| POST | `/api/obras/{obraId}/concluir` | encerra alocações em L23 |
| POST | `/api/obras/{obraId}/cancelar` | encerra alocações em L23 |

## 4. Testes primeiro (RED)

- `iniciarSemResponsavelRetorna422`.
- `iniciarComResponsavelRetorna200`.
- `paralisarNaoEncerraAlocacao`.
- `retomarDePlanejadaRetorna422`.
- `concluirDeEmAndamentoRetorna200`.
- `cancelarDeConcluidaRetorna422`.
- `transicaoSemPermissaoRetorna403`.
- `transicaoGeraAuditoria`.

## 5. Implementação

1. Adicionar métodos no controller.
2. Reutilizar `ObraService`.
3. Mapear `RegraNegocioException` para 422.
4. Não criar endpoint que chame `paralisar()` a partir do diário.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Início exige responsável ativo | domínio + API | 422 |
| Estado final não regride | domínio + API | 422 |
| Paralisação do diário não altera status | serviço | integração |
| Toda transição audita | serviço | integração |

## 7. Segurança

Todos os endpoints usam `@PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")`. Testar obra de outro usuário.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Rotas de transição conforme contrato.
- [ ] 200/401/403/404/422 cobertos.
- [ ] Auditoria por transição.
- [ ] Nenhuma transição automática.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de controller e auditoria.
