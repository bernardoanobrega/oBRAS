# L19 — Repository, DTO e mapper de alocação

**Fase:** 2 · **Commit:** `feat(equipe): persistir alocacao`  
**Status:** Em revisão  
**Depende de:** L18 · **RF/UC:** RF-16 a RF-19; UC05 · **ADR:** ADR-09

## 1. Objetivo

Persistir `AlocacaoObra` e expor DTOs de equipe sem vazar entidade.

## 2. Escopo

**Inclui:** `AlocacaoObraRepository`, consultas por obra e ativas, DTOs de criação/atualização/resposta, mapper.  
**Não inclui:** regra de alocação (L20), endpoints (L22), frontend (L25).

## 3. Contrato

- `AlocacaoObraRepository extends JpaRepository<AlocacaoObra, Long>`.
- `listarPorObra(obraId)`, `buscarAtiva(obraId, usuarioId)`.
- `NovaAlocacaoDto { usuarioId, cargoIds[] }`.
- `AtualizarCargosDto { cargoIds[] }`.
- `AlocacaoObraDto { id, usuarioId, nomeUsuario, dataInicio, dataFim, cargos[] }`.
- Cargo é resolvido por port/service do módulo usuário; não usar `CargoRepository` diretamente.

## 4. Testes primeiro (RED)

- `repositoryListaAlocacoesDaObra`.
- `repositoryBuscaAlocacaoAtiva`.
- `mapperIncluiCargosENomeDoUsuario`.
- `dtoRejeitaCargoIdsVazio`.
- `dtoNaoAceitaDataFimNoCadastro`.

## 5. Implementação

1. Criar repository.
2. Criar DTOs record.
3. Criar mapper.
4. Definir `CargoConsultaPort` em `shared` se necessário.
5. Ajustar mapeamento JPA.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| DTO não expõe entidade | mapper | unitário |
| CargoIds obrigatório no DTO | DTO | validação |
| Consulta ativa filtra `data_fim IS NULL` | repository | integração |

## 7. Segurança

DTO não carrega permissões.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Repository e DTOs prontos.
- [ ] Consulta de ativa correta.
- [ ] Cargo via port/service.
- [ ] Entidade não exposta.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de repository/mapper.
