# L26 — Migration V4: diário, equipe presente e RN-07

**Fase:** 3 · **Commit:** `feat(diario): criar schema do diario`  
**Status:** Concluído  
**Depende de:** L06, L07 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-06

## 1. Objetivo

Criar `registro_diario` e `registro_diario_equipe`, com unicidade por obra/data e exclusão lógica sincronizada.

## 2. Escopo

**Inclui:** tabela `registro_diario`, coluna espelho `excluido`, índice único parcial, N:N com `alocacao_obra`, trigger de sincronização.  
**Não inclui:** ocorrência/paralisia/foto (L27), domínio/serviço (L28+).

## 3. Contrato DB

- `registro_diario(id BIGINT PK REFERENCES auditavel(id), obra_id, data, clima, descricao, excluido)`.
- `clima` limitado a `ENSOLARADO`, `NUBLADO`, `CHUVA_FRACA`, `CHUVA_FORTE`.
- `CREATE UNIQUE INDEX uk_registro_diario_obra_data_ativo ON registro_diario (obra_id, data) WHERE NOT excluido`.
- `registro_diario_equipe(registro_diario_id, alocacao_obra_id)` PK composta.
- Trigger sincroniza `registro_diario.excluido` com `auditavel.excluido`.

## 4. Testes primeiro (RED)

- `migrationV4CriaDiarioEEquipe`.
- `rejeitaDoisDiariosAtivosMesmaObraData`.
- `permiteNovoDiarioAposExclusaoLogica`.
- `triggerSincronizaExcluido`.
- `rejeitaClimaForaDoEnum`.
- `rejeitaEquipeDuplicadaNoDiario`.

## 5. Implementação

1. Criar `V4__diario_ocorrencias_fotos.sql` na parte do diário.
2. Criar função/trigger de sincronização.
3. Criar índice único parcial.
4. Testar com dados de duas obras e mesma data.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Um diário ativo por obra/data | banco | unique parcial |
| Excluído libera novo diário | banco | integração |
| Excluído espelha auditável | banco | trigger |
| Clima no enum | banco | check |

## 7. Segurança

Sem endpoint. Acesso será por obra na API.

## 8. Migração e rollback

Forward-only. Rollback manual em ambiente descartável: dropar trigger, tabelas e índice. Não editar V4 após aplicada.

## 9. Critérios de aceite

- [x] V4 aplica em banco limpo.
- [x] Unique parcial correto.
- [x] Trigger sincroniza.
- [x] Clima restrito.

## 10. Verificação e evidência

Teste de integração PostgreSQL/Testcontainers. Evidência: schema e testes de constraint.
