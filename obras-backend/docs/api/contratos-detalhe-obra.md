# Contratos de API — Detalhe de Obra

Escopo documentado: F3 (diário/ocorrências/fotos) e F4 (painel). Base comum: `/api/obras/{obraId}`.

## F3 — Diário

| Método | Rota | Permissão | Resposta |
|---|---|---|---|
| `GET` | `/diarios` | `DETALHES_OBRA/VISUALIZAR` | `200`, `DiarioResumoDto[]` |
| `POST` | `/diarios` | `DETALHES_OBRA/CRIAR` | `201`, `DiarioDto` |
| `GET` | `/diarios/{diarioId}` | `DETALHES_OBRA/VISUALIZAR` | `200`, `DiarioDto` |
| `PUT` | `/diarios/{diarioId}` | `DETALHES_OBRA/EDITAR` | `200`, `DiarioDto` |
| `DELETE` | `/diarios/{diarioId}` | `DETALHES_OBRA/EXCLUIR` | `204` |

`POST /diarios` — corpo:

```json
{
  "data": "2026-04-20",
  "clima": "SOL",
  "descricao": "texto opcional",
  "equipePresenteIds": [1, 2]
}
```

`PUT /diarios/{diarioId}` usa o mesmo corpo sem `data`. `descricao` aceita até 2000 caracteres. `equipePresenteIds` ausente vira conjunto vazio. Existe no máximo um diário não excluído por obra/data.

Exclusão de diário é lógica: registros relacionados também são marcados como excluídos, com auditoria e rollback transacional em cascata.

## F3 — Ocorrências e paralisação

| Método | Rota | Permissão | Resposta |
|---|---|---|---|
| `POST` | `/diarios/{diarioId}/ocorrencias` | `DETALHES_OBRA/CRIAR` | `201`, `OcorrenciaDto` |
| `PUT` | `/ocorrencias/{ocorrenciaId}` | `DETALHES_OBRA/EDITAR` | `200`, `OcorrenciaDto` |
| `DELETE` | `/ocorrencias/{ocorrenciaId}` | `DETALHES_OBRA/EXCLUIR` | `204` |
| `POST` | `/ocorrencias-rapidas` | `DETALHES_OBRA/CRIAR` | `201`, `OcorrenciaDto` |

Corpo completo:

```json
{
  "categoria": "SEGURANCA",
  "descricao": "texto obrigatório",
  "dataHora": "2026-04-20T10:30:00",
  "paralisacao": {
    "inicio": "2026-04-20T10:30:00",
    "fim": "2026-04-20T12:00:00"
  }
}
```

PUT é substituição completa. `paralisacao: null` remove a paralisação. `dataHora` ausente usa o instante atual.

Corpo de ocorrência rápida:

```json
{
  "data": "2026-04-20",
  "clima": "NUBLADO",
  "categoria": "OUTRO",
  "descricao": "texto obrigatório",
  "dataHora": "2026-04-20T10:30:00"
}
```

`clima` é obrigatório somente quando ainda não existe diário no dia. `categoria` ausente vira `OUTRO`. `dataHora` ausente vira agora e precisa pertencer a `data`. A ocorrência rápida cria diário automático quando necessário.

## F3 — Fotos do mural

| Método | Rota | Permissão | Resposta |
|---|---|---|---|
| `POST` | `/ocorrencias/{ocorrenciaId}/fotos` | `DETALHES_OBRA/CRIAR` | `201`, `FotoDto` |
| `GET` | `/fotos/{fotoId}` | `DETALHES_OBRA/VISUALIZAR` | `200`, bytes com `Content-Type` e `Content-Disposition: inline` |
| `DELETE` | `/fotos/{fotoId}` | `DETALHES_OBRA/EXCLUIR` | `204` |

Upload usa `multipart/form-data` com partes `arquivo` e `legenda` opcional. Formatos aceitos: JPG e PNG, máximo 10 MB, validados por extensão e magic bytes. PDF é rejeitado no mural mesmo quando aceito pelo serviço genérico de armazenamento.

`FotoDto`:

```json
{
  "id": 10,
  "ocorrenciaId": 7,
  "nomeArquivo": "foto.jpg",
  "legenda": "texto opcional",
  "dataEnvio": "2026-04-20T10:31:00"
}
```

A legenda é enviada na parte `legenda`; `nomeArquivo` é somado no retorno. Exclusão de foto é lógica e auditada.

## F4 — Painel

`GET /api/obras/{obraId}/painel` com `DETALHES_OBRA/VISUALIZAR` retorna:

```json
{
  "obra": {},
  "clima": null,
  "kpis": {
    "avancoRealizado": null,
    "avancoPrevisto": null,
    "spi": null,
    "orcamentoTotal": null,
    "executado": null,
    "limiteAlerta": null,
    "fonte": "SIMULADO"
  },
  "equipe": [],
  "maoDeObra": {
    "alocados": 0,
    "presentesHoje": 0,
    "vsOntem": null,
    "fonte": "DIARIO"
  },
  "ocorrencias": [],
  "fotos": []
}
```

Sem diário no dia: `clima` nulo, `presentesHoje` zero, `vsOntem` nulo, `fonte: "DIARIO"`. KPI indisponível permanece nulo; frontend não converte KPI nulo em zero. `fonte: "SIMULADO"` é exibida na UI.

## Erros

| Situação | Status |
|---|---|
| Sem autenticação | `401` |
| Sem permissão na obra/ação | `403` |
| Regra de negócio | `422` |
| Arquivo acima de 10 MB | `413` |
| Validação Bean Validation | `400` |

Erros de validação de campo usam `mensagemErro` e `errosPorCampo` no frontend.
