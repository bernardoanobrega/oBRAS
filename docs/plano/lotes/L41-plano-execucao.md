# L41 — Plano de execução (serviço agregador do painel)

**Lote:** [L41](./L41-service-painel.md) · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-13 · **Responsável:** Thierry
**Branch:** `feat/painel-obra` · **HEAD após pull:** `54c4c69` · **Commit alvo:** `feat(painel): agregar dados da obra`

## 1. Estado atual

- `git pull --ff-only` executado: `feat/painel-obra` passou de `792c1d5` para `54c4c69`; 0 ahead / 0 behind.
- Pull trouxe L31–L36, ADR-19, ADR-20, módulos de orçamento/compras/estoque/documentos e migrations V5–V9.
- `obras-backend/src/main/java/br/com/obras/ObrasApplication.java` está modificado localmente: bean `Clock` com `ZoneId.of("America/Sao_Paulo")`. Remote não tocou nesse arquivo; a mudança sobreviveu ao fast-forward e entra no commit do L41.
- `L41-service-painel.md:4` e `DIVISAO-TAREFAS-THIERRY-CAUA.md:73` atualizados para `Concluído`.
- `docs/adr/README.md:16` e `ADR-13-painel-agrega-modulos.md:3` atualizados para `Aceito`; aprovação de Thierry e Cauã registrada.

## 2. Decisões fechadas

1. **Domínio:** RF-73 pertence ao módulo Detalhe de Obra (`ADR-13:13`, `00-METODO.md:9`). **Pacote Java (decisão revisada em 2026-11-27):** `PainelObraDto` em `br.com.obras.obra.dto`; `PainelObraService` em `br.com.obras.obra.service`; sem subpacote `obra.painel`. Continua distinto de `dashboard` (UC03/RF-03/RF-04). Registro: `docs/adr/README.md`, confirmação `L41 / pacote do painel`.
2. **Fonte de mão de obra:** `maoDeObra.fonte = "DIARIO"` sempre. Nunca `null`, sem campo novo no contrato.
3. **Sem diário hoje:** `clima` permanece objeto com `data=null` e `condicao=null`; `presentesHoje=0`, `vsOntem=null`, `alocados=equipe.size()`, `ocorrencias=[]`, `fotos=[]`.
4. **Ontem:** diário ativo imediatamente anterior, não D-1 literal. Lacuna D-1 → D-2 usa D-2 (`RegistroDiarioRepository` filtra `excluido=false` e ordena `data desc`).
5. **Ocorrências/fotos do painel:** somente as do diário de hoje; sem diário, listas vazias. `fotos` = `flatMap` das fotos das ocorrências ativas (`DiarioMapper` já distribui cada foto uma vez).
6. **KPIs:** seis campos numéricos `BigDecimal`, todos `null`, `fonte="SIMULADO"`. `OrcamentoPort` existe após o pull, mas é por item (`buscarItem`, `registrarCustoMaterial`), não fornece totais de obra; não altera esta decisão.
7. **Dependências do service:** somente `ObraService`, `DiarioService` e `Clock`. Nenhum `Repository` em `PainelObraService` ou `PainelObraDto`.
8. **ADR-13:** service público do módulo satisfaz `ADR-13:15`; port em `shared` só entra quando o módulo fornecedor externo existir com contrato próprio.
9. **Exclusão lógica:** semântica de L36 aceita com pré-gate; pendência de auditoria em L36 não muda filtros do painel.
10. **Dados simulados:** `null` + `fonte` explícita; nenhum número inventado.

## 3. Arquivos a criar

| Arquivo | Conteúdo |
|---|---|
| `obras-backend/src/main/java/br/com/obras/obra/dto/PainelObraDto.java` | Record raiz + records aninhados `ClimaDto`, `KpisDto`, `MaoDeObraDto`; factories `ausente()`, `simulados()`, `de(...)` |
| `obras-backend/src/main/java/br/com/obras/obra/service/PainelObraService.java` | `painel(Long obraId)`; agrega obra, equipe, diário de hoje, diário anterior, ocorrências e fotos |
| `obras-backend/src/test/java/br/com/obras/obra/service/PainelObraServiceTest.java` | 8 testes unitários com Mockito e `Clock.fixed(...)` |
| `obras-backend/src/test/java/br/com/obras/obra/service/PainelObraServiceIntegracaoTest.java` | 3 testes com Testcontainers e fixtures SQL via `JdbcTemplate` |

## 4. Arquivos a alterar

| Arquivo | Alteração |
|---|---|
| `obras-backend/src/main/java/br/com/obras/ObrasApplication.java` | Manter `@Bean Clock clock()` com `America/Sao_Paulo`; já presente localmente |
| `docs/plano/lotes/L41-service-painel.md` | Status `Concluído`; ADR-13; contrato detalhado; 11 nomes de teste; evidência; pré-gate |
| `docs/plano/lotes/DIVISAO-TAREFAS-THIERRY-CAUA.md:73` | Status `Concluído` |
| `docs/adr/README.md:16` | ADR-13 `Aceito (Thierry e Cauã, 2026-11-27)` |
| `docs/adr/ADR-13-painel-agrega-modulos.md:3-4` | `Status: Aceito`; aprovação `Thierry [x] Cauã [x]` |

Sem migration, sem controller neste lote. Endpoint `GET /api/obras/{obraId}/painel` fica no L42.

## 5. Contrato do DTO

```text
PainelObraDto(
  ObraDto obra,
  ClimaDto clima,                 // {data, condicao}; objeto presente mesmo sem diário
  KpisDto kpis,                   // 6 BigDecimal null + fonte "SIMULADO"
  List<EquipeMembroDto> equipe,
  MaoDeObraDto maoDeObra,         // {alocados, presentesHoje, vsOntem, fonte "DIARIO"}
  List<OcorrenciaDto> ocorrencias,
  List<FotoDto> fotos
)
```

- Ordem dos campos segue `L42-api-painel.md:18-31`.
- Nenhum import de `*.domain` em `PainelObraDto` ou `PainelObraService`.
- Tipos reutilizados: `ObraDto`, `EquipeMembroDto`, `DiarioDto`, `DiarioResumoDto`, `OcorrenciaDto`, `FotoDto`.

## 6. Contrato do service

```text
painel(obraId):
  obra        = ObraService.buscar(obraId)
  equipe      = ObraService.equipe(obraId)
  hoje        = LocalDate.now(clock)
  resumos     = DiarioService.listar(obraId)

  diarioHoje  = resumo com data == hoje  ->  DiarioService.buscar(obraId, id)

  sem diario:
    clima = ClimaDto.ausente()
    maoDeObra = (equipe.size(), 0, null, "DIARIO")
    ocorrencias = []
    fotos = []

  com diario:
    clima = (diario.data, diario.clima)
    presentesHoje = diario.equipePresente.size()
    anterior = max(resumos onde data < hoje)
    vsOntem = presentesHoje - anterior.equipePresente.size(), ou null
    ocorrencias = diario.ocorrencias
    fotos = flatMap(ocorrencias.fotos)
```

- `ObraService.buscar` já lança `RecursoNaoEncontradoException` para obra inexistente.
- `DiarioService.buscar` já filtra diário excluído e por obra.
- `DiarioService.listar` devolve resumos ordenados por data desc e sem excluídos.

## 7. Testes primeiro

### Unitários — `PainelObraServiceTest`

1. `painelIncluiObraEEquipeAtiva`
2. `painelSemDiarioHojeIndicaAusencia`
3. `painelComDiarioUsaClimaEPresentes`
4. `vsOntemComparaDiarioAnterior`
5. `kpisSemModuloTemFonteSimulado`
8. `painelObraInexistenteLancaRecursoNaoEncontrado`
9. `painelSemEquipePresenteNoDiarioNaoInventaMaoDeObra`
11. `painelDtoTemCamposObrigatoriosDoContrato`

Reforços: teste 3 cobre `flatMap` com duas fotos em uma ocorrência e nenhuma na outra; teste 4 cobre future, D-2 e D-3; teste 9 cobre `[]` em vez de `null`; teste 11 espelha o JSON de `L42:18-31`.

### Integração — `PainelObraServiceIntegracaoTest`

6. `painelNaoVazaOcorrenciaExcluida`
7. `painelNaoVazaFotoDeOutraObra`
10. `painelNaoVazaOcorrenciaDeOutraObra`

Fixtures via SQL: `usuario`, `auditavel`, `obra`, `alocacao_obra`, `registro_diario`, `ocorrencia`, `registro_fotografico`. IDs de `registro_diario`, `ocorrencia` e `registro_fotografico` são compartilhados com `auditavel` (V3/V4). Exclusão de ocorrência = `UPDATE auditavel SET excluido = true`.

`DiarioService` ganhou `PlatformTransactionManager` no construtor após o pull; `@DataJpaTest` fornece o bean. Teste unitário mocka `DiarioService`, sem impacto.

## 8. Ordem de execução

1. Escrever os 11 testes com service/DTO ausentes ou fixtures pendentes → RED.
2. Criar DTO em `br.com.obras.obra.dto` e service em `br.com.obras.obra.service`.
3. GREEN dos 8 unitários com `mvn test -Dtest=PainelObraServiceTest`.
4. Preencher as fixtures de integração; rodar os 3 com Testcontainers.
5. Pré-gate: `cd obras-backend && mvn test -Dtest='*Diario*'` verde antes do primeiro commit local.
6. Atualizar docs: L41, divisão de tarefas, índice de ADRs e ADR-13.
7. Suíte completa: `cd obras-backend && mvn test`.
8. Revisão cruzada com Cauã; commit único `feat(painel): agregar dados da obra`.

## 9. Evidências de aceite

- Nomes dos 11 testes idênticos aos do L41.
- Saída do `mvn test` completo sem falhas.
- `grep -rn "Repository" obras-backend/src/main/java/br/com/obras/obra/dto/PainelObraDto.java obras-backend/src/main/java/br/com/obras/obra/service/PainelObraService.java` vazio.
- Exemplo de JSON do DTO, incluindo `clima{data:null, condicao:null}` sem diário e `fonte` nunca `null`.
- APIs consumidas de L31–L36 listadas; ressalva de L33–L36 em revisão registrada.
- ADR-13 marcado `Aceito` no índice e no arquivo.

## 10. Riscos e ressalvas

- L33–L36 seguem `Em revisão` na divisão de tarefas; aceitos com ressalva mediante pré-gate dos testes de diário.
- Bean `Clock` entra no contexto de todos os testes; suíte completa é obrigatória.
- `ADR-18`, `ADR-19` e `ADR-20` são novos no pull; não alteram o contrato do painel.
- `OrcamentoPort` e `MaterialPort` novos não entram no L41; KPI permanece `SIMULADO`.
- `L42` deve conferir serialização Jackson; não configurar `non_null`, ou `kpis`/`vsOntem` desapareceriam do JSON.
