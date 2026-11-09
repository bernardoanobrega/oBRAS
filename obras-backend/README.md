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
Módulos implementados: `usuario` (completo), `obra` (só o adapter de alocação).
Migrations: `V1` usuários/permissões, `V2` obras/canteiros/alocações. Próximas: V3 auditoria/diário … V9 documentos.

## Regras
- Módulo nunca usa repository de outro módulo; use o service público ou um *port* em `shared`.
- Controllers: `@PreAuthorize("@perm.pode(#obraId, 'MODULO', 'ACAO')")`.
- Nunca expor entidades JPA; use DTOs.
