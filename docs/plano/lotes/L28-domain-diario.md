# L28 — Domínio RegistroDiario

**Fase:** 3 · **Commit:** `feat(diario): modelar registro diario`  
**Status:** Concluído  
**Depende de:** L26 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-06

## 1. Objetivo

Modelar o diário de campo com clima único, equipe presente e exclusão lógica.

## 2. Escopo

**Inclui:** entidade `RegistroDiario`, enum `CondicaoClimatica`, factory, atualização, vínculo com `Auditavel`, equipe presente.  
**Não inclui:** ocorrências (L29), persistência (L30), serviço (L31).

## 3. Contrato de domínio

- Campos: id, obraId, data, clima, descricao, equipePresente, auditavel.
- Um clima por dia; sem turno, temperatura ou sync.
- Clima obrigatório.
- `data` obrigatória.
- Exclusão lógica via `Auditavel`.
- Equipe presente é conjunto de alocações ativas.

## 4. Testes primeiro (RED)

- `criaDiarioComClimaUnico`.
- `rejeitaClimaNulo`.
- `rejeitaDataNula`.
- `atualizaDescricaoEClima`.
- `exclusaoLogicaMarcaAuditavel`.
- `equipePresenteNaoAceitaDuplicata`.

## 5. Implementação

1. Criar entidade e enum.
2. Criar factory `RegistroDiario.registrar(...)`.
3. Encapsular equipe presente.
4. Não criar métodos de turno/temperatura.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Clima único/dia | domínio + banco | unitário |
| Clima no enum | domínio + banco | unitário/check |
| Exclusão lógica | domínio | unitário |
| Equipe sem duplicata | domínio + banco | unitário |

## 7. Segurança

Sem decisão de permissão no domínio.

## 8. Migração e rollback

Sem migration nova. Depende de L26.

## 9. Critérios de aceite

- [x] Entidade sem dependência web.
- [x] Clima único.
- [x] Exclusão lógica.
- [x] Sem campos de turno/temperatura.

## 10. Verificação e evidência

`mvn test`. Evidência: testes unitários.
