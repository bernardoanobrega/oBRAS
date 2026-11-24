# L53 — Acessibilidade e responsividade

**Fase:** 6 · **Commit:** `feat(ui): revisar acessibilidade e responsividade`  
**Status:** Pendente  
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

- [ ] Teclado e foco.
- [ ] Labels/aria.
- [ ] Responsividade.
- [ ] Sem overflow.
- [ ] Testes/build verdes.

## 10. Verificação e evidência

`npm test` e `npm run build` + inspeção manual. Evidência: checklist e screenshots.
