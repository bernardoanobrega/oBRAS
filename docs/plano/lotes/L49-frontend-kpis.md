# L49 — Frontend: KPIs simulados com fonte

**Fase:** 4 · **Commit:** `feat(painel): exibir kpis simulados`  
**Status:** Em revisão  
**Depende de:** L48 · **RF/UC:** RF-73; UC04, UC21 · **ADR:** ADR-07, ADR-10

## 1. Objetivo

Exibir KPIs de avanço/orçamento de forma honesta enquanto Cronograma e Orçamento não existirem.

## 2. Escopo

**Inclui:** cards de avanço realizado/previsto, SPI, orçamento, executado e limite de alerta; `fonte: SIMULADO`; estado nulo; tooltip explicativo.  
**Não inclui:** cálculo real de cronograma/orçamento; integração com módulos externos.

## 3. Contrato UI

- KPI nulo exibe “—” ou “Não disponível”, nunca zero.
- Badge “SIMULADO” visível.
- Tooltip/texto explica que o dado será real quando módulo existir.
- Sem prometer exportação ou relatório.
- Layout responsivo.

## 4. Testes primeiro (RED)

- `kpiNuloMostraIndisponivel`.
- `badgeSimuladoVisivel`.
- `naoConverteNuloEmZero`.
- `tooltipExplicaFonte`.
- `kpisNaoQuebramLayoutMobile`.

## 5. Implementação

1. Criar `KpiCard`/`KpiGrid`.
2. Ler `kpis` do painel.
3. Renderizar `null` corretamente.
4. Adicionar badge de fonte.
5. Remover qualquer mock hardcoded de valor real.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Dado simulado é explícito | UI | teste |
| Nulo não vira zero | UI | teste |
| Sem promessa de dado real | UI | revisão |
| Responsivo | UI | teste |

## 7. Segurança

Sem impacto de permissão; leitura apenas.

## 8. Migração e rollback

Sem migration. Rollback: remover cards.

## 9. Critérios de aceite

- [ ] KPIs exibidos.
- [ ] Fonte simulada visível.
- [ ] Nulos tratados.
- [ ] Layout responsivo.
- [ ] Testes/build verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke.
