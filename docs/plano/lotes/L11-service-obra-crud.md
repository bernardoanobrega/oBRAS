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

## 11. Auditoria do código (Cauã, 2026-11-25)

Leitura do código em `feat/obra-read-api` contra este contrato, para agilizar a conclusão. **Nada foi implementado**; os itens abaixo são sugestões para o Thierry decidir.

**Já existe**
- `ObraService.listar()` (filtra por `DETALHES_OBRA:VISUALIZAR` via `AutorizacaoPort`), `buscar(id)` (404 via `RecursoNaoEncontradoException`) e `equipe(id)`, com testes em `ObraServiceTest`.
- Métodos usados pelo diário: `obterEntidade`, `alocacaoAtiva`, `alocacoesAtivasPorIds`. **Não alterar a assinatura nem o comportamento** (L30–L36 dependem deles).
- `AuditoriaService.criar(acao, detalhe)` e `registrar(auditavel, acao, detalhe)`, prontos para criação e edição.

**Falta**
- `criar(NovaObraDto)`, `atualizar(id, AtualizarObraDto)` e `definirResponsavel(id, usuarioId)`.
- Pré-requisitos de outros lotes, ainda ausentes:
  - **L08:** `Obra` não tem `criar(...)`, `editar(...)`, `definirResponsavel(...)` nem guardas (nome, tipologia, endereço e datas obrigatórios; `fim >= início`; nasce `PLANEJADA`).
  - **L10:** `NovaObraDto` e `AtualizarObraDto` não existem. O `ObraDto` atual não expõe `auditavelId` (o contrato do L10 pede; avaliar se faz sentido expor).
- Os 6 testes da seção 4.

**Pontos de atenção (sugestões)**
1. **Usuário ativo sem repository cruzado:** não existe port para isso. Sugestão: `shared/security/UsuarioConsultaPort` com `boolean estaAtivo(Long usuarioId)`, implementado no módulo `usuario`, no mesmo padrão do `AutorizacaoPort` (ADR-12).
2. **`definirResponsavel`:** o método é deste lote; o endpoint é do L13.
3. **Auditoria:** a `Obra` guarda `auditavelId` (Long), e não `Auditavel` por composição como as entidades do diário. Ao criar, é preciso chamar `auditoria.criar(CRIACAO, ...)` antes e passar o id; ao editar, `registrar(..., EDICAO, ...)` precisa do `Auditavel`. Talvez falte `AuditoriaService` buscar o `Auditavel` pelo id.
4. **Docker não está disponível** na máquina do Cauã: testes com Testcontainers não rodam lá.
