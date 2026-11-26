# ADR-19 — Ocorrência rápida em corrida: vincular ao diário já criado

**Status:** Proposto · **Data:** 2026-11-26 · **Autor:** Cauã · **Aprovação:** Thierry [ ] Cauã [x]
**Substitui:** —

## Contexto
A ocorrência rápida (L33) cria o diário do dia quando ele ainda não existe. Se duas pessoas registram uma ocorrência rápida ao mesmo tempo, num dia sem diário, as duas tentam criar o diário. O índice único `uk_registro_diario_obra_data_ativo` (ADR-06, RN-07) barra a segunda, que receberia 422 "Já existe registro para o dia" e teria de reenviar.
Para quem está no canteiro, é um erro sem sentido: o diário que ela queria existe, só foi criado por outra pessoa um instante antes.

## Decisão
- Na ocorrência rápida, a duplicidade do diário **não vira erro**: a ocorrência é **vinculada ao diário que a outra requisição criou**, e o clima enviado é ignorado, como já acontece quando o diário existe (ADR-01).
- **Como:** o PostgreSQL invalida a transação depois da violação do índice, então não dá para buscar o diário nela. `DiarioService.ocorrenciaRapida` abre a transação com `TransactionTemplate` (e não `@Transactional`). Na duplicidade (`DiarioDuplicadoException`), descarta a transação que falhou e **repete uma vez** numa transação nova, que já enxerga o diário confirmado.
- Uma repetição basta: o `INSERT` concorrente espera a outra transação terminar. Se ela confirmou, a nova tentativa acha o diário; se desfez, o nosso `INSERT` já passa na primeira vez.
- O `POST` do diário (`criar`) continua respondendo 422 na duplicidade: lá o usuário escolheu criar um diário novo, e precisa saber que já existe um.

## Alternativas consideradas
- **Devolver 422 e pedir para reenviar:** simples, mas é um erro que o usuário não causou nem entende. Rejeitada pelo Cauã.
- **`INSERT ... ON CONFLICT DO NOTHING` em SQL nativo:** evita a nova tentativa, mas tira a criação do diário do JPA e duplica fora do domínio as regras de `RegistroDiario.criar` e a auditoria.
- **Savepoint (`Propagation.NESTED`):** o Hibernate não suporta savepoint pelo JPA, e a sessão fica inconsistente depois do erro.
- **Lock prévio (por exemplo, `SELECT ... FOR UPDATE` na obra):** serializaria toda ocorrência rápida da obra para resolver um caso raro.

## Consequências (migrations / API / UI / lotes afetados)
- **Migrations:** nenhuma. A garantia continua sendo o índice único.
- **API:** `POST /ocorrencias-rapidas` não devolve mais 422 por diário duplicado.
- **Código:** `ocorrenciaRapida` usa `Propagation.NOT_SUPPORTED` + `TransactionTemplate`; não deve ser chamado de dentro de outra transação, senão a nova tentativa não teria efeito.
- **Testes:** `OcorrenciaRapidaIntegracaoTest.corridaSemDiarioVinculaAoDiarioCriado` (duas threads, banco real) e `DiarioServiceTest.corridaNoDiarioRepeteEVinculaAoDiarioCriado`.
- **Lotes afetados:** L33. A UI (L39) não precisa tratar esse caso.
