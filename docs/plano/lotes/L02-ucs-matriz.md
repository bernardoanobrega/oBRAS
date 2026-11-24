# L02 — UCs do Detalhe de Obra e matriz RF→UC

**Fase:** 0 · **Commit:** `docs(requisitos): mapear ucs do detalhe de obra`  
**Status:** Concluído  
**Depende de:** L01 · **RF/UC:** UC04, UC05, UC21 · **ADR:** ADR-03

## 1. Objetivo

Fixar somente os UCs e RFs que o módulo Detalhe de Obra consome, com matriz RF→UC e rastreabilidade para os lotes.

## 2. Escopo

**Inclui:** UC04 (cadastrar/configurar obras), UC05 (equipe/responsável), UC21 (diário/ocorrências); RF-14 a RF-19, RF-64 a RF-68, RF-70, RF-73.  
**Não inclui:** lista global de 21 UCs, UC03, UC06, UC07, UC08–UC20. Esses pertencem a outros módulos e não geram lote neste conjunto.

## 3. Contrato do artefato

- Tabela `UC | nome | RFs | lote(s)`.
- Tabela reversa `RF | UC | regra`.
- RF-73 marcado como painel; RF-70 como permissão por obra.
- Lacunas do módulo registradas explicitamente.

## 4. Testes primeiro (revisão)

- [ ] Apenas UC04, UC05 e UC21 entram na matriz.
- [ ] RF-14 a RF-19, RF-64 a RF-68, RF-70 e RF-73 aparecem em pelo menos um UC.
- [ ] Nenhum RF de outro módulo é puxado para dentro.
- [ ] Cada RF aponta para um lote deste conjunto.

## 5. Implementação

1. Extrair do Apêndice A somente as linhas UC04, UC05 e UC21.
2. Gerar matriz reversa.
3. Vincular cada RF ao lote correspondente.
4. Publicar em `docs/requisitos/matriz-detalhe-obra.md`.

## 6. Invariantes

| Regra | Camada | Verificação |
|---|---|---|
| Escopo do módulo não inclui outros UCs | documentação | revisão |
| RF transversal pode aparecer em mais de um UC | documentação | matriz |
| RF-73 é painel, não avanço físico | documentação | revisão |

## 7. Segurança

Não aplicável.

## 8. Migração e rollback

Não aplicável.

## 9. Critérios de aceite

- [ ] Matriz restrita ao módulo.
- [ ] RFs do módulo rastreados para lotes.
- [ ] Fora de escopo explícito.
- [ ] Nenhuma dependência de Fase 5.

## 10. Verificação e evidência

Conferir Apêndice A do `PLANO.md` e este recorte. Evidência: matriz publicada.
