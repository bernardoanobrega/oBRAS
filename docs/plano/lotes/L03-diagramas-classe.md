# L03 — Diagramas de classe do Detalhe de Obra

**Fase:** 0 · **Commit:** `docs(diagramas): atualizar classes do detalhe`  
**Status:** Em andamento  
**Depende de:** L01, L02 · **RF/UC:** RF-14 a RF-19, RF-64 a RF-68, RF-73; UC04, UC05, UC21 · **ADR:** ADR-01, ADR-02, ADR-04, ADR-08

## 1. Objetivo

Atualizar o modelo de classes do módulo para refletir Obra auditável, foto sempre vinculada a ocorrência, paralisação com categoria e clima único por dia.

## 2. Escopo

**Inclui:** `Obra` + `Auditavel`; remoção/fachada de `adicionarFoto`; `RegistroDiario 1 → 0..* Ocorrencia`; `Ocorrencia → RegistroFotografico` obrigatório; `Paralisacao` com categoria; clima único.  
**Não inclui:** medição, revisão de projeto, avanço físico, orçamento, cronograma e documentos.

## 3. Contrato do artefato

- Diagrama de classes restrito a `Obra`, `AlocacaoObra`, `RegistroDiario`, `Ocorrencia`, `Paralisacao`, `RegistroFotografico`, `RegistroAuditoria` e `Auditavel`; `Canteiro` abandonado no módulo em 2026-11-27, não incluir.
- Legenda de cardinalidades.
- Nota de composição `obra.auditavel_id` vs herança JOINED.
- Nota de `ocorrencia.registro_diario_id NOT NULL` e `registro_fotografico.ocorrencia_id NOT NULL`.

## 4. Testes primeiro (revisão)

- [ ] Nenhuma foto sem ocorrência no diagrama.
- [ ] Nenhuma ocorrência sem diário.
- [ ] `RegistroDiario` tem um clima, sem turno/temperatura.
- [ ] `Obra` aparece como auditável.
- [ ] `adicionarFoto` removido ou marcado como fachada.
- [ ] Nenhuma entidade de outro módulo incluída.

## 5. Implementação

1. Editar diagrama de classes do módulo.
2. Atualizar cardinalidades.
3. Adicionar notas de ADR.
4. Revisar nomes de atributos contra migrations V2.

## 6. Invariantes

| Regra | Camada | Verificação |
|---|---|---|
| Foto sempre de ocorrência | diagrama + banco futuro | revisão |
| Ocorrência sempre de diário | diagrama + banco futuro | revisão |
| Clima único/dia | diagrama + banco futuro | revisão |
| Obra auditável por composição | diagrama | revisão |

## 7. Segurança

Não aplicável.

## 8. Migração e rollback

Não aplicável. Alteração documental.

## 9. Critérios de aceite

- [ ] Diagrama reflete ADR-01/02/04/08.
- [ ] Apêndice B do `PLANO.md` coberto para o módulo.
- [ ] Nenhuma entidade fora do Detalhe de Obra.
- [ ] Time de modelagem revisou.

## 10. Verificação e evidência

Comparar com `PLANO.md` Apêndice B e migrations V2. Evidência: diagrama versionado.
