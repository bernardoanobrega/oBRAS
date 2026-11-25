# L32 — Serviço de ocorrências e paralisações

**Fase:** 3 · **Commit:** `feat(diario): implementar servico de ocorrencias`  
**Status:** Concluído  
**Depende de:** L31 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-08

## 1. Objetivo

Implementar criação, edição e consulta de ocorrências e paralisações vinculadas ao diário.

## 2. Escopo

**Inclui:** ocorrência com categoria/descrição/dataHora/autor; paralisação com início/fim; validação de vínculo com obra/diário; auditoria.  
**Não inclui:** ocorrência rápida (L33), fotos (L35), exclusão (L36).

## 3. Contrato de serviço

- `DiarioService.adicionarOcorrencia(obraId, diarioId, NovaOcorrenciaDto)` → `OcorrenciaDto`.
- `DiarioService.editarOcorrencia(obraId, ocorrenciaId, NovaOcorrenciaDto)` → `OcorrenciaDto`: edição completa. Paralisação ausente remove a paralisação existente.
- **Sem `listar` próprio**: as ocorrências ativas do diário vêm no `DiarioDto` do `buscar(obraId, diarioId)` do diário (L31).
- A paralisação é registrada **junto com a ocorrência** (campo `paralisacao` do DTO, com início e fim) e **não tem categoria própria**: herda a da ocorrência (confirmação do L29).
- A ocorrência só pode ser criada em diário **ativo** da mesma obra. Ocorrência de outra obra, ou de diário excluído, dá 404.
- O autor é a alocação ativa do usuário autenticado (`CurrentUser.id()`). O DTO não tem campo de autor. Usuário não alocado na obra → 422.
- Sem `dataHora`, vale **agora**. A data da ocorrência tem que ser a do diário, senão 422 "A ocorrência deve ser do mesmo dia do diário" ([ADR-18](../../adr/ADR-18-ocorrencia-mesmo-dia-diario.md)).
- Auditar criação e edição.

## 4. Testes primeiro (RED)

- `criaOcorrenciaNoDiarioDaObra`.
- `rejeitaDiarioDeOutraObra`.
- `rejeitaCategoriaNula`.
- `rejeitaDescricaoVazia`.
- `criaParalisacaoComCategoria`.
- `rejeitaParalisacaoComFimMenorQueInicio`.
- `atualizacaoAudita`.
- `autorNaoVemDoPayload`.

**Onde ficaram:**
- Os 7 primeiros estão em `OcorrenciaServiceIntegracaoTest`, com banco real.
- `autorNaoVemDoPayload` é unitário, em `DiarioServiceTest`, como pede a tabela de invariantes.
- `criaParalisacaoComCategoria` prova que a paralisação nasce com a ocorrência e herda a categoria dela.
- Extras:
  - a regra do mesmo dia (ADR-18), em `OcorrenciaTest`;
  - a cascata lógica do L36 (`excluirDiarioMarcaOcorrenciasEFotos` e `excluirOcorrenciaMarcaFotos`), antecipada aqui porque, sem ela, a ocorrência de um diário excluído continuava editável.

## 5. Implementação

1. Implementar em `DiarioService` (serviço único do módulo diário).
2. Validar diário e obra.
3. Usar `CurrentUser.id()` para autor.
4. Registrar auditoria.
5. Traduzir violações de constraint.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Ocorrência sempre em diário da obra | serviço | integração |
| Categoria obrigatória | domínio + DTO | 400/422 |
| Autor é o autenticado | serviço | unitário |
| Paralisação com período válido | domínio + banco | unitário |

## 7. Segurança

Permissão `DETALHES_OBRA:CRIAR/EDITAR`; validar vínculo com a obra da URL.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [x] CRUD de ocorrência.
- [x] Paralisação com categoria (herdada da ocorrência).
- [x] Vínculo com diário/obra.
- [x] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de serviço.

**Evidência (25/11/2026):** `OcorrenciaServiceIntegracaoTest` (9 testes: os 7 de integração do lote e os 2 da cascata do L36, com banco real), `DiarioServiceTest.autorNaoVemDoPayload` e `OcorrenciaTest` (+4, regra do mesmo dia). `mvn test`: **131 testes, 0 falhas**.
