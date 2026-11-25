# L01 — Premissas e ADRs fechados

**Fase:** 0 · **Commit:** `docs(plano): fechar premissas e adrs`  
**Status:** Em andamento  
**Depende de:** nada · **RF/UC:** todos · **ADR:** ADR-01 a ADR-10

## 1. Objetivo

Transformar a seção 11 do `PLANO.md` em decisões explícitas, com dono, impacto e status. Nenhum código neste lote.

## 2. Escopo

**Inclui:** premissas adotadas, pendências que não bloqueiam Fases 1–4, status de cada ADR, critério para reabrir decisão.  
**Não inclui:** implementação, migration, API ou UI.

## 3. Contrato do artefato

- Tabela `ADR | status | decisão | impacto | pendência`.
- Tabela `premissa | risco | validação`.
- Lista de pendências com fase de resolução.
- Marcar ADR-01 a ADR-10 como `aceito`, `aceito com pendência` ou `aberto`.

## 4. Testes primeiro (revisão)

- [ ] Toda premissa da seção 11 aparece com status.
- [ ] Toda pendência tem fase ou critério de fechamento.
- [ ] Nenhuma decisão contradiz ADR existente.
- [ ] RF-67 e clima por ocorrência têm critério formal de mudança.

## 5. Implementação

1. Extrair seção 11 para tabelas.
2. Classificar cada pendência: bloqueia ou não bloqueia Fases 1–4.
3. Registrar dono e data de revisão.
4. Atualizar `PLANO.md` apenas se houver decisão nova.

## 6. Invariantes

| Regra | Camada | Verificação |
|---|---|---|
| Decisão fechada não pode ser reaberta sem ADR | documentação | revisão |
| Pendência não bloqueante não trava Fase 1–4 | documentação | checklist |

## 7. Segurança

Não aplicável. Nenhum segredo ou dado real deve ser incluído.

## 8. Migração e rollback

Não aplicável. Alteração apenas documental.

## 9. Critérios de aceite

- [ ] ADRs com status e impacto.
- [ ] Premissas com risco e validação.
- [ ] Pendências classificadas.
- [ ] `PLANO.md` e docs alinhados.

## 10. Verificação e evidência

Revisão cruzada com `PLANO.md` seções 3, 10 e 11. Evidência: tabela preenchida e link para este lote.
