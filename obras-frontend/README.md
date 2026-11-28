# oBRAS — Frontend

React 18 + TailwindCSS + React Router + Axios (SPA, conforme a Arquitetura de Software). JavaScript.

## Rodar
```powershell
npm install
copy .env.example .env     # opcional
npm run dev                # http://localhost:5173  (proxy /api -> http://localhost:8080)
npm run build
npm test
npm run test:e2e
```
Suba o backend antes (`docker compose up -d db` + `mvn spring-boot:run` com perfil `dev`).
E2E requer `E2E_LOGIN`/`E2E_SENHA` no ambiente e origem `http://localhost:5173` permitida em `CORS_ORIGINS`; `E2E_BASE_URL` é opcional.
Login de desenvolvimento: `admin` / `Admin@12345`.

## Estrutura
- `src/app/` — `App`, `router.jsx` (guards), `nav.js` (menu e abas da obra), `providers/AuthProvider`.
- `src/shared/` — `api/http.js` (Axios + tratamento do ApiError), `auth/` (sessão, `usePermissao`, `<Can>`, `RotaProtegida`),
  `components/` (ui, layout, feedback), `utils/`.
- `src/features/<modulo>/` — espelha os módulos do backend (`pages/`, `components/`, `api/`).

## Como as permissões funcionam
- Menu e rotas globais: `GET /api/me/permissoes` (permissões individuais).
- Painel da obra: `GET /api/me/permissoes?obraId=` (cargos da alocação + exceções individuais).
- `<Can modulo="ESTOQUE" acao="CRIAR" obraId={id}>` esconde botões; a segurança real é do backend.

## Implementado
Login/sessão, layout responsivo (menu lateral no escritório, barra inferior no celular), banner offline,
guards de rota, **Usuários** (lista, cadastro, edição, desativar, permissões individuais),
**Orçamento** (menu lateral e aba da obra: indicadores, planilha orçado x realizado, lançamento de custo,
decisões financeiras e cancelamento).
**Contratos/documentos e prestadores** (upload, revisões, visualização, download e histórico),
**Painel de obra e diário**, **Compras** e **Estoque**.
Demais módulos: telas "em construção" com o escopo (UCs/RFs) da documentação.

## Detalhe de obra, diário e painel
- `src/features/obras/pages/DetalheObraPage.jsx`: painel real, ações de status, edição de parâmetros, KPIs nulos com origem `SIMULADO`, equipe, ocorrências recentes e mural.
- `src/features/obras/pages/DiarioPage.jsx`: diário do dia, ocorrências completas/rápidas, paralisação e upload de fotos JPG/PNG.
- `src/features/obras/api/`: `painelApi`, `diarioApi` e `obrasApi` desempacotam `AxiosResponse` e preservam `mensagemErro`/`errosPorCampo`.
- Modais compartilhados usam `useFocusTrap`: foco preso, Escape fecha e foco volta ao gatilho. Botões de ícone e campos sem texto visível têm rótulo acessível.
- Nenhum KPI nulo é convertido em zero; `fonte: SIMULADO` aparece na interface.
- Sem diário no dia, a página de diário mostra equipe alocada e estado vazio sem dados inventados.
- Testes: `npm test`; compilação: `npm run build`.

## Tema (paleta do Figma)
As cores ficam só em `tailwind.config.js`. Nas telas, use os nomes abaixo, nunca hexadecimal solto
nem as cores padrão do Tailwind (`slate`, `red`, `amber`...).

| Nome | Uso |
|---|---|
| `fundo` | fundo da página e dos campos |
| `superficie`, `superficie-alta` | cards, cabeçalho e modais; hover e cabeçalho de tabela |
| `borda`, `borda-forte` | bordas de cards e divisórias; borda de campos e botões secundários |
| `texto`, `texto-suave`, `texto-fraco` | texto principal, secundário e de apoio |
| `menu`, `menu-ativo`, `menu-texto`, `menu-destaque` | menu lateral e barra inferior |
| `brand`, `brand-claro`, `brand-fundo` | destaque âmbar (botão principal, alerta/atenção) |
| `sucesso`, `perigo`, `info` (+ `-fundo`) | situações ok, crítico/erro e informativo |
| `neutro` | marcadores e itens inativos |

## Regras
- Uma feature não importa de outra feature; o que for comum sobe para `shared/`.
- Toda chamada passa por `shared/api/http.js`. Erros: `mensagemErro(e)` e `errosPorCampo(e)`.
- Upload: validar com `validarArquivo` (PDF/PNG/JPG, 10 MB) antes de enviar; no mural de diário, validar somente JPG/PNG com o validador de fotos.

## Contratos e documentos

- `/documentos`: seletor de obras autorizadas, diretórios por categoria/disciplina, busca por código/título,
  upload, novas revisões, visualização de PDF/imagens e download autenticado.
- `/obras/:obraId/documentos`: o mesmo repositório limitado à obra da rota.
- `/prestadores`: cadastro, edição, desativação e consulta de contratos/histórico.
- Contratos podem receber um prestador no upload ou posteriormente pelo botão “Vincular prestador”.
- A aparência escura/amarela do design fica restrita às rotas deste módulo. O layout responde a telas móveis.
- “Atual” identifica a última revisão publicada. Não há aprovação técnica nem dados fictícios embutidos na interface.
- O módulo exige obras já cadastradas no banco pelo módulo de obras; quando não houver, apresenta estado vazio.
- Testes: `npm test`; compilação: `npm run build`.
