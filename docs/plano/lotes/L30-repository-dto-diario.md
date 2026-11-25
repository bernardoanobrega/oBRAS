# L30 — Repository, DTO e mapper do diário

**Fase:** 3 · **Commit:** `feat(diario): persistir diario`  
**Status:** Concluído  
**Depende de:** L28, L29 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02

## 1. Objetivo

Persistir diário, ocorrências, paralisações e fotos e expor DTOs sem entidades.

## 2. Escopo

**Inclui:** repositories, DTOs de diário/ocorrência/paralisia/foto, mappers, consultas por obra/data.  
**Não inclui:** service/API (L31–L34), upload (L35).

## 3. Contrato

- `RegistroDiarioRepository`: buscar por obra/data ativo, listar por obra.
- `OcorrenciaRepository`: listar por diário, buscar por obra.
- `RegistroFotograficoRepository`: listar por ocorrência.
- DTOs de entrada com validação; saída com ids e nomes.
- Mapper não contém regra de negócio.

**Como ficou (auditoria de 25/11):**
- Repositories:
  - `RegistroDiarioRepository`: `buscarAtivoPorData` (antes `findAtivo`), `buscar`, `buscarComEquipe` e `listarPorObra`;
  - `OcorrenciaRepository`: `buscar`, `listarPorDiario` (com autor e paralisação via `join fetch`) e `contarPorDiarios` (agrupada);
  - `RegistroFotograficoRepository`: `buscar`, `listarPorOcorrencia` e `listarPorDiario`.
- Todas as consultas filtram os excluídos, sem N+1.
- Mapper: `diario.mapper.DiarioMapper`, só no sentido entidade → DTO ([ADR-16](../../adr/ADR-16-mapper-entidade-dto.md)). Os DTOs viraram records sem lógica.
- Saída com nomes: `equipePresente[]` e `autor` como `MembroDiarioDto(alocacaoId, usuarioId, nome)` ([ADR-17](../../adr/ADR-17-diario-expoe-nomes.md)). O frontend precisa ser ajustado.
- JSON ilegível (por exemplo, clima fora do enum) → 400 no `GlobalExceptionHandler`.

## 4. Testes primeiro (RED)

- `repositoryBuscaDiarioAtivoPorObraData`.
- `repositoryNaoRetornaDiarioExcluido`.
- `mapperDiarioIncluiClimaEEquipe`.
- `mapperOcorrenciaIncluiCategoriaEAutor`.
- `dtoDiarioRejeitaClimaInvalido`.
- ~~`dtoFotoRejeitaOcorrenciaNula`~~: substituído, porque não há DTO de entrada de foto (a ocorrência vem da URL). A regra fica coberta por `RegistroFotograficoTest.rejeitaFotoSemOcorrencia` (L29) e pelo `NOT NULL` da V4.

**Testes entregues (25):**
- `DiarioRepositoryTest` (11, Testcontainers): diário ativo por data, excluídos fora, isolamento entre obras, ordem, contagem agrupada, fotos por diário, equipe e autor com nome.
- `DiarioMapperTest` (5).
- `DiarioDtoValidacaoTest` (5): clima nulo, clima inválido e JSON quebrado dão 400.
- `DiarioServiceTest` (+1): `listarContaOcorrenciasNumaConsultaSo`.
- `AlocacaoObraRepositoryTest` (3, módulo `obra`): as alocações usadas pelo diário (equipe e autor) voltam com o usuário já carregado e continuam filtrando por obra e por alocação ativa.

Evidência: `mvn test` com **107 testes, 0 falhas** (25/11/2026).

## 5. Implementação

1. Criar repositories.
2. Criar DTOs record.
3. Criar mappers.
4. Definir queries com filtro de exclusão lógica.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Consulta ignora excluídos | repository | integração |
| DTO não expõe entidade | mapper | unitário |
| Clima validado no DTO | DTO | validação |
| Foto exige ocorrência | DTO | validação |

## 7. Segurança

DTOs não carregam permissão; controller filtra por obra.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [x] Repositories prontos.
- [x] DTOs validados.
- [x] Mappers sem regra.
- [x] Consultas filtram excluídos.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de repository/mapper.
