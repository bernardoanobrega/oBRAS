# oBRAS — Requisitos Detalhados do Módulo Detalhes de Obra

> Versão de trabalho. Decompõe os requisitos do SRS que afetam o **Painel Detalhes de Obra** em requisitos atômicos (uma obrigação por linha), para servir de base ao diagrama de classes e aos diagramas dinâmicos (`state-diagrams.md`).

## 0. Como ler este documento

- **Fontes:** SRS (`requirements.docx`), Casos de Uso (`use-cases.docx`) e Plano de Projeto (`project-plan.docx`).
- **IDs:** `RF-XX.n` é um requisito atômico derivado do `RF-XX` do SRS, que é mantido como "pai" para preservar a rastreabilidade. `RN-nn` são regras de negócio. `P-nn` são pendências.
- **Coluna Fonte:** indica onde a ideia está escrita. **Inferido** significa que não está escrito nos documentos (veio do diagrama ou da equipe) e precisa de confirmação.
- **Conflitos entre documentos não foram resolvidos aqui.** Estão listados na seção 7.

## 1. Escopo do módulo

O Plano de Projeto define o Painel Detalhes de Obra como o "módulo central para rotinas diárias (registro de ocorrências com fotos, aferição e aprovação de medições e consulta de revisões de projetos), edição de equipe e responsável e consulta de informações gerais".

| RF do SRS | Tema | Por que está no módulo |
| --- | --- | --- |
| RF-02 | Cadastro de obras, equipe e responsáveis técnicos | Edição de equipe e responsável |
| RF-05 | Aferição e aprovação de medições | Rotina diária do painel |
| RF-11 | Consulta de projetos executivos e revisões | Acesso a partir do painel; os arquivos pertencem ao módulo Contratos e Documentos (ver P-14) |
| RF-12 | Registro diário de ocorrências | Rotina diária do painel |
| RF-18 | Painel visual de cada obra | É o próprio painel |

**Dependências de outros módulos (não detalhadas aqui):**

- RF-00 e RF-13: usuários e login (módulo de acessos). Inclui a definição de cargos e permissões, e de quem é "usuário autorizado" (ver P-17 e P-23).
- RF-10: upload dos arquivos de projeto (Contratos e Documentos).
- RF-04: avanço físico (Cronograma).
- RF-06 e RF-19: orçamento (Orçamento).

## 2. Requisitos funcionais detalhados

### RF-02 — Cadastro e configuração de obras

> SRS: "O sistema deve permitir o cadastro de novas obras e projetos, possibilitando a inserção de parâmetros operacionais, atribuição de equipe e definição de responsáveis técnicos."
> Prioridade: Alta. Dependências: RF-00, RF-13. Caso de uso: UC03.

| ID | Requisito | Fonte |
| --- | --- | --- |
| RF-02.1 | O sistema deve permitir cadastrar uma nova obra. | SRS RF-02; UC03 |
| RF-02.2 | O sistema deve permitir cadastrar os parâmetros operacionais da obra. | SRS RF-02; UC03 (ver P-04) |
| RF-02.3 | O sistema deve permitir editar os parâmetros operacionais da obra. | SRS RF-02; UC03 (ver P-04) |
| RF-02.4 | O sistema deve permitir atribuir usuários à equipe da obra. | SRS RF-02; UC03 |
| RF-02.5 | O sistema deve permitir definir o responsável técnico da obra. | SRS RF-02; UC03 (ver P-15, P-21) |
| RF-02.6 | O sistema deve permitir editar a equipe de uma obra já cadastrada. | Plano (Painel Detalhes de Obra) |
| RF-02.7 | O sistema deve permitir editar o responsável técnico de uma obra já cadastrada. | Plano (Painel Detalhes de Obra) |
| RF-02.8 | O sistema deve permitir definir as permissões de acesso por perfil no contexto da obra. | UC03 (ver P-16). **Tratado no módulo de acessos** (ver P-23); este módulo apenas verifica as permissões |
| RF-02.9 | O sistema deve restringir o cadastro de obras a usuários autorizados. | UC03 ("usuário autorizado"; ver P-17) |
| RF-02.9 | O sistema deve restringir a edição de obras a usuários autorizados. | UC03 ("usuário autorizado"; ver P-17) |
| RF-02.10 | O sistema deve manter um status do ciclo de vida de cada obra. | Inferido; decisão da equipe (ver P-06) |
| RF-02.11 | O sistema deve registrar o número da ART do responsável técnico da obra. | Inferido; conhecimento de domínio informado pela equipe (ver P-15; **a confirmar**) |

### RF-05 — Aferição e aprovação de medições

> SRS: "O sistema deve permitir que a equipe de campo insira as quantidades de serviços executados e fornecer uma interface para o Gestor da Obra analisar, aprovar ou rejeitar a liberação do fluxo financeiro correspondente."
> Prioridade: Alta. Dependências: RF-02, RF-13. Referência no SRS: UC05 (o caso de uso correspondente é o **UC07**; ver P-01).

| ID | Requisito | Fonte |
| --- | --- | --- |
| RF-05.1 | A equipe de campo deve poder inserir as quantidades de serviços executados (aferição). | SRS RF-05; UC07; SRS 2.3 (Mestre de Obras: "aferições iniciais") |
| RF-05.2 | O Gestor da Obra deve poder analisar as medições aferidas. | SRS RF-05 |
| RF-05.3 | O Gestor da Obra deve poder aprovar uma medição. | SRS RF-05; UC07 |
| RF-05.4 | O Gestor da Obra deve poder rejeitar uma medição. | SRS RF-05 (o UC07 não menciona rejeição; ver P-13) |
| RF-05.5 | A aprovação de uma medição deve liberar o fluxo financeiro correspondente. | SRS RF-05; UC07 (ver P-12) |
| RF-05.6 | A rejeição de uma medição deve bloquear o fluxo financeiro correspondente. | SRS RF-05; UC07 (ver P-12) |
| RF-05.7 | Cada medição deve ter um estado (por exemplo: aferida, aprovada, rejeitada). | Inferido |
| RF-05.8 | O sistema deve permitir o envio de comprovantes de medições por upload de arquivo pelo navegador. | Plano 9.2 (ver P-03) |

### RF-11 — Consulta de projetos executivos e revisões

> SRS: "O sistema deve disponibilizar à equipe de campo o acesso e a visualização das versões mais recentes dos projetos executivos e especificações técnicas, conforme as restrições de conexão à internet."
> Prioridade: Alta. Dependências: RF-10, RF-13. Referência no SRS: UC11 (o caso de uso correspondente é o **UC13**; ver P-01).

| ID | Requisito | Fonte |
| --- | --- | --- |
| RF-11.1 | A equipe de campo deve poder acessar a versão mais recente de cada projeto executivo da obra. | SRS RF-11; UC13 |
| RF-11.2 | A equipe de campo deve poder visualizar a versão mais recente de cada projeto executivo da obra. | SRS RF-11; UC13 |
| RF-11.3 | A equipe de campo deve poder acessar a versão mais recente das especificações técnicas da obra. | SRS RF-11 |
| RF-11.3 | A equipe de campo deve poder visualizar a versão mais recente das especificações técnicas da obra. | SRS RF-11 |
| RF-11.4 | O sistema deve identificar qual é a revisão mais recente de cada projeto. | Inferido (implícito em RF-11 e UC13) |

### RF-12 — Registro diário de ocorrências

> SRS: "O sistema deve permitir o registro diário de ocorrências do canteiro de obras, armazenando informações de equipe, paralisações e clima."
> Prioridade: Média. Dependências: RF-02, RF-13. Referência no SRS: UC12 (o caso de uso correspondente é o **UC06**; ver P-01).

| ID | Requisito | Fonte |
| --- | --- | --- |
| RF-12.1 | O sistema deve permitir registrar diariamente as ocorrências de um canteiro de obras. | SRS RF-12; UC06 |
| RF-12.2 | O sistema deve registrar quais membros da equipe alocada estiveram presentes no dia; os ausentes são os alocados ativos que não constam na lista. | SRS RF-12 (ver P-07) |
| RF-12.3 | O sistema deve permitir registrar paralisações. | SRS RF-12; UC06 |
| RF-12.4 | Cada paralisação deve ter início e fim registrados | Inferido (Plano: rastrear "causas reais de atraso") |
| RF-12.5 | O sistema deve mensurar o impacto de cada paralisação no prazo | Inferido (Plano: rastrear "causas reais de atraso") |
| RF-12.6 | O sistema deve permitir armazenar o clima do dia. | SRS RF-12 (ver P-09) |
| RF-12.7 | O sistema deve permitir registrar imprevistos. | UC06 |
| RF-12.8 | O sistema deve permitir anexar fotos a uma ocorrência. | UC06; SRS (escopo); Plano 9.1 |
| RF-12.9 | O sistema deve oferecer suporte a "digitalização" no registro. | SRS (escopo) (ver P-11) |
| RF-12.10 | Cada ocorrência deve identificar o usuário que a registrou. | Inferido (rastreabilidade, UC06) |
| RF-12.11 | Cada ocorrência deve identificar o usuário que a editou. | Inferido (rastreabilidade, UC06; ver P-19) |
| RF-12.12 | Cada ocorrência deve identificar o usuário que a excluiu. | Inferido (rastreabilidade, UC06; ver P-19) |
| RF-12.13 | O sistema deve permitir consultar as ocorrências de um período. | Inferido |
| RF-12.14 | Cada ocorrência deve ter uma categoria. | Inferido (ver P-10) |
| RF-12.15 | Cada foto deve identificar o usuário que a enviou. | Inferido (rastreabilidade, UC06) |
| RF-12.16 | A edição e a exclusão de ocorrências e de registros diários não devem apagar dados: a edição registra quem editou e quando, e a exclusão é lógica. | Inferido; decisão da equipe (ver P-19, RN-15) |

### RF-18 — Painel visual da obra

> SRS: "O sistema deve exibir em um painel visual contendo uma descrição de cada obra individualmente, contendo um resumo sobre os últimos registros de ocorrência e imprevistos, mão de obra alocada a cada canteiro, resumo de orçamento financeiro, fotos, avanço físico e equipe responsável."
> Prioridade: Alta. Dependências: RF-02, RF-13. Caso de uso: UC03.

| ID | Requisito | Fonte |
| --- | --- | --- |
| RF-18.1 | O sistema deve exibir um painel individual para cada obra. | SRS RF-18 |
| RF-18.2 | O painel deve exibir a descrição da obra. | SRS RF-18; Plano |
| RF-18.3 | O painel deve exibir as informações gerais da obra. | SRS RF-18; Plano |
| RF-18.4 | O painel deve exibir um resumo dos últimos registros de ocorrências. | SRS RF-18 (ver P-18) |
| RF-18.5 | O painel deve exibir um resumo dos últimos registros de imprevistos. | SRS RF-18 (ver P-18) |
| RF-18.6 | O painel deve exibir as equipes alocadas a cada obra. | SRS RF-18 (ver P-08) |
| RF-18.7 | O painel deve exibir um resumo do orçamento financeiro. | SRS RF-18 |
| RF-18.8 | O painel deve exibir fotos da obra. | SRS RF-18 (ver P-18) |
| RF-18.9 | O painel deve exibir o avanço físico. | SRS RF-18 |
| RF-18.10 | O painel deve exibir o responsável técnico. | SRS RF-18 |
| RF-18.11 | O painel deve dar acesso às medições. | Plano |
| RF-18.12 | O painel deve dar acesso ocorrências. | Plano |
| RF-18.13 | O painel deve dar acesso aos projetos. | Plano |

## 3. Regras de negócio

| ID | Regra | Situação / Fonte |
| --- | --- | --- |
| RN-01 | Uma obra nunca é apagada; é cancelada por meio do status CANCELADA. | Inferido (decisão da equipe) |
| RN-02 | CONCLUIDA e CANCELADA são estados finais da obra. | Inferido |
| RN-03 | Registrar uma ocorrência de paralisação não altera o status da obra. | Inferido; coerente com SRS 2.4 |
| RN-04 | Um usuário só pode ter uma alocação ativa por obra, e uma alocação pode ter mais de um Cargo. | Inferido (decisão de modelagem, ver P-22) |
| RN-06 | Uma foto ligada a uma ocorrência deve pertencer à mesma obra da ocorrência. | Inferido |
| RN-07 | Existe no máximo um registro diário não excluído por dia. | Inferido (proposta de modelagem; ajustada pela RN-15) |
| RN-08 | O avanço físico é atualizado por inserção manual. | SRS 2.4 (Restrições de Automação) |
| RN-09 | O Gestor da Obra aprova ou rejeita medições. | SRS RF-05; UC07 (que somente ele possa fazê-lo é interpretação) |
| RN-10 | Anexos aceitam apenas PDF, PNG e JPG, com no máximo 10 MB por arquivo. | SRS RNF-10 |
| RN-11 | Não há operação offline: registrar ou consultar exige conexão. | SRS 2.4 e 2.5 |
| RN-12 | Projetos executivos são gerenciados apenas em 2D. | UC13; SRS (escopo) |
| RN-13 | A consulta de ocorrências por período filtra pela data do registro diário. | Inferido (decisão de modelagem) |
| RN-14 | Ao concluir ou cancelar uma obra, todas as suas alocações ativas são encerradas (`dataFim` = data do evento). Em PARALISADA as alocações continuam, porque a obra pode ser retomada. | Inferido; decisão da equipe (o estado da alocação é derivado de `dataFim`) |
| RN-15 | Ocorrências e registros diários podem ser editados ou excluídos, mas nada é apagado fisicamente: a edição registra quem editou e quando, e a exclusão é lógica e registra quem excluiu e quando. | Inferido; decisão da equipe (ver P-19, RF-12.11, RF-12.12) |
| RN-16 | Uma medição rejeitada é final; uma nova aferição cria uma nova medição. | Inferido; decisão da equipe (ver P-13) |
| RN-17 | Aprovar ou rejeitar uma medição apenas muda o seu estado e sinaliza a liberação ou o bloqueio do fluxo financeiro; o módulo não executa lógica financeira. | Inferido; decisão da equipe (ver P-12; o SRS exclui transações financeiras diretas) |

## 4. Requisitos não funcionais aplicáveis

| ID | Resumo | Efeito no módulo |
| --- | --- | --- |
| RNF-00 | Telas principais em até 4 s (90% dos casos) | O painel agrega muitos dados; vale para RF-18 |
| RNF-02 | Interface responsiva, incluindo celular e tablet | Mestre de Obras usa o diário em campo |
| RNF-03 | Acesso aos dados da obra só para usuários autenticados e com permissão explícita | Define quem vê cada painel |
| RNF-05 | Dados pessoais e trabalhistas conforme a LGPD | Equipe, alocações e autores de registros |
| RNF-10 | Anexos em PDF, PNG ou JPG, até 10 MB | Fotos de ocorrências e projetos |

## 5. Atores e permissões no módulo

Legenda: **✓** = definido nos documentos. **✓\*** = interpretação a confirmar. **?** = não definido.

| Funcionalidade | Administrador | Gestor da Obra | Eng. Orçamentário | Estoquista | Mestre de Obras |
| --- | --- | --- | --- | --- | --- |
| Cadastrar obra (RF-02.1) | ✓ | ? | ? | ? | ? |
| Editar equipe e responsável (RF-02.5) | ✓ | ✓\* | ? | ? | ? |
| Consultar painel da obra (RF-18) | ✓ | ✓\* | ? | ? | ? |
| Ver resumo de orçamento no painel (RF-18.5) | ✓ | ✓\* | ✓\* | ? | ? |
| Registrar ocorrências (RF-12) | ✓ | ✓\* | ? | ? | ✓ |
| Lançar quantidades executadas (RF-05.1) | ✓ | ✓\* | ? | ? | ✓ |
| Aprovar ou rejeitar medição (RF-05.3, 05.4) | ✓ | ✓ | ? | ? | ? |
| Consultar projetos e revisões (RF-11) | ✓ | ✓\* | ? | ? | ✓ |

Base no Plano de Projeto: Administrador tem "acesso global e irrestrito". Gestor da Obra tem "acesso pleno de gestão às obras sob sua responsabilidade". Engenheiro Orçamentário tem acesso aos módulos financeiros. Mestre de Obras tem "acesso operacional de campo".

**Observação:** as permissões são definidas e mantidas no módulo de acessos (P-17, P-23). Esta matriz só indica o que este módulo espera encontrar; ele verifica a permissão, não a define.

## 6. Glossário

As definições são paráfrases nossas. Só "Canteiro de obras" reproduz o glossário do SRS (1.3). Onde a coluna Fonte diz "Termo em", apenas o termo aparece no documento e a definição precisa de validação.

| Termo | Definição | Fonte |
| --- | --- | --- |
| Obra | Projeto de construção cadastrado no sistema. O SRS diz "obras e projetos" (ver P-05). | Termo em SRS RF-02 |
| Canteiro de obras | Ambiente físico onde a construção é realizada. | SRS 1.3 |
| Ocorrência | Imprevisto ou paralisação de campo, incluindo intempéries. | UC06 |
| Registro diário | Registro cronológico, feito por canteiro, do que ocorreu no dia (clima, equipe, ocorrências). | SRS RF-12; proposta de modelagem |
| Paralisação | Interrupção das atividades do canteiro. | Inferido |
| Intempérie | Evento climático adverso que afeta o trabalho. | Termo em UC06 |
| Aferição / medição | Apuração das quantidades de serviços executados, aprovada pelo Gestor da Obra. | UC07 |
| Projeto executivo | Projeto técnico de execução, gerenciado em 2D (PDF ou imagem), com revisões. | Termo em UC13 |
| Revisão | Versão atualizada de um projeto executivo. | UC13 |
| Responsável técnico | Usuário indicado como responsável técnico da obra (`Obra.responsavel`), identificado pelo número da ART. | Termo em SRS RF-02; definição por decisão de modelagem (ver P-15, P-21; **a confirmar**) |
| ART | Anotação de Responsabilidade Técnica: documento legal, registrado no CREA (RRT no CAU, para arquitetos), que vincula o profissional à obra; seu número identifica o responsável técnico. **Não aparece nos documentos do projeto.** | Inferido; conhecimento de domínio informado pela equipe (ver P-15) |
| Cargo | Papel do usuário, com permissões. Termo padronizado: os documentos também usam "perfil" (SRS) e "papel" (Plano). Ver P-16. | UC01; SRS; Plano |

## 7. Pendências e conflitos

| ID | Pendência | Por que importa |
| --- | --- | --- |
| P-01 | As referências de UC no SRS não batem com o documento de casos de uso: RF-05 aponta UC05 (deveria ser UC07), RF-11 aponta UC11 (UC13) e RF-12 aponta UC12 (UC06). | Quebra a rastreabilidade. Este documento usa o mapeamento por conteúdo. |
| P-02 | O RF-00 diz "remover" usuário, e o UC01 e o Plano dizem "deleção". A nota do diagrama diz "desativar, não apagar". **Dependência externa:** este módulo só exige que o usuário não seja apagado fisicamente, para `AlocacaoObra` e os autores continuarem válidos. A política é da equipe de acessos. | `AlocacaoObra` e o autor de `Ocorrencia` apontam para o usuário. Precisa ser alinhado com a equipe de acessos e com a LGPD (RNF-05). |
| P-03 | O Plano 9.2 cita o "envio de comprovantes de medições", mas nenhum RF ou UC descreve esse anexo (tipo de arquivo, obrigatoriedade). | Pode exigir anexos na medição. |
| P-04 | "Parâmetros operacionais" (RF-02) não é definido nos documentos. **Decisão da equipe:** mantêm-se em `Obra` nome, tipologia, endereco, inicioPrevisto e fimPrevisto (todos inferidos) e acrescenta-se `descricao` (RF-18.2). | Levar a definição ao SRS. |
| P-05 | O SRS fala em "obras e projetos". São a mesma entidade? | Pode haver uma classe a mais. |
| P-06 | Nenhum documento define o status da obra ou suas transições. **Decisão da equipe:** `StatusObra` é mantido (PLANEJADA, EM_ANDAMENTO, PARALISADA, CANCELADA, CONCLUIDA) e não é derivado do avanço físico (SRS 2.4). **Transições definidas** em `state-diagrams.md` (DE-01), com a ação de RN-14 em concluir e cancelar. | Levar ao SRS. |
| P-07 | "Informações de equipe" (RF-12) não é definido. **Decisão da equipe (revista):** `equipePresente` registra quais membros alocados estiveram presentes (`AlocacaoObra[]`); os ausentes saem por diferença (alocações ativas não listadas). Não registra horas, então não é controle de ponto, que o SRS exclui. | Levar a definição ao SRS. |
| P-08 | RF-18 pede "mão de obra alocada a cada canteiro", mas `AlocacaoObra` liga usuário e obra, não canteiro. | Talvez falte alocação por canteiro, ou o dado venha do registro diário. |
| P-09 | Relação entre "clima" (RF-12) e "intempérie" (UC06). **Decisão da equipe:** o `clima` do dia fica em `RegistroDiario`; INTEMPERIE é a categoria de uma ocorrência causada pelo clima. Os dois coexistem. | Levar a definição ao SRS. |
| P-10 | As categorias LOGISTICA e SEGURANCA não aparecem nos documentos. **Decisão da equipe:** são mantidas e acrescenta-se OUTRO, para cobrir os "imprevistos" do UC06 que não sejam clima nem paralisação. | Levar a definição ao SRS. |
| P-11 | O que é "digitalização" (SRS, escopo)? Foto de documento em papel? | Pode exigir um tipo de anexo diferente. |
| P-12 | O que significa "liberar o fluxo financeiro", se o SRS exclui transações financeiras diretas? **Resolvida:** aprovar ou rejeitar apenas muda o estado da medição e sinaliza a liberação ou o bloqueio, sem lógica financeira (RN-17). | Define o efeito da aprovação de medição. |
| P-13 | RF-05 prevê rejeitar medição, o UC07 não. **Resolvida:** REJEITADA é estado final; uma nova aferição cria uma nova medição (RN-16). | Define o ciclo de estados da medição. |
| P-14 | O diagrama não tinha classes para medição nem para projeto executivo e revisão. **Resolvida:** `Medicao` entra no diagrama (RF-05). Projetos e revisões (RF-11) ficam de fora: os arquivos pertencem ao RF-10 (Contratos e Documentos), e este módulo apenas consulta. | Define o escopo dos diagramas dinâmicos (UC07 dentro, UC13 fora). |
| P-15 | ART não está nos documentos; "responsáveis técnicos" está (RF-02, UC03). Estava resolvida (responsável técnico = usuário alocado com um Cargo específico, ART removida). **Reaberta pela equipe:** a ART é o documento legal que identifica o responsável técnico. **Proposta, a confirmar:** manter `Obra.responsavel` e acrescentar `art : String` com o número (RF-02.11). Alternativa mais pesada: uma classe `ART` própria, só se houver mais de uma por obra. | Ver P-21 e P-22. |
| P-16 | **Leitura adotada:** `Cargo` é o papel do usuário, com permissões. Base: o Plano fala em permissões "atrelado aos papéis desempenhados na organização" e o RF-15 liga usuários, permissões e obras. | Confirmar com a equipe de acessos. |
| P-17 | Quem é o "usuário autorizado" a cadastrar obras (UC03)? **Dependência externa:** é definido pelo módulo de acessos; este módulo apenas verifica a permissão. | Define a permissão de RF-02.1. |
| P-18 | No painel, "últimos registros" são quantos ou de qual período? "Fotos" são quais? | Define consultas e relações de `RegistroFotografico`. |
| P-19 | Ocorrências podem ser editadas ou apagadas depois de registradas? Estava decidido que não (registro imutável), o que contradizia RF-12.11 e RF-12.12. **Decisão da equipe (revista):** podem ser editadas e excluídas **com trilha de auditoria**, sem apagar nada fisicamente. A edição registra autor e data; a exclusão é lógica; a RN-07 conta só os registros não excluídos. Vale também para o `RegistroDiario` (RN-15). | Levar a regra ao SRS. |
| P-20 | O Plano 9.1 cita anexar "recibos" às ocorrências. Isso vale, dado o RNF-10? | Define o tipo de arquivo aceito. |
| P-21 | O UC01 permite personalizar cargos. Como o sistema identifica o responsável técnico? **Proposta, a confirmar (junto com a P-15):** o responsável é o usuário em `Obra.responsavel`, identificado pelo número da ART, sem depender de um atributo em `Cargo` (módulo de acessos). A obra só pode ser iniciada com responsável definido. | Define a guarda de `iniciar()` e o UC03. |
| P-22 | A unicidade de alocação por usuário e obra (RN-04) impediria que o mesmo usuário fosse, por exemplo, Gestor da Obra e outro papel técnico, pois seriam dois Cargos. O SRS descreve o Gestor como "Engenheiro Residente / Gerente de Projetos", um perfil técnico. **Resolvida:** `AlocacaoObra` aceita um ou mais Cargos, e a unicidade vale para alocações ativas por (usuário, obra). Decisão de modelagem. | Define a RN-04. |
| P-23 | O UC01 atribui ao Administrador a definição de cargos, permissões e níveis de acesso, e o UC03 menciona "a definição das permissões de acesso por perfil" no cadastro da obra. **Resolvida:** permissões são definidas no módulo de acessos; este módulo apenas as verifica (RF-02.8 fica como dependência externa). | Remove a sobreposição entre os módulos; afeta o RF-02.6. |

## 8. Rastreabilidade com o diagrama de classes

Estado do diagrama depois das decisões do RF-02 (alocação com vários Cargos e `dataFim`), do RF-12 (autoria, categoria OUTRO, edição com auditoria) e do RF-05 (`Medicao` incluída).

| Classe | Requisitos e regras | Observação |
| --- | --- | --- |
| `Obra` | RF-02.1 a 02.5, 02.10, 02.11; RF-18.2, 18.10; RN-01 a RN-04, RN-14 | `descricao` cobre o RF-18.2; os demais atributos são decisão da equipe (P-04). `responsavel` e `art` mantidos (P-15). Eventos de status em `state-diagrams.md` |
| `StatusObra` | RF-02.10; RN-02, RN-03 | Inferido (P-06) |
| `CanteiroDeObras` | RN-01; RF-12.1; RF-18.4 | Externa ao diagrama |
| `AlocacaoObra` | RF-02.4, 02.6; RF-18.6; RN-04, RN-14 | Vários Cargos por alocação. Estado derivado de `dataFim`, que encerra a alocação; manter histórico é recomendação, não consta nos documentos |
| `Cargo` | RF-02.4, 02.6 | Ver P-16 |
| `RegistroDiario` | RF-12.1, 12.2, 12.3, 12.6; RN-07, RN-15 | `equipePresente` lista alocações presentes (P-07); campos de auditoria (P-19) |
| `Ocorrencia` | RF-12.4, 12.5, 12.7, 12.10 a 12.12, 12.16; RF-18.4, 18.5; RN-03, RN-15 | Autor via `registrarOcorrencia(..., autor)`; campos de auditoria (P-19) |
| `CategoriaOcorrencia` | RF-12.7, 12.14 | `OUTRO` cobre os imprevistos em geral (P-10) |
| `CondicaoClimatica` | RF-12.6 | Valores são proposta |
| `RegistroFotografico` | RF-12.8, 12.15; RF-18.8; RN-06; RN-10 | Quem enviou: relação com `Usuario` (inferido) |
| `Medicao` | RF-05.1 a 05.7; RN-09, RN-16, RN-17 | Nova (P-14). Descrição de serviço, quantidade e unidade são decisão de modelagem; comprovantes ficam de fora (P-03) |
| `EstadoMedicao` | RF-05.7; RN-16 | Nova. AFERIDA, APROVADA e REJEITADA (final) |
| `Usuario` | RF-12.10; RF-02.3 | Ver P-02 |

**Requisitos sem classe no diagrama:**

- **RF-11 (projetos e revisões):** não existe classe de projeto executivo ou revisão, por decisão da P-14. Os arquivos pertencem ao RF-10 (Contratos e Documentos); este módulo apenas consulta.
- **RF-18.5 e RF-18.7:** dados externos de Orçamento e Cronograma, já representados como classes externas.
- **RF-02.8:** definição de permissões, tratada no módulo de acessos (P-23).

## 9. Alterações pendentes no diagrama de classes

| Classe | Alteração | Origem |
| --- | --- | --- |
| `Obra` | Acrescentar `iniciar()`, `paralisar()`, `retomar()`, `concluir()`, `cancelar()` | P-06; `state-diagrams.md` (DE-01) |
| `Obra` | Acrescentar `art : String` e manter `responsavel` e `alterarResponsavel()` | P-15, P-21 (a confirmar) |
| `Obra` | Acrescentar `aferirMedicao(descricao : String, quantidade : double, unidade : String, autor : Usuario) : Medicao` e a relação com `Medicao` | P-14 |
| `RegistroDiario` | `autor : Usuario[]` passa a `autor : Usuario` | Erro de multiplicidade |
| `RegistroDiario` | `equipePresente : Usuario[]` passa a `equipePresente : AlocacaoObra[]` | P-07 |
| `RegistroDiario` e `Ocorrencia` | Acrescentar `editadoPor`, `editadoEm`, `excluidoPor`, `excluidoEm` | P-19, RN-15 |
| `Ocorrencia` | `autor : Usuario[]` passa a `autor : Usuario` | Erro de multiplicidade |
| `RegistroDiario` | `alterarOcorrencia` e `excluirOcorrencia` passam a receber a ocorrência como parâmetro; `excluirOcorrencia` devolve a ocorrência (exclusão lógica) | P-19 |
| `Medicao`, `EstadoMedicao` | Acrescentar as duas (rascunho abaixo) | P-14 |
