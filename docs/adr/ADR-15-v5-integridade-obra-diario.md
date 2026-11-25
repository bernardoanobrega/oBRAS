# ADR-15 — V5: integridade entre obras no diário e exclusão física em cascata
Status: Proposto
Data: 2026-11-24 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: — (abre exceção à regra "Não criar V5 neste conjunto" do `00-METODO.md`, seção 6)

## Contexto
A auditoria dos lotes L26 e L27 (migration V4) achou duas lacunas no banco:

1. **Isolamento entre obras (RNF-03).** Nada no banco impede que um diário da obra A receba, na equipe presente (`registro_diario_equipe`) ou como autor de ocorrência (`ocorrencia.alocacao_obra_id`), uma alocação da obra B. Hoje só o `DiarioService` garante isso (`alocacoesAtivasPorIds(obraId, ...)` e `alocacaoAtiva(obraId, ...)`).
2. **Exclusão física inconsistente.** Apagar uma obra fisicamente apagava os diários (`ON DELETE CASCADE`), mas era bloqueado quando havia ocorrências, porque `ocorrencia → registro_diario` e `registro_fotografico → ocorrencia` não cascateavam.

A V4 já foi aplicada e é forward-only, então a correção exige uma migration nova.

## Decisão
Criar `V5__integridade_obra_diario.sql` com:

- **Trigger `BEFORE INSERT OR UPDATE`** em `registro_diario_equipe` e em `ocorrencia`, que compara o `obra_id` do diário com o da alocação e lança `check_violation` (`ck_alocacao_mesma_obra_diario`) se forem diferentes. Só compara se os dois registros existem; vínculo inexistente continua sendo barrado pela FK. A regra é "mesma obra", não "alocação ativa": uma alocação encerrada continua válida nos diários antigos.
- **`ON DELETE CASCADE`** em `fk_ocorrencia_registro_diario` e `fk_reg_fotografico_ocorrencia`. Apagar uma obra fisicamente apaga diários, equipe presente, ocorrências, paralisações e fotos dela.

A V5 fica **sem commit** até juntar as próximas mudanças de banco necessárias.

## Alternativas consideradas
- **FK composta** (`obra_id` em `ocorrencia` e em `registro_diario_equipe`, com FKs `(id, obra_id)`): garantia estrutural e um pouco mais rápida, mas exige colunas e índices novos e obriga a transformar o `@ManyToMany` da equipe presente em entidade própria, reabrindo os lotes L28 e L29. Rejeitada pelo custo.
- **Manter só no service**: rejeitada; isolamento entre obras é regra crítica e deve valer também no banco.
- **Manter exclusão física bloqueada**: rejeitada; o comportamento parcial (apaga diário sem ocorrência, bloqueia com ocorrência) é inconsistente.

## Consequências (migrations / API / UI / lotes afetados)
- **Migrations:** nova V5. O teste `migrationV4AplicaSobreV31ComDados` passa a migrar até a versão 4 explicitamente.
- **API/Java:** nenhuma mudança. O service já valida antes; o trigger é a última barreira (se disparar, chega como `DataIntegrityViolationException`).
- **UI:** nenhuma.
- **Limites:** na exclusão física, os registros de `auditavel` e `registro_auditoria` ficam órfãos (o histórico sobrevive) e os arquivos das fotos continuam no storage. Mudar `obra_id` de um diário ou de uma alocação já existente não é verificado pelo trigger (o sistema não faz essa operação).
- **Lotes:** L26 e L27 (auditoria); testes em `V5IntegridadeObraMigrationTest`.
