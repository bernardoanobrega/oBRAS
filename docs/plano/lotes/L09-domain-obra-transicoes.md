# L09 — Domínio Obra: transições e auditoria

**Fase:** 1 · **Commit:** `feat(obra): implementar transicoes auditadas`  
**Status:** Concluído  
**Depende de:** L08 · **RF/UC:** RF-14 a RF-19, RF-73; UC04, UC05 · **ADR:** ADR-04, ADR-05, ADR-08

## 1. Objetivo

Implementar a máquina de estados de `Obra` e registrar auditoria em cada mudança relevante.

## 2. Escopo

**Inclui:** `iniciar`, `paralisar`, `retomar`, `concluir`, `cancelar`, `definirResponsavel`, `atualizarParametros`; estados finais; auditoria `CRIACAO`/`EDICAO`.  
**Não inclui:** cascata de alocação (L23), endpoints (L13), paralisação automática por ocorrência.

## 3. Contrato de domínio

- `PLANEJADA → EM_ANDAMENTO` só com responsável definido e ativo.
- `EM_ANDAMENTO ↔ PARALISADA`.
- `EM_ANDAMENTO → CONCLUIDA`.
- `PLANEJADA | EM_ANDAMENTO | PARALISADA → CANCELADA`.
- `CONCLUIDA` e `CANCELADA` finais.
- `paralisar()` não é chamado por `RegistroDiario`.
- Cada transição gera `RegistroAuditoria` com detalhe de status.

## 4. Testes primeiro (RED)

- `naoIniciaSemResponsavel`.
- `naoIniciaComResponsavelInativo`.
- `iniciaComResponsavelAtivo`.
- `paralisaRetomaConclui`.
- `cancelaAPartirDePlanejadaEmAndamentoEParalisada`.
- `naoAlteraObraConcluidaOuCancelada`.
- `transicaoGeraAuditoriaComStatusAnteriorENovo`.

## 5. Implementação

1. Adicionar métodos de transição.
2. Centralizar validação de estado.
3. Criar `Auditavel.registrar(acao, usuario, detalhe)`.
4. Não chamar `paralisar()` no fluxo de diário.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Estado final não regride | domínio | unitário |
| Início exige responsável ativo | domínio + serviço | unitário |
| Paralisação manual ≠ evento de diário | domínio + serviço | unitário |
| Toda transição audita | domínio/serviço | unitário + integração |

## 7. Segurança

Permissão `DETALHES_OBRA:EDITAR` será aplicada na API. Domínio não decide permissão.

## 8. Migração e rollback

Sem migration nova. Auditoria usa tabelas de L06.

## 9. Critérios de aceite

- [x] Todas as transições válidas cobertas.
- [x] Todas as inválidas rejeitadas.
- [x] Auditoria com detalhe de status.
- [x] Nenhuma transição automática por diário.

## 10. Verificação e evidência

Comando (executado em `obras-backend`):

    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 && export PATH="$JAVA_HOME/bin:$PATH" && mvn test -q

Saída: `exit 0`; resumo Surefire: `Tests run: 303, Failures: 0, Errors: 0, Skipped: 10`.

Testes L09 (`ObraTest`):
- naoIniciaSemResponsavel
- naoIniciaComResponsavelInativo
- iniciaComResponsavelAtivo
- paralisaRetomaConclui
- cancelaAPartirDePlanejadaEmAndamentoEParalisada
- naoAlteraObraConcluidaOuCancelada
- transicaoGeraAuditoriaComStatusAnteriorENovo
- definirResponsavelGeraAuditoriaComIds
- atualizarParametrosGeraAuditoriaDeEdicao

Ressalva: o domínio devolve `RegistroAuditoria` em cada transição/edição; a persistência desse registro fica para o L11 (service). O campo `@Transient Auditavel auditavel` só é hidratado no L11.
