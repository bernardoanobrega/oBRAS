# ADR-20 — ART do responsável técnico como texto na obra
Status: Proposto
Data: 2026-11-26 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: —

## Contexto
A tela de equipe (L25) precisa mostrar o responsável técnico com nome **e** o registro que o torna responsável pela obra: a **ART** (Anotação de Responsabilidade Técnica) do engenheiro ou do arquiteto.

Na prática, a ART é um documento ligado a um profissional **e** a uma obra. Modelar isso por completo pediria um vínculo usuário ↔ ART (ou um campo de registro profissional no usuário, como CREA/CAU), o que mexe no módulo Usuários (ADR-12) e não é necessário para esta entrega.

A tabela `obra` já tem a coluna `art VARCHAR(50)` desde a V2, e o `ObraDto` já a expõe.

## Decisão
- A ART fica **só como texto na obra** (`obra.art`), sem vínculo com o usuário responsável.
- A tela mostra o responsável técnico com **nome** (dentro ou fora da equipe) e a **ART da obra**.
- O nome do responsável vem do backend no `ObraDto` (`responsavelNome`), porque o front não pode ler usuários sem a permissão global do módulo Usuários. Esse campo ainda não existe: fica sinalizado para o L12/L13.
- Sem migration nova: a coluna já existe.

## Alternativas consideradas
1. **Registro profissional (CREA/CAU) no usuário:** mais fiel ao domínio, mas é requisito novo do módulo Usuários e exige migration lá.
2. **Entidade ART ligada ao usuário e à obra:** permite histórico de ARTs e troca de responsável com rastreio, mas é escopo grande para esta fase.
3. **Mostrar só o nome:** mais simples, mas perde a informação que dá a responsabilidade formal.

## Consequências
- **Migrations:** nenhuma.
- **API:** `ObraDto` precisa de `responsavelNome` (pendência para o Thierry, L12/L13). Ao trocar o responsável (L13), a ART **não** muda sozinha; quem troca deve atualizar a ART da obra.
- **UI:** L25 mostra nome + ART no cabeçalho da equipe. O formulário de obra (L17) ainda não edita a ART; decidir em qual tela ela é preenchida.
- **Evolução:** se o projeto precisar, vincular a ART ao usuário responsável (alternativa 1 ou 2) por um ADR novo que substitua este.
