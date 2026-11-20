# L01 — Premissas e ADRs fechados

**Fase:** 0 · **Commit:** `docs(plano): fechar premissas e adrs`
**Depende de:** nada · **RF/UC:** todos · **ADR:** ADR-01 a ADR-11
**Dono:** Thierry · **Revisor:** Cauã · **Data de revisão:** 2026-11-18
**Fonte canônica:** [`PLANO.md`](../../../PLANO.md), seções 3, 10 e 11

## 1. Objetivo

Transformar a seção 11 do `PLANO.md` em decisões explícitas, com dono, impacto e status. Fechar a definição de quem pode criar obra e registrar as pendências que não bloqueiam as Fases 1–4.

## 2. Escopo

**Inclui:** premissas adotadas, pendências, status de cada ADR, critério formal de reabertura e classificação das pendências.
**Não inclui:** implementação, migration, API, UI, modelagem de medição e atualização de diagramas (L03/L04).

## 3. Convenções

- **Aceito:** decisão fechada, sem ponto de decisão em aberto.
- **Aceito com pendência:** decisão válida, mas com um ponto de decisão que falta fechar. Ajustar diagrama ou documento é tarefa de outra L, não pendência.
- **Aberto:** nenhum ADR deve cair aqui. Se cair, precisa de justificativa explícita.
- **Bloqueia F1–F4:** a fase não consegue começar ou terminar sem a resposta. Toda pendência recebe classificação e fase/critério de fechamento.
- **Reabertura:** decisão fechada só reabre por ADR novo que a substitua (ADR-12 em diante). O ADR novo precisa de motivo, impacto em migrations/API/UI, aprovação de Thierry e Cauã, e referência ao ADR substituído.

## 4. Status dos ADRs

O texto completo de cada ADR fica no `PLANO.md` §3. Esta tabela é o índice de status; não duplica decisão.

| ADR | Status | Motivo |
|---|---|---|
| ADR-01 — Clima único por dia | Aceito | Critério de reabertura para RF-67 na seção 7. |
| ADR-02 — Foto sempre vinculada a ocorrência | Aceito | — |
| ADR-03 — Documento de Requisitos é fonte canônica de UCs | Aceito com pendência | Apêndice A ainda tem “nomes propostos”; reescrita/matriz é L02 (Cauã). |
| ADR-04 — `Obra` auditável | Aceito com pendência | Composição via `auditavel_id` decidida. Falta decidir se `AlocacaoObra` é auditável antes da F2. |
| ADR-05 — Responsável técnico opcional em `PLANEJADA` | Aceito | Dívida de código registrada na seção 8. |
| ADR-06 — RN-07 garantida no banco | Aceito com pendência | Texto admite trigger **ou** serviço; escolher o mecanismo ao desenhar a V4 (F3). |
| ADR-07 — Mão de obra sem controle de ponto | Aceito | — |
| ADR-08 — Paralisação não altera status automaticamente | Aceito | Atualização dos diagramas é L03/L04. |
| ADR-09 — Permissões e mobile desde as primeiras fases | Aceito | ADR-11 registra a exceção explícita para criação sem `obraId`. |
| ADR-10 — Cortes de escopo do mockup | Aceito | Sem contradição com a seção 10. |
| [ADR-11 — Criação de obra exige permissão global](../../../PLANO.md#adr-11--criação-de-obra-exige-permissão-global) | Aceito | Exceção ao ADR-09; criador não recebe acesso automático. Pendência residual de permissão global por cargo vai para F6/L51. |

## 5. Premissas

| Premissa | Origem | Risco | Validação | Dono | Revisão |
|---|---|---|---|---|---|
| Responsável técnico deve ser usuário ativo; não precisa estar alocado. | §11 | Responsável sem alocação pode não ver a obra, pois acesso depende de alocação ativa ou `VISUALIZAR` global. | Cenário na F2: definir responsável não alocado e testar acesso. | Thierry | 2026-11-18 |
| Criador de obra não recebe alocação automática; acesso posterior depende de alocação ativa ou permissão global de visualização. | ADR-11 | Criador sem `VISUALIZAR` recebe `201` e depois `403`. | Teste F1 de criar sem visualizar + guarda de navegação no frontend. | Thierry | 2026-11-18 |
| Canteiro pode ser excluído somente se não houver equipamento alocado. | §11 | Módulo de equipamentos não existe; regra não é testável na F1. | Definir port/stub ou adiar a verificação para quando o módulo existir. | Thierry | 2026-11-18 |
| `Obra` auditável por composição (`auditavel_id`). | ADR-04 | Equipe pode exigir JOINED; migração de PK mais pesada. | Teste de migration/backfill V3 em banco com dados. | Thierry | 2026-11-18 |
| `RegistroAuditoria.detalhe` é JSONB opcional. | ADR-04 | Formato livre sem validação pode crescer sem controle. | Definir schema mínimo na V3 e testar leitura/escrita. | Thierry | 2026-11-18 |
| Clima é único por dia; RF-67 interpretado conforme ADR-01. | ADR-01 | Stakeholder pode exigir clima por ocorrência. | Gatilho formal da seção 7. | Stakeholder / Thierry | 2026-11-18 |
| Foto sempre de ocorrência; mural não tem foto avulsa. | ADR-02 | UI pode permitir foto avulsa ou upload de PDF. | Testes negativos nas L29/L35; mural só com fotos de ocorrência. | Cauã | 2026-11-18 |
| Medição será vinculada a item orçamentário quando existir. | §11 / §5.3 | Há tensão com a “âncora financeira” da aprovação; orçamento pode não existir. | Reconciliar em F5; testar aprovação com e sem item orçamentário. | Thierry | 2026-11-18 |

## 6. Pendências

A definição de quem pode criar obra e como o criador ganha acesso foi fechada pelo ADR-11 e não é mais pendência.

| Pendência | Bloqueia F1–F4? | Fechamento |
|---|---|---|
| Limite de alerta de cronograma/SPI | Não | F4, quando o KPI simulado virar real, ou F5. |
| Modelo final de medição e vínculo com orçamento | Não | F5. A V5 inicial da Fase 0 fica fora da L01. |
| Evolução de estoque por canteiro (RF-48) | Não | F5+. |
| Necessidade de auditoria de `AlocacaoObra` | Não, mas decidir **antes de começar a F2** | Início da F2; padrão provisório: não auditável. |
| Clima por ocorrência (exigência do stakeholder) | Não | Gatilho formal da seção 7. |
| Permissão global por cargo para criação de obra | Não | F6/L51. Hoje a concessão de `DETALHES_OBRA:CRIAR` é individual/global por usuário. |

## 7. Critérios formais de reabertura

### RF-67 / clima por ocorrência

- **Gatilho:** exigência escrita do stakeholder ou alteração do texto do RF-67 no Documento de Requisitos.
- **Resultado:** ADR novo que substitui o ADR-01 (próximo número livre; ADR-12 no estado atual).
- **Impacto pré-mapeado:** V4 (`clima` sai de `registro_diario`), ADR-02 (ocorrência rápida), painel, enum de clima e diagramas.

### Permissão global por cargo

- **Gatilho:** necessidade de criar obra por cargo, sem concessão individual.
- **Resultado:** ADR novo que substitui/estende o ADR-11.

### Genérico

Vale o critério da seção 3: ADR novo, com motivo, impacto e referência ao ADR substituído.

## 8. Divergências e dívidas registradas

1. **README × PLANO.** O README fala em “V3 auditoria/diário, V9 documentos”; o plano usa V3 auditoria, V4 diário e V6+ documentos. Também diz que `obra` tem “só o adapter”, mas a API de leitura já existe (`d6a85c2`).
2. **§2 do PLANO desatualizada.** `DetalheObraPage` já está no repositório (`41c5144`) e há API de leitura de obra/equipe (`d6a85c2`). Corrigido nesta L01.
3. **ADR-05 — dívida de código.** `Obra.responsavelId` está `@Column(nullable = false)` e `ObraDto` expõe `responsavelId`. A V3 remove o `NOT NULL`; a F1 ajusta o contrato.
4. **Mock.** Os comentários de `ClimaDiaCard.jsx` e `EquipeResumo.jsx` já declaram os cortes de ADR-01/02/07/10. A verificação restante é de comportamento e dados do mock e da API de leitura.
5. **Cabeçalho do PLANO.** Versão e status atualizados para apontar para esta L01 e para o ADR-11.

## 9. Verificações feitas

- `V1__usuarios_cargos_permissoes.sql` cria o catálogo 9×5, incluindo `DETALHES_OBRA:CRIAR`; não há seeds em `cargo_permissao`.
- `AdminBootstrap` concede todas as permissões individuais ao admin; criação fica disponível por padrão para o admin.
- `PermissaoEvaluator.podeGlobal(...)` já existe no bean `perm`.
- `AutorizacaoService.permissoesEfetivas(usuarioId, obraId)` só resolve cargos quando existe `obraId` e alocação ativa; por isso cargo não autoriza criação.

## 10. Checklist do gate

- [x] Toda premissa da seção 11 tem status.
- [x] Toda pendência tem fase ou critério de fechamento.
- [x] Nenhuma decisão contradiz ADR existente; ADR-11 é exceção explícita ao ADR-09.
- [x] RF-67 e clima por ocorrência têm critério formal de mudança.
- [x] ADRs com status e impacto.
- [x] Premissas com risco e validação.
- [x] Pendências classificadas.
- [x] `PLANO.md` e documentos alinhados.
- [ ] Revisão cruzada com Cauã.
- [ ] Commit único e status `Concluído`.

## 11. Evidência

- Este documento: `docs/plano/L01-premissas-adrs.md`.
- `PLANO.md`: ADR-11 na §3, autorização de criação na §4.5, contrato na §6.1, §2 corrigida, §11 atualizada e cabeçalho na versão 3.
- Lote: `/home/costa/Estudos/ENG-SOFT/lotes/L01-premissas-adrs.md` aponta para este documento.
- Lote documental: sem `mvn test` ou `npm test`; a verificação é revisão cruzada contra `PLANO.md` §3, §10 e §11.

## 12. Fora da L01

Modelagem de medições, atualização de diagramas (L03/L04), reescrita do documento de casos de uso (L02) e qualquer mudança em código ou migration.
