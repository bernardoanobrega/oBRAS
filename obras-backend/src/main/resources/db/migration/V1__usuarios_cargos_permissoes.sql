-- Módulo Usuários (Credencial é @Embeddable em Usuario)
CREATE TABLE usuario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    situacao       VARCHAR(10)  NOT NULL
        CONSTRAINT ck_usuario_situacao CHECK (situacao IN ('ATIVO','INATIVO')),
    data_cadastro  TIMESTAMP    NOT NULL DEFAULT now(),
    login          VARCHAR(100) NOT NULL,
    senha_hash     VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuario_login UNIQUE (login)
);

CREATE TABLE cargo (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    descricao  VARCHAR(300),
    CONSTRAINT uk_cargo_nome UNIQUE (nome)
);

CREATE TABLE permissao (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    modulo  VARCHAR(25) NOT NULL
        CONSTRAINT ck_permissao_modulo CHECK (modulo IN
            ('USUARIOS','DASHBOARD','DETALHES_OBRA','CRONOGRAMA','ORCAMENTO',
             'COMPRAS_FORNECEDORES','ESTOQUE','MAQUINAS_EQUIPAMENTOS','CONTRATOS_DOCUMENTOS')),
    acao    VARCHAR(10) NOT NULL
        CONSTRAINT ck_permissao_acao CHECK (acao IN
            ('VISUALIZAR','CRIAR','EDITAR','EXCLUIR','APROVAR')),
    CONSTRAINT uk_permissao_modulo_acao UNIQUE (modulo, acao)
);

CREATE TABLE cargo_permissao (
    cargo_id      BIGINT NOT NULL REFERENCES cargo(id) ON DELETE CASCADE,
    permissao_id  BIGINT NOT NULL REFERENCES permissao(id),
    PRIMARY KEY (cargo_id, permissao_id)
);
CREATE INDEX ix_cargo_permissao_permissao ON cargo_permissao(permissao_id);

CREATE TABLE permissao_usuario (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT  NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    permissao_id  BIGINT  NOT NULL REFERENCES permissao(id),
    concedida     BOOLEAN NOT NULL,
    CONSTRAINT uk_permissao_usuario UNIQUE (usuario_id, permissao_id)
);
CREATE INDEX ix_permissao_usuario_permissao ON permissao_usuario(permissao_id);

-- Seed: catálogo fixo 9 módulos x 5 ações
INSERT INTO permissao (modulo, acao)
SELECT m, a
FROM unnest(ARRAY['USUARIOS','DASHBOARD','DETALHES_OBRA','CRONOGRAMA','ORCAMENTO',
                  'COMPRAS_FORNECEDORES','ESTOQUE','MAQUINAS_EQUIPAMENTOS','CONTRATOS_DOCUMENTOS']) AS m
CROSS JOIN unnest(ARRAY['VISUALIZAR','CRIAR','EDITAR','EXCLUIR','APROVAR']) AS a;

-- Seed: cargos citados no Documento de Requisitos (permissões definidas depois pelo Administrador)
INSERT INTO cargo (nome, descricao) VALUES
 ('Gestor da Obra',        'Engenheiro Residente / Gerente de Projetos'),
 ('Engenheiro Orçamentário','Estruturação e acompanhamento do orçamento'),
 ('Estoquista',            'Suprimentos, compras e estoque'),
 ('Mestre de Obras',       'Operação de campo no canteiro');
