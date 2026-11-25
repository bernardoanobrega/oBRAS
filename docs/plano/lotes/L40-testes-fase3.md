# L40 — Testes da Fase 3

**Fase:** 3 · **Commit:** `test(diario): cobrir fase 3`  
**Status:** Pendente  
**Depende de:** L26–L39 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-01, ADR-02, ADR-06, ADR-08

## 1. Objetivo

Fechar a Fase 3 com testes de diário, ocorrências, fotos, exclusão, concorrência e UI.

## 2. Escopo

**Inclui:** testes de integração backend; testes de frontend; testes de upload; testes de permissão; testes de concorrência RN-07.  
**Não inclui:** painel (Fase 4).

## 3. Contrato de teste

- PostgreSQL/Testcontainers para migrations e constraints.
- Arquivos JPG/PNG/PDF pequenos como fixtures.
- Testes de concorrência com duas transações.
- Testes de UI para estados e permissões.
- Testes de contrato DTO↔frontend.

## 4. Testes primeiro (RED)

- `umDiarioPorDiaSobConcorrencia`.
- `ocorrenciaSemDiarioFalha`.
- `fotoSemOcorrenciaFalha`.
- `uploadInvalidoFalha`.
- `exclusaoLogicaNaoApaga`.
- `auditoriaDeDiarioOcorrenciaFoto`.
- `muralSemFotoAvulsa`.
- `diarioMobileFirst`.

## 5. Implementação

1. Consolidar fixtures de diário.
2. Rodar suíte backend e frontend.
3. Corrigir flakiness de concorrência.
4. Garantir limpeza de storage temporário.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| RN-07 sob concorrência | banco + serviço | integração |
| Foto sempre de ocorrência | banco + serviço | integração |
| Exclusão lógica | serviço | integração |
| UI mobile-first | frontend | teste/revisão |

## 7. Segurança

Cobrir 401/403 em todos os endpoints de diário, ocorrência e foto.

## 8. Migração e rollback

Sem migration nova. Testar V4 do zero e sobre V3.

## 9. Critérios de aceite

- [ ] Backend e frontend verdes.
- [ ] Concorrência coberta.
- [ ] Upload/exclusão cobertos.
- [ ] Permissões cobertas.

## 10. Verificação e evidência

`mvn test`, `npm test`, `npm run build`. Evidência: relatórios e fixtures.
