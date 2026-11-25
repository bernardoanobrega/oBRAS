# L31 — Serviço de diário e RN-07

**Fase:** 3 · **Commit:** `feat(diario): implementar servico de diario`  
**Status:** Concluído  
**Depende de:** L30 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-06

## 1. Objetivo

Implementar criação, listagem, busca e edição de diário com RN-07 e auditoria.

## 2. Escopo

**Inclui:** `criar`, `listar`, `buscar`, `editar`; unicidade por obra/data; tradução de constraint; equipe presente a partir de alocações ativas; auditoria.  
**Não inclui:** exclusão (L36), ocorrências (L32), API (L34).

## 3. Contrato de serviço

- `DiarioService.criar(obraId, NovoDiarioDto)` → `DiarioDto`. O usuário vem de `CurrentUser.id()`, não de parâmetro.
- `listar(obraId)` → `List<DiarioResumoDto>`, ordenado por data desc.
- `buscar(obraId, diarioId)` → `DiarioDto`; diário de outra obra ou excluído → 404.
- `editar(obraId, diarioId, AlterarDiarioDto)` → `DiarioDto`; a data não muda na edição.
- Se já existir diário ativo para obra/data → 422 “Já existe registro para o dia”.
- Equipe presente só aceita alocações ativas da obra.
- Auditar criação/edição.

## 4. Testes primeiro (RED)

- `criaDiarioDoDia`.
- `rejeitaDiarioDuplicadoAtivo`.
- `permiteDiarioAposExclusaoLogica`.
- `atualizaClimaEDescricao`.
- `equipePresenteUsaSomenteAlocacoesAtivas`.
- `rejeitaEquipeDeOutraObra`.
- `duplicidadeSobConcorrenciaRetorna422`.
- `criacaoEEdicaoAuditam`.

## 5. Implementação

1. Criar `DiarioService` (serviço único do módulo diário).
2. Validar obra e alocações via services/ports do próprio módulo.
3. Inserir e capturar `DataIntegrityViolationException`.
4. Garantir auditoria na mesma transação.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Um diário ativo por obra/data | banco + serviço | integração |
| Excluído libera novo diário | banco + serviço | integração |
| Equipe presente é da obra | serviço | integração |
| Criação/edição audita | serviço | integração |

## 7. Segurança

API aplica `DETALHES_OBRA:CRIAR/EDITAR`; serviço não confia em `obraId` divergente.

## 8. Migração e rollback

Sem migration nova. Depende de L26.

## 9. Critérios de aceite

- [x] CRUD de diário.
- [x] RN-07 no serviço e banco.
- [x] Concorrência tratada.
- [x] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de duplicidade e concorrência.

**Evidência (25/11/2026):** `DiarioServiceIntegracaoTest`, com os 8 testes da seção 4 rodando contra PostgreSQL real (Testcontainers), service, auditoria e `ObraService` reais. O teste de concorrência põe duas threads, cada uma na sua transação, para passarem juntas pela conferência do service. O índice `uk_registro_diario_obra_data_ativo` barra a segunda, e o service devolve 422. `mvn test`: **117 testes, 0 falhas**.
