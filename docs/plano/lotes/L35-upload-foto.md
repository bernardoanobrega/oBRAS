# L35 — Upload e download de fotos

**Fase:** 3 · **Commit:** `feat(diario): implementar upload de fotos`  
**Status:** Em revisão  
**Depende de:** L34 · **RF/UC:** RF-64 a RF-68; UC21 · **RNF:** RNF-10 · **ADR:** ADR-02

## 1. Objetivo

Permitir upload e download de fotos vinculadas a ocorrência, validando tipo real e limite.

## 2. Escopo

**Inclui:** upload multipart, validação JPG/PNG por assinatura, limite 10 MB, vínculo com ocorrência, permissão na obra, download autenticado.  
**Não inclui:** PDF no mural; foto avulsa; exclusão (L36); UI (L39).

## 3. Contrato API

- `POST /api/obras/{obraId}/ocorrencias/{ocorrenciaId}/fotos` multipart `file`, `legenda`.
- Aceita apenas JPG/PNG; rejeita PDF, extensão falsa e > 10 MB.
- `GET /api/obras/{obraId}/fotos/{fotoId}` retorna binário com permissão.
- Metadados: id, ocorrenciaId, legenda, dataEnvio, contentType.

## 4. Testes primeiro (RED)

- `uploadJpgValidoRetorna201`.
- `uploadPngValidoRetorna201`.
- `rejeitaPdfNoMural`.
- `rejeitaArquivoComExtensaoFalsa`.
- `rejeitaArquivoAcimaDe10Mb`.
- `rejeitaOcorrenciaDeOutraObra`.
- `downloadSemPermissaoRetorna403`.
- `downloadRetornaBytesCorretos`.

## 5. Implementação

1. Reutilizar `ArquivoStorageService`.
2. Validar assinatura real, não só extensão.
3. Salvar metadados em `registro_fotografico`.
4. Restringir content-type a JPG/PNG.
5. Auditar criação.

## 6. Invariantes

| Regra | Camada | Teste |
|---|---|---|
| Foto sempre de ocorrência | serviço + banco | integração |
| Somente JPG/PNG | serviço | unitário |
| Limite 10 MB | storage/config | teste |
| Download exige permissão | controller | 403 |

## 7. Segurança

Validar assinatura real; não confiar em `Content-Type`; não expor path interno; download autenticado.

## 8. Migração e rollback

Sem migration. Rollback: remover endpoints; arquivos órfãos exigem limpeza manual.

## 9. Critérios de aceite

- [ ] Upload JPG/PNG.
- [ ] Rejeição de PDF/falso/grande.
- [ ] Download autorizado.
- [ ] Metadados persistidos.

## 10. Verificação e evidência

`mvn test` com arquivos reais pequenos. Evidência: testes de upload/download.
