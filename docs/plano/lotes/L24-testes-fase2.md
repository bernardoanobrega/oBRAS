# L24 — Testes de integração da Fase 2

**Fase:** 2 · **Commit:** `test(equipe): cobrir fase 2`  
**Status:** Pendente  
**Depende de:** L18–L23 · **RF/UC:** RF-16 a RF-19; UC05 · **ADR:** ADR-05, ADR-09

## 1. Objetivo

Fechar a Fase 2 com testes de alocação, cargos, encerramento, responsável e cascata.

## 2. Escopo

**Inclui:** testes de serviço e controller com PostgreSQL real; fixtures de usuário/cargo/obra; concorrência de alocação ativa.  
**Não inclui:** frontend (L25), diário (Fase 3).

## 3. Contrato de teste

- Criar usuários ativos/inativos, cargos e obras.
- Testar alocação duplicada em paralelo.
- Testar cargos vazios e cargo inexistente.
- Testar encerramento e nova alocação.
- Testar cascata e atomicidade.

## 4. Testes primeiro (RED)

- `alocaComCargos`.
- `duplicidadeAtivaSobConcorrencia`.
- `encerraENovaAlocacaoFunciona`.
- `responsavelInativoFalha`.
- `cascataConcluir`.
- `cascataCancelar`.
- `paralisarPreservaAlocacoes`.
- `semPermissaoRetorna403`.

## 5. Implementação

1. Reutilizar `AbstractIntegrationTest`.
2. Criar fixtures de equipe.
3. Rodar suíte completa.
4. Corrigir vazamentos de transação.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Unicidade ativa | banco | concorrência |
| Cargos válidos | serviço | integração |
| Cascata atômica | transação | integração |
| Isolamento por obra | controller | 403 |

## 7. Segurança

Cobrir permissão de criar/editar/excluir equipe e acesso a obra alheia.

## 8. Migração e rollback

Sem migration nova.

## 9. Critérios de aceite

- [ ] Suíte verde.
- [ ] Concorrência coberta.
- [ ] Cascata coberta.
- [ ] Permissões cobertas.

## 10. Verificação e evidência

`mvn test`. Evidência: relatório e nomes dos testes.
