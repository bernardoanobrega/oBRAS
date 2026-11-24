# L30 — Repository, DTO e mapper do diário

**Fase:** 3 · **Commit:** `feat(diario): persistir diario`  
**Status:** Em revisão  
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

## 4. Testes primeiro (RED)

- `repositoryBuscaDiarioAtivoPorObraData`.
- `repositoryNaoRetornaDiarioExcluido`.
- `mapperDiarioIncluiClimaEEquipe`.
- `mapperOcorrenciaIncluiCategoriaEAutor`.
- `dtoDiarioRejeitaClimaInvalido`.
- `dtoFotoRejeitaOcorrenciaNula`.

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

- [ ] Repositories prontos.
- [ ] DTOs validados.
- [ ] Mappers sem regra.
- [ ] Consultas filtram excluídos.

## 10. Verificação e evidência

`mvn test`. Evidência: testes de repository/mapper.
