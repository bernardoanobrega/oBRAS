# L39 — Frontend: ocorrência rápida, upload e mural

**Fase:** 3 · **Commit:** `feat(diario): integrar fotos e mural`  
**Status:** Pendente  
**Depende de:** L38 · **RF/UC:** RF-64 a RF-68; UC21 · **RNF:** RNF-10 · **ADR:** ADR-02

## 1. Objetivo

Implementar o botão de anexar registro fotográfico, upload e mural somente com fotos de ocorrências.

## 2. Escopo

**Inclui:** modal de ocorrência rápida, campo de clima quando não houver diário, upload JPG/PNG, legenda, mural de fotos, estados de UI.  
**Não inclui:** foto avulsa; PDF; exportação de relatório.

## 3. Contrato UI

- “Anexar Registro Fotográfico” abre ocorrência rápida com categoria `OUTRO`.
- Descrição obrigatória; legenda pode preencher descrição.
- Se não houver diário do dia, pede clima.
- Upload aceita somente JPG/PNG e ≤ 10 MB.
- Mural exibe somente fotos de ocorrências.
- Erros de validação do backend exibidos.

## 4. Testes primeiro (RED)

- `abreOcorrenciaRapidaComCategoriaOutro`.
- `exigeDescricaoNaOcorrenciaRapida`.
- `pedeClimaQuandoNaoHaDiario`.
- `naoPedeClimaQuandoDiarioExiste`.
- `uploadRejeitaPdf`.
- `uploadRejeitaArquivoGrande`.
- `muralMostraSomenteFotosDeOcorrencias`.
- `legendaPreencheDescricaoQuandoVazia`.

## 5. Implementação

1. Criar `OcorrenciaRapidaModal`, `FotoUpload`, `MuralFotos`.
2. Usar `validarArquivo` existente.
3. Usar API de L33/L35.
4. Atualizar mural após upload.
5. Não criar foto sem ocorrência.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Foto sempre de ocorrência | UI + API | teste |
| Somente JPG/PNG | UI + API | teste |
| Clima condicional | UI + API | teste |
| Mural sem foto avulsa | UI | teste |

## 7. Segurança

Não confiar em extensão; backend valida assinatura. Download com permissão.

## 8. Migração e rollback

Sem migration. Rollback: remover botão/mural.

## 9. Critérios de aceite

- [ ] Ocorrência rápida funcional.
- [ ] Upload validado.
- [ ] Mural correto.
- [ ] Clima condicional.
- [ ] `npm test`/`npm run build` verdes.

## 10. Verificação e evidência

`npm test` e `npm run build`. Evidência: testes e smoke com upload real.
