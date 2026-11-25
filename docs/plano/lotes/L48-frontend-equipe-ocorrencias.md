# L48 — Frontend: equipe, ocorrências e mural no painel

**Fase:** 4 · **Commit:** `feat(painel): integrar equipe e ocorrencias`  
**Status:** Pendente  
**Depende de:** L47 · **RF/UC:** RF-16 a RF-19, RF-64 a RF-68; UC05, UC21 · **ADR:** ADR-02, ADR-07

## 1. Objetivo

Integrar ao painel a equipe da obra, as ocorrências e o mural de fotos.

## 2. Escopo

**Inclui:** lista de equipe com cargos; ocorrências recentes; mural somente com fotos de ocorrências; links para as telas de equipe/diário; estados de UI.  
**Não inclui:** criar/editar equipe no painel (tela própria); upload no mural.

## 3. Contrato UI

- Equipe exibe nome, cargos e situação.
- Ocorrências exibem categoria, descrição e data/hora.
- Mural exibe somente fotos de ocorrências.
- Links respeitam permissão.
- Dados vêm do `/painel`.
- Sem diário, ocorrências/fotos vazias com aviso.

## 4. Testes primeiro (RED)

- `painelListaEquipeComCargos`.
- `painelListaOcorrenciasRecentes`.
- `muralSoMostraFotosDeOcorrencias`.
- `linkEquipeRespeitaPermissao`.
- `semDiarioMostraAviso`.
- `fotoSemOcorrenciaNaoRenderiza`.

## 5. Implementação

1. Criar `EquipeResumo`, `OcorrenciasRecentes`, `MuralFotos`.
2. Consumir dados de L45.
3. Não buscar endpoint de outro módulo.
4. Ajustar responsividade.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Mural sem foto avulsa | UI | teste |
| Links por permissão | UI | teste |
| Dados do painel | UI | contrato |
| Sem diário não inventa dados | UI | teste |

## 7. Segurança

Links ocultos sem permissão; backend continua validando.

## 8. Migração e rollback

Sem migration. Rollback: remover seções.

## 9. Critérios de aceite

- [ ] Equipe no painel.
- [ ] Ocorrências recentes.
- [ ] Mural correto.
- [ ] Permissões e estados.
- [ ] Testes/build verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke.
