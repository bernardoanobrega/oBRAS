CREATE TABLE registro_diario (
    id         BIGINT NOT NULL,
    obra_id    BIGINT NOT NULL,
    data       DATE NOT NULL,
    clima      VARCHAR(15) NOT NULL
        CONSTRAINT ck_reg_diario_clima CHECK (clima IN
            ('ENSOLARADO','NUBLADO','CHUVA_FRACA','CHUVA_FORTE')),
    descricao  VARCHAR(2000),
    excluido   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_registro_diario PRIMARY KEY (id),
    CONSTRAINT fk_registro_diario_auditavel FOREIGN KEY (id)
        REFERENCES auditavel(id) ON DELETE CASCADE,
    CONSTRAINT fk_registro_diario_obra FOREIGN KEY (obra_id)
        REFERENCES obra(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_registro_diario_obra_data_ativo
    ON registro_diario (obra_id, data)
    WHERE NOT excluido;

CREATE INDEX ix_registro_diario_obra_data ON registro_diario(obra_id, data);

CREATE TABLE registro_diario_equipe (
    registro_diario_id  BIGINT NOT NULL,
    alocacao_obra_id    BIGINT NOT NULL,
    CONSTRAINT pk_reg_diario_equipe PRIMARY KEY (registro_diario_id, alocacao_obra_id),
    CONSTRAINT fk_reg_diario_equipe_diario FOREIGN KEY (registro_diario_id)
        REFERENCES registro_diario(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_diario_equipe_alocacao FOREIGN KEY (alocacao_obra_id)
        REFERENCES alocacao_obra(id)
);

CREATE INDEX ix_reg_diario_equipe_aloc ON registro_diario_equipe(alocacao_obra_id);

CREATE TABLE ocorrencia (
    id                  BIGINT NOT NULL,
    registro_diario_id  BIGINT NOT NULL,
    alocacao_obra_id    BIGINT NOT NULL,
    data_hora           TIMESTAMP NOT NULL,
    categoria           VARCHAR(15) NOT NULL
        CONSTRAINT ck_ocorrencia_categoria CHECK (categoria IN
            ('INTEMPERIE','LOGISTICA','SEGURANCA','OUTRO')),
    descricao           VARCHAR(2000) NOT NULL,
    CONSTRAINT pk_ocorrencia PRIMARY KEY (id),
    CONSTRAINT fk_ocorrencia_auditavel FOREIGN KEY (id)
        REFERENCES auditavel(id) ON DELETE CASCADE,
    CONSTRAINT fk_ocorrencia_registro_diario FOREIGN KEY (registro_diario_id)
        REFERENCES registro_diario(id),
    CONSTRAINT fk_ocorrencia_alocacao FOREIGN KEY (alocacao_obra_id)
        REFERENCES alocacao_obra(id)
);

CREATE INDEX ix_ocorrencia_reg_diario ON ocorrencia(registro_diario_id);
CREATE INDEX ix_ocorrencia_alocacao ON ocorrencia(alocacao_obra_id);

CREATE TABLE paralisacao (
    id                  BIGINT NOT NULL,
    inicio_paralisacao  TIMESTAMP NOT NULL,
    fim_paralisacao     TIMESTAMP,
    CONSTRAINT pk_paralisacao PRIMARY KEY (id),
    CONSTRAINT fk_paralisacao_ocorrencia FOREIGN KEY (id)
        REFERENCES ocorrencia(id) ON DELETE CASCADE,
    CONSTRAINT ck_paralisacao_periodo
        CHECK (fim_paralisacao IS NULL OR fim_paralisacao >= inicio_paralisacao)
);

CREATE TABLE registro_fotografico (
    id             BIGINT NOT NULL,
    ocorrencia_id  BIGINT NOT NULL,
    arquivo        VARCHAR(500) NOT NULL,
    legenda        VARCHAR(300),
    data_envio     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_registro_fotografico PRIMARY KEY (id),
    CONSTRAINT fk_reg_fotografico_auditavel FOREIGN KEY (id)
        REFERENCES auditavel(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_fotografico_ocorrencia FOREIGN KEY (ocorrencia_id)
        REFERENCES ocorrencia(id)
);

CREATE INDEX ix_reg_fotografico_ocorrencia ON registro_fotografico(ocorrencia_id);

CREATE FUNCTION sincronizar_excluido_registro_diario() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE registro_diario
       SET excluido = NEW.excluido
     WHERE id = NEW.id;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_auditavel_excluido_registro_diario
AFTER UPDATE OF excluido ON auditavel
FOR EACH ROW
WHEN (OLD.excluido IS DISTINCT FROM NEW.excluido)
EXECUTE FUNCTION sincronizar_excluido_registro_diario();
