# L23 — Cascata de encerramento ao concluir/cancelar

**Fase:** 2 · **Commit:** `feat(equipe): encerrar alocacoes em cascata`  
**Status:** Em revisão  
**Depende de:** L21, L22 · **RF/UC:** RF-14, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-05

## 1. Objetivo

Encerrar todas as alocações ativas quando a obra for concluída ou cancelada.

## 2. Escopo

**Inclui:** `concluir()` e `cancelar()` chamam encerramento em massa com `dataFim = hoje`; transação única; auditoria.  
**Não inclui:** encerramento ao paralisar; exclusão de alocações.

## 3. Contrato de serviço

- Concluir/cancelar encerra todas as alocações com `data_fim IS NULL` da obra.
- Paralisar não encerra nenhuma.
- Operação atômica: se falhar, status e alocações não mudam.
- Auditar transição de status e encerramentos conforme ADR-04.

## 4. Testes primeiro (RED)

- `concluirEncerraTodasAsAlocacoesAtivas`.
- `cancelarEncerraTodasAsAlocacoesAtivas`.
- `paralisarNaoEncerraAlocacoes`.
- `cascataUsaDataDeHoje`.
- `cascataNaoAlteraAlocacoesJaEncerradas`.
- `falhaNaCascataNaoMudaStatus`.

## 5. Implementação

1. Injetar `AlocacaoObraRepository` no serviço de obra ou coordenar serviços.
2. Executar encerramento em massa dentro da mesma transação.
3. Auditar transição.
4. Testar rollback com falha simulada.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Concluir/cancelar encerra ativas | serviço | integração |
| Paralisar não encerra | serviço | integração |
| DataFim = hoje | serviço | unitário |
| Atomicidade | transação | teste de falha |

## 7. Segurança

Mesma permissão da transição: `DETALHES_OBRA:EDITAR`.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Cascata em concluir/cancelar.
- [ ] Paralisar preserva equipe.
- [ ] Atomicidade comprovada.
- [ ] Auditoria registrada.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de cascata e rollback.
