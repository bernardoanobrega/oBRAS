# oBRAS — Backend

Java 21 · Spring Boot 3.3 · PostgreSQL 16 · Flyway · JWT · RBAC por Cargo/Permissão.

## JDK obrigatório

Sempre usar JDK 21 para build, testes e execução. JDK 25 não processa Lombok neste projeto: o build falha com `cannot find symbol` para getters gerados.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

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
Módulos implementados: `usuario`, `obra` (painel, diário e adapters), `orcamento`, `compras`, `estoque`, `documento`.
Migrations: `V1` usuários/permissões, `V2` obras/canteiros/alocações, `V3`/`V3_1` auditoria, `V4` diário, `V5` orçamento,
`V6` materiais, `V7` compras, `V8` estoque, `V9` contratos/documentos.
A numeração segue a ordem em que os módulos entram na develop.

## Orçamento (UC10 · RF-30 a RF-34, RF-74)
- `GET /api/orcamentos`: orçamentos das obras em que o usuário tem `ORCAMENTO:VISUALIZAR`.
- `/api/obras/{obraId}/orcamento`: `POST` cria, `GET` painel (planilha, decisões e indicadores),
  `PATCH /cancelar`; `itens` (`POST`, `PUT`, `DELETE`), `itens/desvios`, `itens/{id}/custos` (`GET`, `POST`),
  `itens/{id}/custos/{custoId}` (`PUT`, `DELETE`; só custos lançados manualmente); `decisoes` (`POST`) e
  `decisoes/{decisaoId}` (`PUT`), ambos exigem `APROVAR`.
- Saúde: `CRITICA` se realizado > orçado; `ATENCAO` se executado ≥ limite de alerta ou há item em estouro.
- Compras usa `shared/port/OrcamentoPort`, implementado por `OrcamentoAdapter`: consulta o item orçado e
  lança o custo com origem `MATERIAL` e `idOrigem` = pedido (esses custos não são editáveis na tela).

## Regras
- Módulo nunca usa repository de outro módulo; use o service público ou um *port* em `shared`.
- Controllers: `@PreAuthorize("@perm.pode(#obraId, 'MODULO', 'ACAO')")`.
- Nunca expor entidades JPA; use DTOs.

## Compras e Estoque (implementados)
Migrations: `V6` materiais, `V7` fornecedores/pedidos/itens/cotações/histórico, `V8` saldo e movimentação de estoque.
- **Compras** (`/api/obras/{obraId}/pedidos-compra`, `/api/fornecedores`): máquina de estados do PedidoCompra no domínio
  (`SOLICITADO → EM_COTACAO → COMPRADO → EM_TRANSITO → ENTREGUE`; `CANCELADO` só até `EM_COTACAO`).
- **Estoque** (`/api/obras/{obraId}/estoque`, `/api/materiais`): saldo por (material, canteiro); entrada, saída e
  transferência numa única transação, com lock pessimista no saldo.
- Compras → Estoque: `EstoqueService.registrarEntradaEstoque` chama o service público de compras (mesma transação).
- Dependências ainda inexistentes entram por *ports* em `shared.port`: `ObraConsultaPort` (adapter em `obra`),
  `OrcamentoPort` (implementado por `orcamento.service.OrcamentoAdapter`).

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
