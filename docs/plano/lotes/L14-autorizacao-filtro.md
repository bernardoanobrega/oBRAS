# L14 — Autorização e filtro de lista de obras

**Fase:** 1 · **Commit:** `feat(obra): autorizar e filtrar obras`  
**Status:** Pendente  
**Depende de:** L12, L13 · **RF/UC:** RF-70, RF-73; UC04, UC05 · **ADR:** ADR-09

## 1. Objetivo

Garantir permissão por obra em todos os endpoints e filtrar a lista conforme alocação ativa ou permissão global.

## 2. Escopo

**Inclui:** `@PreAuthorize` em todos os endpoints; consulta de obras por alocação ativa; regra global vs alocação; testes de isolamento.  
**Não inclui:** CRUD de alocação (Fase 2), permissões finas por ação (L51).

## 3. Contrato

- `DETALHES_OBRA:VISUALIZAR` global → todas as obras.
- Sem permissão global → obras com `alocacao_obra.data_fim IS NULL` para o usuário.
- Endpoint de detalhe e escrita nega obra sem vínculo.
- `@PreAuthorize` em todos os métodos do controller.

## 4. Testes primeiro (RED)

- `usuarioComPermissaoGlobalVeTodasAsObras`.
- `usuarioAlocadoVeSomenteSuaObra`.
- `usuarioSemAlocacaoVeListaVazia`.
- `usuarioAlocadoNaoAcessaOutraObra`.
- `usuarioSemPermissaoRecebe403`.
- `usuarioInativoRecebe403`.
- `semTokenRecebe401`.

## 5. Implementação

1. Criar query `obrasPorAlocacaoAtiva(usuarioId)` no repository do módulo obra.
2. Ajustar `ObraService.listar(usuarioId)`.
3. Revisar todos os `@PreAuthorize`.
4. Testar com duas obras e dois usuários.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Segurança real é do backend | controller | 403 |
| Lista nunca vaza obra sem vínculo | repository/serviço | integração |
| Permissão global amplia escopo | serviço | integração |
| Obra de outro usuário não é acessível | controller | 403/404 |

## 7. Segurança

Nunca confiar em `obraId` do frontend. Validar vínculo no backend. Não usar repository de usuário fora do port/service.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Todos os endpoints protegidos.
- [ ] Lista filtrada corretamente.
- [ ] Isolamento entre obras comprovado.
- [ ] Testes 401/403 verdes.

## 10. Verificação e evidência

`mvn test` com usuários e alocações reais. Evidência: matriz de acesso.
