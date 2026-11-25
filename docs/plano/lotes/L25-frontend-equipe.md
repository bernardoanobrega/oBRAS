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
