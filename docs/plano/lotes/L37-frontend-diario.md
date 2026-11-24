# L37 — Frontend: DiarioPage mobile-first

**Fase:** 3 · **Commit:** `feat(diario): integrar tela de diario`  
**Status:** Em andamento  
**Depende de:** L34, L36 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-09

## 1. Objetivo

Substituir `DiarioPage` provisória por lista e formulário de diário, mobile-first.

## 2. Escopo

**Inclui:** lista de diários, formulário de criação/edição, clima único, descrição, equipe presente, estados de UI e permissões por obra.  
**Não inclui:** ocorrências (L38), fotos (L39).

## 3. Contrato UI

- Rota `/obras/:obraId/diario`.
- Lista por data desc; vazio e erro.
- Formulário com data, clima, descrição e equipe presente.
- Clima apenas quatro valores do enum.
- Sem turno, temperatura ou sync.
- Mobile-first: cards/lista em vez de tabela larga.

## 4. Testes primeiro (RED)

- `listaDiariosMostraDataClima`.
- `listaVaziaMostraEmptyState`.
- `formularioRejeitaClimaVazio`.
- `formularioRejeitaDataVazia`.
- `edicaoCarregaValores`.
- `semPermissaoOcultaCriarEditar`.
- `layoutNaoUsaTabelaFixaEmMobile`.

## 5. Implementação

1. Criar `features/obras/api/diarioApi.js`.
2. Implementar `DiarioPage` e `DiarioForm`.
3. Usar `usePermissao(obraId)`.
4. Reutilizar `Card`, `Button`, `Input`, `Modal`, `EmptyState`, `ErrorState`.
5. Remover campos de turno/temperatura do mock.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Clima único | UI + API | validação |
| Ações por permissão | UI | teste |
| Mobile-first | UI | revisão/teste |
| Erro do backend exibido | UI | teste |

## 7. Segurança

Frontend apenas oculta ações; backend valida obra e permissão.

## 8. Migração e rollback

Sem migration. Rollback: restaurar `EmConstrucao`.

## 9. Critérios de aceite

- [ ] Lista/form real.
- [ ] Clima único.
- [ ] Mobile-first.
- [ ] Permissões.
- [ ] `npm test`/`npm run build` verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke em viewport mobile.
