-- Base da hierarquia Auditavel (herança JOINED) e do histórico de autoria/edições.
-- Equipamento, alocação de equipamento e demais entidades auditáveis têm id = auditavel.id.

CREATE TABLE auditavel (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    excluido  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE registro_auditoria (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    auditavel_id  BIGINT NOT NULL REFERENCES auditavel(id),
    usuario_id    BIGINT NOT NULL REFERENCES usuario(id),
    acao          VARCHAR(10) NOT NULL
        CONSTRAINT ck_registro_auditoria_acao CHECK (acao IN ('CRIACAO','EDICAO','EXCLUSAO')),
    data_hora     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX ix_registro_auditoria_auditavel ON registro_auditoria(auditavel_id, data_hora);
CREATE INDEX ix_registro_auditoria_usuario   ON registro_auditoria(usuario_id);