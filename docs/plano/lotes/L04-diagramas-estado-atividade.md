# L04 — Diagramas de estado e atividade do Detalhe de Obra

**Fase:** 0 · **Commit:** `docs(diagramas): atualizar estados e atividades`  
**Status:** Em andamento  
**Depende de:** L03 · **RF/UC:** RF-14 a RF-19, RF-64 a RF-68; UC04, UC05, UC21 · **ADR:** ADR-05, ADR-08

## 1. Objetivo

Alinhar estados de `Obra`/`AlocacaoObra` e atividades do diário às decisões fechadas do módulo.

## 2. Escopo

**Inclui:** responsável opcional em `PLANEJADA`; `iniciar()` exige responsável ativo; `paralisar()` manual; paralisação do diário não muda status; categoria obrigatória no ramo de paralisação; ocorrência rápida cria diário com clima.  
**Não inclui:** automação de transição por ocorrência; estados de medição, cronograma ou documentos.

## 3. Contrato do artefato

- Diagrama de estados de `Obra`: `PLANEJADA → EM_ANDAMENTO → PARALISADA → EM_ANDAMENTO`; `CONCLUIDA`/`CANCELADA` finais.
- Diagrama de estados de `AlocacaoObra`: ativa/encerrada.
- Diagrama de atividades do diário com ramo de paralisação e ramo de ocorrência rápida.

## 4. Testes primeiro (revisão)

- [ ] `iniciar()` sem responsável é estado inválido.
- [ ] `paralisar()` não é chamado ao registrar paralisação.
- [ ] Paralisação exige categoria.
- [ ] Ocorrência rápida sem diário pede clima.
- [ ] `CONCLUIDA` e `CANCELADA` não saem do estado final.

## 5. Implementação

1. Atualizar diagrama de estados.
2. Atualizar diagrama de atividades.
3. Marcar ações manuais vs automáticas.
4. Revisar contra `PLANO.md` seção 4.

## 6. Invariantes

| Regra | Camada | Verificação |
|---|---|---|
| Transição de status é decisão de gestão | diagrama + domínio | revisão |
| Paralisação do diário é evento, não transição | diagrama | revisão |
| Responsável opcional só em `PLANEJADA` | diagrama + domínio | revisão |

## 7. Segurança

Não aplicável.

## 8. Migração e rollback

Não aplicável.

## 9. Critérios de aceite

- [ ] Diagramas refletem ADR-05/08.
- [ ] Atividades incluem categoria e clima.
- [ ] Estados finais explícitos.
- [ ] Nenhum estado de módulo externo.

## 10. Verificação e evidência

Comparar com seção 4.1 e 4.2 do `PLANO.md`. Evidência: diagramas versionados.
