# L10 — Repository, DTO e mapper de Obra

**Fase:** 1 · **Commit:** `feat(obra): persistir e expor obra`  
**Status:** Em andamento  
**Depende de:** L08, L09 · **RF/UC:** RF-14, RF-15, RF-73; UC04 · **ADR:** ADR-04

## 1. Objetivo

Persistir `Obra` e expor DTOs sem vazar entidade JPA.

## 2. Escopo

**Inclui:** `ObraRepository`, `NovaObraDto`, `AtualizarObraDto`, `ObraDto`, `ObraMapper`, consulta de lista.  
**Não inclui:** service/controller (L11/L12), filtro de permissão (L14).

## 3. Contrato

- `ObraRepository extends JpaRepository<Obra, Long>`.
- `ObraDto` inclui id, nome, descricao, tipologia, endereco, datas, status, art, responsavelId, auditavelId.
- `NovaObraDto` com Bean Validation: nome, tipologia, endereco, datas; responsavelId opcional.
- `AtualizarObraDto` sem status; status só por transição.
- Mapper estático ou componente, sem regra de negócio.

## 4. Testes primeiro (RED)

- `mapeiaObraParaDtoSemExporEntidade`.
- `novaObraDtoRejeitaCamposObrigatorios`.
- `atualizarObraDtoNaoAceitaStatus`.
- `repositorySalvaEBuscaObraComAuditavel`.

## 5. Implementação

1. Criar repository.
2. Criar DTOs record.
3. Criar mapper.
4. Ajustar entidade para o mapeamento.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| API nunca recebe entidade | controller/DTO | revisão + teste |
| Status não é editável por PUT comum | DTO | validação |
| Datas mantêm tipo `LocalDate` | DTO/domínio | unitário |

## 7. Segurança

DTO não carrega permissões; controller decide.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Repository do módulo obra.
- [ ] DTOs validados.
- [ ] Mapper sem regra de negócio.
- [ ] Entidade não exposta.

## 10. Verificação e evidência

`mvn test`. Evidência: teste de serialização/mapeamento.
