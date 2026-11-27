# Matriz de rastreabilidade: módulo Detalhe de Obra

**Lote:** L02 · **ADR:** ADR-03 (Documento de Requisitos é a fonte canônica de UCs)
**Fontes:** Documento de Requisitos (RF), documento de Casos de Uso (UC), cabeçalhos dos lotes L01–L54.
**Atenção:** o Apêndice A do `PLANO.md`, citado no L02, não está no repositório. Esta matriz foi montada a partir dos documentos canônicos e deve ser conferida contra o `PLANO.md` quando ele for publicado.

## 1. UC → RF → lotes

| UC | Nome | Ator(es) | RFs | Lotes principais |
|---|---|---|---|---|
| UC04 | Cadastrar Obras | Gestor da Obra, Administrador | RF-14, RF-15 | L07–L14, L16, L17, L47 |
| UC05 | Configurar Equipe da Obra | Gestor da Obra, Administrador | RF-16, RF-17, RF-18, RF-19 | L18–L25, L13, L47, L48 |
| UC21 | Registrar Diário de Ocorrências e Imprevistos | Mestre de Obras | RF-64, RF-65, RF-66, RF-67, RF-68 | L26–L40, L46, L48 |
| *(UC a criar no L05)* | Visualizar Painel da Obra | todos com `DETALHES_OBRA:VISUALIZAR` | RF-73 (ADR-13) | L41–L46, L49, L50 |
| *(transversal)* | Acesso por obra | todos | RNF-03 (ADR-12) | L14, L44, L51 |

Os lotes de documentação e qualidade cobrem todos os RFs: L03/L04 (diagramas), L05 (casos de uso), L16/L24/L40/L50 (testes por fase), L52 (E2E), L54 (documentação).

## 2. RF → UC → regra → lotes (matriz reversa)

| RF | Prioridade | UC | O que exige / regra do módulo | Lotes |
|---|---|---|---|---|
| RF-14 | Alta | UC04 | Cadastrar obra. Criação exige permissão global (ADR-11); obra nasce `PLANEJADA` e auditável (ADR-04) | L07–L12, L17 |
| RF-15 | Alta | UC04 | Editar parâmetros operacionais da obra; alteração auditada | L11, L12, L17, L47 |
| RF-16 | Alta | UC05 | Alocar membro da equipe à obra com cargos; uma alocação ativa por usuário e obra | L18–L20, L22, L25 |
| RF-17 | Alta | UC05 | Alterar a equipe: trocar cargos e encerrar alocação; obra concluída ou cancelada encerra em cascata | L20–L23, L25 |
| RF-18 | Alta | UC05 | Definir responsável técnico: usuário ativo, opcional em `PLANEJADA` (ADR-05) | L07, L13, L21 |
| RF-19 | Alta | UC05 | Alterar responsável técnico; alteração auditada | L13, L21, L47 |
| RF-64 | Média | UC21 | Registrar ocorrência no diário; um diário por obra/dia (RN-07, garantida no banco, ADR-06); ocorrência rápida cria o diário do dia | L26, L28, L31–L34, L38 |
| RF-65 | Média | UC21 | Anexar fotos: sempre vinculadas a uma ocorrência (ADR-02); PNG/JPG até 10 MB (RNF-10) | L27, L29, L35, L39 |
| RF-66 | Média | UC21 | Registrar paralisação; não muda o status da obra automaticamente (ADR-08) | L27, L29, L32, L38 |
| RF-67 | Média | UC21 | Clima: **um por dia**, no registro diário (ADR-01) *(ver lacuna 3)* | L26, L28, L31, L37 |
| RF-68 | Média | UC21 | Histórico cronológico de ocorrências da obra; exclusão lógica e auditada | L30, L31, L34, L36, L37 |
| RF-73 | Alta | UC "Visualizar Painel da Obra" (a criar no L05) | Painel agregador: consome dados de outros módulos via service público ou port em `shared`; KPI sem fonte real aparece com `fonte: SIMULADO` (ADR-13) | L41–L46, L49, L50 |

Requisitos não funcionais que afetam o módulo:

| RNF | O que exige no módulo | Lotes |
|---|---|---|
| RNF-03 | Acesso à obra só com permissão explícita, checada no backend (`@PreAuthorize`, lista filtrada); o front só esconde (ADR-12) | L14, L44, L51, L52 |
| RNF-09 | Mensagens claras e consistentes | todos os lotes de UI |
| RNF-10 | Upload PNG/JPG até 10 MB | L35, L39 |

## 3. Fora do escopo do módulo

| Item | Onde fica | Por quê |
|---|---|---|
| UC03, UC06–UC20 | outros módulos | não geram lote neste conjunto |
| RF-70 (admin vê as obras de um usuário) | Usuários | ADR-12 |
| RF-20–RF-24 (cronograma e avanço físico) | Cronograma / Fase 5 | o painel mostra KPI simulado |
| RF-25–RF-29 (medições) | Fase 5 | modelo de medição ainda pendente (L01); escopo confirmado em 2026-11-23 |
| RF-60–RF-63 (revisões de projeto) | Documentos / Fase 5 | escopo confirmado em 2026-11-23 |
| Orçamento, estoque, compras, equipamentos, usuários | módulos próprios | — |

## 4. Decisões tomadas sobre as lacunas (2026-11-23)

| # | Lacuna | Decisão | Registro |
|---|---|---|---|
| 1 | O RF-70 tinha outro sentido no plano | RF-70 vai para o módulo **Usuários**; o acesso por obra passa a ser rastreado como **RNF-03** | [ADR-12](../adr/ADR-12-rf70-modulo-usuarios.md) |
| 2 | O RF-73 não tinha caso de uso | RF-73 é do módulo; o painel **agrega dados dos outros módulos** via ports; o UC será criado no L05 | [ADR-13](../adr/ADR-13-painel-agrega-modulos.md) |
| 3 | O RF-67 (clima) foi reinterpretado | **Manter o ADR-01** (um clima por dia) | [Confirmações](../adr/README.md) |
| 4 | O escopo do painel divergia do Plano de Projeto | **Manter**: medições, revisões e avanço físico fora desta entrega | [Confirmações](../adr/README.md) |
| 5 | O `PLANO.md` não foi publicado | Pendente: pedir ao Thierry e conferir esta matriz | — |

Os ADR-12 e ADR-13 estão como **Propostos** até o Thierry aprovar (regra do L01, seção 3).

## 5. Checklist do L02

- [x] Apenas UC04, UC05 e UC21 entram como UCs do módulo.
- [x] RF-14 a RF-19, RF-64 a RF-68 e RF-73 aparecem na matriz; RF-70 tratado no ADR-12.
- [x] Nenhum RF de outro módulo puxado para dentro (fora de escopo explícito na seção 3).
- [x] Cada RF aponta para pelo menos um lote.
- [x] RF-73 marcado como painel (ADR-13); acesso por obra rastreado como RNF-03 (ADR-12).
- [ ] Conferência com o Apêndice A do `PLANO.md` (depende do arquivo).
