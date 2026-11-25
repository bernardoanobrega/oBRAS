# ADR-13 — Painel da obra (RF-73) agrega dados de outros módulos via ports

Status: Proposto
Data: 2026-11-23 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: — (detalha o L41/L42; compatível com ADR-10)

## Contexto
O RF-73 pede um painel por obra que resuma ocorrências, mão de obra, orçamento, fotos, avanço físico e equipe.
Parte desses dados pertence a outros módulos (cronograma/avanço físico, orçamento), que ainda estão em desenvolvimento em outras branches.
Nenhum caso de uso do documento de Casos de Uso cobre o painel.

## Decisão
1. **O RF-73 pertence ao módulo Detalhe de Obra.** O painel inicial da obra é deste módulo.
2. **O painel é um agregador.** Ele **consome** os dados dos outros módulos e não duplica regra nem tabela deles.
3. **O consumo respeita o isolamento:** o serviço do painel (L41) nunca acessa o repository de outro módulo. Ele usa o service público do módulo ou um **port em `shared`** (mesmo padrão de `AutorizacaoPort`), implementado pelo módulo dono do dado.
4. **Enquanto o módulo fornecedor não existir**, o painel mostra o KPI com `fonte: SIMULADO` (como já faz hoje). Quando o port for implementado, o KPI troca para dado real sem mudar o contrato da tela.
5. **Caso de uso:** o L05 cria o UC "Visualizar Painel da Obra" para dar rastreabilidade ao RF-73. *(Proposta: confirmar no L05.)*

## Alternativas consideradas
- Painel lendo direto as tabelas/repositories dos outros módulos: rejeitada, porque acopla os módulos e viola a regra do README do backend.
- Esperar todos os módulos ficarem prontos para fazer o painel: rejeitada, porque bloqueia a entrega. O KPI simulado resolve isso.
- O front chamar várias APIs e montar o painel sozinho (o que acontece hoje): aceitável como transição, mas o alvo é o endpoint agregador `/painel` (L42), que dá um contrato único e checa a permissão uma vez no backend.

## Consequências
- **API:** `GET /api/obras/{obraId}/painel` (L42), com cada KPI trazendo seu campo `fonte` (`REAL` | `SIMULADO`).
- **Backend:** criar ports em `shared` para os dados externos (ex.: avanço físico e resumo orçamentário). Os módulos Cronograma e Orçamento implementam esses ports quando estiverem prontos. Alinhar com os donos desses módulos.
- **Frontend:** L45 troca as chamadas separadas pela chamada única ao `/painel`. A badge `SIMULADO` continua até a fonte ser real.
- **Lotes afetados:** L41, L42, L45, L49, L05.
