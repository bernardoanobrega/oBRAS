# Lotes de implementação — Módulo Detalhe de Obra

**Plano canônico:** `../PLANO.md`  
**Método:** [`00-METODO.md`](00-METODO.md)  
**Total:** 54 lotes commit-sized.  
**Escopo:** somente o módulo Detalhe de Obra. Fase 5 do `PLANO.md` (medições, revisões de projeto e avanço físico) está fora.

Cada lote segue o método em `00-METODO.md`: contrato antes do código, testes RED, implementação em fatia vertical, invariantes em camadas, permissão no backend, migration forward-only e DoD com evidência.

## Status dos lotes

O status de cada lote fica na linha `**Status:**` do próprio arquivo e na tabela de [`DIVISAO-TAREFAS-THIERRY-CAUA.md`](DIVISAO-TAREFAS-THIERRY-CAUA.md). `Concluído` = feito pelo dono; `Em revisão` = feito por um, mas é do outro (falta o dono auditar); `Em andamento` = parcial; `Pendente` = sem código.

## Índice

| Lote | Fase | Entrega | Commit sugerido |
|---|---|---|---|
| [L01](L01-premissas-adrs.md) | 0 | Premissas e ADRs fechados | `docs(plano): fechar premissas e adrs` |
| [L02](L02-ucs-matriz.md) | 0 | UCs e RFs do módulo + matriz | `docs(requisitos): mapear ucs do detalhe de obra` |
| [L03](L03-diagramas-classe.md) | 0 | Diagramas de classe do módulo | `docs(diagramas): atualizar classes do detalhe` |
| [L04](L04-diagramas-estado-atividade.md) | 0 | Diagramas de estado e atividade | `docs(diagramas): atualizar estados e atividades` |
| [L05](L05-casos-uso-rastreabilidade.md) | 0 | Casos de uso do módulo | `docs(requisitos): remapear casos de uso do detalhe` |
| [L06](L06-migration-v3-auditoria.md) | 1 | V3: auditável + auditoria | `feat(auditoria): criar base de auditoria` |
| [L07](L07-migration-v3-obra.md) | 1 | V3: obra auditável + responsável opcional | `feat(obra): tornar obra auditavel` |
| [L08](L08-domain-obra.md) | 1 | Domínio Obra + estados | `feat(obra): modelar dominio e estados` |
| [L09](L09-domain-obra-transicoes.md) | 1 | Transições + auditoria | `feat(obra): implementar transicoes auditadas` |
| [L10](L10-repository-dto-obra.md) | 1 | Repository/DTO/mapper Obra | `feat(obra): persistir e expor obra` |
| [L11](L11-service-obra-crud.md) | 1 | Serviço CRUD Obra | `feat(obra): implementar servico de obra` |
| [L12](L12-api-obra-crud.md) | 1 | API CRUD Obra | `feat(obra): expor crud de obra` |
| [L13](L13-api-obra-transicoes.md) | 1 | API transições/responsável | `feat(obra): expor transicoes e responsavel` |
| [L14](L14-autorizacao-filtro.md) | 1 | Autorização + filtro lista | `feat(obra): autorizar e filtrar obras` |
| [L15](L15-canteiro-crud.md) | 1 | CRUD canteiro (abandonado) | — |
| [L16](L16-testes-fase1.md) | 1 | Testes backend Fase 1 | `test(obra): cobrir fase 1` |
| [L17](L17-frontend-obras.md) | 1 | Lista/cadastro de obras | `feat(obras): integrar lista e cadastro` |
| [L18](L18-domain-alocacao.md) | 2 | Domínio AlocacaoObra | `feat(equipe): modelar alocacao` |
| [L19](L19-repository-dto-alocacao.md) | 2 | Repository/DTO alocação | `feat(equipe): persistir alocacao` |
| [L20](L20-service-alocar.md) | 2 | Alocar + cargos + unicidade | `feat(equipe): alocar usuario com cargos` |
| [L21](L21-service-encerrar-responsavel.md) | 2 | Encerrar + responsável técnico | `feat(equipe): encerrar alocacao e definir responsavel` |
| [L22](L22-api-alocacoes.md) | 2 | API alocações | `feat(equipe): expor alocacoes` |
| [L23](L23-cascata-conclusao.md) | 2 | Cascata conclusão/cancelamento | `feat(equipe): encerrar alocacoes em cascata` |
| [L24](L24-testes-fase2.md) | 2 | Testes backend Fase 2 | `test(equipe): cobrir fase 2` |
| [L25](L25-frontend-equipe.md) | 2 | Tela EquipeObraPage | `feat(equipe): integrar tela de equipe` |
| [L26](L26-migration-v4-diario.md) | 3 | V4: diário + RN-07 | `feat(diario): criar schema do diario` |
| [L27](L27-migration-v4-ocorrencias.md) | 3 | V4: ocorrência/paralisia/foto | `feat(diario): criar schema de ocorrencias e fotos` |
| [L28](L28-domain-diario.md) | 3 | Domínio RegistroDiario | `feat(diario): modelar registro diario` |
| [L29](L29-domain-ocorrencias.md) | 3 | Domínio Ocorrencia/Paralisacao/Foto | `feat(diario): modelar ocorrencias e fotos` |
| [L30](L30-repository-dto-diario.md) | 3 | Repository/DTO diário | `feat(diario): persistir diario` |
| [L31](L31-service-diario.md) | 3 | Serviço diário + RN-07 | `feat(diario): implementar servico de diario` |
| [L32](L32-service-ocorrencias.md) | 3 | Serviço ocorrências | `feat(diario): implementar servico de ocorrencias` |
| [L33](L33-service-ocorrencia-rapida.md) | 3 | Ocorrência rápida + diário automático | `feat(diario): implementar ocorrencia rapida` |
| [L34](L34-api-diario.md) | 3 | API diário/ocorrências | `feat(diario): expor diario e ocorrencias` |
| [L35](L35-upload-foto.md) | 3 | Upload/download de foto | `feat(diario): implementar upload de fotos` |
| [L36](L36-exclusao-auditoria.md) | 3 | Exclusão lógica + auditoria | `feat(diario): auditar exclusoes` |
| [L37](L37-frontend-diario.md) | 3 | DiarioPage mobile-first | `feat(diario): integrar tela de diario` |
| [L38](L38-frontend-ocorrencias.md) | 3 | Ocorrências/paralisia UI | `feat(diario): integrar ocorrencias` |
| [L39](L39-frontend-fotos.md) | 3 | Ocorrência rápida + mural | `feat(diario): integrar fotos e mural` |
| [L40](L40-testes-fase3.md) | 3 | Testes Fase 3 | `test(diario): cobrir fase 3` |
| [L41](L41-service-painel.md) | 4 | Serviço agregador painel | `feat(painel): agregar dados da obra` |
| [L42](L42-api-painel.md) | 4 | API /painel + contrato | `feat(painel): expor endpoint agregador` |
| [L43](L43-frontend-mover-detalhe.md) | 4 | Mover DetalheObra para feature | `refactor(painel): mover detalhe de obra` |
| [L44](L44-frontend-integrar-shell.md) | 4 | AppShell/router/http/permissão | `feat(painel): integrar detalhe ao app` |
| [L45](L45-frontend-dados-reais.md) | 4 | Substituir mock por API | `feat(painel): consumir dados reais` |
| [L46](L46-frontend-ajustes-adr.md) | 4 | Ajustes ADR-01/02/07/10 | `feat(painel): aplicar cortes de escopo` |
| [L47](L47-frontend-acoes.md) | 4 | Ações de status e edição | `feat(painel): implementar acoes da obra` |
| [L48](L48-frontend-equipe-ocorrencias.md) | 4 | Equipe, ocorrências e mural | `feat(painel): integrar equipe e ocorrencias` |
| [L49](L49-frontend-kpis.md) | 4 | KPIs simulados com fonte | `feat(painel): exibir kpis simulados` |
| [L50](L50-testes-fase4.md) | 4 | Contrato + responsividade | `test(painel): cobrir contrato e responsividade` |
| [L51](L51-permissoes-finas.md) | 6 | Permissões finas | `feat(seguranca): refinar permissoes por acao` |
| [L52](L52-e2e-usabilidade.md) | 6 | E2E + usabilidade | `test(e2e): cobrir fluxo do detalhe de obra` |
| [L53](L53-acessibilidade.md) | 6 | Acessibilidade/responsividade | `feat(ui): revisar acessibilidade e responsividade` |
| [L54](L54-documentacao-final.md) | 6 | Documentação final | `docs(obra): atualizar documentacao final` |

## Ordem de execução

Executar em ordem numérica. Dependências explícitas estão no cabeçalho de cada lote. Não iniciar lote com dependência aberta.

## Fora de escopo

- Medições (RF-25 a RF-29), revisões de projeto (RF-60 a RF-63) e avanço físico (RF-22 a RF-24).
- Orçamento, cronograma, documentos, estoque, compras, equipamentos e usuários.
- Painel usa KPI simulado com `fonte: SIMULADO` até esses módulos existirem.

## Regra de ouro

Se o lote não couber em um commit revisável, dividir antes de começar. Se precisar de mais de um comportamento observável, criar novo `Lxx`.
