# L45 — Frontend: substituir mock por dados reais

**Fase:** 4 · **Commit:** `feat(painel): consumir dados reais`  
**Status:** Em andamento  
**Depende de:** L44 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-07

## 1. Objetivo

Consumir o endpoint `/painel` e as APIs de obra/equipe/diário, removendo constantes mockadas.

## 2. Escopo

**Inclui:** `painelApi.js`, estados loading/erro/vazio, dados de obra, clima, equipe, mão de obra, ocorrências e fotos.  
**Não inclui:** ações de escrita (L47), KPIs reais de orçamento/cronograma.

## 3. Contrato UI

- Carrega `/api/obras/{obraId}/painel`.
- Exibe dados reais; `null` não vira zero.
- `fonte: SIMULADO` visível nos KPIs.
- Sem diário hoje, mostra alocados e aviso.
- Erro de API mostra `ErrorState`.
- Loading mostra `Spinner`/skeleton.

## 4. Testes primeiro (RED)

- `carregaPainelERenderizaObra`.
- `loadingInicial`.
- `erroMostraErrorState`.
- `semDiarioMostraAlocados`.
- `kpiNuloNaoViraZero`.
- `fonteSimuladoEhVisivel`.
- `fotosVemDoPainel`.

## 5. Implementação

1. Criar `painelApi.js`.
2. Remover constantes `OBRA`, `CLIMA`, `EQUIPE`, `OCORRENCIAS`, `FOTOS`.
3. Mapear DTO para componentes.
4. Tratar `null` explicitamente.
5. Manter fallback de erro.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Nenhum mock no caminho real | UI | revisão |
| Null não é zero | UI | teste |
| Fonte simulada explícita | UI | teste |
| Erro não quebra tela | UI | teste |

## 7. Segurança

Somente leitura; backend valida permissão.

## 8. Migração e rollback

Sem migration. Rollback: restaurar constantes mock.

## 9. Critérios de aceite

- [ ] Painel consome API.
- [ ] Sem constantes mockadas.
- [ ] Estados de UI.
- [ ] KPIs nulos explícitos.
- [ ] Testes/build verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke.
