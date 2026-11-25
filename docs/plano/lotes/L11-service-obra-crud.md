# L11 — Serviço CRUD de Obra

**Fase:** 1 · **Commit:** `feat(obra): implementar servico de obra`  
**Status:** Em andamento  
**Depende de:** L10 · **RF/UC:** RF-14, RF-15, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-04, ADR-05

## 1. Objetivo

Implementar criação, listagem, busca e edição de obra com transação, auditoria e validação de responsável.

## 2. Escopo

**Inclui:** `criar`, `listar`, `buscar`, `atualizar`, `definirResponsavel`; auditoria de criação/edição; validação de usuário ativo.  
**Não inclui:** transições (L09 já no domínio), endpoints (L12/L13), filtro por alocação (L14).

## 3. Contrato de serviço

- `criar(NovaObraDto, usuarioId)` → `ObraDto`.
- `listar()` → lista de `ObraDto`.
- `buscar(id)` → `ObraDto` ou 404.
- `atualizar(id, AtualizarObraDto, usuarioId)` → `ObraDto`.
- `definirResponsavel(id, usuarioId, usuarioLogadoId)` → `ObraDto`.
- Usuário responsável deve existir e estar ativo; usar service público ou port, nunca repository de outro módulo.

## 4. Testes primeiro (RED)

- `criaObraComAuditoriaDeCriacao`.
- `buscaObraInexistenteRetorna404`.
- `atualizaParametrosEAudita`.
- `defineResponsavelAtivo`.
- `rejeitaResponsavelInativo`.
- `naoAcessaRepositoryDeUsuario`.

## 5. Implementação

1. Criar `ObraService`.
2. Injetar `ObraRepository`, `AuditavelRepository` e port de usuário.
3. Anotar transações.
4. Traduzir exceções de domínio.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Criação sempre audita | serviço | integração |
| Edição sempre audita | serviço | integração |
| Responsável ativo | serviço | unitário/integração |
| Sem repository cruzado | arquitetura | revisão |

## 7. Segurança

Serviço recebe `usuarioId` do contexto autenticado; não confia em payload.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] CRUD de serviço funcional.
- [ ] Auditoria em criação/edição.
- [ ] Responsável validado.
- [ ] Sem acesso cruzado a repository.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de serviço e auditoria.
