# Método de construção dos lotes

**Fonte:** `../PLANO.md`  
**Objetivo:** maximizar corretude por funcionalidade e manter cada lote pequeno o bastante para virar um commit revisável.  
**Escopo estrito:** somente o módulo **Detalhe de Obra**.

## 0. Escopo deste conjunto

**Dentro:** obra e ciclo de vida, equipe/cargos/responsável técnico, canteiros, diário de campo (ocorrências, paralisações, clima e fotos), painel da obra (RF-73), auditoria das entidades do módulo e permissões por obra.

**Fora:** medições, revisões de projeto, avanço físico e qualquer módulo de orçamento, cronograma, documentos, estoque, compras, equipamentos ou usuários. A Fase 5 do `PLANO.md` não gera lote aqui; o painel apenas exibe KPI simulado com `fonte: SIMULADO` enquanto esses módulos não existirem.

**UCs usados:** UC04, UC05 e UC21. RFs do módulo: RF-14 a RF-19, RF-64 a RF-68, RF-70 e RF-73. RNF-09 e RNF-10 quando afetarem erro ou arquivo.

## 1. Princípios

1. **Rastreabilidade primeiro.** Todo lote declara RF, UC canônico, ADR, fase e dependências. Sem rastreabilidade, o lote não começa.
2. **Contrato antes do código.** Antes de implementar, escrever o contrato do lote: schema, API, DTO, estados de UI, erros e permissões.
3. **Teste primeiro, no nível certo.** Regra de domínio → teste unitário. SQL/constraint → teste de integração com PostgreSQL/Testcontainers. Endpoint/permissão → teste de controller. UI → teste de componente ou fluxo.
4. **Fatia vertical.** Um lote entrega comportamento observável ponta a ponta quando possível; não entregar camada solta sem teste.
5. **Invariante em profundidade.** Regra crítica é validada em mais de uma camada: banco + domínio + serviço + API. Duplicação intencional; cada camada tem teste próprio.
6. **Segurança é do backend.** `@PreAuthorize` em todo endpoint. Esconder botão no frontend é conforto, nunca controle.
7. **Migração forward-only.** Nunca editar migration aplicada. Backfill e `NOT NULL` em passos separados. Toda migration tem plano de rollback lógico.
8. **Evidência ou não terminou.** Cada lote termina com comandos executados, testes verdes e checklist de aceite preenchido.

## 2. Fluxo obrigatório (gates)

### Gate 0 — Escopo congelado
- Ler `PLANO.md`, ADRs e RF/UC citados.
- Escrever “inclui” e “não inclui”.
- Identificar dependências e riscos.

### Gate 1 — Contrato escrito
- **DB:** tabelas, colunas, FKs, checks, índices, backfill.
- **API:** método, rota, request, response, status HTTP, erros.
- **UI:** loading, vazio, erro, sucesso, permissão negada, mobile.
- **Permissão:** módulo, ação, escopo global ou por obra.

### Gate 2 — Testes RED
- Listar nomes dos testes antes do código.
- Cobrir caminho feliz, regra inválida, duplicidade, concorrência quando aplicável, 401/403 e isolamento entre obras.
- Rodar e confirmar falha esperada.

### Gate 3 — Migração
- Criar `Vn__descricao.sql` novo.
- Testar em banco limpo e em banco com dados existentes.
- Validar constraints e índices com SQL real.

### Gate 4 — Domínio
- Entidade e enums sem dependência de framework de transporte.
- Guardas de estado e regras de negócio no domínio.
- Nenhuma entidade JPA exposta na API.

### Gate 5 — Persistência
- Repository apenas do próprio módulo.
- Módulo não acessa repository de outro módulo; usar service público ou port em `shared`.
- Consultas com filtro de obra e de exclusão lógica quando aplicável.

### Gate 6 — Serviço
- Transação explícita.
- Traduzir violação de constraint para mensagem de negócio.
- Auditoria na mesma transação.
- Não confiar em validação do frontend.

### Gate 7 — API
- DTO de entrada e saída.
- Bean Validation.
- `@PreAuthorize` em todos os endpoints.
- Erros no formato `ApiError`.

### Gate 8 — Frontend
- Usar `http.js`, `AppShell`, router e `usePermissao` existentes.
- Implementar estados de carregamento, vazio, erro e sem permissão.
- Mobile-first quando o lote for de diário/painel.
- Não duplicar regra de negócio; apenas refletir o backend.

### Gate 9 — Verificação
Backend:
```bash
cd obras-backend
mvn test
```
Frontend:
```bash
cd obras-frontend
npm test
npm run build
```
Não existe script de lint no frontend; `build` + `test` são os gates disponíveis.

### Gate 10 — DoD e commit
- Checklist de aceite 100%.
- Migration aplicada em banco limpo.
- Testes negativos verdes.
- Documentação do lote atualizada.
- Commit pequeno, com mensagem no padrão do repositório.

## 3. Template aplicado a cada lote

Cada arquivo `Lxx-*.md` tem um cabeçalho e 10 seções numeradas:

- Cabeçalho: fase, commit, dependências, RF/UC, ADR.
1. Objetivo.
2. Escopo incluído e excluído.
3. Contrato DB/API/UI.
4. Testes primeiro (RED).
5. Implementação (GREEN).
6. Invariantes e camadas.
7. Segurança.
8. Migração e rollback.
9. Critérios de aceite.
10. Verificação e evidência.

## 4. Matriz mínima de testes

| Área | O que provar |
|---|---|
| Domínio | transições válidas/inválidas, guardas, estados finais |
| Persistência | constraints, índices únicos parciais, backfill |
| Serviço | regra de negócio, duplicidade, concorrência, cascata |
| API | 201/200/204/400/401/403/404/409/422 |
| Permissão | global vs por obra, usuário sem alocação, obra diferente |
| Auditoria | criação, edição, status, responsável, exclusão |
| Frontend | loading, vazio, erro, sem permissão, sucesso, mobile |
| Contrato | DTO real do backend consumido pelo frontend |

## 5. Definition of Done

- [ ] RF/UC/ADR citados no lote.
- [ ] Contrato DB/API/UI escrito.
- [ ] Testes RED criados antes da implementação.
- [ ] Testes verdes, incluindo negativos.
- [ ] `mvn test` verde.
- [ ] `npm test` verde.
- [ ] `npm run build` verde.
- [ ] Migration testada em banco limpo.
- [ ] Permissões testadas no backend.
- [ ] Estados de UI implementados.
- [ ] Sem TODO pendente no caminho principal.
- [ ] Evidência registrada no lote.
- [ ] Status atualizado na linha `**Status:**` do lote e na tabela da `DIVISAO-TAREFAS-THIERRY-CAUA.md` (`Em andamento` ao começar; `Concluído` se feito pelo dono, `Em revisão` se feito pelo outro).

## 6. Convenções

- Commit: `feat(obra): ...`, `feat(diario): ...`, `test(obra): ...`, `docs(plano): ...`.
- Migration: `V3__auditoria_obra.sql`, `V4__diario_ocorrencias_fotos.sql`. Não criar V5 neste conjunto.
- Pacote backend: `br.com.obras.<modulo>.{domain,repository,service,controller,dto,mapper}`.
- Erros: `RegraNegocioException` → 422; `RecursoNaoEncontradoException` → 404; `AccessDeniedException` → 403.
- Datas: `LocalDate` para dia; `LocalDateTime` para evento.
- Exclusão: lógica para obra, diário, ocorrência e foto; física apenas onde o plano permitir.

## 7. Como executar um lote

1. Abrir o arquivo `Lxx-*.md`.
2. Congelar escopo e contrato.
3. Escrever os testes listados.
4. Rodar e confirmar RED.
5. Implementar na ordem: migration → domínio → persistência → serviço → API → frontend.
6. Rodar os gates de verificação.
7. Preencher evidência e marcar DoD.
8. Commit.
