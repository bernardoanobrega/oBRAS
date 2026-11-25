# L43 — Frontend: mover DetalheObra para a feature

**Fase:** 4 · **Commit:** `refactor(painel): mover detalhe de obra`  
**Status:** Em revisão  
**Depende de:** L42 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-10  
**Contexto completo da próxima sessão:** [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md)

## 1. Objetivo

Mover `DetalheObra.jsx` para dentro do app React e deixar a tela visível dentro do shell, sem trocar o mock por API.

**Decisão da fatia:** L43 e L44 serão executados juntos como uma fatia vertical visível. L43 prepara a página/componentes/mock; L44 integra rota, `AppShell`, `ObraLayout` e permissões. Os cortes de L46 são antecipados para esta fatia.

**Dependência L42:** é apenas de sequência. Como esta fatia usa mock, pode começar antes de L42. Não criar endpoint nem chamada de API.

## 2. Escopo

**Inclui:** mover para `src/features/obras/pages/DetalheObraPage.jsx`; separar componentes grandes; extrair mock para `features/obras/mocks/detalheObraMock.js`; remover `MENU`/`Sidebar`/`Topbar` do scaffold; aplicar os cortes visuais de L46; manter dados mockados; deixar a página pronta para L44 integrar.  
**Não inclui:** dados reais (L45), endpoint de painel, chamada `http`, ações persistidas, upload, exportação e RFs de outros módulos.

## 3. Contrato de arquivo

- Componente em `features/obras/pages/DetalheObraPage.jsx`.
- Subcomponentes em `features/obras/components/detalhe/`.
- Mock em `features/obras/mocks/detalheObraMock.js`.
- Nenhum import de caminho fora de `src`.
- Build deve passar.
- Sem duplicar `AppShell`, `Sidebar` ou `Topbar` do mock.
- Sem `http`, sem `usePermissao` obrigatório nesta etapa; permissões entram em L44.
- Sem imagens `/img/...` inexistentes; usar placeholder local.
- Sem links mortos (`href="#"`).

### Estado atual verificado

- `ResumoObraPage.jsx` é placeholder `EmConstrucao`; a rota `resumo` aponta para ele.
- `DetalheObra.jsx` está em `/home/costa/Estudos/ENG-SOFT/DetalheObra.jsx`, tem 450 linhas, navegação própria e mock local.
- `lucide-react` e Tailwind já estão instalados.
- `public/img` não existe; as imagens do scaffold quebram.
- Frontend tem Vitest em `environment: 'node'`, sem `jsdom`/Testing Library; ver plano de testes abaixo.

### Arquivos alvo

Criar:

- `src/features/obras/pages/DetalheObraPage.jsx`
- `src/features/obras/components/detalhe/ObraHeader.jsx`
- `src/features/obras/components/detalhe/ClimaDiaCard.jsx`
- `src/features/obras/components/detalhe/KpiCard.jsx` (ou `KpiGrid.jsx`)
- `src/features/obras/components/detalhe/EquipeResumo.jsx`
- `src/features/obras/components/detalhe/OcorrenciasRecentes.jsx`
- `src/features/obras/components/detalhe/MuralFotos.jsx`
- `src/features/obras/components/detalhe/primitivos.jsx` (`Badge`, `Avatar`, `ProgressBar`, `IconButton`) ou um arquivo por primitivo
- `src/features/obras/components/detalhe/tokens.js` (tokens de card/tom; facilita trocar tema depois)
- `src/features/obras/mocks/detalheObraMock.js`

Modificar:

- `src/features/obras/pages/ResumoObraPage.jsx`: remover ou reexportar `DetalheObraPage` temporariamente.
- `src/app/router.jsx` só em L44; nesta etapa não é obrigatório.

### Cortes antecipados de L46

- Remover `MENU`, `Sidebar` e `Topbar` do scaffold.
- Remover `Exportar Relatório Consolidado`, `Download`, `RefreshCw` e sincronização meteorológica.
- Remover clima por turno (`manha`/`tarde`), temperatura e alerta de turno; exibir um único clima do dia.
- Remover `Check-in` e divisão `Próprios`/`Terceirizados`; manter total presente e comparação com ontem.
- Remover `frente` da equipe e `sub`/`CREA` do responsável técnico.
- Remover `ImagePlus` e upload de foto avulsa; mural só com fotos de ocorrências (placeholders locais).
- KPIs de avanço/orçamento podem ficar como mock, mas com badge `SIMULADO`.
- Layout responsivo: header quebra no mobile; KPIs `grid-cols-1 md:grid-cols-3`; conteúdo `grid-cols-1 xl:grid-cols-[1fr_400px]`; tabela com `overflow-x-auto`.

## 4. Testes primeiro (RED)

- `detalheObraRenderizaSemErro`.
- `componentesExtraidosMantemProps`.
- `naoRenderizaSidebarPropria`.
- `naoRenderizaCortesDeEscopo`.
- `buildNaoQuebra`.
- `nenhumImportAbsolutoForaDoSrc`.

**Sem dependências novas inicialmente:** usar `react-dom/server` + `MemoryRouter`/`Routes` no ambiente `node` atual. Se precisar de clique/foco/`getByRole`, adicionar `jsdom` + Testing Library só depois.

## 5. Implementação

1. Copiar `DetalheObra.jsx` para `DetalheObraPage.jsx`.
2. Extrair `ObraHeader`, `ClimaDiaCard`, `KpiCard`, `EquipeResumo`, `OcorrenciasRecentes`, `MuralFotos`.
3. Extrair primitivos e tokens de estilo.
4. Extrair mock para `detalheObraMock.js` com shapes estáveis.
5. Remover `MENU`/`Sidebar`/`Topbar` e imports não usados.
6. Aplicar cortes de L46 listados na seção 3.
7. Substituir imagens `/img/...` por placeholders locais.
8. Ajustar imports e responsividade.
9. Rodar testes e build.

### Mock mínimo

- `OBRA`: nome, status, art, tipologia, localizacao, previsao.
- `CLIMA_DIA`: condicao, data, badge.
- `KPIS`: avancoRealizado, avancoPrevisto, spi, orcamentoTotal, executado, saldo, limite, maoDeObraTotal, vsOntem, fonte `SIMULADO`.
- `EQUIPE`: sigla, nome, cargo, tom (sem frente/CREA).
- `OCORRENCIAS`: data, tag, tom, texto.
- `FOTOS`: id, legenda, gradiente (sem `src` externo).

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Componente dentro de `src` | frontend | build |
| Sem layout duplicado | frontend | revisão |
| Props preservadas | frontend | teste |
| Build verde | frontend | build |

## 7. Segurança

Sem mudança de segurança; ainda mock.

## 8. Migração e rollback

Sem migration. Rollback: manter arquivo original em `Estudos/ENG-SOFT/DetalheObra.jsx`.

## 9. Critérios de aceite

- [ ] Arquivo movido para `features/obras/pages/DetalheObraPage.jsx`.
- [ ] Componentes separados em `features/obras/components/detalhe/`.
- [ ] Mock extraído para `features/obras/mocks/detalheObraMock.js`.
- [ ] Cortes de L46 aplicados.
- [ ] Imagens sem caminho quebrado.
- [ ] Layout minimamente responsivo.
- [ ] Build verde.
- [ ] Sem layout duplicado.
- [ ] Sem chamada de API real.

## 10. Verificação e evidência

```bash
cd /home/costa/Estudos/ENG-SOFT/oBRAS/obras-frontend
npm test
npm run build
```

Evidência: diff de arquivos, saída de testes/build e rota aberta em dev sem erro no console.  
Contexto completo e decisões abertas: [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md).
