# L10 — Repository, DTO e mapper de Obra

**Fase:** 1 · **Commit:** `feat(obra): persistir e expor obra`  
**Status:** Concluído  
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

- [x] Repository do módulo obra.
- [x] DTOs validados.
- [x] Mapper sem regra de negócio.
- [x] Entidade não exposta.

## 10. Verificação e evidência

Comando (executado em `obras-backend`):

    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 && export PATH="$JAVA_HOME/bin:$PATH" && mvn test -q

Saída: `exit 0`; resumo Surefire: `Tests run: 303, Failures: 0, Errors: 0, Skipped: 10`.

Testes L10:
- mapeiaObraParaDtoSemExporEntidade
- novaObraDtoRejeitaCamposObrigatorios
- atualizarObraDtoNaoAceitaStatus
- repositorySalvaEBuscaObraComAuditavel

Ajuste collateral: `OrcamentoRepositoryTest` passou a inserir `auditavel_id`, exigido por V3_1.
