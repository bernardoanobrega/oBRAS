# L47 — Frontend: ações de status e edição de parâmetros

**Fase:** 4 · **Commit:** `feat(painel): implementar acoes da obra`  
**Status:** Pendente  
**Depende de:** L46 · **RF/UC:** RF-14, RF-15, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-05, ADR-08

## 1. Objetivo

Implementar ações de ciclo de vida e edição de parâmetros no painel, com confirmação e permissão.

## 2. Escopo

**Inclui:** editar parâmetros; iniciar, paralisar, retomar, concluir, cancelar; confirmação para ações irreversíveis; mensagens de erro; permissões por obra.  
**Não inclui:** paralisação automática por ocorrência; ações de outros módulos.

## 3. Contrato UI

- “Editar Parâmetros” abre formulário com campos editáveis.
- Ações de status só aparecem com `DETALHES_OBRA:EDITAR`.
- `iniciar` desabilitado/explicado sem responsável.
- `concluir`/`cancelar` pedem confirmação e avisam encerramento de equipe.
- Erros 422 exibidos sem perder estado.

## 4. Testes primeiro (RED)

- `editarParametrosAtualizaObra`.
- `iniciarSemResponsavelExplicaBloqueio`.
- `paralisarNaoEncerraEquipeNaUI`.
- `concluirPedeConfirmacao`.
- `cancelarPedeConfirmacao`.
- `acaoSemPermissaoNaoAparece`.
- `erro422EhExibido`.

## 5. Implementação

1. Criar `ObraAcoesMenu` e `EditarParametrosModal`.
2. Usar APIs de L13.
3. Usar `usePermissao(obraId)`.
4. Atualizar painel após ação.
5. Não chamar transição de paralisação ao salvar ocorrência.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Estado final exige confirmação | UI | teste |
| Permissão por ação | UI | teste |
| Início exige responsável | UI + API | teste |
| Sem transição automática | UI | teste |

## 7. Segurança

Backend valida permissão; frontend apenas oculta ações.

## 8. Migração e rollback

Sem migration. Rollback: remover menu de ações.

## 9. Critérios de aceite

- [ ] Ações de status funcionando.
- [ ] Edição de parâmetros.
- [ ] Confirmações.
- [ ] Permissões e erros.
- [ ] Testes/build verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke.
