# Divisão de tarefas — Thierry e Cauã

**Fonte:** [`README.md`](README.md) e [`00-METODO.md`](00-METODO.md)  
**Escopo:** módulo Detalhe de Obra.  
**Regra:** cada lote é uma task e deve virar um commit revisável.  
**Balanceamento:** 27 tasks por pessoa; proxy de esforço = quantidade de testes listados no lote.  
**Próxima fatia recomendada:** L43 + L44 juntos, com mock e cortes de L46 antecipados. Ver [`CONTEXTO-PROXIMA-SESSAO.md`](CONTEXTO-PROXIMA-SESSAO.md).

## Balanceamento

| Pessoa | Tasks | Testes previstos | Fases |
|---|---:|---:|---|
| Thierry | 27 | 196 | F0: 3 · F1: 11 · F2: 7 · F4: 4 · F6: 2 |
| Cauã | 27 | 195 | F0: 2 · F1: 1 · F2: 1 · F3: 15 · F4: 6 · F6: 2 |
| **Total** | **54** | **391** | diferença de 1 teste |

> O número de testes é proxy de esforço, não estimativa de horas. A revisão cruzada não entra na contagem.

## Regras de trabalho

1. O dono executa contrato, testes RED, implementação, verificação e evidência.
2. A outra pessoa revisa antes do commit.
3. Não iniciar task com dependência aberta.
4. Handoff explícito: quem entrega informa lote concluído, comando executado e evidência.
5. Se o lote não couber em um commit revisável, dividir antes de começar; não aumentar escopo.
6. Status possíveis: `Pendente` (sem código), `Em andamento` (iniciado ou parcial), `Em revisão` (feito por um, mas o lote é do outro: falta o dono auditar), `Concluído` (feito pelo dono, DoD cumprido). **Quem mexer em um lote, pessoa ou agente, atualiza o status ao começar e ao terminar, sempre nos dois lugares:** a linha `**Status:**` do arquivo do lote e a coluna Status desta tabela. Só o dono marca `Concluído`.
7. Backend: `cd obras-backend && mvn test`. Frontend: `cd obras-frontend && npm test && npm run build`.

## Tabela mestra

| Task | Lote | Fase | Dono | Depende de | Testes | Status |
|---|---|---|---:|---:|---:|---|
| L01 | Premissas e ADRs fechados | 0 | Thierry | — | 0 | Em andamento |
| L02 | UCs do Detalhe de Obra e matriz RF→UC | 0 | Cauã | L01 | 0 | Concluído |
| L03 | Diagramas de classe do Detalhe de Obra | 0 | Thierry | L01, L02 | 0 | Em andamento |
| L04 | Diagramas de estado e atividade do Detalhe de Obra | 0 | Thierry | L03 | 0 | Em andamento |
| L05 | Casos de uso do Detalhe de Obra e rastreabilidade | 0 | Cauã | L02, L04 | 0 | Pendente |
| L06 | Migration V3: auditável e registro de auditoria | 1 | Thierry | L01–L05 | 7 | Em andamento |
| L07 | Migration V3: obra auditável e responsável opcional | 1 | Thierry | L06 | 5 | Em andamento |
| L08 | Domínio Obra: entidade, enums e guardas | 1 | Thierry | L07 | 8 | Em andamento |
| L09 | Domínio Obra: transições e auditoria | 1 | Thierry | L08 | 13 | Pendente |
| L10 | Repository, DTO e mapper de Obra | 1 | Thierry | L08, L09 | 8 | Em andamento |
| L11 | Serviço CRUD de Obra | 1 | Thierry | L10 | 11 | Em andamento |
| L12 | API CRUD de Obra | 1 | Thierry | L11 | 8 | Em andamento |
| L13 | API de transições e responsável técnico | 1 | Thierry | L12 | 8 | Pendente |
| L14 | Autorização e filtro de lista de obras | 1 | Thierry | L12, L13 | 9 | Pendente |
| L15 | CRUD de canteiro | 1 | Thierry | L14 | 7 | Pendente |
| L16 | Testes de integração da Fase 1 | 1 | Thierry | L06–L15 | 8 | Pendente |
| L17 | Frontend: lista e cadastro de obras | 1 | Cauã | L12–L16 | 7 | Em andamento |
| L18 | Domínio AlocacaoObra e cargos | 2 | Thierry | L16 | 8 | Em andamento |
| L19 | Repository, DTO e mapper de alocação | 2 | Thierry | L18 | 10 | Em andamento |
| L20 | Serviço: alocar usuário com cargos | 2 | Thierry | L19 | 8 | Pendente |
| L21 | Serviço: encerrar alocação e responsável técnico | 2 | Thierry | L20 | 10 | Pendente |
| L22 | API de alocações da equipe | 2 | Thierry | L21 | 8 | Pendente |
| L23 | Cascata de encerramento ao concluir/cancelar | 2 | Thierry | L21, L22 | 6 | Pendente |
| L24 | Testes de integração da Fase 2 | 2 | Thierry | L18–L23 | 8 | Pendente |
| L25 | Frontend: tela de equipe da obra | 2 | Cauã | L22–L24 | 8 | Em andamento |
| L26 | Migration V4: diário, equipe presente e RN-07 | 3 | Cauã | L06, L07 | 10 | Concluído |
| L27 | Migration V4: ocorrências, paralisações e fotos | 3 | Cauã | L26 | 11 | Concluído |
| L28 | Domínio RegistroDiario | 3 | Cauã | L26 | 7 | Concluído |
| L29 | Domínio Ocorrencia, Paralisacao e RegistroFotografico | 3 | Cauã | L28 | 10 | Concluído |
| L30 | Repository, DTO e mapper do diário | 3 | Cauã | L28, L29 | 9 | Concluído |
| L31 | Serviço de diário e RN-07 | 3 | Cauã | L30 | 12 | Concluído |
| L32 | Serviço de ocorrências e paralisações | 3 | Cauã | L31 | 12 | Concluído |
| L33 | Ocorrência rápida e diário automático | 3 | Cauã | L32 | 9 | Em revisão |
| L34 | API de diário e ocorrências | 3 | Cauã | L31–L33 | 7 | Em revisão |
| L35 | Upload e download de fotos | 3 | Cauã | L34 | 10 | Em revisão |
| L36 | Exclusão lógica e auditoria do diário | 3 | Cauã | L31–L35 | 7 | Em revisão |
| L37 | Frontend: DiarioPage mobile-first | 3 | Cauã | L34, L36 | 7 | Em andamento |
| L38 | Frontend: ocorrências e paralisações | 3 | Cauã | L37 | 7 | Pendente |
| L39 | Frontend: ocorrência rápida, upload e mural | 3 | Cauã | L38 | 8 | Pendente |
| L40 | Testes da Fase 3 | 3 | Cauã | L26–L39 | 8 | Pendente |
| L41 | Serviço agregador do painel | 4 | Thierry | L31–L36 | 11 | Pendente |
| L42 | API do painel e contrato | 4 | Thierry | L41 | 7 | Pendente |
| L43 | Frontend: mover DetalheObra para a feature | 4 | Cauã | L42 | 4 | Em revisão |
| L44 | Frontend: integrar shell, router, http e permissão | 4 | Cauã | L43 | 6 | Em revisão |
| L45 | Frontend: substituir mock por dados reais | 4 | Cauã | L44 | 8 | Em andamento |
| L46 | Frontend: aplicar cortes de escopo do mockup | 4 | Cauã | L45 | 8 | Em revisão |
| L47 | Frontend: ações de status e edição de parâmetros | 4 | Thierry | L46 | 9 | Pendente |
| L48 | Frontend: equipe, ocorrências e mural no painel | 4 | Thierry | L47 | 6 | Pendente |
| L49 | Frontend: KPIs simulados com fonte | 4 | Cauã | L48 | 5 | Em revisão |
| L50 | Testes de contrato e responsividade da Fase 4 | 4 | Cauã | L41–L49 | 7 | Pendente |
| L51 | Permissões finas por ação | 6 | Thierry | L14, L22, L34, L42 | 6 | Pendente |
| L52 | E2E e usabilidade do fluxo de obra | 6 | Cauã | L50, L51 | 8 | Pendente |
| L53 | Acessibilidade e responsividade | 6 | Thierry | L52 | 7 | Pendente |
| L54 | Documentação final do módulo | 6 | Cauã | L53 | 0 | Pendente |

## Ordem de execução e paralelismo

1. **F0 em cadeia:** Thierry `L01` → Cauã `L02` → Thierry `L03` → Thierry `L04` → Cauã `L05`.
2. **Base backend + diário:** após Thierry `L07`, Cauã inicia `L26`–`L36`; Thierry segue `L08`–`L24`.
3. **Frontend F1/F2:** Cauã faz `L17` após `L16` e `L25` após `L24`.
4. **Frontend F3:** Cauã faz `L37`–`L40` após `L36`.
5. **Painel backend:** Thierry faz `L41`–`L42` após Cauã concluir `L36`.
6. **Painel frontend inicial:** Cauã faz `L43`–`L46` após Thierry `L42`.
7. **Ações e equipe no painel:** Thierry faz `L47`–`L48` após Cauã `L46`.
8. **KPIs e contrato:** Cauã faz `L49`–`L50` após Thierry `L48`.
9. **Permissões:** Thierry faz `L51` após `L42` e `L34`; pode rodar em paralelo com `L49`–`L50`.
10. **Fechamento:** Cauã `L52` após `L50` e `L51` → Thierry `L53` → Cauã `L54`.

## Handoffs críticos

- `L01` → `L02` → `L03` → `L04` → `L05`.
- `L06`/`L07` → `L26`; Cauã não começa F3 antes das migrations de Thierry.
- `L16` → `L17`; `L24` → `L25`.
- `L31`–`L36` → `L41`–`L42`; Thierry depende do diário de Cauã.
- `L42` → `L43`; Cauã depende do contrato de painel.
- `L46` → `L47`; `L48` → `L49`; `L49` → `L50`.
- `L42`/`L34` → `L51`; `L50`/`L51` → `L52`; `L52` → `L53`; `L53` → `L54`.

## Checklist por pessoa

### Thierry

- [ ] F0: `L01`, `L03`, `L04`
- [ ] F1: `L06`–`L16`
- [ ] F2: `L18`–`L24`
- [ ] F4: `L41`, `L42`, `L47`, `L48`
- [ ] F6: `L51`, `L53`

### Cauã

- [ ] F0: `L02`, `L05`
- [ ] F1: `L17`
- [ ] F2: `L25`
- [ ] F3: `L26`–`L40`
- [ ] F4: `L43`–`L46`, `L49`, `L50`
- [ ] F6: `L52`, `L54`

## Definition of Done por task

- [ ] Contrato DB/API/UI escrito no lote.
- [ ] Testes RED criados antes da implementação.
- [ ] Testes verdes, incluindo negativos.
- [ ] Comando de verificação executado e registrado.
- [ ] Evidência anexada ao lote/PR.
- [ ] Revisão cruzada aprovada.
- [ ] Commit único no padrão do repositório.
- [ ] Status atualizado (`Concluído` ou `Em revisão`) na linha `**Status:**` do lote e na tabela mestra.

## Riscos e mitigação

| Risco | Impacto | Mitigação |
|---|---|---|
| Cauã concentra F3 inteira | Thierry fica bloqueado em `L41`–`L42` se F3 atrasar | Priorizar `L26`–`L36` antes de `L37`–`L40` |
| F4 tem handoff nos dois sentidos | Espera entre `L46`/`L47` e `L48`/`L49` | Revisão cruzada e handoff no mesmo dia |
| F6 é sequencial | Um dev fica ocioso no fim | `L51` roda em paralelo com `L49`–`L50`; revisão cruzada ocupa a outra pessoa |
| Lote cresce além de um commit | Perda de revisabilidade | Dividir antes de começar; criar novo `Lxx` |
| Permissão testada só no frontend | Falha de segurança | `@PreAuthorize` e testes 403 no backend em `L51` |
