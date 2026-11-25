# ADR-14 — Ocorrência rápida: categoria opcional com padrão `OUTRO`
Status: Proposto
Data: 2026-11-24 · Autor: Cauã · Aprovação: Thierry [ ] Cauã [x]
Substitui: — (altera o contrato dos lotes L29, seção 3, e L33, seções 2, 3 e 6)

## Contexto
Os lotes L29 e L33 dizem que a ocorrência rápida usa **sempre** a categoria `OUTRO`.
O código implementado exigia que o cliente enviasse a categoria (`@NotNull` em `NovaOcorrenciaRapidaDto`) e gravava o valor recebido, sem aplicar `OUTRO`.
A divergência apareceu na auditoria do L29.

## Decisão
Na ocorrência rápida (`POST /api/obras/{obraId}/ocorrencias/rapida`):
- `categoria` é **opcional** no DTO;
- se vier nula, o service grava `OUTRO`;
- se vier preenchida, o service mantém o valor informado.

A ocorrência normal (dentro de um diário) continua com categoria obrigatória.
O domínio (`Ocorrencia.criar`) continua rejeitando categoria nula; o padrão é aplicado no service, porque é regra deste caso de uso.

## Alternativas consideradas
- **Sempre `OUTRO`, ignorando o que vier** (contrato original): descarta uma classificação que o usuário quis informar.
- **Categoria obrigatória** (código anterior): obriga uma escolha num fluxo que existe para ser rápido.

## Consequências
- **Migrations:** nenhuma.
- **API:** `categoria` deixa de ser obrigatória no payload da ocorrência rápida; o cliente que já envia a categoria continua funcionando.
- **UI:** o formulário rápido pode omitir a categoria ou oferecê-la como opcional.
- **Lotes afetados:** L29 (seção 3) e L33. O teste `categoriaSempreOutro` do L33 é substituído por `ocorrenciaRapidaComCategoriaNulaUsaOutro` e `ocorrenciaRapidaMantemCategoriaInformada` (`DiarioServiceTest`).
