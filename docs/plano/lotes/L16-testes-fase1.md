# L16 — Testes de integração da Fase 1

**Fase:** 1 · **Commit:** `test(obra): cobrir fase 1`  
**Status:** Pendente  
**Depende de:** L06–L15 · **RF/UC:** RF-14 a RF-19, RF-70, RF-73; UC04, UC05 · **ADR:** ADR-04, ADR-05, ADR-09

## 1. Objetivo

Fechar a Fase 1 com testes de integração reais cobrindo migration, transições, auditoria, autorização e filtro.

## 2. Escopo

**Inclui:** base de teste com PostgreSQL/Testcontainers; testes de migration V3; testes de controller; testes de isolamento; testes de auditoria.  
**Não inclui:** frontend (L17), equipe (Fase 2), diário (Fase 3).

## 3. Contrato de teste

- `@SpringBootTest` com Testcontainers PostgreSQL.
- Flyway aplicado do zero em cada suíte.
- Usuários, cargos e alocações criados por SQL/fixtures.
- JWT real ou `@WithMockUser` + contexto de permissão.
- Testes de erro conferem `ApiError`.

## 4. Testes primeiro (RED)

- `migrationV3AplicaDoZero`.
- `obraCriadaComAuditoria`.
- `transicoesValidasEInvalidas`.
- `iniciarSemResponsavelFalha`.
- `usuarioSemPermissaoRecebe403`.
- `listaFiltraPorAlocacao`.
- `obraDeOutroUsuarioNaoAcessivel`.

## 5. Implementação

1. Criar `AbstractIntegrationTest`.
2. Criar fixtures de usuário/cargo/alocação.
3. Rodar suíte completa.
4. Corrigir falhas de isolamento entre testes.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Migration funciona em banco limpo | integração | Flyway |
| Permissão é aplicada no backend | integração | 401/403 |
| Auditoria persiste na mesma transação | integração | query |
| Lista não vaza obra | integração | fixtures |

## 7. Segurança

Cobrir token ausente, token inválido, usuário inativo, permissão global e por obra.

## 8. Migração e rollback

Sem migration nova. Testes usam V3.

## 9. Critérios de aceite

- [ ] Suíte de integração verde.
- [ ] Migration testada do zero.
- [ ] Transições e auditoria cobertas.
- [ ] Autorização coberta.

## 10. Verificação e evidência

`mvn test`. Evidência: relatório de testes e nomes dos casos.
