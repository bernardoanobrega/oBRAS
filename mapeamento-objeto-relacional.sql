-- =====================================================================
-- OBRAS - Esquema relacional
-- =====================================================================

-- ---------------------------------------------------------------------
-- USUÁRIOS  (Credencial é @Embeddable em Usuario)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    situacao       VARCHAR(10)  NOT NULL
        CONSTRAINT ck_usuario_situacao CHECK (situacao IN ('ATIVO','INATIVO')),
    data_cadastro  TIMESTAMP    NOT NULL DEFAULT now(),
    -- @Embedded Credencial
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

-- Catálogo fixo de permissões (pode ser populado por seed)
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

-- N:N Cargo <-> Permissao
CREATE TABLE cargo_permissao (
    cargo_id      BIGINT NOT NULL REFERENCES cargo(id) ON DELETE CASCADE,
    permissao_id  BIGINT NOT NULL REFERENCES permissao(id),
    PRIMARY KEY (cargo_id, permissao_id)
);
CREATE INDEX ix_cargo_permissao_permissao ON cargo_permissao(permissao_id);

-- Permissões individuais do usuário (composição de Usuario)
CREATE TABLE permissao_usuario (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT  NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    permissao_id  BIGINT  NOT NULL REFERENCES permissao(id),
    concedida     BOOLEAN NOT NULL,
    CONSTRAINT uk_permissao_usuario UNIQUE (usuario_id, permissao_id)
);
CREATE INDEX ix_permissao_usuario_permissao ON permissao_usuario(permissao_id);

-- Regras de negócio:
--   - Não existe tabela usuario_cargo: cargos chegam ao usuário somente via
--     alocacao_obra -> alocacao_obra_cargo.
--   - Um administrador é um usuário com permissões individuais em
--     permissao_usuario, sem necessidade de linha em alocacao_obra.
--   - permissao_usuario.concedida = false retira uma permissão que o cargo concederia.
--   - Usuário nunca é apagado fisicamente: desativar() só altera situacao.

-- ---------------------------------------------------------------------
-- CONTROLE DE ESTOQUE / COMPRAS / DOCUMENTOS: tabelas independentes
-- ---------------------------------------------------------------------
CREATE TABLE fornecedor (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    razao_social  VARCHAR(200) NOT NULL,
    cnpj          VARCHAR(18)  NOT NULL,
    email         VARCHAR(150),
    telefone      VARCHAR(20),
    ativo         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_fornecedor_cnpj UNIQUE (cnpj)
);

CREATE TABLE material (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    descricao       VARCHAR(500),
    unidade_medida  VARCHAR(20)  NOT NULL
);

CREATE TABLE prestador_servico (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_razao_social  VARCHAR(200) NOT NULL,
    cpf_cnpj           VARCHAR(18)  NOT NULL,
    contato            VARCHAR(200) NOT NULL,
    situacao           VARCHAR(10)  NOT NULL
        CONSTRAINT ck_prestador_situacao CHECK (situacao IN ('ATIVO','INATIVO')),
    CONSTRAINT uk_prestador_cpf_cnpj UNIQUE (cpf_cnpj)
);

-- ---------------------------------------------------------------------
-- DETALHES DE OBRA
-- ---------------------------------------------------------------------
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

-- Regra de negócio (não expressável em DDL puro): Obra possui 1..* Canteiro
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

-- N:N AlocacaoObra <-> Cargo
CREATE TABLE alocacao_obra_cargo (
    alocacao_obra_id  BIGINT NOT NULL REFERENCES alocacao_obra(id) ON DELETE CASCADE,
    cargo_id          BIGINT NOT NULL REFERENCES cargo(id),
    PRIMARY KEY (alocacao_obra_id, cargo_id)
);
CREATE INDEX ix_alocacao_obra_cargo_cargo ON alocacao_obra_cargo(cargo_id);

-- Hierarquia Auditavel: herança JOINED + exclusão lógica
CREATE TABLE auditavel (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    excluido  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE registro_auditoria (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    auditavel_id  BIGINT NOT NULL REFERENCES auditavel(id),
    usuario_id    BIGINT NOT NULL REFERENCES usuario(id),
    acao          VARCHAR(10) NOT NULL
        CONSTRAINT ck_reg_auditoria_acao CHECK (acao IN ('CRIACAO','EDICAO','EXCLUSAO')),
    data_hora     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX ix_reg_auditoria_auditavel ON registro_auditoria(auditavel_id, data_hora);
CREATE INDEX ix_reg_auditoria_usuario   ON registro_auditoria(usuario_id);

CREATE TABLE registro_diario (
    id         BIGINT PRIMARY KEY REFERENCES auditavel(id) ON DELETE CASCADE,
    obra_id    BIGINT NOT NULL REFERENCES obra(id) ON DELETE CASCADE,
    data       DATE NOT NULL,
    clima      VARCHAR(15) NOT NULL
        CONSTRAINT ck_reg_diario_clima CHECK (clima IN
            ('ENSOLARADO','NUBLADO','CHUVA_FRACA','CHUVA_FORTE')),
    descricao  VARCHAR(2000)
);
CREATE INDEX ix_registro_diario_obra_data ON registro_diario(obra_id, data);

-- N:N RegistroDiario <-> AlocacaoObra (equipe presente)
CREATE TABLE registro_diario_equipe (
    registro_diario_id  BIGINT NOT NULL REFERENCES registro_diario(id) ON DELETE CASCADE,
    alocacao_obra_id    BIGINT NOT NULL REFERENCES alocacao_obra(id),
    PRIMARY KEY (registro_diario_id, alocacao_obra_id)
);
CREATE INDEX ix_reg_diario_equipe_aloc ON registro_diario_equipe(alocacao_obra_id);

CREATE TABLE ocorrencia (
    id                  BIGINT PRIMARY KEY REFERENCES auditavel(id) ON DELETE CASCADE,
    registro_diario_id  BIGINT REFERENCES registro_diario(id),       -- 0..1
    alocacao_obra_id    BIGINT NOT NULL REFERENCES alocacao_obra(id),
    data_hora           TIMESTAMP NOT NULL,
    categoria           VARCHAR(15) NOT NULL
        CONSTRAINT ck_ocorrencia_categoria CHECK (categoria IN
            ('INTEMPERIE','LOGISTICA','SEGURANCA','OUTRO')),
    descricao           VARCHAR(2000) NOT NULL
);
CREATE INDEX ix_ocorrencia_reg_diario ON ocorrencia(registro_diario_id);
CREATE INDEX ix_ocorrencia_alocacao   ON ocorrencia(alocacao_obra_id);

CREATE TABLE paralisacao (
    id                   BIGINT PRIMARY KEY REFERENCES ocorrencia(id) ON DELETE CASCADE,
    inicio_paralisacao   TIMESTAMP NOT NULL,
    fim_paralisacao      TIMESTAMP,
    CONSTRAINT ck_paralisacao_periodo
        CHECK (fim_paralisacao IS NULL OR fim_paralisacao >= inicio_paralisacao)
);

CREATE TABLE registro_fotografico (
    id             BIGINT PRIMARY KEY REFERENCES auditavel(id) ON DELETE CASCADE,
    ocorrencia_id  BIGINT REFERENCES ocorrencia(id),                 -- 0..1
    arquivo        VARCHAR(500) NOT NULL,
    legenda        VARCHAR(300),
    data_envio     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX ix_reg_fotografico_ocorrencia ON registro_fotografico(ocorrencia_id);

-- ---------------------------------------------------------------------
-- ORÇAMENTO  (IndicadorSaudeFinanceira é @Transient: não possui tabela)
-- ---------------------------------------------------------------------
CREATE TABLE orcamento (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id                  BIGINT NOT NULL REFERENCES obra(id),
    data_elaboracao          DATE NOT NULL,
    data_ultima_atualizacao  DATE NOT NULL,
    percentual_limite_alerta DOUBLE PRECISION NOT NULL
        CONSTRAINT ck_orcamento_limite CHECK (percentual_limite_alerta >= 0),
    CONSTRAINT uk_orcamento_obra UNIQUE (obra_id)                    -- 1:1 com Obra
);

CREATE TABLE item_orcado (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orcamento_id           BIGINT NOT NULL REFERENCES orcamento(id) ON DELETE CASCADE,
    codigo                 VARCHAR(50)  NOT NULL,
    descricao              VARCHAR(300) NOT NULL,
    unidade                VARCHAR(20)  NOT NULL,
    quantidade             DOUBLE PRECISION NOT NULL CHECK (quantidade >= 0),
    custo_unitario_orcado  NUMERIC(15,2) NOT NULL CHECK (custo_unitario_orcado >= 0)
);
CREATE INDEX ix_item_orcado_orcamento ON item_orcado(orcamento_id);
-- Sugestão (não está no modelo): UNIQUE (orcamento_id, codigo)

CREATE TABLE custo_realizado (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_orcado_id   BIGINT NOT NULL REFERENCES item_orcado(id) ON DELETE CASCADE,
    usuario_id       BIGINT NOT NULL REFERENCES usuario(id),
    data_lancamento  DATE NOT NULL,
    descricao        VARCHAR(300) NOT NULL,
    valor            NUMERIC(15,2) NOT NULL,
    origem           VARCHAR(25) NOT NULL
        CONSTRAINT ck_custo_origem CHECK (origem IN
            ('MATERIAL','MAO_DE_OBRA','EQUIPAMENTO','SERVICO_TERCEIRIZADO','OUTRO')),
    id_origem        BIGINT,        -- referência polimórfica (depende de "origem"): SEM FK
    CONSTRAINT ck_custo_id_origem CHECK (id_origem IS NULL OR origem <> 'OUTRO')
);
CREATE INDEX ix_custo_item    ON custo_realizado(item_orcado_id);
CREATE INDEX ix_custo_usuario ON custo_realizado(usuario_id);
CREATE INDEX ix_custo_origem  ON custo_realizado(origem, id_origem);

CREATE TABLE decisao_financeira (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orcamento_id   BIGINT NOT NULL REFERENCES orcamento(id) ON DELETE CASCADE,
    usuario_id     BIGINT NOT NULL REFERENCES usuario(id),
    data_decisao   DATE NOT NULL,
    descricao      VARCHAR(500)  NOT NULL,
    justificativa  VARCHAR(2000) NOT NULL,
    valor_impacto  NUMERIC(15,2) NOT NULL
);
CREATE INDEX ix_decisao_orcamento ON decisao_financeira(orcamento_id);
CREATE INDEX ix_decisao_usuario   ON decisao_financeira(usuario_id);

-- ---------------------------------------------------------------------
-- CRONOGRAMA
-- ---------------------------------------------------------------------
CREATE TABLE cronograma (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id                  BIGINT NOT NULL REFERENCES obra(id),
    data_ultima_atualizacao  DATE NOT NULL,
    status                   VARCHAR(15) NOT NULL
        CONSTRAINT ck_cronograma_status CHECK (status IN
            ('VAZIO','PLANEJADO','EM_ANDAMENTO','CONCLUIDO','CANCELADO')),
    CONSTRAINT uk_cronograma_obra UNIQUE (obra_id)                   -- 1:1 com Obra
);

CREATE TABLE fase (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cronograma_id    BIGINT NOT NULL REFERENCES cronograma(id) ON DELETE CASCADE,
    nome             VARCHAR(150) NOT NULL,
    descricao        VARCHAR(1000),
    ordem            INTEGER NOT NULL CHECK (ordem > 0),
    inicio_previsto  DATE NOT NULL,
    fim_previsto     DATE NOT NULL,
    inicio_real      DATE,
    fim_real         DATE,
    status           VARCHAR(15) NOT NULL
        CONSTRAINT ck_fase_status CHECK (status IN
            ('NAO_INICIADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA')),
    CONSTRAINT ck_fase_previsto CHECK (fim_previsto >= inicio_previsto),
    CONSTRAINT ck_fase_real     CHECK (fim_real IS NULL OR inicio_real IS NULL OR fim_real >= inicio_real)
);
CREATE INDEX ix_fase_cronograma ON fase(cronograma_id, ordem);
-- Sugestão (não está no modelo): UNIQUE (cronograma_id, ordem) DEFERRABLE INITIALLY DEFERRED

CREATE TABLE avanco_fisico (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fase_id               BIGINT NOT NULL REFERENCES fase(id) ON DELETE CASCADE,
    usuario_id            BIGINT NOT NULL REFERENCES usuario(id),
    data_registro         DATE NOT NULL,
    percentual_acumulado  DOUBLE PRECISION NOT NULL
        CONSTRAINT ck_avanco_percentual CHECK (percentual_acumulado BETWEEN 0 AND 100),
    observacao            VARCHAR(1000)
);
CREATE INDEX ix_avanco_fase    ON avanco_fisico(fase_id, data_registro);
CREATE INDEX ix_avanco_usuario ON avanco_fisico(usuario_id);

-- ---------------------------------------------------------------------
-- COMPRAS E FORNECEDORES
-- ---------------------------------------------------------------------
CREATE TABLE pedido_compra (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fornecedor_id     BIGINT NOT NULL REFERENCES fornecedor(id),
    obra_id           BIGINT NOT NULL REFERENCES obra(id),
    usuario_id        BIGINT NOT NULL REFERENCES usuario(id),
    data_solicitacao  DATE NOT NULL,
    data_prevista     DATE,
    valor_total       NUMERIC(15,2) NOT NULL DEFAULT 0 CHECK (valor_total >= 0),
    status            VARCHAR(15) NOT NULL
        CONSTRAINT ck_pedido_status CHECK (status IN
            ('SOLICITACAO','EM_COTACAO','COMPRADO','EM_TRANSITO','ENTREGUE','CANCELADO')),
    prioridade        VARCHAR(10) NOT NULL
        CONSTRAINT ck_pedido_prioridade CHECK (prioridade IN ('BAIXA','MEDIA','ALTA')),
    CONSTRAINT ck_pedido_datas CHECK (data_prevista IS NULL OR data_prevista >= data_solicitacao)
);
CREATE INDEX ix_pedido_fornecedor ON pedido_compra(fornecedor_id);
CREATE INDEX ix_pedido_obra       ON pedido_compra(obra_id, status);
CREATE INDEX ix_pedido_usuario    ON pedido_compra(usuario_id);

-- Regra de negócio: PedidoCompra possui 1..* ItemPedido
CREATE TABLE item_pedido (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_compra_id  BIGINT NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    material_id       BIGINT NOT NULL REFERENCES material(id),
    quantidade        DOUBLE PRECISION NOT NULL CHECK (quantidade > 0),
    valor_unitario    NUMERIC(15,2) NOT NULL CHECK (valor_unitario >= 0)
);
CREATE INDEX ix_item_pedido_pedido   ON item_pedido(pedido_compra_id);
CREATE INDEX ix_item_pedido_material ON item_pedido(material_id);

-- ---------------------------------------------------------------------
-- CONTROLE DE ESTOQUE
-- ---------------------------------------------------------------------
CREATE TABLE saldo_estoque (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT NOT NULL REFERENCES material(id),
    obra_id           BIGINT NOT NULL REFERENCES obra(id),
    quantidade_atual  DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (quantidade_atual >= 0),
    quantidade_minima DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (quantidade_minima >= 0),
    CONSTRAINT uk_saldo_material_obra UNIQUE (material_id, obra_id)
);
CREATE INDEX ix_saldo_obra ON saldo_estoque(obra_id);

CREATE TABLE movimentacao_estoque (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    saldo_estoque_id    BIGINT NOT NULL REFERENCES saldo_estoque(id), -- saldo gerenciado (a obra vem do saldo)
    material_id         BIGINT NOT NULL REFERENCES material(id),
    pedido_compra_id    BIGINT REFERENCES pedido_compra(id),         -- 0..1
    tipo                VARCHAR(20) NOT NULL
        CONSTRAINT ck_mov_tipo CHECK (tipo IN ('ENTRADA_COMPRA','SAIDA_CONSUMO','TRANSFERENCIA')),
    quantidade          DOUBLE PRECISION NOT NULL CHECK (quantidade > 0),
    data_movimentacao   TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX ix_mov_saldo    ON movimentacao_estoque(saldo_estoque_id, data_movimentacao);
CREATE INDEX ix_mov_material ON movimentacao_estoque(material_id, data_movimentacao);
CREATE INDEX ix_mov_pedido   ON movimentacao_estoque(pedido_compra_id);
-- A obra da movimentação é obtida via saldo_estoque.obra_id (sem obra_id redundante).
-- Opcional, para impedir material_id divergente do material do saldo:
--   ALTER TABLE saldo_estoque ADD CONSTRAINT uk_saldo_id_material UNIQUE (id, material_id);
--   ALTER TABLE movimentacao_estoque ADD CONSTRAINT fk_mov_saldo_material
--       FOREIGN KEY (saldo_estoque_id, material_id) REFERENCES saldo_estoque(id, material_id);

-- ---------------------------------------------------------------------
-- MÁQUINAS E EQUIPAMENTOS
-- ---------------------------------------------------------------------
CREATE TABLE equipamento (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id        BIGINT NOT NULL REFERENCES usuario(id),
    codigo_patrimonio VARCHAR(50)  NOT NULL,
    descricao         VARCHAR(300) NOT NULL,
    tipo              VARCHAR(15) NOT NULL
        CONSTRAINT ck_equip_tipo CHECK (tipo IN ('MAQUINA','FERRAMENTA','OUTRO')),
    origem            VARCHAR(10) NOT NULL
        CONSTRAINT ck_equip_origem CHECK (origem IN ('PROPRIO','LOCADO')),
    fim_locacao       DATE,
    status            VARCHAR(15) NOT NULL
        CONSTRAINT ck_equip_status CHECK (status IN
            ('DISPONIVEL','ALOCADO','EM_MANUTENCAO','INATIVO')),
    CONSTRAINT uk_equipamento_patrimonio UNIQUE (codigo_patrimonio),
    CONSTRAINT ck_equip_locacao CHECK (fim_locacao IS NULL OR origem = 'LOCADO')
);
CREATE INDEX ix_equipamento_usuario ON equipamento(usuario_id);
CREATE INDEX ix_equipamento_status  ON equipamento(status);

-- Manutencao antes de AlocacaoEquipamento (esta referencia aquela)
CREATE TABLE manutencao (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    equipamento_id  BIGINT NOT NULL REFERENCES equipamento(id) ON DELETE CASCADE,
    tipo            VARCHAR(12) NOT NULL
        CONSTRAINT ck_manutencao_tipo CHECK (tipo IN ('PREVENTIVA','CORRETIVA')),
    data_inicio     DATE NOT NULL,
    data_fim        DATE,
    descricao       VARCHAR(1000) NOT NULL,
    CONSTRAINT ck_manutencao_periodo CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);
CREATE INDEX ix_manutencao_equipamento ON manutencao(equipamento_id);

CREATE TABLE alocacao_equipamento (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    equipamento_id           BIGINT NOT NULL REFERENCES equipamento(id) ON DELETE CASCADE,
    canteiro_id              BIGINT NOT NULL REFERENCES canteiro(id),
    usuario_id               BIGINT NOT NULL REFERENCES usuario(id),
    manutencao_id            BIGINT REFERENCES manutencao(id),       -- 0..1, 1:1 com Manutencao
    data_inicio              DATE NOT NULL,
    data_prevista_devolucao  DATE NOT NULL,
    data_fim                 DATE,
    condicao_devolucao       VARCHAR(10)
        CONSTRAINT ck_aloc_equip_condicao CHECK (condicao_devolucao IN ('BOA','DANIFICADO')),
    responsavel              VARCHAR(150) NOT NULL,
    observacao               VARCHAR(1000),
    CONSTRAINT uk_aloc_equip_manutencao UNIQUE (manutencao_id),
    CONSTRAINT ck_aloc_equip_prevista CHECK (data_prevista_devolucao >= data_inicio),
    CONSTRAINT ck_aloc_equip_fim      CHECK (data_fim IS NULL OR data_fim >= data_inicio),
    -- condição só faz sentido após a devolução; manutenção só se danificado
    CONSTRAINT ck_aloc_equip_condicao_fim CHECK (condicao_devolucao IS NULL OR data_fim IS NOT NULL),
    CONSTRAINT ck_aloc_equip_manut_danif  CHECK (manutencao_id IS NULL OR condicao_devolucao = 'DANIFICADO')
);
CREATE INDEX ix_aloc_equip_equipamento ON alocacao_equipamento(equipamento_id);
CREATE INDEX ix_aloc_equip_canteiro    ON alocacao_equipamento(canteiro_id);
CREATE INDEX ix_aloc_equip_usuario     ON alocacao_equipamento(usuario_id);

-- ---------------------------------------------------------------------
-- CONTRATOS E DOCUMENTOS  (ArquivoDocumento é @Embeddable em VersaoDocumento)
-- ---------------------------------------------------------------------
CREATE TABLE documento (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    obra_id        BIGINT NOT NULL REFERENCES obra(id),
    prestador_id   BIGINT REFERENCES prestador_servico(id),          -- 0..1
    titulo         VARCHAR(200) NOT NULL,
    categoria      VARCHAR(25) NOT NULL
        CONSTRAINT ck_documento_categoria CHECK (categoria IN
            ('CONTRATO','LICENCA','ALVARA','ESPECIFICACAO_TECNICA','PROJETO_EXECUTIVO')),
    data_cadastro  DATE NOT NULL DEFAULT CURRENT_DATE               -- updatable=false (ver trigger abaixo)
);
CREATE INDEX ix_documento_obra      ON documento(obra_id, categoria);
CREATE INDEX ix_documento_prestador ON documento(prestador_id);

-- Regra de negócio: Documento possui 1..* VersaoDocumento
CREATE TABLE versao_documento (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    documento_id      BIGINT NOT NULL REFERENCES documento(id) ON DELETE CASCADE,
    autor_envio_id    BIGINT NOT NULL REFERENCES usuario(id),
    numero            INTEGER NOT NULL CHECK (numero > 0),
    data_envio        TIMESTAMP NOT NULL DEFAULT now(),
    -- @Embedded ArquivoDocumento
    arquivo_nome_original  VARCHAR(255) NOT NULL,
    arquivo_formato        VARCHAR(5)   NOT NULL
        CONSTRAINT ck_versao_formato CHECK (arquivo_formato IN ('PDF','PNG','JPG')),
    arquivo_tamanho_bytes  BIGINT       NOT NULL CHECK (arquivo_tamanho_bytes >= 0),
    arquivo_localizador    VARCHAR(500) NOT NULL,
    CONSTRAINT uk_versao_documento_numero UNIQUE (documento_id, numero)
);
CREATE INDEX ix_versao_autor ON versao_documento(autor_envio_id);

CREATE TABLE historico_documental_prestador (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    documento_id  BIGINT NOT NULL REFERENCES documento(id),
    versao_id     BIGINT NOT NULL REFERENCES versao_documento(id),
    prestador_id  BIGINT NOT NULL REFERENCES prestador_servico(id),
    autor_id      BIGINT NOT NULL REFERENCES usuario(id),
    acao          VARCHAR(20) NOT NULL
        CONSTRAINT ck_hist_acao CHECK (acao IN ('VINCULO_CONTRATO','INCLUSAO_VERSAO')),
    data_hora     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX ix_hist_documento ON historico_documental_prestador(documento_id);
CREATE INDEX ix_hist_versao    ON historico_documental_prestador(versao_id);
CREATE INDEX ix_hist_prestador ON historico_documental_prestador(prestador_id, data_hora);
CREATE INDEX ix_hist_autor     ON historico_documental_prestador(autor_id);

-- ---------------------------------------------------------------------
-- updatable=false em documento.data_cadastro
-- ---------------------------------------------------------------------
CREATE FUNCTION fn_documento_data_cadastro_imutavel() RETURNS trigger AS $$
BEGIN
    IF NEW.data_cadastro IS DISTINCT FROM OLD.data_cadastro THEN
        RAISE EXCEPTION 'documento.data_cadastro não pode ser alterada';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_documento_data_cadastro
    BEFORE UPDATE ON documento
    FOR EACH ROW EXECUTE FUNCTION fn_documento_data_cadastro_imutavel();