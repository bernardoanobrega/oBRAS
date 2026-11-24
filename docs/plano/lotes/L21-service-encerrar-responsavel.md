# L21 — Serviço: encerrar alocação e responsável técnico

**Fase:** 2 · **Commit:** `feat(equipe): encerrar alocacao e definir responsavel`  
**Status:** Pendente  
**Depende de:** L20 · **RF/UC:** RF-16, RF-18, RF-19; UC05 · **ADR:** ADR-05

## 1. Objetivo

Encerrar alocação ativa e definir o responsável técnico da obra.

## 2. Escopo

**Inclui:** `encerrar(obraId, alocacaoId)` com `dataFim = hoje`; `definirResponsavel` com usuário ativo; responsável não precisa estar alocado; não reativar alocação encerrada.  
**Não inclui:** cascata ao concluir/cancelar (L23), endpoints (L22).

## 3. Contrato de serviço

- `encerrar` só aceita alocação ativa da obra.
- Encerrada não volta a ativa; nova alocação é criada.
- `definirResponsavel` exige usuário existente e ativo.
- Responsável é fonte única em `Obra.responsavel`; não duplicar como cargo obrigatório.
- Auditar mudança de responsável em `Obra`.

## 4. Testes primeiro (RED)

- `encerraAlocacaoAtivaComDataDeHoje`.
- `rejeitaEncerrarAlocacaoJaEncerrada`.
- `rejeitaEncerrarAlocacaoDeOutraObra`.
- `novaAlocacaoAposEncerramentoEhPermitida`.
- `defineResponsavelAtivo`.
- `rejeitaResponsavelInativo`.
- `responsavelNaoPrecisaEstarAlocado`.
- `mudancaDeResponsavelAudita`.

## 5. Implementação

1. Implementar `encerrar`.
2. Implementar `definirResponsavel` no `ObraService` ou `AlocacaoObraService` com coordenação.
3. Garantir transação única para encerramento + auditoria.
4. Não reativar registro existente.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Encerrada não reativa | domínio/serviço | unitário |
| DataFim = hoje | serviço | unitário |
| Responsável ativo | serviço | unitário |
| Responsável não exige alocação | serviço | unitário |

## 7. Segurança

API usa `DETALHES_OBRA:EDITAR`. Serviço não aceita `usuarioId` do payload para autoria; usa contexto.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Encerramento correto.
- [ ] Nova alocação permitida.
- [ ] Responsável ativo.
- [ ] Auditoria de responsável.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de serviço e auditoria.
