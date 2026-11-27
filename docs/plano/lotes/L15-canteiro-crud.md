# L15 — CRUD de canteiro

**Fase:** 1 · **Commit:** —

**Status:** Abandonado

**Depende de:** L14 · **RF/UC:** RF-14, RF-15; UC04 · **ADR:** ADR-09, ADR-10

> Decisão de 2026-11-27: `Canteiro` não integra o módulo Detalhe de Obra. Tabela `canteiro` em V2 e IDs em V8 permanecem como referência de estoque/máquinas. Não implementar este lote; contrato abaixo é histórico.

## 1. Objetivo

Implementar cadastro, listagem, edição e exclusão de canteiros vinculados à obra.

## 2. Escopo

**Inclui:** domínio `Canteiro`, repository, service, controller, DTOs; exclusão bloqueada se houver equipamento alocado.  
**Não inclui:** módulo de equipamentos; CRUD de equipamentos.

## 3. Contrato API

| Método | Rota | Permissão |
|---|---|---|
| GET | `/api/obras/{obraId}/canteiros` | `DETALHES_OBRA:VISUALIZAR` |
| POST | `/api/obras/{obraId}/canteiros` | `DETALHES_OBRA:CRIAR` |
| PUT | `/api/obras/{obraId}/canteiros/{id}` | `DETALHES_OBRA:EDITAR` |
| DELETE | `/api/obras/{obraId}/canteiros/{id}` | `DETALHES_OBRA:EXCLUIR` |

`DELETE` retorna 409/422 se houver equipamento alocado.

## 4. Testes primeiro (RED)

- `criaCanteiroNaObra`.
- `listaSomenteCanteirosDaObra`.
- `naoCriaCanteiroEmObraDeOutroUsuario`.
- `atualizaNomeDoCanteiro`.
- `excluiCanteiroSemEquipamento`.
- `bloqueiaExclusaoComEquipamentoAlocado`.
- `semPermissaoRetorna403`.

## 5. Implementação

1. Criar entidade/repository/service/controller.
2. Criar port `EquipamentoAlocadoPort` em `shared` ou no módulo obra.
3. Adapter temporário retorna `false` até módulo de equipamentos existir, com teste de contrato.
4. Validar que o canteiro pertence à obra da URL.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Canteiro sempre pertence a uma obra | banco + domínio | FK/teste |
| Exclusão bloqueada com equipamento | serviço | unitário |
| Canteiro de outra obra não é acessível | controller | 403/404 |

## 7. Segurança

Todas as rotas com `@PreAuthorize` por obra. Não confiar no `obraId` do body; usar path.

## 8. Migração e rollback

Sem migration nova; tabela existe em V2. Rollback: remover endpoints, manter dados.

## 9. Critérios de aceite

- [ ] CRUD completo.
- [ ] Exclusão bloqueada por equipamento.
- [ ] Isolamento por obra.
- [ ] Permissões testadas.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de controller e serviço.
