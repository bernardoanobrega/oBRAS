-- Módulo Orçamento (UC10 | RF-30 a RF-34, RF-74).
-- IndicadorSaudeFinanceira não tem tabela: é calculado a partir de itens, custos e decisões.
-- Orçamento nunca é apagado: cancelar() só altera o status.

CREATE TABLE orcamento (
    id                        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id                   BIGINT       NOT NULL REFERENCES obra(id),
    data_elaboracao           DATE         NOT NULL,
    data_ultima_atualizacao   DATE         NOT NULL,
    percentual_limite_alerta  NUMERIC(5,2) NOT NULL,
    status                    VARCHAR(15)  NOT NULL,
    CONSTRAINT uk_orcamento_obra_id UNIQUE (obra_id),
    CONSTRAINT ck_orcamento_percentual_limite_alerta
        CHECK (percentual_limite_alerta > 0 AND percentual_limite_alerta <= 100),
    CONSTRAINT ck_orcamento_status
        CHECK (status IN ('ELABORACAO','SAUDAVEL','ATENCAO','CRITICA','CANCELADO'))
);

CREATE TABLE item_orcado (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orcamento_id           BIGINT        NOT NULL REFERENCES orcamento(id) ON DELETE CASCADE,
    codigo                 VARCHAR(50)   NOT NULL,
    descricao              VARCHAR(300)  NOT NULL,
    unidade                VARCHAR(20)   NOT NULL,
    quantidade             NUMERIC(15,4) NOT NULL,
    custo_unitario_orcado  NUMERIC(15,2) NOT NULL,
    CONSTRAINT uk_item_orcado_orcamento_id_codigo UNIQUE (orcamento_id, codigo),
    CONSTRAINT ck_item_orcado_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_orcado_custo_unitario_orcado CHECK (custo_unitario_orcado >= 0)
);

CREATE TABLE custo_realizado (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_orcado_id   BIGINT        NOT NULL REFERENCES item_orcado(id) ON DELETE CASCADE,
    usuario_id       BIGINT        NOT NULL REFERENCES usuario(id),
    data_lancamento  DATE          NOT NULL,
    descricao        VARCHAR(300)  NOT NULL,
    valor            NUMERIC(15,2) NOT NULL,
    origem           VARCHAR(25)   NOT NULL,
    id_origem        BIGINT,
    CONSTRAINT ck_custo_realizado_valor CHECK (valor > 0),
    CONSTRAINT ck_custo_realizado_origem
        CHECK (origem IN ('MATERIAL','MAO_DE_OBRA','EQUIPAMENTO','SERVICO_TERCEIRIZADO','OUTRO')),
    CONSTRAINT ck_custo_realizado_id_origem CHECK (id_origem IS NULL OR origem <> 'OUTRO')
);
CREATE INDEX ix_custo_realizado_item_orcado_id ON custo_realizado(item_orcado_id);
CREATE INDEX ix_custo_realizado_usuario_id ON custo_realizado(usuario_id);

CREATE TABLE decisao_financeira (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orcamento_id   BIGINT        NOT NULL REFERENCES orcamento(id) ON DELETE CASCADE,
    usuario_id     BIGINT        NOT NULL REFERENCES usuario(id),
    data_decisao   DATE          NOT NULL,
    descricao      VARCHAR(500)  NOT NULL,
    justificativa  VARCHAR(2000) NOT NULL,
    valor_impacto  NUMERIC(15,2) NOT NULL
);
CREATE INDEX ix_decisao_financeira_orcamento_id ON decisao_financeira(orcamento_id);
CREATE INDEX ix_decisao_financeira_usuario_id ON decisao_financeira(usuario_id);
