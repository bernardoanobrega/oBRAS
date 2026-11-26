# L33 — Ocorrência rápida e diário automático

**Fase:** 3 · **Commit:** `feat(diario): implementar ocorrencia rapida`  
**Status:** Concluído  
**Depende de:** L32 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02, ADR-14, ADR-18, ADR-19

## 1. Objetivo

Criar ocorrência rápida para registro fotográfico, criando o diário do dia quando necessário.

## 2. Escopo

**Inclui:** endpoint/service de ocorrência rápida; categoria padrão `OUTRO`; descrição obrigatória; autor autenticado; dataHora agora quando não informada; se não houver diário, exigir clima e criar diário; corrida na criação do diário (ADR-19).  
**Não inclui:** upload da foto (L35), mural (L39).

## 3. Contrato de serviço

- `DiarioService.ocorrenciaRapida(obraId, NovaOcorrenciaRapidaDto)` → `OcorrenciaDto`. Usuário via `CurrentUser.id()`, nunca por parâmetro.
- Payload: `data` (obrigatória), `clima` opcional, `categoria` opcional, `descricao` (obrigatória), `dataHora` opcional. **Sem `legenda`** (vai no upload da foto, L35) e **sem `paralisacao`** (formulário completo, L38).
- Se o diário ativo do dia existir, `clima` é ignorado (ADR-01).
- Se não existir, `clima` é obrigatório (422) e o diário é criado **sem descrição** e sem equipe presente.
- Categoria ausente vira `OUTRO`; a informada é mantida (ADR-14).
- `dataHora` ausente vale "agora" e precisa ser do dia `data`, senão 422 (ADR-18).
- Diário e ocorrência na mesma transação: se a ocorrência falhar, o diário automático também é desfeito.
- Corrida: se outra requisição criar o diário do dia ao mesmo tempo, a ocorrência é vinculada a esse diário, sem erro (ADR-19).

## 4. Testes primeiro (RED)

- `criaOcorrenciaRapidaComDiarioExistente`.
- `criaDiarioQuandoNaoExiste`.
- `exigeClimaQuandoDiarioNaoExiste`.
- `ignoraClimaQuandoDiarioExiste`.
- `categoriaSempreOutro` (substituído pelo ADR-14).
- `descricaoObrigatoria`.
- `autorEDataHoraSaoDoMomento`.
- `auditaCriacao`.

**Onde ficaram:**
- `OcorrenciaRapidaIntegracaoTest` (banco real, sem a transação do teste): os 7 testes acima, `categoriaPadraoOutro`, `transacaoUnicaDesfazDiarioSeOcorrenciaFalhar` e `corridaSemDiarioVinculaAoDiarioCriado` (duas threads).
- `DiarioServiceTest` (unitários): `ocorrenciaRapidaSemDiarioExigeClima`, `ocorrenciaRapidaComCategoriaNulaUsaOutro`, `ocorrenciaRapidaMantemCategoriaInformada` (ADR-14) e `corridaNoDiarioRepeteEVinculaAoDiarioCriado` (ADR-19).

## 5. Implementação

1. Criar DTO `OcorrenciaRapidaDto`.
2. Implementar em `DiarioService` (serviço único do módulo), reutilizando `criarDiario` e `criarOcorrencia`.
3. Garantir transação única para diário + ocorrência, aberta por `TransactionTemplate`, com uma nova tentativa na corrida (ADR-19).
4. Não criar foto avulsa; foto será vinculada depois.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Sem diário, clima obrigatório | serviço | 422 |
| Com diário, clima ignorado | serviço | unitário |
| Categoria `OUTRO` | serviço | unitário |
| Transação única | serviço | integração |
| Corrida vincula ao diário criado | serviço | integração (2 threads) |

## 7. Segurança

Permissão `DETALHES_OBRA:CRIAR`; autor do contexto.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [x] Ocorrência rápida funcional.
- [x] Diário automático.
- [x] Clima condicional.
- [x] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes com e sem diário.

**Evidência (26/11/2026):** `OcorrenciaRapidaIntegracaoTest` 10/10 e `DiarioServiceTest` 10/10. `mvn test` na `develop`: 251 testes, 3 erros, todos em `OrcamentoRepositoryTest` (módulo Orçamento), que insere obra sem `auditavel_id`, obrigatório desde a V3; falha alheia a este lote.
