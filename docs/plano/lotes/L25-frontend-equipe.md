# L25 — Frontend: tela de equipe da obra

**Fase:** 2 · **Commit:** `feat(equipe): integrar tela de equipe`  
**Status:** Em andamento  
**Depende de:** L22–L24 · **RF/UC:** RF-16 a RF-19; UC05 · **ADR:** ADR-09

## 1. Objetivo

Substituir `EquipeObraPage` provisória por gestão real de alocações, cargos e responsável técnico.

## 2. Escopo

**Inclui:** lista de alocações, modal de alocar, seleção múltipla de cargos, encerrar alocação, exibir responsável técnico; estados de UI; permissões por obra.  
**Não inclui:** edição de cargos da obra além da alocação; painel (Fase 4).

## 3. Contrato UI

- Rota `/obras/:obraId/equipe`.
- `usePermissao(obraId)` para ações.
- Alocar só com `DETALHES_OBRA:CRIAR`.
- Encerrar/alterar cargos só com `DETALHES_OBRA:EDITAR`.
- Responsável exibido no cabeçalho da equipe.
- Estados loading/vazio/erro/sem permissão.

## 4. Testes primeiro (RED)

- `listaEquipeMostraAlocacoes`.
- `listaVaziaMostraEmptyState`.
- `botaoAlocarOcultoSemPermissao`.
- `formularioExigeAoMenosUmCargo`.
- `encerrarPedeConfirmacao`.
- `erroDeDuplicidadeExibeMensagem`.
- `responsavelTecnicoEhExibido`.

## 5. Implementação

1. Criar `features/obras/api/equipeApi.js`.
2. Implementar `EquipeObraPage`, `AlocarUsuarioModal`, `CargosSelect`.
3. Reutilizar `Modal`, `Button`, `Input`, `Badge`, `EmptyState`, `ErrorState`.
4. Não duplicar regra de unicidade; exibir erro do backend.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Ações refletem permissão | UI | teste |
| Cargos obrigatórios no form | UI | validação |
| Erro do backend exibido | UI | teste |
| Responsável visível | UI | teste |

## 7. Segurança

Frontend apenas oculta ações; backend valida. Não enviar `obraId` divergente.

## 8. Migração e rollback

Sem migration. Rollback: restaurar `EmConstrucao`.

## 9. Critérios de aceite

- [ ] Lista de equipe real.
- [ ] Alocar/encerrar funcionando.
- [ ] Cargos múltiplos.
- [ ] Responsável exibido.
- [ ] `npm test`/`npm run build` verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke manual.

## 11. Andamento (2026-11-26, Cauã)

- **Feito no front:** `equipeApi.js` (rotas do L22 + cargos + candidatos), `adaptadoresEquipe.js` (`paraAlocacoes`, `responsavelTecnico`), hook `useEquipe`, `components/equipe/CargosSelect.jsx` e `AlocarUsuarioModal.jsx` (alocar e alterar cargos, com `validarAlocacao` pura), `EquipeObraPage` com lista real, "Alocar profissional" (`CRIAR` da obra), alterar cargos e encerrar (`EDITAR` da obra) com confirmação, e cabeçalho do responsável técnico com nome e ART (ADR-20).
- **Testes:** os 7 da seção 4 + complementares em `src/test/equipeObraPage.test.jsx` e `src/test/equipeForm.test.jsx`. `npm test` 137/137 e `npm run build` verdes.
- **Falta para `Concluído`:** smoke manual contra o backend real. **Depende do L20–L22 (Thierry).** Até lá a aba Equipe mostra erro, porque `GET /alocacoes` não existe: não levar esta tela para a `develop` antes do L22.

### Pendências para o backend (o que o front já chama)
Todas marcadas com `PENDENTE BACKEND` em `obras-frontend/src/features/obras/api/equipeApi.js`. Se o backend fizer diferente, ajustar o front lá.

| Rota / campo | O front espera | Origem |
|---|---|---|
| `GET /api/obras/{id}/alocacoes` | `[{ id, usuarioId, nome, cargos: [{ id, nome }], dataInicio }]`, só ativas | L22 (formato não definido no lote) |
| `POST /api/obras/{id}/alocacoes` | body `{ usuarioId, cargoIds }`; duplicado → 409/422 com `ApiError.mensagem` | L22 |
| `PUT /api/obras/{id}/alocacoes/{alocacaoId}/cargos` | body `{ cargoIds }` | L22 |
| `DELETE /api/obras/{id}/alocacoes/{alocacaoId}` | 204, encerra (não apaga) | L22 / L21 |
| `GET /api/cargos` | `[{ id, nome }]` | **não está em nenhum lote** |
| `GET /api/obras/{id}/alocacoes/candidatos` | `[{ id, nome }]`: usuários ativos não alocados; exigir só `DETALHES_OBRA:CRIAR` da obra (não `USUARIOS:VISUALIZAR` global) | **não está em nenhum lote** |
| `ObraDto.responsavelNome` | nome do responsável, mesmo fora da equipe | ADR-20 / L12–L13 |
