# ADR-16 — Conversão entidade → DTO em classe mapper
Status: Proposto
Data: 2026-11-25 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: — (altera a convenção "Saída: `XDto` com `static XDto de(Entidade)`")

## Contexto
Até o L30, cada DTO de saída tinha um método `static de(Entidade)`. No diário, a conversão passou a precisar de dados que não estão na entidade: as fotos ativas, buscadas por consulta para respeitar a exclusão lógica e evitar N+1, e os nomes da equipe e do autor (ADR-17). Com isso, os `de()` ganhavam parâmetros e os DTOs deixavam de ser só dados.

## Decisão
- Cada módulo tem um **mapper escrito à mão**, um `@Component` em `br.com.obras.<modulo>.mapper`, por exemplo `diario.mapper.DiarioMapper`. Ele faz toda a conversão **entidade → DTO de saída**. Os DTOs voltam a ser `record`s sem lógica.
- O mapper **só converte**: não acessa repository, não valida e não decide o que entra. O service escolhe os dados (por exemplo, só as fotos ativas) e os entrega ao mapper.
- A conversão **DTO de entrada → entidade** continua no service, chamando as fábricas do domínio (`criar(...)`, `editar(...)`), porque criar uma entidade envolve regra de negócio e carregamento de outras entidades, como a obra e as alocações.
- O diário foi o primeiro módulo migrado (L30). O módulo `obra` (`ObraDto`, `EquipeMembroDto`) e o `usuario` (`UsuarioDto`) ainda usam `de()` e migram quando forem tocados, a começar pelo L10.

## Alternativas consideradas
- **Manter `static de()` no DTO**: rejeitada; mistura dados e lógica e não recebe bem dados vindos de fora da entidade.
- **MapStruct (gerado)**: rejeitada por ora; é uma dependência nova com processador de anotações, que precisa conviver com o Lombok, para poucos DTOs. Além disso, o código gerado é menos explicável na revisão.
- **Mapper também DTO → entidade**: rejeitada; esconderia regra de negócio no mapper ou o obrigaria a usar repositories.

## Consequências (migrations / API / UI / lotes afetados)
- **Migrations:** nenhuma.
- **API:** nenhuma por este ADR; o formato dos DTOs muda pelo ADR-17.
- **Testes:** cada mapper ganha um teste unitário próprio (`DiarioMapperTest`). Nos testes de service, o mapper entra como `@Spy` real.
- **Lotes:** L30 (feito no diário). L10 e o módulo `usuario` adotam o padrão quando forem implementados. A convenção do `CLAUDE.local.md` deve ser atualizada após a aprovação.
