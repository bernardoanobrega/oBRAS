-- Alinhado ao diagrama: saldo por (material, CANTEIRO); movimentação com origem/destino e autor.
-- Quantidades em NUMERIC(15,3) (não double) para que "saldo == 0" seja exato.
CREATE TABLE saldo_estoque (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id        BIGINT NOT NULL REFERENCES material(id),
    canteiro_id        BIGINT NOT NULL REFERENCES canteiro(id),
    quantidade_atual   NUMERIC(15,3) NOT NULL DEFAULT 0 CHECK (quantidade_atual >= 0),
    quantidade_minima  NUMERIC(15,3) NOT NULL DEFAULT 0 CHECK (quantidade_minima >= 0),
    CONSTRAINT uk_saldo_material_canteiro UNIQUE (material_id, canteiro_id)
);
CREATE INDEX ix_saldo_canteiro ON saldo_estoque(canteiro_id);

CREATE TABLE movimentacao_estoque (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo                VARCHAR(20) NOT NULL
        CONSTRAINT ck_mov_tipo CHECK (tipo IN ('ENTRADA_COMPRA','SAIDA_CONSUMO','TRANSFERENCIA')),
    material_id         BIGINT NOT NULL REFERENCES material(id),
    quantidade          NUMERIC(15,3) NOT NULL CHECK (quantidade > 0),
    canteiro_origem_id  BIGINT REFERENCES canteiro(id),
    canteiro_destino_id BIGINT REFERENCES canteiro(id),
    pedido_compra_id    BIGINT REFERENCES pedido_compra(id),
    usuario_id          BIGINT NOT NULL REFERENCES usuario(id),
    data_movimentacao   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_mov_campos CHECK (
        (tipo = 'ENTRADA_COMPRA' AND canteiro_destino_id IS NOT NULL AND canteiro_origem_id IS NULL
            AND pedido_compra_id IS NOT NULL)
        OR (tipo = 'SAIDA_CONSUMO' AND canteiro_origem_id IS NOT NULL AND canteiro_destino_id IS NULL
            AND pedido_compra_id IS NULL)
        OR (tipo = 'TRANSFERENCIA' AND canteiro_origem_id IS NOT NULL AND canteiro_destino_id IS NOT NULL
            AND canteiro_origem_id <> canteiro_destino_id AND pedido_compra_id IS NULL)
    )
);
CREATE INDEX ix_mov_material_data ON movimentacao_estoque(material_id, data_movimentacao);
CREATE INDEX ix_mov_origem        ON movimentacao_estoque(canteiro_origem_id, data_movimentacao);
CREATE INDEX ix_mov_destino       ON movimentacao_estoque(canteiro_destino_id, data_movimentacao);
CREATE INDEX ix_mov_pedido        ON movimentacao_estoque(pedido_compra_id);
CREATE INDEX ix_mov_usuario       ON movimentacao_estoque(usuario_id);
