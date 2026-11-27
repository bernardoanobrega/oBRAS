# Matriz de permissões — Detalhes da obra

| Método | Rota | Ação | Escopo | Estado da evidência |
|---|---|---|---|---|
| POST | `/api/obras` | CRIAR | Global (`obraId=null`) | Conhecida no contrato L12; `@PreAuthorize` no controller |
| GET | `/api/obras` | VISUALIZAR | Autenticado + filtro no service | Conhecida; não trocada por `podeGlobal` |
| GET | `/api/obras/{obraId}` | VISUALIZAR | Obra | Controller L12 |
| PUT | `/api/obras/{obraId}` | EDITAR | Obra | Controller L12 |
| PUT | `/api/obras/{obraId}/responsavel` | EDITAR | Obra | Controller L13 |
| POST | `/api/obras/{obraId}/iniciar` | EDITAR | Obra | Controller L13 |
| POST | `/api/obras/{obraId}/paralisar` | EDITAR | Obra | Controller L13 |
| POST | `/api/obras/{obraId}/retomar` | EDITAR | Obra | Controller L13 |
| POST | `/api/obras/{obraId}/concluir` | EDITAR | Obra | Controller L13 |
| POST | `/api/obras/{obraId}/cancelar` | EDITAR | Obra | Controller L13 |
| GET | `/api/obras/{obraId}/equipe` | VISUALIZAR | Obra | Controller L12 |
| GET | `/api/obras/{obraId}/alocacoes` | VISUALIZAR | Obra | Contrato L22 |
| POST | `/api/obras/{obraId}/alocacoes` | CRIAR | Obra | Contrato L22 |
| PUT | `/api/obras/{obraId}/alocacoes/{id}/cargos` | EDITAR | Obra | Contrato L22 |
| PATCH | `/api/obras/{obraId}/alocacoes/{id}/encerrar` | EDITAR | Obra | Contrato L22 |
| GET | `/api/obras/{obraId}/alocacoes/candidatos` | CRIAR | Obra | Contrato adicional do front L25 |
| GET | `/api/cargos` | CRIAR | Global ou alguma obra | Política restrita validada no L51: `EDITAR` |
| POST | `/api/cargos` | EDITAR | Global ou alguma obra | No-op explícito do L22 |
| PUT | `/api/cargos/{id}` | EDITAR | Global ou alguma obra | No-op explícito do L22 |
| DELETE | `/api/cargos/{id}` | EDITAR | Global ou alguma obra | No-op explícito do L22 |
| GET | `/api/obras/{obraId}/diarios` | VISUALIZAR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/diarios` | CRIAR | Obra | Auditado em L34; sem reescrita |
| GET | `/api/obras/{obraId}/diarios/{id}` | VISUALIZAR | Obra | Auditado em L34; sem reescrita |
| PUT | `/api/obras/{obraId}/diarios/{id}` | EDITAR | Obra | Auditado em L34; sem reescrita |
| DELETE | `/api/obras/{obraId}/diarios/{id}` | EXCLUIR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/diarios/{id}/ocorrencias` | CRIAR | Obra | Auditado em L34; sem reescrita |
| PUT | `/api/obras/{obraId}/ocorrencias/{id}` | EDITAR | Obra | Auditado em L34; sem reescrita |
| DELETE | `/api/obras/{obraId}/ocorrencias/{id}` | EXCLUIR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/ocorrencias-rapidas` | CRIAR | Obra | Auditado em L34; sem reescrita |
| POST | `/api/obras/{obraId}/ocorrencias/{id}/fotos` | CRIAR | Obra | Auditado em L34; sem reescrita |
| GET | `/api/obras/{obraId}/fotos/{id}` | VISUALIZAR | Obra | Auditado em L34; sem reescrita |
| DELETE | `/api/obras/{obraId}/fotos/{id}` | EXCLUIR | Obra | Auditado em L34; sem reescrita |
| GET | `/api/obras/{obraId}/painel` | VISUALIZAR | Obra | Auditado em L42; sem reescrita |
| GET | `/api/documentos/obras` | VISUALIZAR | Global ou alguma obra | Anotação adicionada no L51 |
| GET | `/api/documentos/obras/{obraId}` | VISUALIZAR | Obra | Anotação adicionada no L51 |
| GET | `/api/documentos/obras/{obraId}/prestadores` | VISUALIZAR | Obra | Anotação adicionada no L51 |
| GET | `/api/documentos/obras/{obraId}/{id}` | VISUALIZAR | Obra | Anotação adicionada no L51 |
| POST | `/api/documentos/obras/{obraId}` | CRIAR | Obra | Anotação adicionada no L51 |
| POST | `/api/documentos/obras/{obraId}/{id}/versoes` | CRIAR | Obra | Anotação adicionada no L51 |
| PUT | `/api/documentos/obras/{obraId}/{id}/prestador` | EDITAR | Obra | Anotação adicionada no L51 |
| GET | `/api/documentos/obras/{obraId}/{id}/versoes/{numero}/arquivo` | VISUALIZAR | Obra | Anotação adicionada no L51 |
| GET | `/api/prestadores` | VISUALIZAR | Global ou alguma obra | Anotação adicionada no L51 |
| POST | `/api/prestadores` | CRIAR | Global ou alguma obra | Anotação adicionada no L51 |
| PUT | `/api/prestadores/{id}` | EDITAR | Global ou alguma obra | Anotação adicionada no L51 |
| PATCH | `/api/prestadores/{id}/desativar` | EXCLUIR | Global ou alguma obra | Anotação adicionada no L51 |
| GET | `/api/prestadores/{id}/historico` | VISUALIZAR | Global ou alguma obra | Anotação adicionada no L51 |

## Allowlist de rotas públicas

- `POST /api/auth/login` — login; sem `@PreAuthorize` por contrato.
- `GET /api/me/permissoes` — exige autenticação; `@PreAuthorize("isAuthenticated()")` adicionado no L51.
- `GlobalExceptionHandler` — `@RestControllerAdvice`, sem handler mapeado; fora da varredura.

## Lacunas registradas

- `podeGlobal` não substitui escopo de obra: rotas com `{obraId}` usam `@perm.pode(#obraId, ...)`.
- Auditoria de L34/L42 foi somente auditada; anotações existentes não foram reescritas.
- `CargoController` expõe no-ops para POST/PUT/DELETE; política atual usa `EDITAR` em escopo global/alguma obra.
