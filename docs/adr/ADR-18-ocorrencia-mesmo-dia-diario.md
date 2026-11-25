# ADR-18 — Ocorrência sempre no mesmo dia do diário
Status: Proposto
Data: 2026-11-25 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: —

## Contexto
A ocorrência pertence ao diário de um dia (`registro_diario.data`), mas a `dataHora` dela podia ser de qualquer dia: era possível registrar, no diário de 18/11, uma ocorrência de 25/11. Isso quebra o histórico cronológico de ocorrências da obra (RF-68) e o mural do painel.

## Decisão
- A data da `dataHora` da ocorrência **tem que ser igual** à `data` do diário. Caso contrário: 422, "A ocorrência deve ser do mesmo dia do diário".
- A regra fica no **domínio**, em `Ocorrencia.criar` e `Ocorrencia.editar`, e vale para todo caminho que cria ou edita ocorrência: o normal (L32) e a ocorrência rápida (L33).
- **Sem padrão especial** para a `dataHora` ausente: continua valendo "agora". Para o diário de hoje, isso funciona. Para o diário de um dia passado, "agora" é de outro dia e é recusado, então quem registra atrasado **informa o horário real** da ocorrência.
- Na edição, `dataHora` ausente mantém a atual, que já é do dia certo.

## Alternativas consideradas
- **Padrão "dia do diário + horário atual"**: rejeitada; gravaria um horário inventado num registro atrasado, e o horário da ocorrência precisa ser o real.
- **Validar só no service**: rejeitada; a regra é do domínio e precisa valer em todos os caminhos de criação e edição.
- **Constraint no banco**: não adotada por ora; exigiria migration nova (V5, ainda sem commit) e um trigger comparando duas tabelas. Pode entrar junto com a V5 se for decidido.

## Consequências (migrations / API / UI / lotes afetados)
- **Migrations:** nenhuma.
- **API:** criar ou editar ocorrência com uma data diferente da do diário passa a dar 422. Ocorrência sem `dataHora` em diário de um dia passado também dá 422.
- **UI:** o formulário de ocorrência (L38) e a ocorrência rápida (L39) precisam enviar a `dataHora` quando o diário não for de hoje, e mostrar a mensagem de erro.
- **Lotes:** L29 (domínio), L32 e L33 (service), L38 e L39 (front).
