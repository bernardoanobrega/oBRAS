# ADRs: Architecture Decision Records

Registro das decisões de arquitetura e de escopo do módulo Detalhe de Obra.
Os ADR-01 a ADR-11 estão no `PLANO.md`, seção 3; o índice de status está em [`../plano/L01-premissas-adrs.md`](../plano/L01-premissas-adrs.md).
Os ADRs a partir do 12 ficam nesta pasta, um arquivo por decisão.

## Regras (conforme L01, seção 3)
- Decisão fechada só reabre por um ADR novo que a substitua.
- O ADR novo precisa de: motivo, impacto em migrations/API/UI, aprovação de Thierry **e** Cauã, e referência ao ADR substituído.
- Status possíveis: `Proposto` → `Aceito` / `Rejeitado` / `Substituído por ADR-xx`.

## Índice
| ADR | Título | Status | Data |
|---|---|---|---|
| [ADR-12](./ADR-12-rf70-modulo-usuarios.md) | RF-70 pertence ao módulo Usuários; acesso por obra é RNF-03 | Proposto (Cauã), aguarda Thierry | 2026-11-23 |
| [ADR-13](./ADR-13-painel-agrega-modulos.md) | Painel da obra (RF-73) agrega dados de outros módulos via ports | Proposto (Cauã), aguarda Thierry | 2026-11-23 |
| [ADR-14](./ADR-14-categoria-padrao-ocorrencia-rapida.md) | Ocorrência rápida: categoria opcional com padrão `OUTRO` | Proposto (Cauã), aguarda Thierry | 2026-11-24 |

## Confirmações (sem ADR novo)
| Data | Item | Decisão |
|---|---|---|
| 2026-11-23 | RF-67 / clima | **Manter ADR-01**: um clima por dia, no registro diário. O gatilho de reabertura continua o do L01, seção 7 |
| 2026-11-23 | Medições e revisões no painel | **Manter o escopo atual**: medições (RF-25–29), revisões (RF-60–63) e avanço físico (RF-20–24) ficam fora do módulo nesta entrega (Fase 5 / outros módulos), apesar de o Plano de Projeto citá-los no painel |
| 2026-11-24 | L28 / nomes no domínio do diário | **Manter os nomes do código**: enum `Clima` (o lote cita `CondicaoClimatica`) e fábrica `RegistroDiario.criar(...)` (o lote cita `registrar`), seguindo a convenção `criar(...)` do projeto |
| 2026-11-24 | L28 / clima e data obrigatórios | **Validar também no domínio**: `RegistroDiario.criar` rejeita data e clima nulos e `editar` rejeita clima nulo, com `RegraNegocioException` (422), além do `@NotNull` no DTO e do `NOT NULL` no banco. Motivo: a ocorrência rápida cria diário por um segundo caminho, cujo DTO tem clima opcional |
| 2026-11-24 | L28 / exclusão lógica | **Manter no service**: `RegistroDiario.excluirLogico()` marca só o próprio `excluido`; o `Auditavel` é marcado por `AuditoriaService.excluir`, na mesma transação. O teste de domínio do lote passa a se chamar `exclusaoLogicaMarcaExcluidoSemApagarDados` |
| 2026-11-24 | L28 / equipe presente sem duplicata | **Igualdade de `AlocacaoObra` por id** (módulo `obra`): `equals` compara o id e a classe real (proxy do Hibernate incluído), alocação sem id só é igual a si mesma, `hashCode` constante por classe. O `Set` de `equipePresente` passa a deduplicar por id em memória; no banco, a PK composta `pk_reg_diario_equipe` continua garantindo |
| 2026-11-24 | L29 / paralisação "com categoria" | **Manter o desenho**: `Paralisacao` não tem categoria própria; compartilha o id da ocorrência (`@MapsId`) e herda a categoria dela |
| 2026-11-24 | L29 / vínculos e campos obrigatórios | **Validar também no domínio**: `Ocorrencia.criar` rejeita diário, autor e categoria nulos e descrição vazia; `Paralisacao.criar` rejeita ocorrência nula; `RegistroFotografico.criar` rejeita ocorrência nula e arquivo vazio, com `RegraNegocioException` (422), além do DTO e do banco |
| 2026-11-24 | L29 / edição de ocorrência | **Edição completa, não parcial**: o `PUT` reenvia categoria, descrição, dataHora e paralisação (o formulário abre preenchido com os valores atuais). `Ocorrencia.editar` rejeita categoria nula e descrição vazia e mantém o estado anterior; `dataHora` nula mantém a atual; paralisação ausente continua significando "remover". Motivo: na edição parcial, "não enviado" e "apagar" ficariam ambíguos, e a paralisação seria removida sem querer |

## Template
```
# ADR-NN — Título
Status: Proposto | Aceito | Rejeitado | Substituído por ADR-xx
Data: AAAA-MM-DD · Autor: · Aprovação: Thierry [ ] Cauã [ ]
Substitui: (ADR-xx ou —)

## Contexto
## Decisão
## Alternativas consideradas
## Consequências (migrations / API / UI / lotes afetados)
```
