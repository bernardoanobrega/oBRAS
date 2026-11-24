# L18 — Domínio AlocacaoObra e cargos

**Fase:** 2 · **Commit:** `feat(equipe): modelar alocacao`  
**Status:** Em andamento  
**Depende de:** L16 · **RF/UC:** RF-16 a RF-19; UC05 · **ADR:** ADR-09, ADR-10

## 1. Objetivo

Modelar a alocação de usuário na obra com cargos, período e encerramento.

## 2. Escopo

**Inclui:** entidade `AlocacaoObra`, vínculo N:N com `Cargo`, guards de cargos não vazios, data de início/fim, estado ativo.  
**Não inclui:** repository/service/API (L19–L22), cascata (L23).

## 3. Contrato de domínio

- Campos: id, obraId, usuarioId, dataInicio, dataFim, cargos.
- `cargos` não pode ser vazio.
- `dataFim >= dataInicio`.
- Alocação ativa = `dataFim == null`.
- Encerrar define `dataFim`; alocação encerrada não reativa.
- Uma alocação ativa por usuário/obra (garantida no banco em V2).

## 4. Testes primeiro (RED)

- `criaAlocacaoAtivaComCargos`.
- `rejeitaAlocacaoSemCargos`.
- `rejeitaDataFimAntesDoInicio`.
- `encerraDefinindoDataFim`.
- `naoReativaAlocacaoEncerrada`.
- `cargosSaoUnicosPorAlocacao`.

## 5. Implementação

1. Criar entidade e enum/estado se necessário.
2. Mapear tabela `alocacao_obra` e `alocacao_obra_cargo`.
3. Criar factory `AlocacaoObra.alocar(...)`.
4. Manter domínio sem controller.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Cargos não vazios | domínio + serviço | unitário |
| Período válido | domínio + banco | unitário/check |
| Uma ativa por usuário/obra | banco | índice único |
| Encerrada não reativa | domínio | unitário |

## 7. Segurança

Sem decisão de permissão no domínio.

## 8. Migração e rollback

Sem migration nova; tabelas existem em V2.

## 9. Critérios de aceite

- [ ] Entidade mapeada.
- [ ] Guards testados.
- [ ] Cargos N:N.
- [ ] Nenhuma entidade exposta.

## 10. Verificação e evidência

`mvn test`. Evidência: testes unitários de domínio.
