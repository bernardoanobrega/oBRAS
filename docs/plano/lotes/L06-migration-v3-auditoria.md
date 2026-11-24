# L06 — Migration V3: auditável e registro de auditoria

**Fase:** 1 · **Commit:** `feat(auditoria): criar base de auditoria`  
**Status:** Em andamento  
**Depende de:** L01–L05 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-04

## 1. Objetivo

Criar a base persistente de auditoria usada por Obra, diário, ocorrência e foto.

## 2. Escopo

**Inclui:** `auditavel`, `registro_auditoria`, `detalhe JSONB`, FK para usuário, índices.  
**Não inclui:** coluna em `obra` (L07), trigger de diário (L26), API.

## 3. Contrato DB

- `auditavel(id BIGINT PK, excluido BOOLEAN NOT NULL DEFAULT false)`.
- `registro_auditoria(id, auditavel_id NOT NULL, usuario_id NOT NULL, acao VARCHAR, data_hora TIMESTAMP, detalhe JSONB)`.
- `acao` restrita a `CRIACAO`, `EDICAO`, `EXCLUSAO`.
- Índice por `auditavel_id, data_hora`.

## 4. Testes primeiro (RED)

- `migrationV3CriaTabelasDeAuditoria`.
- `registroAuditoriaAceitaDetalheJsonb`.
- `registroAuditoriaRejeitaAcaoInvalida`.
- `auditavelIniciaNaoExcluido`.

## 5. Implementação

1. Criar `V3__auditoria_obra.sql` somente com auditoria.
2. Rodar Flyway em banco limpo.
3. Rodar em banco com V1/V2 aplicadas.
4. Validar JSONB e FK com SQL real.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Auditoria sempre referencia auditável e usuário | banco | FK |
| Ação limitada ao enum | banco | check |
| Exclusão lógica inicia falsa | banco | default |

## 7. Segurança

Sem endpoint. Dados de auditoria não podem ser alterados pela API.

## 8. Migração e rollback

Forward-only. Rollback manual: `DROP TABLE registro_auditoria; DROP TABLE auditavel;` apenas em ambiente descartável.

## 9. Critérios de aceite

- [ ] V3 aplica em banco limpo.
- [ ] V3 aplica sobre V2 com dados.
- [ ] Constraints testadas.
- [ ] Nenhuma alteração em V1/V2.

## 10. Verificação e evidência

`mvn test` com teste de migration. Evidência: log Flyway e schema conferido.
