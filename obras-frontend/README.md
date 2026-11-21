# oBRAS — Frontend

React 18 + TailwindCSS + React Router + Axios (SPA, conforme a Arquitetura de Software). JavaScript.

## Rodar
```powershell
npm install
copy .env.example .env     # opcional
npm run dev                # http://localhost:5173  (proxy /api -> http://localhost:8080)
npm run build
npm test
```
Suba o backend antes (`docker compose up -d db` + `mvn spring-boot:run` com perfil `dev`).
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
guards de rota, **Usuários** (lista, cadastro, edição, desativar, permissões individuais).
Contratos/documentos e prestadores: interface integrada à API, com upload, revisões, visualização, download e histórico. Demais módulos: telas "em construção" com o escopo (UCs/RFs) da documentação.

## Regras
- Uma feature não importa de outra feature; o que for comum sobe para `shared/`.
- Toda chamada passa por `shared/api/http.js`. Erros: `mensagemErro(e)` e `errosPorCampo(e)`.
- Upload: validar com `validarArquivo` (PDF/PNG/JPG, 10 MB) antes de enviar.

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
