CREATE TABLE obra (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome             VARCHAR(200) NOT NULL,
    descricao        VARCHAR(1000),
    tipologia        VARCHAR(20)  NOT NULL
        CONSTRAINT ck_obra_tipologia CHECK (tipologia IN
            ('RESIDENCIAL','COMERCIAL','INDUSTRIAL','INFRAESTRUTURA','REFORMA','RESTAURO')),
    endereco         VARCHAR(300) NOT NULL,
    inicio_previsto  DATE NOT NULL,
    fim_previsto     DATE NOT NULL,
    status           VARCHAR(20)  NOT NULL
        CONSTRAINT ck_obra_status CHECK (status IN
            ('PLANEJADA','EM_ANDAMENTO','PARALISADA','CANCELADA','CONCLUIDA')),
    art              VARCHAR(50),
    responsavel_id   BIGINT NOT NULL REFERENCES usuario(id),
    CONSTRAINT ck_obra_periodo CHECK (fim_previsto >= inicio_previsto)
);
CREATE INDEX ix_obra_responsavel ON obra(responsavel_id);
CREATE INDEX ix_obra_status      ON obra(status);

-- Regra de negócio (fora do DDL): Obra possui 1..* Canteiro
CREATE TABLE canteiro (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id        BIGINT NOT NULL REFERENCES obra(id) ON DELETE CASCADE,
    nome_canteiro  VARCHAR(150) NOT NULL
);
CREATE INDEX ix_canteiro_obra ON canteiro(obra_id);

CREATE TABLE alocacao_obra (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id      BIGINT NOT NULL REFERENCES obra(id) ON DELETE CASCADE,
    usuario_id   BIGINT NOT NULL REFERENCES usuario(id),
    data_inicio  DATE NOT NULL,
    data_fim     DATE,
    CONSTRAINT ck_alocacao_obra_periodo CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);
CREATE INDEX ix_alocacao_obra_obra    ON alocacao_obra(obra_id);
CREATE INDEX ix_alocacao_obra_usuario ON alocacao_obra(usuario_id);

CREATE TABLE alocacao_obra_cargo (
    alocacao_obra_id  BIGINT NOT NULL REFERENCES alocacao_obra(id) ON DELETE CASCADE,
    cargo_id          BIGINT NOT NULL REFERENCES cargo(id),
    PRIMARY KEY (alocacao_obra_id, cargo_id)
);
CREATE INDEX ix_alocacao_obra_cargo_cargo ON alocacao_obra_cargo(cargo_id);

-- Um usuário só pode ter uma alocação ativa por obra
CREATE UNIQUE INDEX uk_alocacao_ativa ON alocacao_obra(obra_id, usuario_id) WHERE data_fim IS NULL;
