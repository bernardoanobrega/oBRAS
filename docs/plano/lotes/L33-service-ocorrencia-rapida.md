# L33 — Ocorrência rápida e diário automático

**Fase:** 3 · **Commit:** `feat(diario): implementar ocorrencia rapida`  
**Status:** Em revisão  
**Depende de:** L32 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02

## 1. Objetivo

Criar ocorrência rápida para registro fotográfico, criando o diário do dia quando necessário.

## 2. Escopo

**Inclui:** endpoint/service de ocorrência rápida; categoria `OUTRO`; descrição obrigatória; autor autenticado; dataHora agora; se não houver diário, exigir clima e criar diário.  
**Não inclui:** upload da foto (L35), mural (L39).

## 3. Contrato de serviço

- `criarRapida(obraId, OcorrenciaRapidaDto, usuarioId)`.
- Payload: `data`, `clima` opcional, `categoria`, `descricao`, `legenda`.
- Se diário do dia existir, `clima` é ignorado.
- Se não existir, `clima` é obrigatório e o diário é criado.
- Categoria sempre `OUTRO` na ocorrência rápida.
- Descrição obrigatória; legenda pode compor a descrição.

## 4. Testes primeiro (RED)

- `criaOcorrenciaRapidaComDiarioExistente`.
- `criaDiarioQuandoNaoExiste`.
- `exigeClimaQuandoDiarioNaoExiste`.
- `ignoraClimaQuandoDiarioExiste`.
- `categoriaSempreOutro`.
- `descricaoObrigatoria`.
- `autorEDataHoraSaoDoMomento`.
- `auditaCriacao`.

## 5. Implementação

1. Criar DTO `OcorrenciaRapidaDto`.
2. Reutilizar `RegistroDiarioService` e `OcorrenciaService`.
3. Garantir transação única para diário + ocorrência.
4. Não criar foto avulsa; foto será vinculada depois.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Sem diário, clima obrigatório | serviço | 422 |
| Com diário, clima ignorado | serviço | unitário |
| Categoria `OUTRO` | serviço | unitário |
| Transação única | serviço | integração |

## 7. Segurança

Permissão `DETALHES_OBRA:CRIAR`; autor do contexto.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Ocorrência rápida funcional.
- [ ] Diário automático.
- [ ] Clima condicional.
- [ ] Auditoria.

## 10. Verificação e evidência

`mvn test`. Evidência: testes com e sem diário.
