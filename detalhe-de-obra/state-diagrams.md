# oBRAS — Diagramas de Estados do Módulo Detalhes de Obra

> Versão de trabalho. Identifica quais classes do módulo precisam de diagrama de estados (DTE), define estados, eventos, guardas e ações de cada uma, e lista o que falta decidir. Serve de base para os arquivos `.puml` dos diagramas de estados.

## 0. Como ler este documento

- **Fontes:** `requirements.md` (derivado do SRS), `class-diagram.puml`, Casos de Uso (`obras_use_cases.docx`) e os slides da disciplina (Parte 14, Diagramas UML Dinâmicos).
- **IDs:** `DE-nn` é um diagrama de estados. `DE-nn.m` é uma transição atômica desse diagrama. `PE-nn` são pendências específicas dos diagramas de estados. Os IDs `RF-`, `RN-` e `P-` são os do `requirements.md`.
- **Notação das transições:** `evento [guarda] / ação`, conforme os slides 7 a 9. O evento corresponde a uma operação pública da classe (slide 13).
- **Coluna Fonte:** **Inferido** significa que não está escrito nos documentos (veio da equipe ou da modelagem) e precisa de confirmação. **Confirmado pela equipe** indica decisão tomada durante a construção destes diagramas.
- **Os Casos de Uso não descrevem fluxos**, só um parágrafo por UC. Por isso nenhuma transição vem de um fluxo escrito; as fontes são os RF, as RN e a modelagem.

## 1. Critério de seleção

Pelos slides, o diagrama de estados é feito por classe, e nem toda classe precisa de um. Só entram classes com comportamento relevante, isto é, com um ciclo de vida em que o estado muda por eventos e condiciona o que pode ser feito (slides 14 e 15). Os eventos saem das operações públicas da classe e das regras de negócio (slide 13).

Uma classe entra quando atende a pelo menos um destes critérios:

1. Tem um atributo de estado explícito (enum).
2. Tem um ciclo de vida descrito nos requisitos (aprovar, rejeitar, encerrar).
3. Tem regra de negócio que depende de em que fase o objeto está.

## 2. Classes avaliadas

| Classe | Decisão | ID | Prioridade | Justificativa |
| --- | --- | --- | --- | --- |
| `Obra` | **Sim** | DE-01 | Alta | Tem `StatusObra`; RN-01 a RN-03 definem regras de ciclo de vida. |
| `Medicao` | **Condicional** (P-14) | DE-02 | Alta | RF-05.3 a 05.7 descrevem aferir, aprovar e rejeitar. Hoje não existe como classe. |
| `AlocacaoObra` | **Sim** | DE-03 | Média | `dataFim` encerra a alocação; RN-04 vale só para alocações ativas. |
| `Ocorrencia` (paralisação) | **Condicional** (P-19) | DE-04 | Baixa | Paralisação tem início e fim (RF-12.4), mas o registro é imutável pela P-19. |
| `Revisao` de projeto | **Condicional** (P-14) | DE-05 | Baixa | RF-11.4 exige identificar a revisão mais recente. Hoje não existe como classe. |
| `RegistroDiario` | Não | — | — | Um por dia (RN-07), sem ciclo de vida nos documentos. Imutável (P-19). |
| `RegistroFotografico` | Não | — | — | Objeto de dados, sem estados. |
| `Usuario`, `Cargo` | Fora do módulo | — | — | Pertencem ao módulo de acessos (P-02, P-21). |
| `Orcamento`, `Cronograma` | Fora do módulo | — | — | Classes externas. |

**Conjunto mínimo sugerido:** DE-01, DE-02 e DE-03. DE-04 e DE-05 só valem a pena se as pendências que as condicionam forem resolvidas a favor.

## 3. Diagramas de estados

### DE-01 — Obra

> Classe: `Obra`. Atributo de estado: `status : StatusObra`. Prioridade: Alta.
> Requisitos relacionados: RF-02.10, RN-01, RN-02, RN-03; P-06.

**Estados**

| Estado | Condição | Fonte |
| --- | --- | --- |
| PLANEJADA | Obra cadastrada que ainda não começou. É o estado inicial. | Inferido (P-06) |
| EM_ANDAMENTO | Obra em execução. | Inferido (P-06) |
| PARALISADA | Obra com atividades interrompidas. | Inferido (P-06) |
| CONCLUIDA | Obra finalizada. Estado final (RN-02). | Inferido (RN-02) |
| CANCELADA | Obra cancelada, nunca apagada (RN-01). Estado final (RN-02). | Inferido (RN-01, RN-02) |

**Transições** (tabela confirmada pela equipe)

| ID | Origem | Evento [guarda] / ação | Destino | Fonte |
| --- | --- | --- | --- | --- |
| DE-01.1 | (inicial) | cadastrar [usuário autorizado] | PLANEJADA | RF-02.1, RF-02.9; confirmado pela equipe |
| DE-01.2 | PLANEJADA | iniciar() [há responsável técnico alocado] | EM_ANDAMENTO | Confirmado pela equipe; guarda depende de P-21 |
| DE-01.3 | EM_ANDAMENTO | paralisar() | PARALISADA | Confirmado pela equipe |
| DE-01.4 | PARALISADA | retomar() | EM_ANDAMENTO | Confirmado pela equipe |
| DE-01.5 | EM_ANDAMENTO | concluir() | CONCLUIDA | Confirmado pela equipe |
| DE-01.6 | PLANEJADA | cancelar() | CANCELADA | RN-01; confirmado pela equipe |
| DE-01.7 | EM_ANDAMENTO | cancelar() | CANCELADA | RN-01; confirmado pela equipe |
| DE-01.8 | PARALISADA | cancelar() | CANCELADA | RN-01; confirmado pela equipe |
| DE-01.9 | CONCLUIDA | (nenhum) | estado final | RN-02 |
| DE-01.10 | CANCELADA | (nenhum) | estado final | RN-02 |

**Regras que o diagrama deve deixar visíveis**

- **RN-03:** registrar uma ocorrência de paralisação **não** dispara DE-01.3. `paralisar()` é um evento explícito e separado.
- **P-06:** o status não é derivado do avanço físico (SRS 2.4). Nenhuma transição tem o avanço físico como guarda.
- **RN-02:** CONCLUIDA e CANCELADA não têm transição de saída.

### DE-02 — Medicao (condicional a P-14)

> Classe: `Medicao` (ainda não existe no diagrama de classes). Prioridade: Alta.
> Requisitos relacionados: RF-05.1 a RF-05.7, RN-09; P-12, P-13.

**Estados**

| Estado | Condição | Fonte |
| --- | --- | --- |
| AFERIDA | Quantidades lançadas pela equipe de campo, aguardando decisão do Gestor da Obra. É o estado inicial. | RF-05.1, RF-05.7 |
| APROVADA | Aprovada pelo Gestor da Obra; o fluxo financeiro correspondente é liberado. | RF-05.3, RF-05.5 |
| REJEITADA | Rejeitada pelo Gestor da Obra; o fluxo financeiro correspondente é bloqueado. | RF-05.4, RF-05.6 |

**Transições**

| ID | Origem | Evento [guarda] / ação | Destino | Fonte |
| --- | --- | --- | --- | --- |
| DE-02.1 | (inicial) | aferir(quantidades) [usuário é da equipe de campo] | AFERIDA | RF-05.1, UC07 |
| DE-02.2 | AFERIDA | aprovar(gestor) [gestor é o Gestor da Obra] / liberar fluxo financeiro | APROVADA | RF-05.3, RF-05.5, RN-09 (ver P-12) |
| DE-02.3 | AFERIDA | rejeitar(gestor) [gestor é o Gestor da Obra] / bloquear fluxo financeiro | REJEITADA | RF-05.4, RF-05.6, RN-09 (ver P-12) |
| DE-02.4 | REJEITADA | corrigir e reenviar | AFERIDA | Inferido; só existe se a P-13 permitir |
| DE-02.5 | APROVADA | (nenhum) | estado final | Inferido |

**Pontos a decidir:** DE-02.4 depende da P-13. O efeito "liberar/bloquear fluxo financeiro" depende da P-12 (o SRS exclui transações financeiras diretas).

### DE-03 — AlocacaoObra

> Classe: `AlocacaoObra`. Prioridade: Média.
> Requisitos relacionados: RF-02.4 a RF-02.7, RN-04; P-02, P-21, P-22.

**Estados**

| Estado | Condição | Fonte |
| --- | --- | --- |
| ATIVA | Usuário alocado na obra, com um ou mais Cargos. `dataFim` vazia. É o estado inicial. | RF-02.4; RN-04 |
| ENCERRADA | Alocação com `dataFim` preenchida. Estado final. | Diagrama de classes; confirmado pela equipe (PE-05) |

**Decisão (PE-05):** o estado é derivado de `dataFim`, sem atributo de estado explícito. Não é permitida `dataFim` futura.

**Transições**

| ID | Origem | Evento [guarda] / ação | Destino | Fonte |
| --- | --- | --- | --- | --- |
| DE-03.1 | (inicial) | alocarUsuario(u, cargos) [não existe alocação ATIVA do mesmo usuário na mesma obra; cargos não vazio] / registrar dataInicio; retorna a `AlocacaoObra` criada | ATIVA | RF-02.4; RN-04; P-22 |
| DE-03.2 | ATIVA | definirCargos(cargos) [cargos não vazio] | ATIVA | RF-02.5, RF-02.7; P-22 |
| DE-03.3 | ATIVA | encerrarAlocacao() / registrar dataFim; retorna a `AlocacaoObra` encerrada, preservando o histórico | ENCERRADA | RF-02.6; diagrama de classes; confirmado pela equipe (PE-04) |
| DE-03.4 | ENCERRADA | (nenhum) | estado final | Inferido |

**Regras que o diagrama deve deixar visíveis**

- **RN-04 / P-22:** a unicidade vale só para alocações **ativas** por (usuário, obra). Uma alocação ENCERRADA não impede uma nova.
- **DE-03.2** é uma transição reflexiva (ATIVA para ATIVA); mostra que trocar Cargos não encerra a alocação.

### DE-04 — Ocorrencia de paralisação (condicional a P-19)

> Classe: `Ocorrencia`, apenas quando `categoria = PARALISACAO`. Prioridade: Baixa.
> Requisitos relacionados: RF-12.3, RF-12.4, RF-12.5; P-19.

**Estados**

| Estado | Condição | Fonte |
| --- | --- | --- |
| EM_CURSO | `inicioParalisacao` preenchido e `fimParalisacao` vazio. É o estado inicial. | RF-12.4 |
| ENCERRADA | `fimParalisacao` preenchido. Estado final. | RF-12.4 |

**Transições**

| ID | Origem | Evento [guarda] / ação | Destino | Fonte |
| --- | --- | --- | --- | --- |
| DE-04.1 | (inicial) | registrarOcorrencia(PARALISACAO, ...) / registrar inicioParalisacao | EM_CURSO | RF-12.3, RF-12.4 |
| DE-04.2 | EM_CURSO | encerrarParalisacao(fim) [fim ≥ inicioParalisacao] / calcular impacto no prazo | ENCERRADA | RF-12.4, RF-12.5; **conflita com P-19** |
| DE-04.3 | ENCERRADA | (nenhum) | estado final | Inferido |

**Por que é condicional:** DE-04.2 altera um registro depois de criado, e a P-19 diz que ocorrências são imutáveis. Só é viável se a regra for ajustada (por exemplo, permitindo apenas o preenchimento do fim) ou se a paralisação for registrada já com início e fim.

### DE-05 — Revisao de projeto executivo (condicional a P-14)

> Classe: `Revisao` (ainda não existe no diagrama de classes). Prioridade: Baixa.
> Requisitos relacionados: RF-11.1, RF-11.2, RF-11.4, RN-12.

**Estados**

| Estado | Condição | Fonte |
| --- | --- | --- |
| VIGENTE | Revisão mais recente do projeto; é a que a equipe de campo consulta. É o estado inicial. | RF-11.1, RF-11.4 |
| SUPERADA | Existe uma revisão mais nova do mesmo projeto. Estado final. | Inferido (RF-11.4) |

**Transições**

| ID | Origem | Evento [guarda] / ação | Destino | Fonte |
| --- | --- | --- | --- | --- |
| DE-05.1 | (inicial) | publicarRevisao(arquivo) [PDF ou imagem, até 10 MB] / marcar a anterior como SUPERADA | VIGENTE | RF-11.4; RN-10, RN-12 |
| DE-05.2 | VIGENTE | publicarRevisao(nova) | SUPERADA | RF-11.4 |
| DE-05.3 | SUPERADA | (nenhum) | estado final | Inferido |

**Observação:** o upload dos arquivos de projeto pertence ao módulo de Contratos e Documentos (RF-10). Este diagrama só faz sentido aqui se a P-14 trouxer projetos e revisões para o diagrama de classes.

## 4. Operações e classes a acrescentar ao diagrama de classes

Pelo slide 15, construir os DTEs pode gerar novos atributos e operações. Para os diagramas acima ficarem consistentes com o `class-diagram.puml`:

| Classe | Acréscimo | Motivo |
| --- | --- | --- |
| `Obra` | `iniciar()`, `paralisar()`, `retomar()`, `concluir()`, `cancelar()` | Eventos de DE-01. Hoje só existe `alterarResponsavel()`. |
| `Obra` | Remover `desalocarUsuario(u)`; alterar `encerrarAlocacao(a)` para retornar `AlocacaoObra` em vez de `void`; manter `alocarUsuario(u, cargos) : AlocacaoObra` | Decisão da PE-04. `encerrarAlocacao` é o evento de DE-03.3 e devolve a alocação encerrada, sem perder dados. |
| `AlocacaoObra` | `estaAtiva() : boolean` (opcional) | Estado derivado de `dataFim` (DE-03, PE-05). |
| `Medicao` (nova) | atributos `id`, `idObra`, `estado : EstadoMedicao`; operações `aferir()`, `aprovar(gestor : Usuario)`, `rejeitar(gestor : Usuario)` | Eventos de DE-02. Exige resolver a P-14. |
| `EstadoMedicao` (nova) | enum com AFERIDA, APROVADA, REJEITADA | Estados de DE-02. |
| `Ocorrencia` | `encerrarParalisacao(fim : DateTime)` | Evento de DE-04. Exige resolver a P-19. |
| `Revisao` (nova) | atributos de versão e `estado`; operação `publicarRevisao()` | Eventos de DE-05. Exige resolver a P-14. |

## 5. Regras de negócio que viram guardas ou restrições

| Regra | Onde aparece | Efeito no diagrama |
| --- | --- | --- |
| RN-01 | DE-01 | Não existe transição de exclusão; o cancelamento leva a CANCELADA. |
| RN-02 | DE-01 | CONCLUIDA e CANCELADA são estados finais. |
| RN-03 | DE-01 | Nenhuma transição de `Obra` é disparada por registrar ocorrência. |
| RN-04 | DE-03 | Guarda de DE-03.1: unicidade de alocação ativa por (usuário, obra). |
| RN-09 | DE-02 | Guarda de DE-02.2 e DE-02.3: apenas o Gestor da Obra decide. |
| RN-10 | DE-05 | Guarda de DE-05.1: tipos e tamanho do arquivo. |
| RN-08 | DE-01 | Reforça que o avanço físico não muda o status da obra. |

## 6. Pendências e conflitos

| ID | Pendência | Por que importa |
| --- | --- | --- |
| PE-01 | O que acontece com as alocações ativas, as medições pendentes e a possibilidade de registrar ocorrências quando a obra vai para CONCLUIDA ou CANCELADA? | Define as ações das transições DE-01.5 a DE-01.8. |
| PE-02 | Quem pode disparar cada mudança de status da obra (Administrador, Gestor da Obra)? A matriz da seção 5 do `requirements.md` não cobre isso (relacionada à P-17). | Define as guardas de DE-01. |
| PE-03 | A guarda de DE-01.2 (responsável técnico alocado) depende de como o sistema identifica o responsável técnico (P-21). | Sem a P-21, a guarda não pode ser verificada. |
| PE-04 | **Resolvida:** `desalocarUsuario(u) : Usuario` e `encerrarAlocacao(a) : void` eram a mesma operação. Mantém-se só `encerrarAlocacao(a)`, que passa a retornar `AlocacaoObra` (a alocação encerrada, com histórico), preservando a intenção de não perder dados. `desalocarUsuario` é removida. | Define o evento de DE-03.3. Decisão da equipe. |
| PE-05 | **Resolvida:** o estado de `AlocacaoObra` é derivado de `dataFim`, sem atributo explícito; `dataFim` futura não é permitida. | DE-03 usa condição sobre atributo, sem enum de estado. Decisão da equipe. |
| PE-06 | Encerrar a alocação do único responsável técnico deixa a obra sem responsável? | Possível guarda em DE-03.3 (ver P-21). |
| PE-07 | Incluir um estado EM_ANALISE em DE-02 para cobrir o RF-05.2 ("analisar")? | Muda o diagrama de três para quatro estados. |
| PE-08 | A medição APROVADA é realmente final, ou pode ser revertida? | Define DE-02.5. |
| PE-09 | O registro de ocorrência é imutável (P-19), mas o diagrama de classes tem `alterarOcorrencia()` e `excluirOcorrencia()`. | Conflito que bloqueia DE-04. |
| PE-10 | A numeração do `requirements.md` pula a RN-05, embora a seção 8 e a P-22 a citem; seu conteúdo está na RN-04. | Rastreabilidade das guardas de DE-03. |

## 7. Ordem de produção e rastreabilidade

| Ordem | Diagrama | Arquivo previsto | Depende de |
| --- | --- | --- | --- |
| 1 | DE-01 Obra | `obra-states.puml` (pronto) | PE-03 (só a guarda de DE-01.2) |
| 2 | DE-03 AlocacaoObra | `alocacao-states.puml` (pronto) | PE-04 e PE-05 resolvidas |
| 3 | DE-02 Medicao | `state-medicao.puml` | P-14, P-12, P-13 |
| 4 | DE-04 Ocorrencia | `state-ocorrencia.puml` | PE-09 |
| 5 | DE-05 Revisao | `state-revisao.puml` | P-14 |

**Requisitos sem diagrama de estados:** RF-18 (painel) e RF-12.1 a 12.3 (registro diário), por não terem ciclo de vida próprio.