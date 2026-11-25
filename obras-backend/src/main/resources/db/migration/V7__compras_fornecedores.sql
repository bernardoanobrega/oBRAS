-- Alinhado ao diagrama de classes de Compras e Fornecedores.
CREATE TABLE fornecedor (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_razao_social  VARCHAR(200) NOT NULL,
    cpf_cnpj           VARCHAR(18)  NOT NULL,
    email              VARCHAR(150),
    telefone           VARCHAR(20),
    situacao           VARCHAR(10)  NOT NULL
        CONSTRAINT ck_fornecedor_situacao CHECK (situacao IN ('ATIVO','INATIVO')),
    CONSTRAINT uk_fornecedor_cpf_cnpj UNIQUE (cpf_cnpj)
);

CREATE TABLE pedido_compra (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id           BIGINT NOT NULL REFERENCES obra(id),
    usuario_id        BIGINT NOT NULL REFERENCES usuario(id),           -- solicitante
    fornecedor_id     BIGINT REFERENCES fornecedor(id),                 -- 0..1: definido ao selecionar a cotação
    data_solicitacao  DATE NOT NULL,
    data_prevista     DATE,
    valor_total       NUMERIC(15,2) NOT NULL DEFAULT 0 CHECK (valor_total >= 0),
    status            VARCHAR(15) NOT NULL
        CONSTRAINT ck_pedido_status CHECK (status IN
            ('SOLICITADO','EM_COTACAO','COMPRADO','EM_TRANSITO','ENTREGUE','CANCELADO')),
    prioridade        VARCHAR(10) NOT NULL
        CONSTRAINT ck_pedido_prioridade CHECK (prioridade IN ('BAIXA','MEDIA','ALTA')),
    CONSTRAINT ck_pedido_datas CHECK (data_prevista IS NULL OR data_prevista >= data_solicitacao)
);
CREATE INDEX ix_pedido_obra_status ON pedido_compra(obra_id, status);
CREATE INDEX ix_pedido_fornecedor  ON pedido_compra(fornecedor_id);
CREATE INDEX ix_pedido_usuario     ON pedido_compra(usuario_id);

CREATE TABLE item_pedido (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_compra_id  BIGINT NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    material_id       BIGINT NOT NULL REFERENCES material(id),
    -- 0..1. SEM FK por enquanto: a tabela item_orcado nasce com o módulo Orçamento,
    -- que deve acrescentar: ALTER TABLE item_pedido ADD CONSTRAINT fk_item_pedido_item_orcado
    --     FOREIGN KEY (item_orcado_id) REFERENCES item_orcado(id);
    item_orcado_id    BIGINT,
    quantidade        NUMERIC(15,3) NOT NULL CHECK (quantidade > 0),
    valor_unitario    NUMERIC(15,2) NOT NULL CHECK (valor_unitario >= 0)
);
CREATE INDEX ix_item_pedido_pedido   ON item_pedido(pedido_compra_id);
CREATE INDEX ix_item_pedido_material ON item_pedido(material_id);

CREATE TABLE cotacao (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_compra_id      BIGINT NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    fornecedor_id         BIGINT NOT NULL REFERENCES fornecedor(id),
    data_cotacao          TIMESTAMP NOT NULL DEFAULT now(),
    valor_total_proposto  NUMERIC(15,2) NOT NULL CHECK (valor_total_proposto > 0),
    prazo_entrega_dias    INTEGER NOT NULL CHECK (prazo_entrega_dias >= 0),
    selecionada           BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX ix_cotacao_pedido     ON cotacao(pedido_compra_id);
CREATE INDEX ix_cotacao_fornecedor ON cotacao(fornecedor_id);
-- no máximo uma cotação selecionada por pedido
CREATE UNIQUE INDEX uk_cotacao_selecionada ON cotacao(pedido_compra_id) WHERE selecionada;

CREATE TABLE historico_status_pedido (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_compra_id  BIGINT NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    status            VARCHAR(15) NOT NULL
        CONSTRAINT ck_hist_pedido_status CHECK (status IN
            ('SOLICITADO','EM_COTACAO','COMPRADO','EM_TRANSITO','ENTREGUE','CANCELADO')),
    data_hora         TIMESTAMP NOT NULL DEFAULT now(),
    usuario_id        BIGINT NOT NULL REFERENCES usuario(id)
);
CREATE INDEX ix_hist_pedido         ON historico_status_pedido(pedido_compra_id, data_hora);
CREATE INDEX ix_hist_pedido_usuario ON historico_status_pedido(usuario_id);
