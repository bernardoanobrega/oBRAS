# L20 — Serviço: alocar usuário com cargos

**Fase:** 2 · **Commit:** `feat(equipe): alocar usuario com cargos`  
**Status:** Em revisão  
**Depende de:** L19 · **RF/UC:** RF-16, RF-17; UC05 · **ADR:** ADR-09

## 1. Objetivo

Implementar a alocação de usuário ativo na obra com um ou mais cargos.

## 2. Escopo

**Inclui:** `alocar(obraId, NovaAlocacaoDto)`, validação de usuário ativo, cargos existentes, cargos não vazios, unicidade de alocação ativa, `dataInicio = hoje`.  
**Não inclui:** encerramento (L21), cascata (L23), endpoints (L22).

## 3. Contrato de serviço

- `alocar` → `AlocacaoObraDto`.
- Rejeitar usuário inexistente/inativo.
- Rejeitar `cargoIds` vazio ou cargo inexistente.
- Rejeitar usuário já alocado ativo na obra.
- Definir `dataInicio = LocalDate.now()`.
- Auditar criação se `AlocacaoObra` for auditável; caso contrário registrar pendência.

## 4. Testes primeiro (RED)

- `alocaUsuarioAtivoComCargos`.
- `rejeitaUsuarioInativo`.
- `rejeitaCargosVazios`.
- `rejeitaCargoInexistente`.
- `rejeitaAlocacaoAtivaDuplicada`.
- `dataInicioEhHoje`.
- `alocacaoNaoAceitaUsuarioDeOutraObra`.

## 5. Implementação

1. Criar `AlocacaoObraService.alocar`.
2. Usar `ObraService.buscar`/port para validar obra.
3. Usar port de usuário para situação ativa.
4. Usar port de cargo para validar IDs.
5. Traduzir constraint única para mensagem de negócio.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Cargos não vazios | serviço + banco | unitário/integração |
| Uma alocação ativa por usuário/obra | banco + serviço | integração |
| Usuário ativo | serviço | unitário |
| DataInicio = hoje | serviço | unitário |

## 7. Segurança

Permissão `DETALHES_OBRA:CRIAR` na API. Serviço recebe `obraId` validado.

## 8. Migração e rollback

Sem migration. Depende do índice único de V2.

## 9. Critérios de aceite

- [ ] Alocação funcional.
- [ ] Duplicidade bloqueada.
- [ ] Cargos validados.
- [ ] Usuário ativo exigido.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de serviço e constraint.
