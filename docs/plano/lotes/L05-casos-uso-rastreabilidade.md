# L05 — Casos de uso do Detalhe de Obra e rastreabilidade

**Fase:** 0 · **Commit:** `docs(requisitos): remapear casos de uso do detalhe`  
**Status:** Pendente  
**Depende de:** L02, L04 · **RF/UC:** UC04, UC05, UC21 · **ADR:** ADR-03

## 1. Objetivo

Reescrever somente os casos de uso que o módulo Detalhe de Obra implementa, com fluxo principal, alternativos, RFs e ADRs.

## 2. Escopo

**Inclui:** UC04, UC05 e UC21; atores; pré-condições; fluxo principal; fluxos alternativos; permissão negada; vínculo com RFs e ADRs.  
**Não inclui:** os outros 18 UCs do documento canônico global. O remapeamento global é trabalho de requisitos, não deste módulo.

## 3. Contrato do artefato

- Documento/matriz com 3 UCs.
- Cada UC referencia RFs e ADRs.
- Cada RF do módulo aponta para UC e lote.
- Fluxo alternativo cobre erro, permissão negada e ausência de diário.

## 4. Testes primeiro (revisão)

- [ ] UC04 cobre criar, listar, editar e configurar obra.
- [ ] UC05 cobre alocar, cargos, encerrar e responsável técnico.
- [ ] UC21 cobre diário, ocorrência, paralisação, clima e foto.
- [ ] Fluxo de permissão negada presente nos UCs de escrita.
- [ ] Nenhum UC de outro módulo incluído.

## 5. Implementação

1. Reaproveitar estrutura do documento atual.
2. Reescrever apenas UC04, UC05 e UC21.
3. Inserir matriz de rastreabilidade para os lotes L06–L54.
4. Revisar com stakeholder.

## 6. Invariantes

| Regra | Camada | Verificação |
|---|---|---|
| Escopo restrito a 3 UCs | documentação | revisão |
| Todo RF do módulo tem UC | documentação | matriz |
| Todo UC cita RF e ADR | documentação | revisão |

## 7. Segurança

Incluir fluxo de permissão negada em UC04, UC05 e UC21.

## 8. Migração e rollback

Não aplicável. Manter versão antiga arquivada.

## 9. Critérios de aceite

- [ ] Documento dos 3 UCs aprovado.
- [ ] Rastreabilidade RF→UC→lote.
- [ ] Stakeholder revisou.
- [ ] Fora de escopo explícito.

## 10. Verificação e evidência

Conferir com L02 e Documento de Requisitos. Evidência: documento/matriz versionada.
