# L51 — Permissões finas por ação

**Fase:** 6 · **Commit:** `feat(seguranca): refinar permissoes por acao`  
**Status:** Em revisão  
**Depende de:** L14, L22, L34, L42 · **RF/UC:** RF-70; UC04, UC05, UC21 · **ADR:** ADR-09

## 1. Objetivo

Revisar e fechar a matriz de permissões por ação do módulo Detalhe de Obra.

## 2. Escopo

**Inclui:** matriz `rota × módulo × ação × escopo`; revisão de todos os `@PreAuthorize`; testes de 401/403 por ação; documentação da matriz.  
**Não inclui:** criação de novos módulos ou permissões globais de outros módulos.

## 3. Contrato de permissões

- Visualizar obra/equipe/diário/painel: `DETALHES_OBRA:VISUALIZAR`.
- Criar obra/alocação/diário/ocorrência/foto: `DETALHES_OBRA:CRIAR`.
- Editar obra/alocação/diário/ocorrência/status: `DETALHES_OBRA:EDITAR`.
- Excluir diário/ocorrência/foto: `DETALHES_OBRA:EXCLUIR`.
- Nenhum endpoint sem `@PreAuthorize`.

## 4. Testes primeiro (RED)

- `matrizCobreTodosOsEndpoints`.
- `endpointSemPreAuthorizeFalhaNoTeste`.
- `cadaAcaoTemTeste403`.
- `usuarioComVisualizarNaoCria`.
- `usuarioComCriarNaoExclui`.
- `permissaoGlobalNaoSubstituiEscopoDeObraSemRegra`.

## 5. Implementação

1. Gerar inventário de rotas.
2. Cruzar com `AcaoPermissao`.
3. Corrigir anotações divergentes.
4. Publicar matriz em `docs/seguranca/permissoes-detalhe-obra.md`.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Toda rota protegida | controller | inventário |
| Ação correta por rota | controller | 403 |
| Escopo por obra | controller | 403 |
| Frontend não é segurança | UI | revisão |

## 7. Segurança

Esta é a auditoria final de segurança do módulo. Não aceitar atalho de permissão no frontend.

## 8. Migração e rollback

Sem migration. Permissões já existem em V1.

## 9. Critérios de aceite

- [ ] Matriz completa.
- [ ] Todos os endpoints protegidos.
- [ ] 403 por ação testado.
- [ ] Documentação publicada.

## 10. Verificação e evidência

`mvn test` + revisão da matriz. Evidência: matriz e relatório de testes.
