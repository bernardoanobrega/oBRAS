# L52 — E2E e usabilidade do fluxo de obra

**Fase:** 6 · **Commit:** `test(e2e): cobrir fluxo do detalhe de obra`  
**Status:** Em revisão
**Depende de:** L50, L51 · **RF/UC:** RF-14 a RF-19, RF-64 a RF-68, RF-70, RF-73; UC04, UC05, UC21 · **ADR:** ADR-09, ADR-10

## 1. Objetivo

Validar o fluxo completo do módulo e ajustar mensagens de usabilidade.

## 2. Escopo

**Inclui:** E2E de cadastro de obra, equipe, diário, ocorrência, foto, painel e transições; mensagens de erro; confirmações; estados vazios.  
**Não inclui:** módulos externos.

## 3. Contrato E2E

1. Criar obra sem responsável.
2. Alocar equipe e definir responsável.
3. Iniciar obra.
4. Criar diário e ocorrência.
5. Anexar foto.
6. Ver painel atualizado.
7. Paralisar/retomar/concluir.
8. Conferir auditoria e permissões.

## 4. Testes primeiro (RED)

- `e2eCriaObraSemResponsavel`.
- `e2eIniciaSomenteComResponsavel`.
- `e2eAlocaEquipe`.
- `e2eRegistraDiarioEOcorrencia`.
- `e2eAnexaFoto`.
- `e2ePainelRefleteDados`.
- `e2eConcluiEEncerraEquipe`.
- `e2eUsuarioSemPermissaoNaoAcessa`.

## 5. Implementação

1. Criar suíte E2E com dados únicos no banco de desenvolvimento, sem destruir registros existentes.
2. Usar dados determinísticos.
3. Ajustar mensagens e confirmações encontradas.
4. Corrigir fluxos quebrados.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Fluxo ponta a ponta funciona | E2E | suíte |
| Permissão bloqueia de verdade | E2E | 403 |
| Mensagens claras | UI | revisão |
| Dados persistem | E2E | banco |

## 7. Segurança

Cobrir acesso negado e sessão expirada no E2E.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [x] Fluxo E2E completo verde.
- [x] Mensagens revisadas.
- [x] Permissões validadas.
- [x] Sem regressão nos testes anteriores.

## 10. Verificação e evidência

`mvn -q test`, `npm test -- --run`, `npm run build`, `git diff --check` + suíte E2E. Evidência: `/tmp/opencode/e2e-run2.log`, relatório HTML em `obras-frontend/playwright-report/` e capturas em `obras-frontend/test-results/` apenas nas falhas.

## 11. Estado verificado

- Suíte implementada em `obras-frontend/e2e/obra-fluxo.spec.js` com `playwright.config.js`, helpers e script `npm run test:e2e`. Dependências `@playwright/test`, Chromium, `jsdom`, `@testing-library/react` e `@testing-library/user-event` instaladas após aprovação.
- Os oito nomes RED estão presentes e verdes: `e2eCriaObraSemResponsavel`, `e2eIniciaSomenteComResponsavel`, `e2eAlocaEquipe`, `e2eRegistraDiarioEOcorrencia`, `e2eAnexaFoto`, `e2ePainelRefleteDados`, `e2eConcluiEEncerraEquipe`, `e2eUsuarioSemPermissaoNaoAcessa`.
- Correções reais: baseURL E2E passou a `http://localhost:5173` porque o backend aceita essa origem em `CORS_ORIGINS`; helpers passaram a calcular datas em `America/Maceio`; seletores ambíguos de `Alocar` e `Anexar foto` foram escopados; Vitest passou a ignorar `e2e/**`.
- Execução: `E2E_LOGIN=<user> E2E_SENHA=<senha> VITE_PROXY_TARGET=http://127.0.0.1:8080 npm run test:e2e` → `8 passed (23.1s)`. Log: `/tmp/opencode/e2e-run2.log`.
- Regressão: `mvn -q test` exit 0; `npm test -- --run` 34 arquivos/252 testes verdes; `npm run build` exit 0; `git diff --check` exit 0.
- Dados E2E usam sufixo único no banco de desenvolvimento; não houve limpeza destrutiva.
- Lote está `Em revisão`: falta auditoria do dono e registro manual de screenshots de sucesso/responsividade.
