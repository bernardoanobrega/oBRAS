# oBRAS — Backend

Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Flyway · JWT · RBAC por Cargo/Permissão.

## Subir em desenvolvimento
```bash
docker compose up -d db                      # Postgres
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```
- Swagger: http://localhost:8080/swagger-ui.html
- Admin inicial (perfil `dev`): `admin` / `Admin@12345`
- Login: `POST /api/auth/login` → usar `Authorization: Bearer <token>`

## Produção
Variáveis obrigatórias: `DB_URL DB_USER DB_PASSWORD JWT_SECRET` (≥ 32 chars).
Para criar o 1º admin: `BOOTSTRAP_ADMIN_ENABLED=true BOOTSTRAP_ADMIN_PASSWORD=...` e desligue depois.

## Estrutura
`shared/` (segurança, exceções, storage) · um pacote por módulo com `controller/service/repository/domain/dto/mapper`.
Módulos implementados: `usuario` (completo), `orcamento` (completo), `obra` (só o adapter de alocação).
Migrations: `V1` usuários/permissões, `V2` obras/canteiros/alocações, `V10` orçamento
(número provisório: a numeração entre módulos ainda será combinada pelo grupo).

## Orçamento (UC10 · RF-30 a RF-34, RF-74)
- `GET /api/orcamentos`: orçamentos das obras em que o usuário tem `ORCAMENTO:VISUALIZAR`.
- `/api/obras/{obraId}/orcamento`: `POST` cria, `GET` painel (planilha, decisões e indicadores),
  `PATCH /cancelar`; `itens` (`POST`, `PUT`, `DELETE`), `itens/desvios`, `itens/{id}/custos` (`GET`, `POST`),
  `itens/{id}/custos/{custoId}` (`PUT`, `DELETE`; só custos lançados manualmente); `decisoes` (`POST`) e
  `decisoes/{decisaoId}` (`PUT`), ambos exigem `APROVAR`.
- Saúde: `CRITICA` se realizado > orçado; `ATENCAO` se executado ≥ limite de alerta ou há item em estouro.
- Outros módulos (ex.: Compras) lançam custo por `OrcamentoService.registrarCusto`.

## Regras
- Módulo nunca usa repository de outro módulo; use o service público ou um *port* em `shared`.
- Controllers: `@PreAuthorize("@perm.pode(#obraId, 'MODULO', 'ACAO')")`.
- Nunca expor entidades JPA; use DTOs.
