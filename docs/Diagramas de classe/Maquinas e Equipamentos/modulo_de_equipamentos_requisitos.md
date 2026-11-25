# Módulo de Máquinas e Ferramentas (Equipamentos)
*Documento de Requisitos Atualizado*

Este documento consolida os requisitos, permissões e regras de negócio exclusivas do módulo de Equipamentos, desconsiderando os fluxos de materiais de consumo.

## 1. Glossário Específico

| Termo | Definição |
| --- | --- |
| Equipamento | Ativo físico não consumível (máquina ou ferramenta) controlado por unidade patrimonial. |
| Alocação | Vínculo temporário de um equipamento a um canteiro de obra. |
| Base | Local de origem dos equipamentos que não possuem alocação ativa (depósito central da empresa). |
| Manutenção | Registro do período em que o equipamento está indisponível para conserto (corretiva) ou revisão (preventiva). |

## 2. Atores e Permissões

| Capacidade | Gestor da Obra | Mestre de Obras |
| --- | --- | --- |
| Cadastrar, editar e inativar equipamentos | **Executa** | — |
| Registrar manutenção preventiva (manual) | **Executa** | — |
| Alocar equipamento (da Base para Canteiro) | Consulta | **Executa** |
| Registrar devolução (do Canteiro para Base) | Consulta | **Executa** |
| Consultar disponibilidade e painéis | Consulta | Consulta |

## 3. Requisitos Funcionais

### RFP-E01 — Cadastro de Equipamento
| ID | Requisito |
| --- | --- |
| RFP-E01.1 | O sistema deve permitir ao Gestor da Obra cadastrar equipamentos com código (patrimônio) único e descrição. |
| RFP-E01.2 | O equipamento deve ter um tipo (`MAQUINA`, `FERRAMENTA` ou `OUTRO`). |
| RFP-E01.3 | O equipamento deve ter origem `PROPRIO` ou `LOCADO`; se `LOCADO`, informar obrigatoriamente a data `fimLocacao`. |
| RFP-E01.4 | O sistema deve gerar um alerta visual (ex: "Locação a Vencer") para equipamentos locados que se aproximam da data de `fimLocacao`. |
| RFP-E01.5 | O equipamento deve poder ser desativado (`INATIVO`), nunca excluído. |
| RFP-E01.6 | Cada registro no sistema representa uma unidade física única. |

### RFP-E02 — Manutenção
| ID | Requisito |
| --- | --- |
| RFP-E02.1 | O sistema deve permitir registrar manutenção do tipo `PREVENTIVA` (ação manual do Gestor) ou `CORRETIVA` (gerada automaticamente por devolução com dano). |
| RFP-E02.2 | A operação de envio manual para manutenção preventiva exigirá data de início e descrição, e mudará o status para `EM_MANUTENCAO`. |
| RFP-E02.3 | Durante a manutenção, o equipamento não pode ser alocado. |
| RFP-E02.4 | A conclusão da manutenção deve registrar a data de fim e devolver o equipamento ao status `DISPONIVEL`. |
| RFP-E02.5 | O histórico de manutenções deve ser preservado de forma imutável. |

### RF-19 — Alocação e Devolução
| ID | Requisito |
| --- | --- |
| RF-19.1 | O Mestre de Obras deve poder alocar um equipamento `DISPONIVEL` ao seu canteiro, registrando data de início, data prevista de devolução, responsável e observação. |
| RF-19.2 | A devolução deve registrar a data de fim e a condição física (`BOA` ou `DANIFICADO`); o equipamento volta automaticamente à Base (status `DISPONIVEL`). |
| RF-19.3 | Devolução com condição `DANIFICADO` deve abrir automaticamente um registro de manutenção corretiva (mudando o status para `EM_MANUTENCAO`). |
| RF-19.4 | A transferência entre canteiros deve ser feita via sistema como uma operação dupla: devolução para a Base e nova alocação para o destino. |
| RF-19.5 | Alocações encerradas devem ser preservadas como histórico. |

### RF-20 — Disponibilidade
| ID | Requisito |
| --- | --- |
| RF-20.1 | O sistema deve exibir a situação atual do equipamento: `DISPONIVEL`, `ALOCADO`, `EM_MANUTENCAO` ou `INATIVO`. |
| RF-20.2 | O sistema deve exibir a localização atual: o canteiro (se `ALOCADO`) ou a Base (para outros status). |
| RF-20.3 | O sistema deve sinalizar visualmente "ATRASADO" quando a data prevista de devolução passar sem que a devolução tenha ocorrido. |

### RF-34 — Painel de Máquinas Alocadas por Obra
| ID | Requisito |
| --- | --- |
| RF-34.1 | O painel deve listar os equipamentos alocados à obra, com canteiro, data de início, data prevista de devolução e responsável. |
| RF-34.2 | O painel deve destacar visualmente equipamentos com devolução atrasada. |

## 4. Regras de Negócio (RN-EQP)

*   **RN-EQP-01 (Unicidade de Alocação):** Um equipamento tem no máximo uma alocação ativa por vez.
*   **RN-EQP-02 (Localização Lógica):** Sem alocação ativa, o equipamento está logicamente/fisicamente na "Base".
*   **RN-EQP-03 (Restrição de Alocação):** Equipamento com status `EM_MANUTENCAO` ou `INATIVO` não pode ser alocado a nenhum canteiro.
*   **RN-EQP-04 (Restrição de Manutenção):** Apenas equipamentos com status `DISPONIVEL` podem ser enviados manualmente para manutenção preventiva. Equipamentos `ALOCADO`s devem ser devolvidos primeiro.
*   **RN-EQP-05 (Bloqueio de Encerramento de Obra):** O sistema deve impedir que uma obra seja movida para o status `CONCLUIDA` ou `CANCELADA` caso possua equipamentos com status `ALOCADO` em seus canteiros. O Mestre de Obras deve realizar a devolução explícita no sistema antes do encerramento.
*   **RN-EQP-06 (Restrição de Prazo de Locação):** A "data prevista de devolução" de uma alocação de um equipamento `LOCADO` não pode ultrapassar o seu atributo `fimLocacao`.
*   **RN-EQP-07 (Integridade de Dados):** Equipamento e histórico de alocações/manutenções nunca são excluídos do sistema, garantindo rastreabilidade contábil e operacional.
*   **RN-EQP-08 (Cálculo de Atraso):** "Atrasado" não é um estado na máquina de estados do equipamento, mas uma interface visual derivada da comparação entre a data atual e a data prevista de devolução.