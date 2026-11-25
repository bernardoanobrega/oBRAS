# L42 — API do painel e contrato

**Fase:** 4 · **Commit:** `feat(painel): expor endpoint agregador`  
**Status:** Pendente  
**Depende de:** L41 · **RF/UC:** RF-73; UC04, UC05, UC21 · **ADR:** ADR-07, ADR-10

## 1. Objetivo

Expor `GET /api/obras/{obraId}/painel` com contrato estável para o frontend.

## 2. Escopo

**Inclui:** controller, DTO de resposta, permissão `DETALHES_OBRA:VISUALIZAR`, testes de contrato.  
**Não inclui:** escrita no painel; endpoints de outros módulos.

## 3. Contrato API

`GET /api/obras/{obraId}/painel` → 200 com:

```json
{
  "obra": {},
  "clima": { "data": "2026-11-11", "condicao": "ENSOLARADO" },
  "kpis": { "avancoRealizado": null, "avancoPrevisto": null, "spi": null,
            "orcamentoTotal": null, "executado": null, "limiteAlerta": null,
            "fonte": "SIMULADO" },
  "equipe": [],
  "maoDeObra": { "alocados": 4, "presentesHoje": 3, "vsOntem": 1, "fonte": "DIARIO" },
  "ocorrencias": [],
  "fotos": []
}
```

## 4. Testes primeiro (RED)

- `getPainelRetorna200`.
- `painelDeObraInexistenteRetorna404`.
- `painelSemPermissaoRetorna403`.
- `painelDeOutraObraRetorna403`.
- `contratoTemCamposObrigatorios`.
- `kpisTemFonteSimuladoQuandoNulo`.
- `semTokenRetorna401`.

## 5. Implementação

1. Criar `PainelObraController`.
2. Aplicar `@PreAuthorize`.
3. Não adicionar campos sem contrato.
4. Documentar no OpenAPI.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Contrato estável | controller/DTO | contrato |
| Permissão por obra | controller | 403 |
| Erro padronizado | handler | contrato |
| Nenhum dado de outro módulo | DTO | revisão |

## 7. Segurança

Validar vínculo do usuário com a obra; não confiar no frontend.

## 8. Migração e rollback

Sem migration.

## 9. Critérios de aceite

- [ ] Endpoint conforme contrato.
- [ ] 200/401/403/404.
- [ ] Campos obrigatórios.
- [ ] OpenAPI atualizado.

## 10. Verificação e evidência

`mvn test` e Swagger. Evidência: JSON de exemplo e testes.
