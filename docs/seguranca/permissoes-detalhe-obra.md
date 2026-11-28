# Matriz de permissões — Detalhes da obra

Fonte canônica do módulo Detalhe de Obra. Toda alteração de `@PreAuthorize` deve atualizar esta tabela no mesmo commit.

| Método | Rota | Módulo | Ação | Escopo | Estado da evidência |
|---|---|---|---|---|---|
| POST | `/api/obras` | DETALHES_OBRA | CRIAR | Global (`obraId=null`) | `@perm.podeGlobal`; contrato L12 |
| GET | `/api/obras` | DETALHES_OBRA | VISUALIZAR | Autenticado + filtro no service | `isAuthenticated()`; sem `podeGlobal` |
| GET | `/api/obras/{obraId}` | DETALHES_OBRA | VISUALIZAR | Obra | `@perm.pode(#obraId, ...)`; L12 |
| PUT | `/api/obras/{obraId}` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L51 |
| PUT | `/api/obras/{obraId}/responsavel` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13/L51 |
| POST | `/api/obras/{obraId}/iniciar` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13 |
| POST | `/api/obras/{obraId}/paralisar` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13/L51 |
| POST | `/api/obras/{obraId}/retomar` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13/L51 |
| POST | `/api/obras/{obraId}/concluir` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13/L51 |
| POST | `/api/obras/{obraId}/cancelar` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L13/L51 |
| GET | `/api/obras/{obraId}/equipe` | DETALHES_OBRA | VISUALIZAR | Obra | `@perm.pode(#obraId, ...)`; L12/L51 |
| GET | `/api/obras/{obraId}/alocacoes` | DETALHES_OBRA | VISUALIZAR | Obra | `@perm.pode(#obraId, ...)`; L22 |
| POST | `/api/obras/{obraId}/alocacoes` | DETALHES_OBRA | CRIAR | Obra | `@perm.pode(#obraId, ...)`; L22 |
| PUT | `/api/obras/{obraId}/alocacoes/{alocacaoId}/cargos` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L22 |
| PATCH | `/api/obras/{obraId}/alocacoes/{alocacaoId}/encerrar` | DETALHES_OBRA | EDITAR | Obra | `@perm.pode(#obraId, ...)`; L22 |
| GET | `/api/obras/{obraId}/alocacoes/candidatos` | DETALHES_OBRA | CRIAR | Obra | `@perm.pode(#obraId, ...)`; L25 |
| GET | `/api/cargos` | DETALHES_OBRA | EDITAR | Global ou alguma obra | `podeGlobal` ou `podeEmAlgumaObra`; L51 |
| POST | `/api/cargos` | DETALHES_OBRA | EDITAR | Global ou alguma obra | No-op explícito do L22; L51 |
| PUT | `/api/cargos/{id}` | DETALHES_OBRA | EDITAR | Global ou alguma obra | No-op explícito do L22; L51 |
| DELETE | `/api/cargos/{id}` | DETALHES_OBRA | EDITAR | Global ou alguma obra | No-op explícito do L22; L51 |
| GET | `/api/obras/{obraId}/diarios` | DETALHES_OBRA | VISUALIZAR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/diarios` | DETALHES_OBRA | CRIAR | Obra | Auditado em L34; sem reescrita |
| GET | `/api/obras/{obraId}/diarios/{diarioId}` | DETALHES_OBRA | VISUALIZAR | Obra | Auditado em L34; sem reescrita |
| PUT | `/api/obras/{obraId}/diarios/{diarioId}` | DETALHES_OBRA | EDITAR | Obra | Auditado em L34; sem reescrita |
| DELETE | `/api/obras/{obraId}/diarios/{diarioId}` | DETALHES_OBRA | EXCLUIR | Obra | Revisado em L36 |
| POST | `/api/obras/{obraId}/diarios/{diarioId}/ocorrencias` | DETALHES_OBRA | CRIAR | Obra | Auditado em L34; sem reescrita |
| PUT | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}` | DETALHES_OBRA | EDITAR | Obra | Auditado em L34; sem reescrita |
| DELETE | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}` | DETALHES_OBRA | EXCLUIR | Obra | Revisado em L36 |
| POST | `/api/obras/{obraId}/ocorrencias-rapidas` | DETALHES_OBRA | CRIAR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/ocorrencias/{ocorrenciaId}/fotos` | DETALHES_OBRA | CRIAR | Obra | Revisado em L35 |
| GET | `/api/obras/{obraId}/fotos/{fotoId}` | DETALHES_OBRA | VISUALIZAR | Obra | Revisado em L35 |
| DELETE | `/api/obras/{obraId}/fotos/{fotoId}` | DETALHES_OBRA | EXCLUIR | Obra | Revisado em L36 |
| GET | `/api/obras/{obraId}/painel` | DETALHES_OBRA | VISUALIZAR | Obra | Auditado em L42; sem reescrita |
| GET | `/api/documentos/obras` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Global ou alguma obra | Anotação revisada no L51 |
| GET | `/api/documentos/obras/{obraId}` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Obra | Anotação revisada no L51 |
| GET | `/api/documentos/obras/{obraId}/prestadores` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Obra | Anotação revisada no L51 |
| GET | `/api/documentos/obras/{obraId}/{id}` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Obra | Anotação revisada no L51 |
| POST | `/api/documentos/obras/{obraId}` | CONTRATOS_DOCUMENTOS | CRIAR | Obra | Anotação revisada no L51 |
| POST | `/api/documentos/obras/{obraId}/{id}/versoes` | CONTRATOS_DOCUMENTOS | CRIAR | Obra | Anotação revisada no L51 |
| PUT | `/api/documentos/obras/{obraId}/{id}/prestador` | CONTRATOS_DOCUMENTOS | EDITAR | Obra | Anotação revisada no L51 |
| GET | `/api/documentos/obras/{obraId}/{id}/versoes/{numero}/arquivo` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Obra | Anotação revisada no L51 |
| GET | `/api/prestadores` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Global ou alguma obra | Anotação revisada no L51 |
| POST | `/api/prestadores` | CONTRATOS_DOCUMENTOS | CRIAR | Global ou alguma obra | Anotação revisada no L51 |
| PUT | `/api/prestadores/{id}` | CONTRATOS_DOCUMENTOS | EDITAR | Global ou alguma obra | Anotação revisada no L51 |
| PATCH | `/api/prestadores/{id}/desativar` | CONTRATOS_DOCUMENTOS | EXCLUIR | Global ou alguma obra | Anotação revisada no L51 |
| GET | `/api/prestadores/{id}/historico` | CONTRATOS_DOCUMENTOS | VISUALIZAR | Global ou alguma obra | Anotação revisada no L51 |

## Allowlist de rotas públicas

- `POST /api/auth/login` — login; sem `@PreAuthorize` por contrato.
- `GET /api/me/permissoes` — exige autenticação; `@PreAuthorize("isAuthenticated()")`.
- `GlobalExceptionHandler` — `@RestControllerAdvice`, sem handler mapeado; fora da varredura.

## Invariantes

- Rota com `{obraId}` usa `@perm.pode(#obraId, modulo, acao)`; `podeGlobal` não substitui escopo de obra.
- Rota global usa `podeGlobal` ou `podeEmAlgumaObra` conforme a política da matriz.
- `GET /api/obras` é a única rota da matriz sem par módulo/ação: `isAuthenticated()` + filtro no service.
- `CargoController` expõe no-ops para POST/PUT/DELETE; política atual usa `EDITAR` em escopo global/alguma obra.
