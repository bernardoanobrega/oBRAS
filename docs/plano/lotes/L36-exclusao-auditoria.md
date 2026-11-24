# L36 — Exclusão lógica e auditoria do diário

**Fase:** 3 · **Commit:** `feat(diario): auditar exclusoes`  
**Status:** Em revisão  
**Depende de:** L31–L35 · **RF/UC:** RF-64 a RF-68; UC21 · **ADR:** ADR-04, ADR-06

## 1. Objetivo

Implementar exclusão lógica de diário, ocorrência e foto, com auditoria e consistência de vínculos.

## 2. Escopo

**Inclui:** `DELETE` lógico de diário/ocorrência/foto; sincronização de `excluido`; auditoria `EXCLUSAO`; regra de cascata lógica ao excluir diário.  
**Não inclui:** exclusão física; restauração.

## 3. Contrato de serviço

- Excluir diário marca `auditavel.excluido = true` e sincroniza `registro_diario.excluido`.
- Excluir diário marca ocorrências e fotos vinculadas como excluídas na mesma transação.
- Excluir ocorrência marca fotos vinculadas como excluídas.
- Excluir foto marca apenas a foto.
- Nenhuma linha é removida fisicamente.
- Toda exclusão gera `RegistroAuditoria` com `EXCLUSAO`.

## 4. Testes primeiro (RED)

- `excluirDiarioNaoRemoveLinhaFisicamente`.
- `excluirDiarioMarcaOcorrenciasEFotos`.
- `excluirOcorrenciaMarcaFotos`.
- `excluirFotoMantemOcorrencia`.
- `exclusaoAudita`.
- `diarioExcluidoNaoApareceNaLista`.
- `consultaNaoRetornaFotoExcluida`.

## 5. Implementação

1. Adicionar métodos de exclusão nos services.
2. Atualizar `auditavel.excluido` e coluna espelho.
3. Garantir transação única.
4. Filtrar excluídos em todas as consultas.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Exclusão é lógica | serviço + banco | query |
| Excluído não aparece | repository | integração |
| Cascata lógica consistente | serviço | integração |
| Exclusão audita | serviço | integração |

## 7. Segurança

Permissão `DETALHES_OBRA:EXCLUIR`; validar vínculo com a obra.

## 8. Migração e rollback

Sem migration nova. Trigger de L26 sincroniza diário.

## 9. Critérios de aceite

- [ ] Exclusão lógica nos três níveis.
- [ ] Cascata consistente.
- [ ] Auditoria.
- [ ] Consultas filtram excluídos.

## 10. Verificação e evidência

`mvn test`. Evidência: queries antes/depois e testes.
