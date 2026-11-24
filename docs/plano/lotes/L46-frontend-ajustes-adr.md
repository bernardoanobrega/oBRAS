# L46 — Frontend: aplicar cortes de escopo do mockup

**Fase:** 4 · **Commit:** `feat(painel): aplicar cortes de escopo`  
**Status:** Em revisão  
**Depende de:** L45 · **RF/UC:** RF-64 a RF-68, RF-73; UC21 · **ADR:** ADR-01, ADR-02, ADR-07, ADR-10  
**Contexto completo da próxima sessão:** [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md)

## 1. Objetivo

Remover do painel tudo que foi cortado nas ADRs: clima por turno, temperatura, sync, foto avulsa, check-in, frente/CREA e exportação sem RF.

**Relação com a fatia visual:** os cortes serão **antecipados em L43/L44** para a tela já nascer correta. L46 permanece como verificação final/regressão, executada depois de L45, para garantir que os cortes não voltaram com a integração de dados reais. Não bloquear L43/L44 esperando L45.

## 2. Escopo

**Inclui:** remoção de turno/temperatura/sync; clima único do dia; remoção de foto avulsa; remoção de check-in/próprios/terceirizados; remoção de frente/CREA; remoção de “Exportar Relatório Consolidado”; layout responsivo; checklist de regressão dos cortes.  
**Não inclui:** novas features de outros módulos; refazer a página já entregue em L43/L44.

## 3. Contrato UI

- Clima exibe apenas condição do dia e data.
- Mural só fotos de ocorrências.
- Equipe sem frente/CREA.
- Mão de obra sem check-in.
- Nenhum botão de exportação sem RF.
- Layout funciona em mobile/tablet.

## 4. Testes primeiro (RED)

- `naoRenderizaTurnoManhaTarde`.
- `naoRenderizaTemperatura`.
- `naoRenderizaSincronizacaoMeteorologica`.
- `naoRenderizaFotoAvulsa`.
- `naoRenderizaCheckIn`.
- `naoRenderizaFrenteNemCrea`.
- `naoRenderizaExportarRelatorio`.
- `layoutResponsivoSemSidebarFixa`.

## 5. Implementação

1. Revisar se L43/L44 já removeram constantes e componentes cortados.
2. Se algo voltou com L45, remover de novo.
3. Ajustar textos e badges (`SIMULADO` nos KPIs).
4. Ajustar grid para mobile.
5. Revisar contra ADR-10.
6. Rodar testes de regressão dos cortes.

### Checklist de cortes

- [ ] Sem `TURNO DA MANHÃ`/`TURNO DA TARDE`.
- [ ] Sem temperatura.
- [ ] Sem sincronização meteorológica.
- [ ] Sem upload de foto avulsa.
- [ ] Sem check-in.
- [ ] Sem próprios/terceirizados.
- [ ] Sem frente/CREA.
- [ ] Sem exportação.
- [ ] Clima único do dia.
- [ ] Mural só com fotos de ocorrências.
- [ ] Layout responsivo.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Clima único/dia | UI | teste |
| Foto sempre de ocorrência | UI | teste |
| Sem funcionalidade sem RF | UI | revisão |
| Layout responsivo | UI | teste/revisão |

## 7. Segurança

Não aplicável além de manter permissões.

## 8. Migração e rollback

Sem migration. Rollback: restaurar componentes removidos.

## 9. Critérios de aceite

- [ ] Todos os cortes aplicados e verificados após L45.
- [ ] Nenhum botão sem RF.
- [ ] Layout responsivo.
- [ ] Testes/build verdes.
- [ ] Nenhum corte reintroduzido pela integração de dados.

## 10. Verificação e evidência

```bash
cd /home/costa/Estudos/ENG-SOFT/oBRAS/obras-frontend
npm test
npm run build
```

Evidência: checklist ADR-10, testes de regressão dos cortes e smoke mobile.  
Contexto completo: [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md).
