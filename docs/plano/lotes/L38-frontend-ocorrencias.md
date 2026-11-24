# L38 — Frontend: ocorrências e paralisações

**Fase:** 3 · **Commit:** `feat(diario): integrar ocorrencias`  
**Status:** Pendente  
**Depende de:** L37 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-08

## 1. Objetivo

Adicionar lista e formulário de ocorrências e paralisações ao diário.

## 2. Escopo

**Inclui:** lista de ocorrências do diário, criação/edição, categoria obrigatória, descrição, dataHora, paralisação com início/fim.  
**Não inclui:** ocorrência rápida/fotos (L39), exclusão visual além do botão.

## 3. Contrato UI

- Ocorrência exibe categoria, descrição, dataHora e autor.
- Paralisação exibe início/fim e categoria.
- Categoria obrigatória em ambos.
- Formulário valida descrição e período.
- Estados loading/vazio/erro/sem permissão.

> ⚠️ **Obrigatório: edição completa** (decisão de 2026-11-24, ver Confirmações em `docs/adr/README.md`).
> O `PUT` de ocorrência **substitui o estado inteiro**: o backend interpreta `paralisacao` ausente/nula como **"remover a paralisação"** e apaga o registro.
> Por isso o formulário de edição **deve**:
> 1. abrir preenchido com **todos** os valores atuais (categoria, descrição, dataHora **e paralisação**, se existir);
> 2. reenviar **todos** os campos no `PUT`, mesmo os que o usuário não alterou;
> 3. só omitir `paralisacao` quando o usuário removê-la explicitamente (ex.: desmarcar "houve paralisação").
>
> Se o front enviar só os campos alterados, **a paralisação é apagada sem aviso**. Não implementar edição parcial (PATCH) sem um ADR novo.

## 4. Testes primeiro (RED)

- `listaOcorrenciasMostraCategoriaEDescricao`.
- `formularioRejeitaCategoriaVazia`.
- `formularioRejeitaDescricaoVazia`.
- `paralisacaoRejeitaFimMenorQueInicio`.
- `edicaoCarregaOcorrencia` (inclusive a paralisação existente).
- `edicaoSemMexerNaParalisacaoReenviaParalisacao`.
- `edicaoEnviaTodosOsCamposNoPut`.
- `removerParalisacaoEnviaParalisacaoNula`.
- `semPermissaoOcultaAcoes`.
- `erroDoBackendEhExibido`.

## 5. Implementação

1. Criar componentes `OcorrenciaList`, `OcorrenciaForm`, `ParalisacaoForm`.
2. Usar API de L34.
3. Reutilizar `Badge` para categoria.
4. Não chamar `paralisar()` da obra ao salvar paralisação.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Categoria obrigatória | UI + API | validação |
| Paralisação não muda status da obra | UI + serviço | teste |
| Período válido | UI + API | validação |
| Ações por permissão | UI | teste |
| Edição completa: `PUT` reenvia tudo; paralisação só some se o usuário removê-la | UI | teste |

## 7. Segurança

Backend valida vínculo com diário/obra; frontend apenas reflete.

## 8. Migração e rollback

Sem migration. Rollback: remover componentes e rota.

## 9. Critérios de aceite

- [ ] Ocorrências no diário.
- [ ] Paralisação com categoria.
- [ ] Sem transição automática.
- [ ] Estados de UI.
- [ ] Editar uma ocorrência com paralisação, sem mexer nela, **mantém a paralisação**.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke.
