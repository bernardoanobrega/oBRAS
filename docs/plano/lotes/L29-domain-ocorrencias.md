# L29 — Domínio Ocorrencia, Paralisacao e RegistroFotografico

**Fase:** 3 · **Commit:** `feat(diario): modelar ocorrencias e fotos`  
**Status:** Em revisão  
**Depende de:** L28 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-02, ADR-08

## 1. Objetivo

Modelar ocorrência, paralisação e foto com vínculos obrigatórios e categoria.

## 2. Escopo

**Inclui:** entidades, enums `CategoriaOcorrencia`, regras de descrição/autor/data, paralisação com início/fim e categoria, foto vinculada a ocorrência.  
**Não inclui:** upload/storage (L35), serviço/API (L32+).

## 3. Contrato de domínio

- `Ocorrencia`: diário obrigatório, categoria obrigatória, descrição obrigatória, dataHora, autor.
- `Paralisacao`: ocorrência com início e fim opcional; `fim >= inicio`.
- `RegistroFotografico`: ocorrência obrigatória, arquivo, legenda, dataEnvio.
- Categoria `OUTRO` usada na ocorrência rápida.
- Nenhuma foto avulsa.

## 4. Testes primeiro (RED)

- `criaOcorrenciaComDiarioECategoria`.
- `rejeitaOcorrenciaSemDiario`.
- `rejeitaOcorrenciaSemDescricao`.
- `criaParalisacaoComCategoria`.
- `rejeitaParalisacaoComFimMenorQueInicio`.
- `criaFotoVinculadaAOcorrencia`.
- `rejeitaFotoSemOcorrencia`.

## 5. Implementação

1. Criar entidades e enums.
2. Criar factories.
3. Encapsular período da paralisação.
4. Não permitir foto sem ocorrência.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Ocorrência sempre em diário | domínio + banco | unitário |
| Foto sempre em ocorrência | domínio + banco | unitário |
| Categoria obrigatória | domínio + banco | unitário |
| Período da paralisação válido | domínio + banco | unitário |

## 7. Segurança

Autor da ocorrência vem do contexto autenticado, não do payload.

## 8. Migração e rollback

Sem migration nova. Depende de L27.

## 9. Critérios de aceite

- [x] Entidades e enums.
- [x] Vínculos obrigatórios.
- [x] Período validado.
- [x] Nenhuma foto avulsa.

## 10. Verificação e evidência

`mvn test`. Evidência: testes unitários.

**Evidência (2026-11-24, commit `9677451`):** `mvn test -Dtest=OcorrenciaTest,ParalisacaoTest,RegistroFotograficoTest,RegistroDiarioTest,DiarioServiceTest` → 33 testes, 0 falhas, BUILD SUCCESS. Os 7 testes da seção 4 existem e passam (`OcorrenciaTest`, `ParalisacaoTest`, `RegistroFotograficoTest`). As constraints do banco são cobertas por `V4DiarioMigrationTest` (L27), que exige Docker e não rodou nesta máquina.

**Decisões da auditoria:** ADR-14 (categoria opcional com padrão `OUTRO` na ocorrência rápida, `Proposto`); Confirmações em `docs/adr/README.md`: paralisação herda a categoria da ocorrência; guardas também no domínio; edição de ocorrência é completa (regra repassada ao L38).
