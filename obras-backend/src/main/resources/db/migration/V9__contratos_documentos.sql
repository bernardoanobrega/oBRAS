-- RF-39, RF-41, RF-58 a RF-63 e RF-71. Contrato é uma categoria de documento.
CREATE TABLE prestador_servico (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 nome_razao_social VARCHAR(200) NOT NULL,
 cpf_cnpj VARCHAR(14) NOT NULL UNIQUE,
 contato VARCHAR(200) NOT NULL,
 situacao VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO','INATIVO'))
);
CREATE TABLE documento (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 obra_id BIGINT NOT NULL REFERENCES obra(id),
 prestador_id BIGINT REFERENCES prestador_servico(id),
 codigo VARCHAR(60) NOT NULL,
 titulo VARCHAR(200) NOT NULL,
 categoria VARCHAR(25) NOT NULL CHECK (categoria IN ('CONTRATO','LICENCA','ALVARA','ESPECIFICACAO_TECNICA','PROJETO_EXECUTIVO')),
 disciplina VARCHAR(30) NOT NULL CHECK (disciplina IN ('GERAL','ARQUITETONICO','ESTRUTURAL','ELETRICO','HIDROSSANITARIO')),
 data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK (prestador_id IS NULL OR categoria = 'CONTRATO'),
 UNIQUE (obra_id, codigo)
);
CREATE INDEX ix_documento_obra_categoria ON documento(obra_id, categoria);
CREATE INDEX ix_documento_prestador ON documento(prestador_id);
CREATE TABLE versao_documento (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 documento_id BIGINT NOT NULL REFERENCES documento(id),
 numero INTEGER NOT NULL CHECK (numero > 0),
 autor_envio_id BIGINT NOT NULL REFERENCES usuario(id),
 data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 descricao VARCHAR(1000) NOT NULL,
 arquivo_nome_original VARCHAR(255) NOT NULL,
 arquivo_formato VARCHAR(5) NOT NULL CHECK (arquivo_formato IN ('PDF','PNG','JPG')),
 arquivo_tamanho_bytes BIGINT NOT NULL CHECK (arquivo_tamanho_bytes > 0 AND arquivo_tamanho_bytes <= 10485760),
 arquivo_localizador VARCHAR(500) NOT NULL UNIQUE,
 UNIQUE (documento_id, numero)
);
CREATE TABLE historico_documental_prestador (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 documento_id BIGINT NOT NULL REFERENCES documento(id),
 versao_id BIGINT NOT NULL REFERENCES versao_documento(id),
 prestador_id BIGINT NOT NULL REFERENCES prestador_servico(id),
 autor_id BIGINT NOT NULL REFERENCES usuario(id),
 acao VARCHAR(20) NOT NULL CHECK (acao IN ('VINCULO_CONTRATO','INCLUSAO_VERSAO')),
 data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_hist_prestador_data ON historico_documental_prestador(prestador_id, data_hora);
