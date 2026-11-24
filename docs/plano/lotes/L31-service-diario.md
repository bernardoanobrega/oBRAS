# L31 — Serviço de diário e RN-07

**Fase:** 3 · **Commit:** `feat(diario): implementar servico de diario`  
**Status:** Em revisão  
**Depende de:** L30 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-06

## 1. Objetivo

Implementar criação, listagem, busca e edição de diário com RN-07 e auditoria.

## 2. Escopo

**Inclui:** `criar`, `listar`, `buscar`, `atualizar`; unicidade por obra/data; tradução de constraint; equipe presente a partir de alocações ativas; auditoria.  
**Não inclui:** exclusão (L36), ocorrências (L32), API (L34).

## 3. Contrato de serviço

- `criar(obraId, DiarioDto, usuarioId)` → `DiarioDto`.
- `listar(obraId)` ordenado por data desc.
- `buscar(obraId, diarioId)`.
- `atualizar(obraId, diarioId, DiarioDto, usuarioId)`.
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

1. Criar `RegistroDiarioService`.
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

- [ ] CRUD de diário.
- [ ] RN-07 no serviço e banco.
- [ ] Concorrência tratada.
- [ ] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de duplicidade e concorrência.
