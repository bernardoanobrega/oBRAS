# L17 — Frontend: lista e cadastro de obras

**Fase:** 1 · **Commit:** `feat(obras): integrar lista e cadastro`  
**Status:** Em andamento  
**Depende de:** L12–L14, L16 · **RF/UC:** RF-14, RF-15, RF-18; UC04, UC05 · **ADR:** ADR-05, ADR-09

## 1. Objetivo

Substituir `ObrasPage` provisória por lista real e formulário de cadastro/edição, usando `http.js` e permissões.

## 2. Escopo

**Inclui:** `features/obras/api/obrasApi.js`; lista com filtro por status; modal de cadastro/edição; responsável opcional; estados loading/vazio/erro/sem permissão.  
**Não inclui:** painel de detalhe (Fase 4), equipe (Fase 2), diário (Fase 3).

## 3. Contrato UI

- Rota `/obras` renderiza lista.
- Botão “Nova obra” só com `DETALHES_OBRA:CRIAR`.
- Ações editar só com `DETALHES_OBRA:EDITAR`.
- Formulário valida nome, tipologia, endereço, datas; responsável opcional.
- Erro de backend exibido via `mensagemErro`/`errosPorCampo`.
- Layout responsivo com componentes existentes.

## 4. Testes primeiro (RED)

- `listaMostraLoadingDepoisDados`.
- `listaVaziaMostraEmptyState`.
- `erroDeApiMostraErrorState`.
- `botaoNovaObraOcultoSemPermissao`.
- `formularioRejeitaPeriodoInvertido`.
- `formularioPermiteResponsavelVazio`.
- `mensagemDeErroDoBackendEhExibida`.

## 5. Implementação

1. Criar `obrasApi.js` com `http`.
2. Implementar `ObrasPage` e `ObraFormModal`.
3. Usar `usePermissao(null)` para ações globais.
4. Reutilizar `Button`, `Input`, `Modal`, `EmptyState`, `ErrorState`, `Spinner`.
5. Não duplicar regra de transição de status.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Frontend não é fonte de segurança | UI | botão oculto + 403 |
| Responsável opcional no cadastro | UI | validação |
| Erros do backend são exibidos | UI | teste |
| Sem chamada direta a axios | API | revisão |

## 7. Segurança

Esconder ações sem permissão; backend continua validando. Não armazenar token fora de `sessao`.

## 8. Migração e rollback

Sem migration. Rollback: restaurar `ObrasPage` anterior.

## 9. Critérios de aceite

- [ ] Lista real integrada.
- [ ] Cadastro/edição funcionando.
- [ ] Responsável opcional.
- [ ] Estados de UI completos.
- [ ] `npm test` e `npm run build` verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke manual.

## 11. Andamento (2026-11-25, Cauã)

- **Feito no front:** `criarObra`/`editarObra` em `obrasApi.js`; hook `useObras`; `ObraFormModal` (com `validarObra`, `paraCorpoObra`, `estadoDoErro` e `ObraFormulario` exportados para teste); `ObrasPage` com "Nova obra" (`<Can>` sem `obraId`, ADR-11) e "Editar" por linha (`<Can obraId>`, permissão da obra).
- **Testes:** os 7 da seção 4 + complementares em `src/test/obrasPage.test.jsx` e `src/test/obraForm.test.jsx`. `npm test` 91/91 e `npm run build` verdes.
- **Decisões:** responsável técnico fora do formulário (ADR-05; definido pelo `PUT /responsavel` do L13); ART fora do formulário (não está no contrato do lote); testes sem `jsdom`, com `renderToStaticMarkup` + mocks.
- **Design:** o Figma (`Telas Eng.Software`) não tem tela de lista/cadastro de obras; segue o padrão da tela "9. Usuários" (tema escuro, destaque laranja, busca com ícone, badges com ponto, ação por ícone de lápis), reaproveitando `detalhe/tokens.js` e `primitivos.jsx`. `shared/ui/Modal` ganhou `tema="escuro"` opcional (padrão continua claro; Usuários não muda).
- **Paleta provisória (pedido do Cauã, 2026-11-25):** verde `#008542` / amarelo `#FDB913` até a padronização oficial. Definida só em `tailwind.config.js` (`petro.*` e escala `brand`); verde = botão principal e foco, amarelo = ícones, links e seleção. Âmbar continua só como cor de **significado** (Paralisada, Atenção).
- **Falta para `Concluído`:** smoke manual contra o backend real. **Depende do L11/L12 (Thierry).** Combinar com o Thierry: (1) o corpo do `PUT` é igual ao do `POST`, sem `status` e `responsavelId`; (2) o `PUT` não apaga a `art` quando ela não é enviada.
