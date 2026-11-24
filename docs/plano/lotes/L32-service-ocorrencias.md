# L32 — Serviço de ocorrências e paralisações

**Fase:** 3 · **Commit:** `feat(diario): implementar servico de ocorrencias`  
**Status:** Em revisão  
**Depende de:** L31 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-08

## 1. Objetivo

Implementar criação, edição e consulta de ocorrências e paralisações vinculadas ao diário.

## 2. Escopo

**Inclui:** ocorrência com categoria/descrição/dataHora/autor; paralisação com início/fim; validação de vínculo com obra/diário; auditoria.  
**Não inclui:** ocorrência rápida (L33), fotos (L35), exclusão (L36).

## 3. Contrato de serviço

- `criar(obraId, diarioId, OcorrenciaDto, usuarioId)`.
- `atualizar(obraId, ocorrenciaId, OcorrenciaDto, usuarioId)`.
- `listar(obraId, diarioId)`.
- `registrarParalisacao` recebe categoria e período.
- Ocorrência só pode ser criada em diário da mesma obra.
- Autor = usuário autenticado; dataHora = agora se não informada.
- Auditar criação/edição.

## 4. Testes primeiro (RED)

- `criaOcorrenciaNoDiarioDaObra`.
- `rejeitaDiarioDeOutraObra`.
- `rejeitaCategoriaNula`.
- `rejeitaDescricaoVazia`.
- `criaParalisacaoComCategoria`.
- `rejeitaParalisacaoComFimMenorQueInicio`.
- `atualizacaoAudita`.
- `autorNaoVemDoPayload`.

## 5. Implementação

1. Criar `OcorrenciaService`.
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

- [ ] CRUD de ocorrência.
- [ ] Paralisação com categoria.
- [ ] Vínculo com diário/obra.
- [ ] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de serviço.
