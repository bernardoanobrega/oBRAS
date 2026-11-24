# L41 — Serviço agregador do painel

**Fase:** 4 · **Commit:** `feat(painel): agregar dados da obra`  
**Status:** Pendente  
**Depende de:** L31–L36 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-01, ADR-07, ADR-10

## 1. Objetivo

Agregar dados reais de obra, equipe, diário, ocorrências e fotos em um único DTO de painel.

## 2. Escopo

**Inclui:** `obra`, `clima`, `kpis`, `equipe`, `maoDeObra`, `ocorrencias`, `fotos`; KPIs simulados com `fonte: SIMULADO` quando módulo não existir.  
**Não inclui:** cálculo real de orçamento/cronograma; endpoints de outros módulos.

## 3. Contrato de serviço

- `painel(obraId)` → `PainelObraDto`.
- Clima = diário do dia, se existir.
- `maoDeObra.alocados` = alocações ativas.
- `presentesHoje` = equipe presente do diário do dia.
- `vsOntem` = comparação com diário anterior, se existir.
- Sem diário hoje, indicar ausência e mostrar alocados.
- KPIs de orçamento/cronograma = `null` + `fonte: SIMULADO`.

## 4. Testes primeiro (RED)

- `painelIncluiObraEEquipeAtiva`.
- `painelSemDiarioHojeIndicaAusencia`.
- `painelComDiarioUsaClimaEPresentes`.
- `vsOntemComparaDiarioAnterior`.
- `kpisSemModuloTemFonteSimulado`.
- `painelNaoVazaOcorrenciaExcluida`.
- `painelNaoVazaFotoDeOutraObra`.

## 5. Implementação

1. Criar `PainelObraService`.
2. Reutilizar services de obra, alocação e diário.
3. Não acessar repository de outro módulo.
4. Montar DTO com dados reais + simulados explícitos.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Dado simulado marcado | serviço | unitário |
| Sem diário não inventa presença | serviço | unitário |
| Excluídos não aparecem | serviço | integração |
| Isolamento por obra | serviço | integração |

## 7. Segurança

Serviço recebe `obraId` já autorizado pelo controller.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] DTO de painel completo.
- [ ] Dados reais de equipe/diário.
- [ ] KPIs simulados explícitos.
- [ ] Sem vazamento entre obras.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de agregação.
