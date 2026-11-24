# L07 — Migration V3: obra auditável e responsável opcional

**Fase:** 1 · **Commit:** `feat(obra): tornar obra auditavel`  
**Status:** Em andamento  
**Depende de:** L06 · **RF/UC:** RF-14, RF-15, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-04, ADR-05

## 1. Objetivo

Vincular cada obra a um `auditavel` e permitir `responsavel_id` nulo em `PLANEJADA`.

## 2. Escopo

**Inclui:** `obra.auditavel_id` único, backfill 1:1, `NOT NULL`, FK; `ALTER COLUMN responsavel_id DROP NOT NULL`; índice.  
**Não inclui:** editar `V2__obras.sql`; lógica de transição.

## 3. Contrato DB

1. Adicionar `auditavel_id BIGINT NULL`.
2. Inserir um `auditavel` para cada obra existente.
3. Preencher `obra.auditavel_id`.
4. Aplicar `NOT NULL` e `UNIQUE`.
5. Adicionar FK para `auditavel(id)`.
6. `ALTER TABLE obra ALTER COLUMN responsavel_id DROP NOT NULL`.

## 4. Testes primeiro (RED)

- `backfillCriaUmAuditavelPorObra`.
- `obraNaoAceitaAuditavelDuplicado`.
- `obraAceitaResponsavelNulo`.
- `obraExistenteMantemResponsavel`.
- `migrationV3AplicaSobreV2ComDados`.

## 5. Implementação

1. Escrever SQL em passos ordenados.
2. Testar com duas obras e responsáveis distintos.
3. Conferir contagem `obra = auditavel` após backfill.
4. Garantir que nenhuma FK existente foi reescrita.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Toda obra tem exatamente um auditável | banco | unique + not null |
| Obra existente não perde responsável | banco | backfill |
| Responsável pode ser nulo | banco | insert |
| Obra nunca é apagada fisicamente | banco + serviço | teste futuro |

## 7. Segurança

Sem endpoint novo.

## 8. Migração e rollback

Forward-only. Rollback lógico: manter `auditavel_id` e reverter `NOT NULL` apenas em ambiente descartável. Não dropar `auditavel` com auditoria gravada.

## 9. Critérios de aceite

- [ ] Backfill 1:1 comprovado.
- [ ] `auditavel_id` único e `NOT NULL`.
- [ ] `responsavel_id` aceita nulo.
- [ ] V2 intacta.

## 10. Verificação e evidência

Teste de integração com PostgreSQL/Testcontainers. Evidência: SQL de conferência e teste verde.
