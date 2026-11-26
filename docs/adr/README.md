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
| [ADR-15](./ADR-15-v5-integridade-obra-diario.md) | V5: integridade entre obras no diário e exclusão física em cascata | Proposto (Cauã), aguarda Thierry | 2026-11-24 |
| [ADR-16](./ADR-16-mapper-entidade-dto.md) | Conversão entidade → DTO em classe mapper | Proposto (Cauã), aguarda Thierry | 2026-11-25 |
| [ADR-17](./ADR-17-diario-expoe-nomes.md) | DTOs do diário expõem nome da equipe presente e do autor | Proposto (Cauã), aguarda Thierry | 2026-11-25 |
| [ADR-18](./ADR-18-ocorrencia-mesmo-dia-diario.md) | Ocorrência sempre no mesmo dia do diário | Proposto (Cauã), aguarda Thierry | 2026-11-25 |
| [ADR-19](./ADR-19-corrida-ocorrencia-rapida.md) | Ocorrência rápida em corrida: vincular ao diário já criado | Proposto (Cauã), aguarda Thierry | 2026-11-26 |
| [ADR-20](./ADR-20-art-texto-na-obra.md) | ART do responsável técnico como texto na obra, sem vínculo com o usuário | Proposto (Cauã), aguarda Thierry | 2026-11-26 |

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
| 2026-11-24 | L27 / autor da ocorrência | **Manter `alocacao_obra_id`** (o contrato citava `autor_id`): o autor é a alocação do usuário na obra, não o usuário, porque é ela que diz quem ele era naquela obra; o domínio do L29 usa `AlocacaoObra autor`. Contrato do L27 corrigido |
| 2026-11-25 | L30 / clima inválido no JSON | **400, não 500**: valor fora do enum, data mal formatada ou JSON quebrado falham no Jackson, antes da Bean Validation. O `GlobalExceptionHandler` (`shared`) passa a tratar `HttpMessageNotReadableException` como 400 "Dados inválidos", indicando o campo quando possível. Vale para toda a API |
| 2026-11-25 | L30 / teste `dtoFotoRejeitaOcorrenciaNula` | **Substituído**: não existe DTO de entrada de foto; a ocorrência vem da URL (`/ocorrencias/{ocorrenciaId}/fotos`) e não chega nula. A regra já é provada por `RegistroFotograficoTest.rejeitaFotoSemOcorrencia` (L29) e pelo `NOT NULL` da V4 |
| 2026-11-25 | L30 / consultas sem N+1 | **Uma consulta por tipo de dado**: a lista de diários conta as ocorrências numa consulta agrupada (`contarPorDiarios`); o detalhe busca ocorrências com autor e paralisação (`join fetch`), fotos ativas do diário inteiro (`listarPorDiario`) e equipe com nomes (`buscarComEquipe`). As fotos excluídas passam a ser filtradas na query, não em memória. No módulo `obra` (L19), `findAtivasPorIds` e `findAtivaPorUsuario` ganharam `join fetch a.usuario`, para que a equipe editada e o autor de uma ocorrência nova também saiam com o nome sem consulta extra (`AlocacaoObraRepositoryTest`) |
| 2026-11-25 | L30 / nome de método | `findAtivo` renomeado para **`buscarAtivoPorData`**, no padrão português dos demais (`buscar`, `listarPorObra`) |
| 2026-11-25 | L31 / contrato do service | **O contrato segue o código**: `DiarioService` (não `RegistroDiarioService`), `editar` (não `atualizar`), entrada `NovoDiarioDto` / `AlterarDiarioDto` (o `DiarioDto` é de saída) e usuário via `CurrentUser.id()`, nunca por parâmetro, para o cliente não poder agir em nome de outro usuário. Contrato do L31 corrigido |
| 2026-11-25 | Obra excluída logicamente (L11) | **`ObraService` ignora obra excluída**: `ObraRepository.buscarAtiva` e `listarAtivas` juntam com `auditavel` e filtram `excluido = false`. Obra excluída passa a dar 404 em todo o módulo, diário incluído (`ObraRepositoryTest`) |
| 2026-11-25 | L32 / contrato do service | **O contrato segue o código**: `DiarioService.adicionarOcorrencia` / `editarOcorrencia` (não `OcorrenciaService.criar` / `atualizar`), entrada `NovaOcorrenciaDto` e autor via `CurrentUser.id()`. **Sem `listar` próprio**: as ocorrências vêm no `DiarioDto` do `buscar` do diário. **Paralisação sem categoria própria**: nasce com a ocorrência e herda a categoria dela (confirmação do L29) |
| 2026-11-25 | L32 + L36 / exclusão do diário | **Cascata lógica, como o L36 define**: excluir o diário marca como excluídas as ocorrências ativas e as fotos dele; excluir a ocorrência marca as fotos. Cada item ganha o próprio `EXCLUSAO` na auditoria, na mesma transação, e nada é apagado fisicamente. Assim, ocorrência de diário excluído não pode mais ser editada nem receber foto. Antecipado no L32 porque era um furo no vínculo ocorrência–diário |
| 2026-11-26 | L33 / contrato do service | **O contrato segue o código**: `DiarioService.ocorrenciaRapida(obraId, NovaOcorrenciaRapidaDto)` (não `criarRapida`), autor via `CurrentUser.id()`, categoria opcional com padrão `OUTRO` (ADR-14). **`dataHora` opcional**: ausente vale "agora" e precisa cair no dia `data` (ADR-18); para registrar um dia passado, informa-se o horário |
| 2026-11-26 | L33 / campos do payload | **Sem `legenda` e sem `paralisacao`**: a legenda é da foto e vai no upload (L35), e o DTO a aceitava e descartava em silêncio; a paralisação vai pelo formulário completo (L38). O contrato dizia "legenda pode compor a descrição", o que não é feito |
| 2026-11-26 | L33 / diário automático | **Nasce sem descrição**: antes recebia o texto da ocorrência, misturando o resumo do dia com o registro pontual. Equipe presente vazia, clima do payload |

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
