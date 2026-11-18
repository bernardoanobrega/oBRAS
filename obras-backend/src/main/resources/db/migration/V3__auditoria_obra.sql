CREATE TABLE auditavel (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    excluido  BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE registro_auditoria (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    auditavel_id  BIGINT NOT NULL,
    usuario_id    BIGINT NOT NULL,
    acao          VARCHAR(10) NOT NULL,
    data_hora     TIMESTAMP   NOT NULL DEFAULT now(),
    detalhe       JSONB,
    CONSTRAINT ck_reg_auditoria_acao CHECK (acao IN ('CRIACAO','EDICAO','EXCLUSAO')),
    CONSTRAINT fk_reg_auditoria_auditavel FOREIGN KEY (auditavel_id) REFERENCES auditavel(id),
    CONSTRAINT fk_reg_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE INDEX ix_reg_auditoria_auditavel ON registro_auditoria(auditavel_id, data_hora);
