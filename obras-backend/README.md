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
Módulos implementados: `usuario`, `documento`; `obra` fornece adapters de alocação e consulta documental.
Migrations: `V1` usuários/permissões, `V2` obras/canteiros/alocações. V9 contratos/documentos. Os números V3–V8 permanecem reservados aos demais módulos; antes de integrar essas migrations, alinhe a sequência com a equipe.

## Regras
- Módulo nunca usa repository de outro módulo; use o service público ou um *port* em `shared`.
- Controllers: `@PreAuthorize("@perm.pode(#obraId, 'MODULO', 'ACAO')")`.
- Nunca expor entidades JPA; use DTOs.

## Contratos e documentos

RF-39, RF-41, RF-58 a RF-63 e RF-71; UC17–UC20. Contrato é uma categoria de documento.
O módulo usa `JdbcClient` em `DocumentoRepository`, transações no service e DTOs na API.
A consulta de obras passa por `ObraDocumentalPort`, implementado pelo módulo `obra`.
Não depende de branches ainda não integradas nem cria obras de demonstração em produção.

- `/api/documentos/obras`: obras em que o usuário pode consultar documentos.
- `/api/documentos/obras/{obraId}`: GET lista; POST multipart cria documento e revisão 1.
- `/api/documentos/obras/{obraId}/{id}`: GET documento e versões (mais recente primeiro).
- `/api/documentos/obras/{obraId}/{id}/versoes`: POST multipart publica revisão.
- `/api/documentos/obras/{obraId}/{id}/versoes/{numero}/arquivo`: GET visualiza; `?download=true` baixa.
- `/api/documentos/obras/{obraId}/prestadores`: GET prestadores ativos para vínculo.
- `/api/documentos/obras/{obraId}/{id}/prestador`: PUT `{ "prestadorId": 1 }` vincula contrato ainda sem prestador.
- `/api/prestadores`: GET lista; POST cadastra.
- `/api/prestadores/{id}`: PUT edita; `/{id}/desativar`: PATCH desativa; `/{id}/historico`: GET contratos e eventos autorizados.

Upload inicial: `codigo`, `titulo`, `categoria`, `disciplina` (padrão GERAL),
`prestadorId` (opcional, somente CONTRATO), `descricao`, `arquivo`.
Nova revisão: `descricao` e `arquivo`. Categorias e disciplinas são validadas no servidor.

Regras implementadas:
- Última revisão publicada é a atual, conforme os diagramas. A aprovação “em análise/vigente” do mockup não foi introduzida.
- Revisões e arquivos anteriores não são sobrescritos; a numeração usa lock do documento e unicidade no banco.
- Código único por obra. Diretórios agrupam categorias e disciplinas; não são pastas arbitrárias.
- Prestadores têm CPF/CNPJ normalizado, único, com 11/14 dígitos; não há validação dos dígitos verificadores.
- Desativação preserva contratos/histórico e impede novos vínculos. Não há exclusão física pela API.
- Segurança é aplicada no service em toda operação, inclusive download e histórico; permissões globais regem o cadastro de prestadores.
- PDF/PNG/JPG até 10 MiB (mesmo limite já adotado pelo projeto); requisição multipart aceita 11 MiB para acomodar os metadados.
- O arquivo é descartado em rollback confirmado da transação; falhas de escrita também tentam remover arquivos parciais.
- Armazenamento local exige volume persistente no deploy. Limpeza após término abrupto do processo continua sendo uma necessidade operacional.

### Testes de integração

Os testes usam um PostgreSQL real e **limpam as tabelas de negócio**. Informe exclusivamente um banco descartável,
com usuário/senha `obras` e permissão de criação de tabelas:

```bash
OBRAS_TEST_DB_URL=jdbc:postgresql://localhost:5432/obras_test mvn test
```

Sem essa variável, os testes de integração são explicitamente ignorados; os testes unitários continuam executando.
A suíte cobre permissões, acesso entre obras, revisões concorrentes, rollback de arquivos, histórico e validação de uploads.
