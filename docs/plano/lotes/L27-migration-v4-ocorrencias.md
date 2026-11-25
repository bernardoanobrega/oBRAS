# L27 — Migration V4: ocorrências, paralisações e fotos

**Fase:** 3 · **Commit:** `feat(diario): criar schema de ocorrencias e fotos`  
**Status:** Concluído  
**Depende de:** L26 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-02, ADR-08

## 1. Objetivo

Criar as tabelas de ocorrência, paralisação e registro fotográfico com vínculos obrigatórios.

## 2. Escopo

**Inclui:** `ocorrencia`, `paralisacao`, `registro_fotografico`, enums de categoria, constraints, índices.  
**Não inclui:** domínio/serviço/API (L29+), upload (L35).

## 3. Contrato DB

- `ocorrencia(id BIGINT PK REFERENCES auditavel(id), registro_diario_id NOT NULL, categoria, descricao, data_hora, alocacao_obra_id)`; o autor é a alocação ativa do usuário na obra (não o `usuario_id`).
- `categoria` inclui `OUTRO`; paralisação também exige categoria.
- `paralisacao(id PK REFERENCES ocorrencia(id), inicio, fim)` com `fim IS NULL OR fim >= inicio`.
- `registro_fotografico(id BIGINT PK REFERENCES auditavel(id), ocorrencia_id NOT NULL, arquivo, legenda, data_envio)`.
- Índices por `registro_diario_id`, `ocorrencia_id` e data.

## 4. Testes primeiro (RED)

- `migrationV4CriaOcorrenciaParalisiaFoto`.
- `rejeitaOcorrenciaSemDiario`.
- `rejeitaFotoSemOcorrencia`.
- `rejeitaParalisacaoComFimAntesDoInicio`.
- `rejeitaCategoriaInvalida`.
- `permiteParalisacaoSemFim`.
- `permiteFotoJpgOuPngNoSchema`.

## 5. Implementação

1. Completar `V4__diario_ocorrencias_fotos.sql`.
2. Criar FKs e checks.
3. Criar índices de consulta.
4. Testar inserts inválidos.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Ocorrência sempre em diário | banco | FK not null |
| Foto sempre em ocorrência | banco | FK not null |
| Paralisação com período válido | banco | check |
| Categoria obrigatória | banco | not null + check |

## 7. Segurança

Sem endpoint. Autor será o usuário autenticado na API.

## 8. Migração e rollback

Forward-only. Rollback manual em ambiente descartável.

## 9. Critérios de aceite

- [x] Tabelas e FKs criadas.
- [x] Constraints negativas testadas.
- [x] Índices de consulta.
- [x] V4 aplica sobre banco com V3.

## 10. Verificação e evidência

Testes de integração. Evidência: schema e testes de constraint.
