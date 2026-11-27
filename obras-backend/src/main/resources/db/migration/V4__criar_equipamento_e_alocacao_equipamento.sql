-- Módulo de Máquinas e Equipamentos (Diagrama de Classes de Projeto).
-- Depende da V3 (tabela auditavel).
-- Equipamento e alocação nunca são excluídos fisicamente: a desativação usa o status INATIVO.

CREATE TABLE equipamento (
    id                 BIGINT PRIMARY KEY REFERENCES auditavel(id),
    codigo_patrimonio  VARCHAR(50)  NOT NULL,
    descricao          VARCHAR(300) NOT NULL,
    tipo               VARCHAR(15)  NOT NULL
        CONSTRAINT ck_equipamento_tipo CHECK (tipo IN ('MAQUINA','FERRAMENTA','OUTRO')),
    status             VARCHAR(15)  NOT NULL
        CONSTRAINT ck_equipamento_status CHECK (status IN ('DISPONIVEL','ALOCADO','INATIVO')),
    CONSTRAINT uk_equipamento_codigo_patrimonio UNIQUE (codigo_patrimonio)
);
CREATE INDEX ix_equipamento_status ON equipamento(status);

CREATE TABLE alocacao_equipamento (
    id                       BIGINT PRIMARY KEY REFERENCES auditavel(id),
    equipamento_id           BIGINT NOT NULL REFERENCES equipamento(id),
    canteiro_id              BIGINT NOT NULL REFERENCES canteiro(id),
    responsavel_id           BIGINT NOT NULL REFERENCES usuario(id),
    data_inicio              DATE NOT NULL,
    data_prevista_devolucao  DATE NOT NULL,
    data_fim                 DATE,
    observacao               VARCHAR(1000),
    observacao_devolucao     VARCHAR(1000),
    CONSTRAINT ck_alocacao_equipamento_prevista
        CHECK (data_prevista_devolucao >= data_inicio),
    CONSTRAINT ck_alocacao_equipamento_fim
        CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    CONSTRAINT ck_alocacao_equipamento_observacao_devolucao
        CHECK (observacao_devolucao IS NULL OR data_fim IS NOT NULL)
);
CREATE INDEX ix_alocacao_equipamento_equipamento ON alocacao_equipamento(equipamento_id, data_inicio);
CREATE INDEX ix_alocacao_equipamento_canteiro    ON alocacao_equipamento(canteiro_id);
CREATE INDEX ix_alocacao_equipamento_responsavel ON alocacao_equipamento(responsavel_id);

-- Invariante: no máximo uma alocação ativa (data_fim vazia) por equipamento.
CREATE UNIQUE INDEX uk_alocacao_equipamento_ativa
    ON alocacao_equipamento(equipamento_id) WHERE data_fim IS NULL;