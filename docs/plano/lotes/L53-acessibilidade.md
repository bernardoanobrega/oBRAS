# L53 — Acessibilidade e responsividade

**Fase:** 6 · **Commit:** `feat(ui): revisar acessibilidade e responsividade`  
**Status:** Em revisão
**Depende de:** L52 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-09, ADR-10

## 1. Objetivo

Garantir que o módulo Detalhe de Obra seja utilizável em celular/tablet e acessível por teclado e leitor de tela.

## 2. Escopo

**Inclui:** navegação por teclado, foco visível, labels, contraste, aria em modais, responsividade de lista/formulário/mural, ausência de overflow.  
**Não inclui:** auditoria WCAG completa do sistema inteiro.

## 3. Contrato de UI

- Modais com foco preso e `Esc` para fechar.
- Botões com `aria-label` quando só ícone.
- Formulários com label associado.
- Tabelas/listas adaptadas para mobile.
- Contraste mínimo legível.
- Nenhum scroll horizontal involuntário.

## 4. Testes primeiro (RED)

- `modalFechaComEsc`.
- `focoVoltaAoBotaoAposFechar`.
- `camposTemLabelAssociado`.
- `botoesDeIconeTemAriaLabel`.
- `layoutMobileSemOverflow`.
- `layoutTabletSemSidebarFixa`.
- `navegacaoPorTabFunciona`.

## 5. Implementação

1. Revisar componentes compartilhados usados pelo módulo.
2. Ajustar `Modal`, `Button`, `Input` quando necessário.
3. Ajustar CSS responsivo.
4. Rodar testes e inspeção manual.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Teclado funciona | UI | teste |
| Labels associados | UI | teste |
| Sem overflow mobile | UI | teste |
| Foco visível | UI | revisão |

## 7. Segurança

Sem impacto direto; não remover permissões ao ajustar UI.

## 8. Migração e rollback

Sem migration. Rollback: reverter CSS/componentes.

## 9. Critérios de aceite

- [x] Teclado e foco.
- [x] Labels/aria.
- [x] Responsividade.
- [x] Sem overflow.
- [x] Testes/build verdes.

## 10. Verificação e evidência

`npm test -- --run`, `npm run build` e suíte E2E + inspeção manual. Evidência: testes DOM/SSR, checklist e relatório E2E; screenshots manuais ainda não registrados.

## 11. Estado verificado

- Implementado `src/shared/hooks/useFocusTrap.js`: foco inicial, `Escape`, ciclo `Tab`/`Shift+Tab` e retorno de foco ao elemento anterior.
- `src/shared/components/ui/Modal.jsx` usa o hook e mantém `role="dialog"`, `aria-modal="true"`, `aria-label={titulo}` e botão de fechar com `aria-label`.
- `src/features/obras/components/diario/DiarioFormModal.jsx` tem label associado ao seletor de equipe.
- Testes de layout presentes: `layoutMobileSemOverflow`, `layoutTabletSemSidebarFixa`, `kpisNaoQuebramLayoutMobile`.
- Testes SSR verdes: `camposTemLabelAssociado` e `botoesDeIconeTemAriaLabel` em `src/test/l53Acessibilidade.test.jsx`.
- Dependências `jsdom`, `@testing-library/react` e `@testing-library/user-event` instaladas após aprovação; `vite.config.js` usa `environmentMatchGlobs` para executar somente o teste DOM em `jsdom`, preservando `environment: 'node'` no restante.
- Testes DOM verdes em `src/test/l53AcessibilidadeDom.test.jsx`: `modalFechaComEsc`, `focoVoltaAoBotaoAposFechar` e `navegacaoPorTabFunciona`.
- `npm test -- --run`: 34 arquivos/252 testes verdes; `npm run build` exit 0; suíte E2E 8/8.
- Lote está `Em revisão`: falta auditoria do dono e screenshots manuais de responsividade.
