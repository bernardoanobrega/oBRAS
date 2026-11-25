-- Catálogo de materiais (módulo Estoque). Fica antes de compras: item_pedido referencia material.
CREATE TABLE material (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    descricao       VARCHAR(500),
    unidade_medida  VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_material_nome_unidade UNIQUE (nome, unidade_medida)
);
