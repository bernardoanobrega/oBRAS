# ADR-12 — RF-70 pertence ao módulo Usuários; acesso por obra é RNF-03

Status: Proposto
Data: 2026-11-23 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: — (corrige referências dos lotes; não altera nenhum ADR-01 a 11)

## Contexto
O plano de lotes usa o RF-70 como "permissão por obra / filtro de acesso" (L14, L44, L51, L52).
No Documento de Requisitos, porém, o RF-70 (prioridade Média) diz que **o Administrador visualiza as obras às quais um usuário está vinculado**. É uma consulta da gestão de usuários.
A regra "só acessa a obra quem tem permissão explícita" é o **RNF-03**.

## Decisão
1. O **RF-70 sai do módulo Detalhe de Obra** e passa a ser responsabilidade do **módulo Usuários** (tela do usuário mostrando suas obras/alocações).
2. O controle de acesso por obra (`@PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', ...)")`, lista filtrada, `usePermissao`) passa a ser rastreado como **RNF-03**.

## Alternativas consideradas
- Manter o RF-70 no módulo com o sentido de permissão: rejeitada, porque contradiz o Documento de Requisitos, que é a fonte canônica (ADR-03).
- Implementar o RF-70 no Detalhe de Obra: rejeitada, porque a consulta parte do usuário, não da obra.

## Consequências
- **Lotes:** em L14, L44, L51 e L52, trocar a referência "RF-70" por "RNF-03". O escopo técnico desses lotes **não muda**.
- **Matriz L02:** RF-70 movido para "fora de escopo" (módulo Usuários).
- **Módulo Usuários:** se precisar de dados de alocação para o RF-70, deve consumir um service público ou port do módulo `obra` (a regra de isolamento continua valendo). O port `ContextoAlocacaoPort` já existe e pode servir de base.
- **Migrations/API/UI do módulo:** sem impacto.
