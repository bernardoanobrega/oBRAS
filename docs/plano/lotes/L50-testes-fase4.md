# L50 — Testes de contrato e responsividade da Fase 4

**Fase:** 4 · **Commit:** `test(painel): cobrir contrato e responsividade`  
**Status:** Pendente  
**Depende de:** L41–L49 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-09, ADR-10

## 1. Objetivo

Fechar a Fase 4 com testes de contrato entre backend e frontend, permissões e responsividade.

## 2. Escopo

**Inclui:** contrato do `/painel`; testes de UI para dados reais, nulos, permissões, mobile/tablet; smoke de rotas.  
**Não inclui:** E2E completo (L52), módulos externos.

## 3. Contrato de teste

- Fixture de `/painel` com dados reais e nulos.
- Testes de componente com loading/erro/vazio.
- Testes de viewport mobile/tablet.
- Testes de rota protegida.
- Conferência de campos do DTO.

## 4. Testes primeiro (RED)

- `contratoPainelBateComFrontend`.
- `painelRenderizaDadosReais`.
- `painelRenderizaNulos`.
- `painelRespeitaPermissao`.
- `layoutMobileSemOverflow`.
- `layoutTabletSemSidebarFixa`.
- `rotasProtegidasRedirecionam`.

## 5. Implementação

1. Criar fixtures de contrato.
2. Rodar testes de componente.
3. Ajustar CSS responsivo.
4. Corrigir divergências DTO↔UI.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Contrato único | backend/frontend | contrato |
| Permissão refletida | UI | teste |
| Sem overflow mobile | UI | teste |
| Rotas protegidas | router | teste |

## 7. Segurança

Cobrir 401/403 e redirecionamento de login.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Contrato coberto.
- [ ] Responsividade coberta.
- [ ] Permissões cobertas.
- [ ] `mvn test`, `npm test`, `npm run build` verdes.

## 10. Verificação e evidência

Comandos dos três gates. Evidência: relatórios e screenshots mobile/tablet.
