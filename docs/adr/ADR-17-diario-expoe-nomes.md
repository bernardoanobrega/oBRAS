# ADR-17 — DTOs do diário expõem nome da equipe presente e do autor
Status: Proposto
Data: 2026-11-25 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: —

## Contexto
O contrato do L30 pede "saída com ids e nomes", mas o `DiarioDto` devolvia só `equipePresenteIds` e o `OcorrenciaDto` só `autorUsuarioId`. A tela do diário e o mural do painel precisam mostrar **quem** estava presente e **quem** registrou cada ocorrência, e o frontend não tem como transformar esses ids em nomes sem chamadas extras.

## Decisão
- Novo DTO `MembroDiarioDto(alocacaoId, usuarioId, nome)`, que representa uma pessoa da obra citada no diário.
- `DiarioDto.equipePresente: List<MembroDiarioDto>`, em ordem alfabética pelo nome. **Substitui** `equipePresenteIds`.
- `OcorrenciaDto.autor: MembroDiarioDto`. **Substitui** `autorUsuarioId`.
- Os DTOs de **entrada** não mudam: `equipePresenteIds` continua sendo a lista de ids de alocação enviada pelo cliente.
- O nome vem de `AlocacaoObra → Usuario`, um relacionamento JPA que já existe. O repository do módulo `usuario` não é usado, então a regra de isolamento entre módulos é respeitada.
- As consultas carregam usuário e alocação na mesma ida ao banco (`join fetch`), sem N+1.

## Alternativas consideradas
- **Manter os ids e acrescentar campos com nome**: rejeitada; duplicaria dados para preservar um front que vai ser ajustado de qualquer forma.
- **Front resolve os nomes chamando `/equipe`**: rejeitada; exige uma chamada extra e não acha quem já foi desalocado da obra, mas aparece em diários antigos.

## Consequências (migrations / API / UI / lotes afetados)
- **Migrations:** nenhuma.
- **API:** quebra de contrato nas respostas de diário e ocorrência: saem `equipePresenteIds` e `autorUsuarioId`, entram `equipePresente[]` e `autor`.
- **UI:** `features/obras/api/adaptadoresDiario.js` (`paraDiario`, `paraOcorrencia`), `DiarioDetalhe.jsx` e os mocks de teste precisam ser ajustados (L37/L38). Até lá, a contagem "N na equipe" do detalhe mostra 0.
- **Lotes:** L30 (backend), L37/L38 (frontend), L48 (mural do painel).
