# L44 — Frontend: integrar shell, router, http e permissão

**Fase:** 4 · **Commit:** `feat(painel): integrar detalhe ao app`  
**Status:** Em revisão  
**Depende de:** L43 · **RF/UC:** RF-70, RF-73; UC04, UC05, UC21 · **ADR:** ADR-09  
**Contexto completo da próxima sessão:** [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md)

## 1. Objetivo

Integrar `DetalheObraPage` ao `AppShell`, router e `usePermissao`, deixando a tela visível em `/obras/:obraId/resumo`.

**Decisão da fatia:** L44 será executado junto com L43 como fatia visual. Nesta fatia, L44 cobre **shell/router/permissão**; `http.js` e 401 ficam para L45, quando existir endpoint de painel. Não criar chamada fake.

## 2. Escopo

**Inclui:** rota de detalhe existente, uso de `AppShell`/`ObraLayout`, `usePermissao(obraId)`, remoção de navegação própria do mock, ações condicionadas por permissão, cortes visuais de L46 antecipados.  
**Não inclui:** dados reais (L45), endpoint de painel, `http` real, 401/403 de API, ações persistidas (L47).

## 3. Contrato UI

- Rota: `/obras/:obraId/resumo` (path existente).
- `router.jsx` importa `DetalheObraPage` e renderiza em `resumo`.
- `IndexObra` continua redirecionando para `resumo`.
- Nenhum sidebar/topbar próprio.
- Página dentro de `AppShell` + `ObraLayout`.
- Ações condicionadas por `usePermissao(obraId)`:
  - `Editar Parâmetros` exige `DETALHES_OBRA:EDITAR`.
  - `Nova Ocorrência` exige `DETALHES_OBRA:CRIAR`.
  - Sem permissão: ocultar, não apenas desabilitar.
- 401/403 de API ficam para L45; a rota já tem guard `DETALHES_OBRA:VISUALIZAR`.
- Tabs do `ObraLayout` continuam funcionando.
- Manter rótulo `Resumo` nesta fatia (menor mudança); título interno `Detalhe de Obra`.

## 4. Testes primeiro (RED)

- `rotaDetalheRenderizaDentroDoAppShell`.
- `naoRenderizaSidebarPropria`.
- `usaUsePermissaoComObraId`.
- `acaoSemPermissaoNaoAparece`.
- `acaoComPermissaoAparece`.
- `tabsDoObraLayoutFuncionam`.

**Sem dependências novas inicialmente:** usar `react-dom/server` + `MemoryRouter`/`Routes`; mockar `usePermissao`. `401DisparaEventoDeSessao` fica para L45, quando houver chamada real.

## 5. Implementação

1. Importar `DetalheObraPage` no `router.jsx` e trocar `ResumoObraPage` na rota `resumo`.
2. Garantir que a página não renderiza `Sidebar`/`Topbar`/`MENU`.
3. Usar `usePermissao(obraId)` na página e passar `pode` aos componentes.
4. Ocultar ações sem permissão.
5. Manter mock; não chamar `http`.
6. Rodar testes e build.

### Arquivos alvo

- `src/app/router.jsx`: trocar import/render da rota `resumo`.
- `src/features/obras/pages/DetalheObraPage.jsx`: usar `useParams` + `usePermissao`.
- `src/features/obras/pages/ResumoObraPage.jsx`: remover ou reexportar temporariamente.
- `src/app/nav.js`: não obrigatório; só alterar rótulo se o time decidir.
- `src/features/obras/pages/ObraLayout.jsx`: não alterar, salvo duplicação visual real.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Segurança no backend | UI | 403 quando houver API (L45) |
| Sem layout duplicado | UI | revisão/teste |
| Rota protegida | router | teste |
| Permissão por obra | UI | teste com `usePermissao` mockado |
| 401 trata sessão | http | adiado para L45 |

## 7. Segurança

Não confiar em permissão do frontend; ações continuam protegidas no backend.

## 8. Migração e rollback

Sem migration. Rollback: remover rota e restaurar navegação mock.

## 9. Critérios de aceite

- [ ] `/obras/:obraId/resumo` renderiza `DetalheObraPage` dentro do `AppShell`.
- [ ] Sem layout duplicado (`Sidebar`/`Topbar`/`MENU` próprios removidos).
- [ ] Permissões por obra aplicadas; ações sem permissão ocultas.
- [ ] `IndexObra` continua redirecionando para `resumo`.
- [ ] Tabs do `ObraLayout` continuam funcionando.
- [ ] Nenhuma chamada de API real nesta fatia.
- [ ] Build/test verdes.

## 10. Verificação e evidência

```bash
cd /home/costa/Estudos/ENG-SOFT/oBRAS/obras-frontend
npm test
npm run build
```

Evidência: testes de rota/permissão, build e rota aberta em dev.  
Contexto completo e decisões abertas: [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md).
