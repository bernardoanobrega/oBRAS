# L52 — E2E e usabilidade do fluxo de obra

**Fase:** 6 · **Commit:** `test(e2e): cobrir fluxo do detalhe de obra`  
**Status:** Pendente  
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

1. Criar suíte E2E com banco limpo.
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

- [ ] Fluxo E2E completo verde.
- [ ] Mensagens revisadas.
- [ ] Permissões validadas.
- [ ] Sem regressão nos testes anteriores.

## 10. Verificação e evidência

`mvn test`, `npm test`, `npm run build` + suíte E2E. Evidência: relatório e gravação/trace.
