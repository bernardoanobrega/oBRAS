# L08 — Domínio Obra: entidade, enums e guardas

**Fase:** 1 · **Commit:** `feat(obra): modelar dominio e estados`  
**Status:** Em andamento  
**Depende de:** L07 · **RF/UC:** RF-14, RF-15, RF-18, RF-19; UC04, UC05 · **ADR:** ADR-04, ADR-05

## 1. Objetivo

Criar o modelo de domínio de `Obra` sem lógica de transporte, com estado inicial e guardas básicas.

## 2. Escopo

**Inclui:** entidade `Obra`, enums `StatusObra` e `TipologiaObra`, factory de cadastro, validação de período, responsável opcional, vínculo com `Auditavel`.  
**Não inclui:** transições (L09), persistência (L10), API.

## 3. Contrato de domínio

- Campos: nome, descricao, tipologia, endereco, inicioPrevisto, fimPrevisto, status, art, responsavelId, auditavel.
- Cadastro inicia em `PLANEJADA`.
- `fimPrevisto >= inicioPrevisto`.
- `nome`, `endereco`, `tipologia`, datas obrigatórios.
- `responsavelId` pode ser nulo.
- Nenhum setter público; métodos de negócio explícitos.

## 4. Testes primeiro (RED)

- `cadastraObraPlanejadaSemResponsavel`.
- `rejeitaPeriodoInvertido`.
- `rejeitaNomeVazio`.
- `rejeitaTipologiaNula`.
- `obraCriadaTemAuditavelNaoExcluido`.

## 5. Implementação

1. Criar pacote `br.com.obras.obra.domain`.
2. Implementar entidade e enums.
3. Criar factory `Obra.cadastrar(...)`.
4. Manter domínio independente de controller/DTO.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Obra nasce `PLANEJADA` | domínio | unitário |
| Período válido | domínio + banco | unitário + check |
| Responsável opcional | domínio + banco | unitário + insert |
| Auditável obrigatório | domínio | unitário |

## 7. Segurança

Nenhuma decisão de permissão no domínio.

## 8. Migração e rollback

Sem migration nova. Depende de L07.

## 9. Critérios de aceite

- [ ] Entidade sem dependência de web.
- [ ] Enums persistidos como `STRING`.
- [ ] Guardas testadas.
- [ ] Nenhuma entidade exposta.

## 10. Verificação e evidência

`mvn test`. Evidência: testes unitários de domínio.
