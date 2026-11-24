# L54 — Documentação final do módulo

**Fase:** 6 · **Commit:** `docs(obra): atualizar documentacao final`  
**Status:** Pendente  
**Depende de:** L53 · **RF/UC:** todos do módulo · **ADR:** ADR-01 a ADR-10

## 1. Objetivo

Fechar a documentação do módulo Detalhe de Obra com estado real, decisões, rotas, permissões e pendências.

## 2. Escopo

**Inclui:** README do backend/frontend; matriz de permissões; contratos de API; migrations; ADRs; pendências; instruções de execução e teste.  
**Não inclui:** documentação de módulos externos.

## 3. Contrato do artefato

- Atualizar `obras-backend/README.md`: módulos implementados e migrations.
- Atualizar `obras-frontend/README.md`: rotas e features.
- Publicar matriz de permissões.
- Publicar contratos de API do módulo.
- Listar pendências reais sem prometer Fase 5.
- Atualizar `PLANO.md` com o que foi entregue.

## 4. Testes primeiro (revisão)

- [ ] README não diz que `obra` é só adapter.
- [ ] Migrations listadas corretamente.
- [ ] Rotas documentadas.
- [ ] Permissões documentadas.
- [ ] Pendências sem módulos externos.
- [ ] Comandos de teste corretos.

## 5. Implementação

1. Atualizar READMEs.
2. Publicar docs de API/permissões.
3. Revisar links internos.
4. Conferir comandos.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Documentação reflete código | docs | revisão |
| Sem promessa de Fase 5 | docs | revisão |
| Comandos corretos | docs | execução |

## 7. Segurança

Não documentar segredos, tokens ou credenciais reais.

## 8. Migração e rollback

Sem migration. Rollback: reverter docs.

## 9. Critérios de aceite

- [ ] READMEs atualizados.
- [ ] API/permissões documentadas.
- [ ] Pendências listadas.
- [ ] Links e comandos válidos.

## 10. Verificação e evidência

Revisão final + execução dos comandos documentados. Evidência: links e diff.
