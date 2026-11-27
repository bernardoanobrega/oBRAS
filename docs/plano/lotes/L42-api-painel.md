# L42 — API do painel e contrato

**Fase:** 4 · **Commit:** `feat(painel): expor endpoint agregador`  
**Status:** Concluído  
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

- [x] Endpoint conforme contrato.
- [x] 200/401/403/404.
- [x] Campos obrigatórios.
- [x] OpenAPI atualizado.

## 10. Verificação e evidência

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean test -Dtest=PainelObraControllerTest` → `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`; casos: `getPainelRetorna200`, `painelDeObraInexistenteRetorna404`, `painelSemPermissaoRetorna403`, `painelDeOutraObraRetorna403`, `contratoTemCamposObrigatorios`, `kpisTemFonteSimuladoQuandoNulo`, `semTokenRetorna401`.
- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test` → `Tests run: 282, Failures: 0, Errors: 3, Skipped: 10`; 3 erros pré-existentes em `OrcamentoRepositoryTest.inserirObraEUsuario:54` (`INSERT INTO obra` sem `auditavel_id`, coluna `NOT NULL`), fora do escopo L42. JDK 25 default quebra Lombok; usar JDK 21.
- Swagger real capturado via instância temporária em `:8081` (`spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=8081 --app.bootstrap.admin.enabled=false"`): `GET /api/obras/{obraId}/painel` visível, tag `Painel da obra`, 200 com `$ref: #/components/schemas/PainelObraDto`, respostas 401/403/404 documentadas, bearer global aplicado por `OpenApiConfig`. Media type gerado para 200: `*/*`.
- Contrato HTTP validado por `PainelObraControllerTest` (MockMvc): campos obrigatórios presentes; sem diário → `clima.data`/`clima.condicao` `null`, `maoDeObra.presentesHoje=0`, `maoDeObra.vsOntem=null`, `maoDeObra.fonte="DIARIO"`, `kpis.fonte="SIMULADO"`.
- Divergência registrada: `L42-api-painel.md:23` mostra clima preenchido; sem diário o contrato devolve campos `null` (decisão L41).
- Ressalva de processo: revisão cruzada com Cauã dispensada por decisão do usuário em 2026-11-27; registrada como ressalva de processo, não técnica.
